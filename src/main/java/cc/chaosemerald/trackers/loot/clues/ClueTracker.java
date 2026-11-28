package cc.chaosemerald.trackers.loot.clues;

import cc.chaosemerald.ChaosEmeraldApiClient;
import cc.chaosemerald.trackers.Tracker;
import lombok.Value;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Singleton
public class ClueTracker implements Tracker {

    private static final String CLUE = "clue scroll (";
    private static final String CASKET = "reward casket (";

    private final ItemManager itemManager;
    private final ChaosEmeraldApiClient apiClient;

    private boolean seenInventory = false;
    private final Map<String, Integer> clueIds = new HashMap<>();
    private final Map<String, Integer> casketCounts = new HashMap<>();
    private final Map<String, Integer> steps = new HashMap<>();
    private final Set<String> partial = new HashSet<>();
    private final Map<Integer, Progress> putAway = new HashMap<>();

    @Inject
    ClueTracker(ItemManager itemManager, ChaosEmeraldApiClient apiClient){
        this.itemManager = itemManager;
        this.apiClient = apiClient;
    }

    public void onEnable(){}
    public void onDisable(){}

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged e){
        if(e.getContainerId() != InventoryID.INV)
            return;

        Map<String, Integer> newClueIds = new HashMap<>();
        Map<String, Integer> newCasketCounts = new HashMap<>();

        for(Item item : e.getItemContainer().getItems()){
            if(item.getId() <= 0)
                continue;

            String name = itemManager.getItemComposition(item.getId()).getName().toLowerCase();

            if(name.startsWith(CLUE)){
                newClueIds.put(tierOf(name, CLUE), item.getId());
            } else if(name.startsWith(CASKET)){
                newCasketCounts.merge(tierOf(name, CASKET), item.getQuantity(), Integer::sum);
            }
        }

        Set<String> tiers = new HashSet<>(clueIds.keySet());
        tiers.addAll(newClueIds.keySet());

        for(String tier : tiers){
            Integer before = clueIds.get(tier);
            Integer after = newClueIds.get(tier);

            if(before == null && after != null){
                clueAppeared(tier, after);
            } else if(before != null && after != null && !before.equals(after)){
                steps.merge(tier, 1, Integer::sum);
            } else if(before != null && after == null){
                clueGone(tier, before, newCasketCounts);
            }
        }

        clueIds.clear();
        clueIds.putAll(newClueIds);
        casketCounts.clear();
        casketCounts.putAll(newCasketCounts);
        seenInventory = true;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e){
        if(e.getGameState() == GameState.LOGIN_SCREEN){
            seenInventory = false;
            clueIds.clear();
            casketCounts.clear();
            steps.clear();
            partial.clear();
            putAway.clear();
        }
    }

    private void clueAppeared(String tier, int clueId){
        Progress saved = putAway.remove(clueId);
        if(saved != null){
            steps.put(tier, saved.getSteps());
            setPartial(tier, saved.isPartial());
            return;
        }

        steps.put(tier, 0);
        setPartial(tier, !seenInventory);
    }

    private void clueGone(String tier, int clueId, Map<String, Integer> newCasketCounts){
        boolean casketGained = newCasketCounts.getOrDefault(tier, 0) > casketCounts.getOrDefault(tier, 0);

        if(casketGained){
            int done = steps.getOrDefault(tier, 0) + 1;
            apiClient.sendClueCasketPayload(new ClueCasketRecord(tier, done, !partial.contains(tier)));
        } else {
            putAway.put(clueId, new Progress(steps.getOrDefault(tier, 0), partial.contains(tier)));
        }

        steps.remove(tier);
        partial.remove(tier);
    }

    private void setPartial(String tier, boolean isPartial){
        if(isPartial)
            partial.add(tier);
        else
            partial.remove(tier);
    }

    private static String tierOf(String name, String prefix){
        return name.substring(prefix.length(), name.length() - 1);
    }

    @Value
    private static class Progress {
        int steps;
        boolean partial;
    }

}
