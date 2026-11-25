package cc.chaosemerald.trackers.loot.raids;

import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.Text;

import java.util.ArrayList;
import java.util.List;

public final class RaidParty {

    private RaidParty(){}

    public static List<String> of(Client client, String source){
        String lower = source.toLowerCase();

        if(lower.contains("theatre of blood"))
            return fromVarcs(client, VarClientID.TOB_CLIENT_NAME0, 5);

        if(lower.contains("tombs of amascut"))
            return fromVarcs(client, VarClientID.TOA_CLIENT_NAME0, 8);

        if(lower.contains("fractured archive"))
            return fromVarcs(client, VarClientID.TFA_CLIENT_NAME0, 8);

        if(lower.contains("chambers of xeric"))
            return fromXericSidebar(client);

        return new ArrayList<>();
    }

    private static List<String> fromVarcs(Client client, int firstId, int count){
        List<String> names = new ArrayList<>();
        for(int i = 0; i < count; i++){
            String name = client.getVarcStrValue(firstId + i);
            if(name != null && !name.isEmpty())
                names.add(clean(name));
        }

        return names;
    }

    private static List<String> fromXericSidebar(Client client){
        List<String> names = new ArrayList<>();
        Widget list = client.getWidget(InterfaceID.RaidsSidepanel.LIST);
        if(list == null || list.getChildren() == null)
            return names;

        for(Widget child : list.getChildren()){
            String name = child.getName();
            if(name != null && !name.isEmpty())
                names.add(clean(name));
        }

        return names;
    }

    private static String clean(String name){
        return Text.removeTags(name).replace('\u00A0', ' ').trim();
    }

}