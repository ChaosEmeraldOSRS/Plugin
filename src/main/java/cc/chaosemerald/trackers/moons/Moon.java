package cc.chaosemerald.trackers.moons;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.VarbitID;

@Getter
@RequiredArgsConstructor
public enum Moon {

    BLOOD("Blood Moon", VarbitID.PMOON_BOSS_BLOOD_DEAD),
    BLUE("Blue Moon", VarbitID.PMOON_BOSS_BLUE_DEAD),
    ECLIPSE("Eclipse Moon", VarbitID.PMOON_BOSS_ECLIPSE_DEAD);

    private final String name;
    private final int deadVarbit;

}