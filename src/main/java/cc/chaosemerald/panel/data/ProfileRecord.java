package cc.chaosemerald.panel.data;

import lombok.Value;

@Value
public class ProfileRecord {

    String rsn;
    boolean found;

    String avatarUrl;

    String rank;
    int favour;

    String currentRank;
    int currentRankFavour;

    String nextRank;
    int nextRankFavour;

    String rankIcon;

}
