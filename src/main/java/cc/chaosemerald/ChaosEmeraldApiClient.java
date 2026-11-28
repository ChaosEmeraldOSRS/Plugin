package cc.chaosemerald;

import cc.chaosemerald.panel.data.PanelRecord;
import cc.chaosemerald.trackers.collection.CollectionLogRecord;
import cc.chaosemerald.trackers.loot.barrows.BarrowsChestRecord;
import cc.chaosemerald.trackers.loot.clues.ClueCasketRecord;
import cc.chaosemerald.trackers.clanbroadcast.ClanBroadcastRecord;
import cc.chaosemerald.trackers.loot.LootRecord;
import cc.chaosemerald.trackers.loot.raids.RaidCompletionRecord;
import cc.chaosemerald.trackers.loot.moons.LunarChestRecord;
import cc.chaosemerald.trackers.pet.PetRecord;
import cc.chaosemerald.trackers.xp.XPRecord;
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
import javax.swing.*;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;
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
        if(!lower.startsWith("http://") && !lower.startsWith("https://"))
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

    // POST Requests
    public void sendKCPayload(LootRecord record){
        post("v1/kc", record);
    }

    public void sendLunarChestPayload(LunarChestRecord record){
        post("v1/lunar-chest", record);
    }

    public void sendBarrowsChestPayload(BarrowsChestRecord record){
        post("v1/barrows-chest", record);
    }

    public void sendClanBroadcastPayload(ClanBroadcastRecord record){
        post("v1/clan-broadcast", record);
    }

    public void sendCollectionLogPayload(CollectionLogRecord record){
        post("v1/collection-log", record);
    }

    public void sendPetPayload(PetRecord record){
        post("v1/pet", record);
    }

    public void sendXPPayload(XPRecord record){
        post("v1/xp", record);
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
        post("v1/heartbeat", new JsonObject());
    }
    // END POST Requests

    // GET Requests
    public void fetchPanel(Consumer<PanelRecord> content, Runnable failed){
        get("v1/panel", PanelRecord.class, content, failed);
    }
    // END GET Requests

    private void post(String path, Object payload){
        HttpUrl base = getBaseUrl();
        if(base == null)
            return;

        HttpUrl url = base.newBuilder().addPathSegments(path).build();

        JsonObject json = gson.toJsonTree(payload).getAsJsonObject();
        json.addProperty("id", UUID.randomUUID().toString());

        RequestBody body = RequestBody.create(JSON, gson.toJson(json));

        Request request = getPlayerDetails(url).post(body).build();

        httpClient.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(@Nonnull Call call, @Nonnull IOException e){
                log.warn("POST {} failed", url, e);
            }

            @Override
            public void onResponse(@Nonnull Call call, @Nonnull Response response){
                try(response){
                    if(!response.isSuccessful()){
                        log.warn("POST {} returned {}", url, response.code());
                    }
                }
            }
        });
    }

    private <T> void get(String path, Class<T> type, Consumer<T> content, Runnable failure){
        HttpUrl base = getBaseUrl();
        if(base == null)
            return;

        HttpUrl url = base.newBuilder().addPathSegments(path).build();
        Request request = getPlayerDetails(url).build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn("GET {} failed", url, e);
                SwingUtilities.invokeLater(failure);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try(response){
                    if(!response.isSuccessful() || response.body() == null){
                        log.warn("GET {} returned {}", url, response.code());
                        SwingUtilities.invokeLater(failure);
                        return;
                    }

                    T result = gson.fromJson(response.body().charStream(), type);
                    SwingUtilities.invokeLater(() -> content.accept(result));
                }
            }
        });
    }

    private Request.Builder getPlayerDetails(HttpUrl url){
        ClanChannel clanChannel = client.getClanChannel();
        String clan = clanChannel != null ? clanChannel.getName() : null;

        Player player = client.getLocalPlayer();
        String rsn = player != null ? player.getName() : null;

        Request.Builder builder = new Request.Builder().url(url);
        if(clan != null)
            builder.header("X-Clan", clan);

        if(rsn != null)
            builder.header("X-Rsn", rsn);

        builder.header("X-World", String.valueOf(client.getWorld()));

        String worldTypes = client.getWorldType().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        if(!worldTypes.isEmpty())
            builder.header("X-World-Type", worldTypes);

        return builder;
    }

}
