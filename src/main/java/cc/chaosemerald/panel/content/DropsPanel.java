package cc.chaosemerald.panel.content;

import cc.chaosemerald.panel.data.DropRecord;
import cc.chaosemerald.panel.data.ImageLoader;
import cc.chaosemerald.panel.data.PanelRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DropsPanel extends JPanel {

    public DropsPanel(ImageLoader imageLoader, PanelRecord record){
        setLayout(new BorderLayout(7, 7));
        setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel dropPanelList = new JPanel();
        dropPanelList.setLayout(new BoxLayout(dropPanelList, BoxLayout.Y_AXIS));
        dropPanelList.setOpaque(false);

        for(DropRecord dropRecord : record.getDrops()){
            dropPanelList.add(new DropSection(imageLoader, dropRecord));
            dropPanelList.add(Box.createVerticalStrut(8));
        }

        add(dropPanelList, BorderLayout.NORTH);
    }


}
