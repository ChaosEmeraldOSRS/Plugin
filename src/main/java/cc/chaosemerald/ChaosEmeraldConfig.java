package cc.chaosemerald;

import net.runelite.client.config.*;

@ConfigGroup(ChaosEmeraldConfig.GROUP)
public interface ChaosEmeraldConfig extends Config {

    String GROUP = "ChaosEmerald";

    // Base URL
    @ConfigItem(
        keyName = "baseUrl",
        name = "Website URL",
        description = "The website the plugin talks to. Keep as default!"
    )

    default String baseUrl(){
        return "https://plugin.chaosemerald.cc";
    }
    // End Base URL

    // Website Sharing
    @ConfigSection(
        name = "Website Sharing",
        description = "Toggle which data is sent to the configured website URL",
        position = 1
    )
    String websiteDataSection = "websiteSharing";

    @ConfigItem(
        keyName = "sendLoot",
        name = "Send Loot",
        description = "Send the loot source, kill count, items gained and gear worn whenever you receive loot",
        section = websiteDataSection,
        position = 1
    )
    default boolean sendLoot(){
        return false;
    }

    @ConfigItem(
        keyName = "sendClanBroadcasts",
        name = "Send Clan Broadcasts",
        description = "Send any clan broadcasts seen in the chat",
        section = websiteDataSection,
        position = 2,
        warning = "This setting sends ALL clan broadcasts to the website which helps track coffer deposits and collection logs. It does not send clan player messages."
    )
    default boolean sendClanBroadcasts(){
        return false;
    }
    // End Website Sharing

    // Sidebar
    @ConfigSection(
        name = "Sidebar",
        description = "Sidebar settings",
        closedByDefault = true,
        position = 10
    )
    String sidebarSection = "sidebar";

    @ConfigItem(
        keyName = "showSidebar",
        name = "Show sidebar",
        description = "Show the ChaosEmerald panel",
        section = sidebarSection,
        position = 0
    )
    default boolean showSidebar(){
        return true;
    }

    @Range(min = 1, max = 100)
    @ConfigItem(
        keyName = "sidebarPriority",
        name = "Sidebar position",
        description = "Lower numbers sit higher in the sidebar",
        section = sidebarSection,
        position = 1
    )
    default int sidebarPriority(){
        return 5;
    }

    // End Sidebar
}
