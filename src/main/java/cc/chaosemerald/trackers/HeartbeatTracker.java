package cc.chaosemerald.trackers;

import cc.chaosemerald.ChaosEmeraldConfig;
import cc.chaosemerald.ChaosEmeraldApiClient;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HeartbeatTracker implements Tracker {

    private static final int CHECK_IN_TICKS = 1000;

    private final Client client;
    private final ChaosEmeraldConfig config;
    private final ChaosEmeraldApiClient apiClient;

    private int lastCheckInTick = -1;

    @Inject
    HeartbeatTracker(Client client, ChaosEmeraldConfig config, ChaosEmeraldApiClient apiClient){
        this.client = client;
        this.config = config;
        this.apiClient = apiClient;
    }

    @Subscribe
    public void onGameTick(GameTick e){
        if(!config.sendLoot() || client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
            return;

        int now = client.getTickCount();
        if(lastCheckInTick != -1 && now - lastCheckInTick < CHECK_IN_TICKS)
            return;

        lastCheckInTick = now;
        apiClient.sendHeartbeat();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e){
        if(e.getGameState() == GameState.LOGIN_SCREEN)
            lastCheckInTick = -1;
    }
}