package cc.chaosemerald.trackers.loot.barrows;

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
public class BarrowsTracker {

    private static final int CRYPT_REGION = 14231;

    private final Client client;
    private final ChaosEmeraldApiClient apiClient;

    private final Set<BarrowsBrother> brothersBeaten = EnumSet.noneOf(BarrowsBrother.class);
    private int lastChestCount = -1;

    @Inject
    BarrowsTracker(Client client, ChaosEmeraldApiClient apiClient){
        this.client = client;
        this.apiClient = apiClient;
    }

    @Subscribe
    public void onGameTick(GameTick e){
        brothersBeaten.clear();

        Player player = client.getLocalPlayer();
        if(player == null || player.getWorldLocation().getRegionID() != CRYPT_REGION)
            return;

        for(BarrowsBrother brother : BarrowsBrother.values()){
            if(client.getVarbitValue(brother.getDeadVarbit()) == 1)
                brothersBeaten.add(brother);
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged e){
        if(e.getVarpId() != VarPlayerID.TOTAL_BARROWS_CHESTS)
            return;

        int chestCount = e.getValue();
        boolean opened = lastChestCount != -1 && chestCount == lastChestCount + 1;
        lastChestCount = chestCount;

        if(!opened)
            return;

        List<String> brothersBeatenList = brothersBeaten.stream()
                .map(BarrowsBrother::getName)
                .collect(Collectors.toList());

        apiClient.sendBarrowsChestPayload(new BarrowsChestRecord(chestCount, brothersBeatenList));
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e){
        if(e.getGameState() == GameState.LOGIN_SCREEN)
            lastChestCount = -1;
    }
}
