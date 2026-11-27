package cc.chaosemerald.panel.data;

import lombok.Value;

@Value
public class DropRecord {

    String id;
    String rsn;
    String item;
    String icon;
    String kind;
    long value;
    String valueText;
    String createdAt;
    String ago;

}
