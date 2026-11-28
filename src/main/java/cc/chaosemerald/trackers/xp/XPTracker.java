package cc.chaosemerald.trackers.xp;

import cc.chaosemerald.ChaosEmeraldApiClient;
import cc.chaosemerald.trackers.Tracker;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Singleton
public class XPTracker implements Tracker {

    private final Map<Skill, Integer> xp = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> levels = new EnumMap<>(Skill.class);
    private final Set<Skill> changed = EnumSet.noneOf(Skill.class);
    private Instant lastSent = Instant.now();
    private boolean fullSkillsSent;
    private boolean loginBatch;

    private final ChaosEmeraldApiClient apiClient;
    private final Client client;
    private final ClientThread clientThread;

    @Inject
    public XPTracker(ChaosEmeraldApiClient apiClient, Client client, ClientThread clientThread){
        this.apiClient = apiClient;
        this.client = client;
        this.clientThread = clientThread;
    }

    @Override
    public void onEnable(){
        clientThread.invoke(() -> {
            if(client.getGameState() != GameState.LOGGED_IN)
                return;

            for(Skill skill : Skill.values()){
                xp.put(skill, client.getSkillExperience(skill));
                levels.put(skill, client.getRealSkillLevel(skill));
            }
            fullSkillsSent = false;
            loginBatch = true;
        });
    }

    @Override
    public void onDisable(){}

    @Subscribe
    public void onStatChanged(StatChanged e){
        Skill skill = e.getSkill();
        Integer previousXp = xp.put(skill, e.getXp());
        Integer previousLevel = levels.put(skill, e.getLevel());

        if(previousXp == null){
            loginBatch = true;
            return;
        }

        if(e.getXp() <= previousXp)
            return;

        changed.add(skill);
        if(previousLevel == null || e.getLevel() <= previousLevel)
            return;

        sendBatch();
    }

    @Subscribe
    public void onGameTick(GameTick e){
        if(loginBatch){
            if(client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null && client.getClanChannel() != null){
                loginBatch = false;
                sendBatch();
                return;
            }
        }
        if(changed.isEmpty() || Duration.between(lastSent, Instant.now()).toMinutes() < 3)
            return;

        sendBatch();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e){
        if(e.getGameState() == GameState.LOGIN_SCREEN || e.getGameState() == GameState.HOPPING){
            reset();
        }
    }

    private void reset(){
        xp.clear();
        levels.clear();
        changed.clear();
        fullSkillsSent = false;
        loginBatch = false;
    }

    private void sendBatch(){
        if(fullSkillsSent && changed.isEmpty())
            return;

        Map<String, Integer> skills = new HashMap<>();
        for(Skill skill : fullSkillsSent ? changed : xp.keySet()){
            skills.put(skill.getName(), xp.get(skill));
        }

        if(skills.isEmpty())
            return;

        apiClient.sendXPPayload(new XPRecord(skills));
        changed.clear();
        fullSkillsSent = true;

        lastSent = Instant.now();
    }
}
