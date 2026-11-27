package cc.chaosemerald.panel.data;

import lombok.Value;

@Value
public class EventRecord {

    int id;
    String title;
    String location;
    Integer world;
    String startsAt;
    String endsAt;
    String icon;

}
