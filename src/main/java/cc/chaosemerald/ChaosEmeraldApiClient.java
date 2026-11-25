package cc.chaosemerald;

import cc.chaosemerald.trackers.barrows.BarrowsChestRecord;
import cc.chaosemerald.trackers.clues.ClueCasketRecord;
import cc.chaosemerald.trackers.coffer.CofferRecord;
import cc.chaosemerald.trackers.loot.LootRecord;
import cc.chaosemerald.trackers.loot.raids.RaidCompletionRecord;
import cc.chaosemerald.trackers.moons.LunarChestRecord;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.clan.ClanChannel;
import okhttp3.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class ChaosEmeraldApiClient {

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final ChaosEmeraldConfig config;
    private final Client client;

    private boolean badUrlWarned = false;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Inject
    ChaosEmeraldApiClient(OkHttpClient okHttpClient, Gson gson, ChaosEmeraldConfig config, Client client){
        this.httpClient = okHttpClient;
        this.gson = gson;
        this.config = config;
        this.client = client;
    }

    @Nullable
    public HttpUrl getBaseUrl(){
        String configUrl = config.baseUrl().trim();
        String lower = configUrl.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://"))
            configUrl = "https://" + configUrl;

        HttpUrl url = HttpUrl.parse(configUrl);

        if(url == null){
            warnBadUrl();
            return null;
        }

        boolean isLocal = url.host().equals("localhost") || url.host().equals("127.0.0.1");
        if(url.scheme().equals("http") && !isLocal)
            url = url.newBuilder().scheme("https").build();

        return url;
    }

    private void warnBadUrl(){
        if(badUrlWarned)
            return;

        log.warn("ChaosEmerald website URL is invalid: {}", config.baseUrl());

        badUrlWarned = true;
    }

    public void sendKCPayload(LootRecord record){
        if(!config.sendLoot())
            return;

        post("v1/kc", record);
    }

    public void sendLunarChestPayload(LunarChestRecord record){
        if(!config.sendLoot())
            return;

        post("v1/lunar-chest", record);
    }

    public void sendBarrowsChestPayload(BarrowsChestRecord record){
        if(!config.sendLoot())
            return;

        post("v1/barrows-chest", record);
    }

    public void sendCofferPayload(CofferRecord record){
        if(!config.sendCoffer())
            return;

        post("v1/coffer", record);
    }

    public void sendClueCasketPayload(ClueCasketRecord record){
        if(!config.sendLoot())
            return;

        post("v1/clue-casket", record);
    }

    public void sendRaidCompletionPayload(RaidCompletionRecord record){
        if(!config.sendLoot())
            return;

        post("v1/raid-complete", record);
    }

    public void sendHeartbeat(){
        if(!config.sendLoot())
            return;

        post("v1/heartbeat", new JsonObject());
    }

    private void post(String path, Object payload){
        HttpUrl base = getBaseUrl();
        if (base == null){
            return;
        }

        ClanChannel clanChannel = client.getClanChannel();
        String clan = clanChannel != null ? clanChannel.getName() : null;

        Player player = client.getLocalPlayer();
        String rsn = player != null ? player.getName() : null;

        HttpUrl url = base.newBuilder().addPathSegments(path).build();

        JsonObject json = gson.toJsonTree(payload).getAsJsonObject();
        json.addProperty("id", UUID.randomUUID().toString());
        RequestBody body = RequestBody.create(JSON, gson.toJson(json));

        Request.Builder builder = new Request.Builder().url(url).post(body);
        if (clan != null){
            builder.header("X-Clan", clan);
        }
        if (rsn != null){
            builder.header("X-Rsn", rsn);
        }

        builder.header("X-World", String.valueOf(client.getWorld()));

        String worldTypes = client.getWorldType().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
        if (!worldTypes.isEmpty()){
            builder.header("X-World-Type", worldTypes);
        }

        Request request = builder.build();

        httpClient.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(@Nonnull Call call, @Nonnull IOException e){
                log.warn("POST {} failed", url, e);
            }

            @Override
            public void onResponse(@Nonnull Call call, @Nonnull Response response){
                try (response){
                    if (!response.isSuccessful()){
                        log.warn("POST {} returned {}", url, response.code());
                    }
                }
            }
        });
    }

}
