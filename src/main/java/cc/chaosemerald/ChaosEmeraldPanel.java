package cc.chaosemerald;

import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

public class ChaosEmeraldPanel extends PluginPanel {

    @Inject
    ChaosEmeraldPanel(){
        setLayout(new BorderLayout());
        add(new JLabel("ChaosEmerald"), BorderLayout.NORTH);
    }

}
