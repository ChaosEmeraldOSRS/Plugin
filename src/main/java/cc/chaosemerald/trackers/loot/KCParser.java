package cc.chaosemerald.trackers.loot;

import lombok.Value;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class KCParser {

    private static final Pattern COUNT = Pattern.compile("^Your (?<subject>.+?) count is: ?(?<count>[\\d,]+)");
    private static final Pattern CLUE = Pattern.compile("^You have completed (?<count>[\\d,]+) (?<tier>[a-z]+) Treasure Trails?");

    private static final Map<String, String> RENAMES = Map.of(
            "barrows chest", "Barrows",
            "lunar chest", "Lunar Chest",
            "gauntlet completion", "Crystalline Hunllef",
            "corrupted gauntlet completion", "Corrupted Hunllef");

    private static final List<String> PREFIXES = List.of("completed ", "subdued ");
    private static final List<String> SUFFIXES = List.of(" kill", " success", " harvest", " opened", " lap");

    private KCParser(){}

    @Value
    static class ParsedCount {
        String source;
        int count;
    }

    @Nullable
    static ParsedCount parse(String message){
        Matcher clue = CLUE.matcher(message);
        if(clue.find())
            return result("Clue Scroll (" + clue.group("tier") + ")", clue.group("count"));

        Matcher count = COUNT.matcher(message);
        if(count.find())
            return result(source(count.group("subject")), count.group("count"));

        return null;
    }

    @Nullable
    private static String source(String subject){
        String lower = subject.toLowerCase();

        String renamed = RENAMES.get(lower);
        if(renamed != null)
            return renamed;

        for(String prefix : PREFIXES){
            if(lower.startsWith(prefix))
                return subject.substring(prefix.length());
        }

        for(String suffix : SUFFIXES){
            if(lower.endsWith(suffix))
                return subject.substring(0, subject.length() - suffix.length());
        }

        return null;
    }

    @Nullable
    private static ParsedCount result(@Nullable String source, String count){
        if(source == null)
            return null;

        try {
            return new ParsedCount(source, Integer.parseInt(count.replace(",", "")));
        } catch(NumberFormatException e){
            return null;
        }
    }
}
