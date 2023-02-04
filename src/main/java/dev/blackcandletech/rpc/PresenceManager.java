package dev.blackcandletech.rpc;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PresenceManager {

    private final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private final ArrayList<DiscordRichPresence> presences = new ArrayList<>();

    private DiscordEventHandlers eventHandlers;
    private JSONFile configFile;

    public static void main(String[] args) {
        new PresenceManager().initialize();
    }

    public PresenceManager () {
        Runtime.getRuntime().addShutdownHook(shutdown());

        try {
            this.configFile = new JSONFile("config.json", true);
        } catch (ParseException e) {
            System.out.println("Exception while trying to parse config.json from resources: " + e.getMessage());
            return;
        }

        this.eventHandlers = new DiscordEventHandlers.Builder().setReadyEventHandler((user) -> {
            System.out.printf("Welcome %s#%s!%n", user.username, user.discriminator);
        }).build();

        schedulePresenceUpdates();

        SCHEDULER.scheduleAtFixedRate(callGarbageCollection(), 10, 10, TimeUnit.MINUTES);
    }

    public void initialize() {
        DiscordRPC.discordInitialize("1061717435574657044", this.eventHandlers, true);
        scheduleCallbacks();
    }

    private void scheduleCallbacks() {
        SCHEDULER.scheduleAtFixedRate(DiscordRPC::discordRunCallbacks, 0, 30, TimeUnit.SECONDS);
    }

    private void schedulePresenceUpdates() {
        JSONObject presences = (JSONObject) configFile.getValue("presences");
        for (Object key : presences.keySet()) {
            JSONObject obj = (JSONObject) presences.get(key);

            String state = (String) obj.get("state");
            String details = (String) obj.get("details");
            JSONObject largeImage = (JSONObject) obj.get("largeImage");
            JSONObject smallImage = (JSONObject) obj.get("smallImage");

            DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder(state);
            if(details != null) builder.setDetails(details);

            if(largeImage != null) {
                String imageKey = (String) largeImage.get("key");
                String imageText = (String) largeImage.get("text");
                builder.setBigImage(imageKey, imageText);
            }

            if(smallImage != null) {
                String imageKey = (String) smallImage.get("key");
                String imageText = (String) smallImage.get("text");
                builder.setSmallImage(imageKey, imageText);
            }

             this.presences.add(builder.build());
        }

        SCHEDULER.scheduleAtFixedRate(() -> {
            int choice = new Random().nextInt(this.presences.size());
            DiscordRPC.discordUpdatePresence(this.presences.get(choice));
        }, 0, 30, TimeUnit.SECONDS);
    }

    public Runnable callGarbageCollection() {
        return System::gc;
    }

    public Thread shutdown() {
        return new Thread(DiscordRPC::discordShutdown);
    }

}
