package cc.chaosemerald.panel.data;

import lombok.Value;

import java.util.List;

@Value
public class OTWEventRecord {

    String title;
    String subject;
    String icon;
    String startsAt;
    String endsAt;
    String timeLeft;
    List<Standing> top;

    @Value
    public static class Standing {
        int rank;
        String rsn;
        long gained;
        String gainedText;
        String place;
    }
}
