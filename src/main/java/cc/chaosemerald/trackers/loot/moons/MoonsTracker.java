package cc.chaosemerald.trackers.loot.moons;

import cc.chaosemerald.ChaosEmeraldApiClient;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class MoonsTracker {

    private final Client client;
    private final ChaosEmeraldApiClient apiClient;

    private final Set<Moon> beatenMoons = EnumSet.noneOf(Moon.class);
    private int lastChestCount = -1;

    private static final int LUNAR_CHEST_REGION = 6037;

    @Inject
    MoonsTracker(Client client, ChaosEmeraldApiClient apiClient){
        this.client = client;
        this.apiClient = apiClient;
    }

    @Subscribe
    public void onGameTick(GameTick e){
        beatenMoons.clear();

        Player p = client.getLocalPlayer();
        if(p == null || p.getWorldLocation().getRegionID() != LUNAR_CHEST_REGION)
            return;

        for(Moon moon : Moon.values()){
            if(client.getVarbitValue(moon.getDeadVarbit()) == 1)
                beatenMoons.add(moon);
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged e){
        if(e.getVarpId() != VarPlayerID.TOTAL_PMOON_CHESTS)
            return;

        int chestCount = e.getValue();
        boolean opened = lastChestCount != -1 && chestCount == lastChestCount + 1;
        lastChestCount = chestCount;

        if(!opened)
            return;

        List<String> beatenMoonsList = beatenMoons.stream()
                .map(Moon::getName)
                .collect(Collectors.toList());

        apiClient.sendLunarChestPayload(new LunarChestRecord(chestCount, beatenMoonsList));
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e){
        if(e.getGameState() == GameState.LOGIN_SCREEN)
            lastChestCount = -1;
    }

}
