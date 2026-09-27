package az.org.shoriufoundation.itemduper;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;

public final class UnlimitedItems {
    public static final String MARKER = "itemduper:unlimited";
    private static boolean lanSafetyDisabled;

    private UnlimitedItems() {}

    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            dispatcher.register(ClientCommands.literal("makeunlimited")
                .executes(UnlimitedItems::toggle));
        });
        ClientTickEvents.END_CLIENT_TICK.register(UnlimitedItems::disableWhenLanOpens);
    }

    private static int toggle(CommandContext<FabricClientCommandSource> context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.getSingleplayerServer() == null
                || client.getSingleplayerServer().isPublished()) {
            context.getSource().sendFeedback(
                Component.literal("ItemDuper yalnızca LAN'a açılmamış tek oyunculu dünyada kullanılabilir."));
            return 0;
        }
        ItemStack stack = client.player.getMainHandItem();
        if (stack.isEmpty()) {
            context.getSource().sendFeedback(Component.literal("Önce eline bir eşya al."));
            return 0;
        }
        CustomData oldData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = oldData == null ? new CompoundTag() : oldData.copyTag();
        boolean wasEnabled = tag.getBoolean(MARKER).orElse(false);
        boolean enabled = !wasEnabled;
        setMarker(stack, enabled);

        // The integrated server owns the authoritative inventory stack. Keep its
        // matching hand stack marked too so server-side item consumption is covered.
        MinecraftServer server = client.getSingleplayerServer();
        server.execute(() -> {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(client.player.getUUID());
            if (serverPlayer == null) return;
            ItemStack serverStack = serverPlayer.getMainHandItem();
            if (!serverStack.isEmpty() && ItemStack.isSameItem(serverStack, stack)) {
                setMarker(serverStack, enabled);
            }
        });
        context.getSource().sendFeedback(Component.literal(wasEnabled
            ? "Sınırsız kullanım kapatıldı." : "Eldeki eşya artık tüketilmeyecek."));
        return 1;
    }

    public static boolean isUnlimited(ItemStack stack) {
        if (!isPrivateSingleplayer(Minecraft.getInstance())) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean(MARKER).orElse(false);
    }

    private static boolean isPrivateSingleplayer(Minecraft client) {
        return client.player != null && client.level != null
            && client.getSingleplayerServer() != null
            && !client.getSingleplayerServer().isPublished();
    }

    private static void disableWhenLanOpens(Minecraft client) {
        MinecraftServer server = client.getSingleplayerServer();
        if (server == null || !server.isPublished()) {
            lanSafetyDisabled = false;
            return;
        }
        if (lanSafetyDisabled) return;

        // Fail closed immediately, then remove the saved marker so closing LAN
        // later cannot silently reactivate the item.
        lanSafetyDisabled = true;
        if (client.player != null) {
            clearMarkers(client.player.getInventory().getNonEquipmentItems());
            client.gui.chatListener().handleSystemMessage(Component.literal(
                "LAN açıldığı için ItemDuper kapatıldı; işaretli eşyalar artık tüketilebilir."), false);
            java.util.UUID playerId = client.player.getUUID();
            server.execute(() -> {
                ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerId);
                if (serverPlayer != null) clearMarkers(serverPlayer.getInventory().getNonEquipmentItems());
            });
        }
    }

    private static void clearMarkers(Iterable<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (isMarked(stack)) setMarker(stack, false);
        }
    }

    private static boolean isMarked(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean(MARKER).orElse(false);
    }

    private static void setMarker(ItemStack stack, boolean enabled) {
        CustomData oldData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = oldData == null ? new CompoundTag() : oldData.copyTag();
        if (enabled) tag.putBoolean(MARKER, true); else tag.remove(MARKER);
        stack.set(DataComponents.CUSTOM_DATA, tag.isEmpty() ? null : CustomData.of(tag));
    }
}
