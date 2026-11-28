package cc.chaosemerald.trackers.collection;

import lombok.Value;

@Value
public class CollectionLogRecord {

    String itemName;
    Integer itemId;
    String source;
    Integer obtained;
    Integer total;

}