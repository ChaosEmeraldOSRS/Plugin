package cc.chaosemerald.panel;

import cc.chaosemerald.panel.data.EventRecord;
import cc.chaosemerald.panel.data.ImageLoader;
import cc.chaosemerald.panel.data.PanelRecord;
import cc.chaosemerald.panel.data.ProfileRecord;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.ProgressBar;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;

public class EventsPanel extends JPanel {

    EventsPanel(ImageLoader imageLoader, PanelRecord record){
        setLayout(new BorderLayout(7, 7));
        setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel eventPanelList = new JPanel();
        eventPanelList.setLayout(new BoxLayout(eventPanelList, BoxLayout.Y_AXIS));
        eventPanelList.setOpaque(false);

        eventPanelList.add(new OTWSection(imageLoader, record.getBossOfTheWeek()));
        eventPanelList.add(Box.createVerticalStrut(8));
        eventPanelList.add(new OTWSection(imageLoader, record.getSkillOfTheWeek()));
        eventPanelList.add(Box.createVerticalStrut(8));

        for(EventRecord eventRecord : record.getEvents()){
            eventPanelList.add(new EventSection(imageLoader, eventRecord));
            eventPanelList.add(Box.createVerticalStrut(8));
        }

        add(eventPanelList, BorderLayout.NORTH);
    }


}
