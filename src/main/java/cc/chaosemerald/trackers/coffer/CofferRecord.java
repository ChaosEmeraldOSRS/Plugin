package cc.chaosemerald.trackers.coffer;

import lombok.Value;

@Value
public class CofferRecord {

    String player;
    String action;
    long amount;

}