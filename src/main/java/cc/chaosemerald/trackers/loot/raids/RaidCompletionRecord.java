package cc.chaosemerald.trackers.loot.raids;

import lombok.Value;

import java.util.List;

@Value
public class RaidCompletionRecord {

    String source;
    int kc;
    List<String> party;

}