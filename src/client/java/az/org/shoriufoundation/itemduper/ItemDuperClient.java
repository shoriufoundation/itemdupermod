package az.org.shoriufoundation.itemduper;

import net.fabricmc.api.ClientModInitializer;

public final class ItemDuperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        UnlimitedItems.initialize();
    }
}
