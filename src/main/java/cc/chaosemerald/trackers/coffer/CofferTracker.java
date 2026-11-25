package cc.chaosemerald.trackers.coffer;

import cc.chaosemerald.ChaosEmeraldApiClient;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class CofferTracker {

    private static final Pattern FORMATTED = Pattern.compile(
            "^<int_0=(?<amount>\\d+)>(?<player>.+?) has (?<action>deposited|withdrawn) <int_0:spacer> coins? (?:into|from) the coffer\\.$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PLAIN = Pattern.compile(
            "^(?<player>.+?) has (?<action>deposited|withdrawn) (?<amount>[\\d,]+|one) coins? (?:into|from) the coffer\\.$",
            Pattern.CASE_INSENSITIVE);

    private final ChaosEmeraldApiClient apiClient;

    @Inject
    CofferTracker(ChaosEmeraldApiClient apiClient){
        this.apiClient = apiClient;
    }

    @Subscribe
    public void onChatMessage(ChatMessage e){
        if(e.getType() != ChatMessageType.CLAN_MESSAGE)
            return;

        String message = e.getMessage();
        if(!message.toLowerCase().contains("coffer"))
            return;

        Matcher matcher = FORMATTED.matcher(message);
        if(!matcher.find()){
            matcher = PLAIN.matcher(message);

            if(!matcher.find())
                return;
        }

        String player = Text.toJagexName(Text.removeTags(matcher.group("player")));
        String action = matcher.group("action").equalsIgnoreCase("deposited") ? "DEPOSIT" : "WITHDRAW";

        String rawAmount = matcher.group("amount");
        long amount = rawAmount.equalsIgnoreCase("one") ? 1 : Long.parseLong(rawAmount.replace(",", ""));

        apiClient.sendCofferPayload(new CofferRecord(player, action, amount));
    }

}
