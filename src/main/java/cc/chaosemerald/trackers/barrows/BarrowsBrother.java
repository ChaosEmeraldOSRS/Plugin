package cc.chaosemerald.trackers.barrows;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.VarbitID;

@Getter
@RequiredArgsConstructor
public enum BarrowsBrother {

    AHRIM("Ahrim the Blighted", VarbitID.BARROWS_KILLED_AHRIM),
    DHAROK("Dharok the Wretched", VarbitID.BARROWS_KILLED_DHAROK),
    GUTHAN("Guthan the Infested", VarbitID.BARROWS_KILLED_GUTHAN),
    KARIL("Karil the Tainted", VarbitID.BARROWS_KILLED_KARIL),
    TORAG("Torag the Corrupted", VarbitID.BARROWS_KILLED_TORAG),
    VERAC("Verac the Defiled", VarbitID.BARROWS_KILLED_VERAC);

    private final String name;
    private final int deadVarbit;

}
