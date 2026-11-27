package cc.chaosemerald;

import cc.chaosemerald.panel.ChaosEmeraldPanel;
import cc.chaosemerald.trackers.HeartbeatTracker;
import cc.chaosemerald.trackers.barrows.BarrowsTracker;
import cc.chaosemerald.trackers.clues.ClueTracker;
import cc.chaosemerald.trackers.coffer.CofferTracker;
import cc.chaosemerald.trackers.loot.LootTracker;
import cc.chaosemerald.trackers.moons.MoonsTracker;
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

import java.awt.image.BufferedImage;
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
    private CofferTracker cofferTracker;

    @Inject
    private ClueTracker clueTracker;

    @Override
    protected void startUp() throws Exception {
        eventBus.register(heartbeatTracker);
        eventBus.register(lootTracker);
        eventBus.register(moonsTracker);
        eventBus.register(barrowsTracker);
        eventBus.register(cofferTracker);
        eventBus.register(clueTracker);

        loadPanel();
    }

    @Override
    protected void shutDown() throws Exception {
        eventBus.unregister(lootTracker);
        eventBus.unregister(moonsTracker);
        eventBus.unregister(barrowsTracker);
        eventBus.unregister(cofferTracker);
        eventBus.unregister(clueTracker);
        eventBus.unregister(heartbeatTracker);

        unloadPanel();
    }

    @Provides
    ChaosEmeraldConfig provideConfig(ConfigManager configManager){
        return configManager.getConfig(ChaosEmeraldConfig.class);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged e){
        if(!e.getGroup().equals(ChaosEmeraldConfig.GROUP))
            return;

        switch(e.getKey()){
            case "showSidebar":
            case "sidebarPriority":
                unloadPanel();
                loadPanel();
                break;
        }
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
}