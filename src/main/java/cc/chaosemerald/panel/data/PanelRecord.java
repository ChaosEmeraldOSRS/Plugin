package cc.chaosemerald.panel.data;

import lombok.Value;

import java.util.List;

@Value
public class PanelRecord {

    ProfileRecord profile;
    OTWEventRecord bossOfTheWeek;
    OTWEventRecord skillOfTheWeek;
    List<EventRecord> events;
    List<DropRecord> drops;

}
