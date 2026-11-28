package cc.chaosemerald.trackers.coffer;

import cc.chaosemerald.ChaosEmeraldApiClient;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClanBroadcastTracker {

    private final ChaosEmeraldApiClient apiClient;

    @Inject
    ClanBroadcastTracker(ChaosEmeraldApiClient apiClient){
        this.apiClient = apiClient;
    }

    @Subscribe
    public void onChatMessage(ChatMessage e) {
        if(e.getType() != ChatMessageType.CLAN_MESSAGE)
            return;

        String message = e.getMessage();

        apiClient.sendClanBroadcast(new ClanBroadcastRecord(message));
    }

}
