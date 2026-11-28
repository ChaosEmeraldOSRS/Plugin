package cc.chaosemerald;

import cc.chaosemerald.panel.ChaosEmeraldPanel;
import cc.chaosemerald.trackers.HeartbeatTracker;
import cc.chaosemerald.trackers.Tracker;
import cc.chaosemerald.trackers.collection.CollectionLogTracker;
import cc.chaosemerald.trackers.loot.barrows.BarrowsTracker;
import cc.chaosemerald.trackers.loot.clues.ClueTracker;
import cc.chaosemerald.trackers.clanbroadcast.ClanBroadcastTracker;
import cc.chaosemerald.trackers.loot.LootTracker;
import cc.chaosemerald.trackers.loot.moons.MoonsTracker;
import cc.chaosemerald.trackers.xp.XPTracker;
import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import javax.swing.SwingUtilities;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

@PluginDescriptor(
    name = "ChaosEmerald",
    description = "Helper for the ChaosEmerald clan website.",
    tags = {"clan", "loot", "coffer", "kc"}
)
public class ChaosEmeraldPlugin extends Plugin {

    @Inject
    private ChaosEmeraldConfig config;

    @Inject
    private ClientToolbar clientToolbar;
    private ChaosEmeraldPanel panel;
    private NavigationButton navButton;

    @Inject
    private EventBus eventBus;

    @Inject
    private HeartbeatTracker heartbeatTracker;

    @Inject
    private LootTracker lootTracker;

    @Inject
    private MoonsTracker moonsTracker;

    @Inject
    private BarrowsTracker barrowsTracker;

    @Inject
    private ClanBroadcastTracker broadcastTracker;

    @Inject
    private ClueTracker clueTracker;

    @Inject
    private CollectionLogTracker collectionLogTracker;

    @Inject
    private XPTracker xpTracker;

    @Inject
    private Client client;

    private boolean reloadPanel;
    private final Set<Tracker> trackerRegistry = new HashSet<>();

    @Override
    protected void startUp() throws Exception {
        if(config.sendLoot()){
            registerLoot();
        }
        if(config.sendClanBroadcasts()) {
            registerTracker(broadcastTracker);
        }
        if(config.sendXP()) {
            registerTracker(xpTracker);
        }

        checkHeartbeatRegistration();

        loadPanel();
    }

    public void registerLoot(){
        registerTracker(lootTracker);
        registerTracker(moonsTracker);
        registerTracker(barrowsTracker);
        registerTracker(clueTracker);
        registerTracker(collectionLogTracker);
    }

    @Override
    protected void shutDown() throws Exception {
        unregisterLoot();
        unregisterTracker(broadcastTracker);
        unregisterTracker(xpTracker);
        unregisterTracker(heartbeatTracker);

        unloadPanel();
    }

    public void unregisterLoot() {
        unregisterTracker(lootTracker);
        unregisterTracker(moonsTracker);
        unregisterTracker(barrowsTracker);
        unregisterTracker(clueTracker);
        unregisterTracker(collectionLogTracker);
    }

    public void checkHeartbeatRegistration(){
        // only load heartbeat if any tracker is enabled
        if(!config.sendLoot()
            && !config.sendXP()
            && !config.sendClanBroadcasts()
        ){
            unregisterTracker(heartbeatTracker);
        }else{
            registerTracker(heartbeatTracker);
        }
    }

    @Provides
    ChaosEmeraldConfig provideConfig(ConfigManager configManager){
        return configManager.getConfig(ChaosEmeraldConfig.class);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged e){
        if(!e.getGroup().equals(ChaosEmeraldConfig.GROUP))
            return;

        checkHeartbeatRegistration();

        switch(e.getKey()){
            case "showSidebar":
            case "sidebarPriority":
                unloadPanel();
                loadPanel();
                break;
            case "sendLoot":
                if(config.sendLoot()) {
                    registerLoot();
                }else{
                    unregisterLoot();
                }
                break;
            case "sendXP":
                if(config.sendXP()) {
                    registerTracker(xpTracker);
                }else{
                    unregisterTracker(xpTracker);
                }
                break;
            case "sendClanBroadcasts":
                if(config.sendClanBroadcasts()) {
                    registerTracker(broadcastTracker);
                }else{
                    unregisterTracker(broadcastTracker);
                }
                break;
        }
    }

    private void registerTracker(Tracker tracker){
        if(trackerRegistry.contains(tracker))
            return;

        eventBus.register(tracker);
        trackerRegistry.add(tracker);
        tracker.onEnable();
    }

    private void unregisterTracker(Tracker tracker){
        if(!trackerRegistry.contains(tracker))
            return;

        eventBus.unregister(tracker);
        trackerRegistry.remove(tracker);
        tracker.onDisable();
    }

    private void loadPanel(){
        if(!config.showSidebar() || navButton != null)
            return;

        if(panel == null)
            panel = injector.getInstance(ChaosEmeraldPanel.class);

        BufferedImage icon = ImageUtil.loadImageResource(getClass(), "panel_icon.png");

        navButton = NavigationButton.builder()
                .tooltip("ChaosEmerald")
                .icon(icon)
                .priority(config.sidebarPriority())
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    private void unloadPanel(){
        if(navButton == null)
            return;

        clientToolbar.removeNavigation(navButton);
        navButton = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e){
        if(e.getGameState() == GameState.LOGGED_IN)
            reloadPanel = true;
    }

    @Subscribe
    public void onGameTick(GameTick tick){
        if(!reloadPanel || client.getLocalPlayer() == null || client.getLocalPlayer().getName() == null)
            return;

        reloadPanel = false;
        if(navButton != null)
            SwingUtilities.invokeLater(panel::onActivate);
    }
}