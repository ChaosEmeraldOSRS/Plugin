package cc.chaosemerald.trackers.pet;

import cc.chaosemerald.ChaosEmeraldApiClient;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.CollationElementIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class PetTracker {

    private static final Pattern PET = Pattern.compile("^You (?:have a funny feeling like you|feel something weird sneaking)");
    private static final Pattern UNTRADEABLE = Pattern.compile("^Untradeable drop: (?<item>.+?)\\.?$");
    private static final Pattern COLLECTION_LOG = Pattern.compile("^New item added to your collection log: (?<item>.+?)\\.?$");
    private static final int MAX_TICKS_WAIT = 5;

    private static boolean waitingForName;
    private boolean isDuplicate;
    private String petName;
    private int ticksWaited;

    private final ChaosEmeraldApiClient apiClient;

    @Inject
    PetTracker(ChaosEmeraldApiClient apiClient){
        this.apiClient = apiClient;
    }

    public void onEnable(){}
    public void onDisable(){}

    @Subscribe
    public void onChatMessage(ChatMessage e){
        String message = Text.removeTags(e.getMessage());

        if(e.getType() != ChatMessageType.GAMEMESSAGE)
            return;

        if(PET.matcher(message).find()){
            waitingForName = true;

            if(message.contains("would have been"))
                isDuplicate = true;

            petName = null;
            ticksWaited = 0;
            return;
        }

        if(!waitingForName)
            return;

        Matcher m = UNTRADEABLE.matcher(message);

        if(!m.find())
            m = COLLECTION_LOG.matcher(message);

        if(m.find())
            petName = m.group("item");
    }

    @Subscribe
    public void onGameTick(GameTick e){
        if(!waitingForName)
            return;

        ticksWaited++;
        if(petName == null && ticksWaited <= MAX_TICKS_WAIT)
            return;

        apiClient.sendPetPayload(new PetRecord(petName, isDuplicate));
        waitingForName = false;
    }

}
