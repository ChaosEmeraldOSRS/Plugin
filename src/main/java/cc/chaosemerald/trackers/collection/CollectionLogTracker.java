package cc.chaosemerald.trackers.collection;

import cc.chaosemerald.ChaosEmeraldApiClient;
import cc.chaosemerald.trackers.Tracker;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class CollectionLogTracker implements Tracker {

    private static final Pattern COLLECTION_LOG = Pattern.compile("^New item added to your collection log: (?<item>.+?)\\.?$");

    private final ChaosEmeraldApiClient apiClient;
    private final Client client;

    @Inject
    CollectionLogTracker(ChaosEmeraldApiClient apiClient, Client client){
        this.apiClient = apiClient;
        this.client = client;
    }

    @Subscribe
    public void onChatMessage(ChatMessage e){
        if(e.getType() != ChatMessageType.GAMEMESSAGE)
            return;

        String message = e.getMessage();
        if(!message.toLowerCase().contains("item added to your collection log"))
            return;

        Matcher m = COLLECTION_LOG.matcher(Text.removeTags(message));
        if(!m.find())
            return;

        String itemName = m.group("item");
        int obtained = client.getVarpValue(VarPlayerID.COLLECTION_COUNT);
        int total = client.getVarpValue(VarPlayerID.COLLECTION_COUNT_MAX);

        apiClient.sendCollectionLogPayload(new CollectionLogRecord(itemName, null, null, obtained, total));
    }

}
