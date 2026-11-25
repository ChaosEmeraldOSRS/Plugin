package cc.chaosemerald.trackers.loot;

import cc.chaosemerald.ChaosEmeraldApiClient;
import cc.chaosemerald.trackers.loot.raids.RaidCompletionRecord;
import cc.chaosemerald.trackers.loot.raids.RaidParty;
import lombok.Value;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.util.Text;
import net.runelite.http.api.loottracker.LootRecordType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class LootTracker {

    private static final List<String> RAIDS = List.of(
            "chambers of xeric", "theatre of blood",
            "tombs of amascut", "fractured archive"
    );

    private static final int KC_MAX_AGE_TICKS = 10;
    private static final long PICKPOCKET_MIN_VALUE = 100_000;

    private final Client client;
    private final ItemManager itemManager;
    private final ChaosEmeraldApiClient apiClient;

    private final Map<String, KillCount> lastCounts = new HashMap<>();
    private final List<PendingLoot> pending = new ArrayList<>();

    private final Map<String, Integer> raidCounts = new HashMap<>();
    private final Set<String> sentRaidLoot = new HashSet<>();

    @Inject
    LootTracker(Client client, ItemManager itemManager, ChaosEmeraldApiClient apiClient){
        this.client = client;
        this.itemManager = itemManager;
        this.apiClient = apiClient;
    }

    @Subscribe
    public void onChatMessage(ChatMessage e){
        if(e.getType() != ChatMessageType.GAMEMESSAGE)
            return;

        KCParser.ParsedCount parsed = KCParser.parse(Text.removeTags(e.getMessage()));
        if(parsed == null)
            return;

        String key = parsed.getSource().toLowerCase();
        if(isRaid(key)){
            raidCounts.put(key, parsed.getCount());
            List<String> party = RaidParty.of(client, parsed.getSource());
            apiClient.sendRaidCompletionPayload(new RaidCompletionRecord(parsed.getSource(), parsed.getCount(), party));
            return;
        }

        lastCounts.put(key, new KillCount(parsed.getCount(), client.getTickCount()));
    }

    @Subscribe
    public void onLootReceived(LootReceived e){
        if (e.getType() == LootRecordType.PLAYER)
            return;

        if(e.getType() == LootRecordType.PICKPOCKET && !hasValuableStack(e.getItems()))
            return;

        LootRecord.LootRecordBuilder lootBuilder = LootRecord.builder()
                .source(e.getName())
                .type(e.getType().name())
                .equipment(readEquipment())
                .inventory(readInventory());

        if("Doom of Mokhaiotl".equals(e.getName()) && e.getMetadata() instanceof Integer)
            lootBuilder.delveLevel((Integer) e.getMetadata());

        for (ItemStack stack : e.getItems()){
            lootBuilder.item(buildItem(stack.getId(), stack.getQuantity()));
        }

        pending.add(new PendingLoot(lootBuilder, e.getName(), client.getTickCount()));
    }

    @Subscribe
    public void onGameTick(GameTick e){
        int now = client.getTickCount();

        Iterator<PendingLoot> it = pending.iterator();
        while(it.hasNext()){
            PendingLoot loot = it.next();
            if(now <= loot.getTick())
                continue;

            String key = loot.getSource().toLowerCase();

            if(isRaid(key)){
                Integer raidKc = raidCounts.remove(key);
                if(raidKc != null){
                    if(!sentRaidLoot.add(key + ":" + raidKc)){
                        it.remove();
                        continue;
                    }
                    loot.getBuilder().kc(raidKc);
                }
            } else {
                KillCount kc = lastCounts.remove(key);
                if(kc != null && (loot.getTick() - kc.getTick()) <= KC_MAX_AGE_TICKS)
                    loot.getBuilder().kc(kc.getCount());
            }

            apiClient.sendKCPayload(loot.getBuilder().build());
            it.remove();
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e){
        if(e.getGameState() == GameState.LOGIN_SCREEN){
            pending.clear();
            lastCounts.clear();
        }
    }

    private Map<String, LootRecord.Item> readEquipment(){
        Map<String, LootRecord.Item> equipment = new LinkedHashMap<>();

        ItemContainer worn = client.getItemContainer(InventoryID.WORN);
        if(worn == null)
            return equipment;

        for(EquipmentInventorySlot slot : EquipmentInventorySlot.values()){
            Item item = worn.getItem(slot.getSlotIdx());
            if(item != null && item.getId() > 0)
                equipment.put(slot.name(), buildItem(item.getId(), item.getQuantity()));
        }

        return equipment;
    }

    private List<LootRecord.Item> readInventory(){
        List<LootRecord.Item> inventory = new ArrayList<>();
        ItemContainer inv = client.getItemContainer(InventoryID.INV);
        if(inv == null)
            return inventory;

        for(Item item : inv.getItems()){
            if(item.getId() > 0)
                inventory.add(buildItem(item.getId(), item.getQuantity()));
        }

        return inventory;
    }

    private LootRecord.Item buildItem(int id, int quantity){
        String name = itemManager.getItemComposition(id).getName();
        long value = itemManager.getItemPrice(id) * quantity;

        return new LootRecord.Item(id, name, quantity, value);
    }

    private static boolean isRaid(String source){
        for(String raid : RAIDS){
            if(source.contains(raid))
                return true;
        }
        return false;
    }

    private boolean hasValuableStack(Collection<ItemStack> items){
        for(ItemStack stack : items){
            if(itemManager.getItemPrice(stack.getId()) * stack.getQuantity() >= PICKPOCKET_MIN_VALUE)
                return true;
        }
        return false;
    }

    @Value
    private static class KillCount {
        int count;
        int tick;
    }

    @Value
    private static class PendingLoot {
        LootRecord.LootRecordBuilder builder;
        String source;
        int tick;
    }
}
