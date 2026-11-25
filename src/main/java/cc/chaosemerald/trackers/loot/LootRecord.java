package cc.chaosemerald.trackers.loot;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class LootRecord {

    String source;
    String type;
    Integer kc;
    Integer delveLevel;

    @Singular
    List<Item> items;

    Map<String, Item> equipment;
    List<Item> inventory;

    @Value
    public static class Item {
        int id;
        String name;
        int quantity;
        long value;
    }

}