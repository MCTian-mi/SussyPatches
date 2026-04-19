package dev.tianmi.sussypatches.common.helper;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.fml.common.IFMLSidedHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.StartupQuery;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.Side;

final class TestSidedHandler implements IFMLSidedHandler {

    @Override
    public List<String> getAdditionalBrandingInformation() {
        return Collections.emptyList();
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }

    @Override
    public void haltGame(String message, Throwable exception) {
        throw new RuntimeException(message, exception);
    }

    @Override
    public void showGuiScreen(Object clientGuiElement) {}

    @Override
    public void queryUser(StartupQuery query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void beginServerLoading(MinecraftServer server) {}

    @Override
    public void finishServerLoading() {}

    @Override
    public File getSavesDirectory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MinecraftServer getServer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDisplayCloseRequested() {
        return false;
    }

    @Override
    public boolean shouldServerShouldBeKilledQuietly() {
        return false;
    }

    @Override
    public void addModAsResource(ModContainer container) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCurrentLanguage() {
        return "en_US";
    }

    @Override
    public void serverStopped() {}

    @Override
    public NetworkManager getClientToServerNetworkManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public INetHandler getClientPlayHandler() {
        return null;
    }

    @Override
    public void fireNetRegistrationEvent(EventBus bus, NetworkManager manager, Set<String> channelSet,
                                         String channel, Side side) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean shouldAllowPlayerLogins() {
        return false;
    }

    @Override
    public void allowLogins() {}

    @Override
    public IThreadListener getWorldThread(INetHandler net) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processWindowMessages() {}

    @Override
    public String stripSpecialChars(String message) {
        return message;
    }

    @Override
    public void reloadRenderers() {}

    @Override
    public void fireSidedRegistryEvents() {}

    @Override
    public CompoundDataFixer getDataFixer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDisplayVSyncForced() {
        return false;
    }
}
