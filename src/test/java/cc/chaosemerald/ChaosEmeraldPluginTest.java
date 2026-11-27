package cc.chaosemerald;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ChaosEmeraldPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(ChaosEmeraldPlugin.class);
        RuneLite.main(args);
    }
}