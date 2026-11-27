package cc.chaosemerald.panel;

import cc.chaosemerald.panel.data.DropRecord;
import cc.chaosemerald.panel.data.ImageLoader;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;

public class DropSection extends JPanel {

    DropSection(ImageLoader imageLoader, DropRecord record){
        setLayout(new BorderLayout(7, 7));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JPanel dropHeader = new JPanel(new BorderLayout(7, 0));
        dropHeader.setOpaque(false);

        JPanel dropDetails = new JPanel(new BorderLayout());
        dropDetails.setOpaque(false);
        JLabel name = new JLabel(record.getItem());
        name.setFont(FontManager.getRunescapeBoldFont());
        name.setForeground(ColorScheme.LIGHT_GRAY_COLOR.brighter());
        dropDetails.add(name, BorderLayout.CENTER);

        JLabel otwTimeLeft = new JLabel(record.getRsn() + " · " + record.getAgo());
        otwTimeLeft.setForeground(ColorScheme.MEDIUM_GRAY_COLOR.brighter().brighter());
        dropDetails.add(otwTimeLeft, BorderLayout.SOUTH);

        // Avatar Icon
        JLabel avatarIcon = new JLabel();
        avatarIcon.setOpaque(true);
        avatarIcon.setBackground(ColorScheme.DARK_GRAY_COLOR);
        avatarIcon.setBorder(new EmptyBorder(4, 4, 4, 4));
        avatarIcon.setPreferredSize(new Dimension(32, 32));

        imageLoader.load(record.getIcon(), image -> {
            Image sharp = new BaseMultiResolutionImage(
                    ImageUtil.resizeImage(image, 24, 24),
                    ImageUtil.resizeImage(image, 36, 36),
                    ImageUtil.resizeImage(image, 48, 48));
            avatarIcon.setIcon(new ImageIcon(sharp));
        });

        dropHeader.add(avatarIcon, BorderLayout.WEST);
        dropHeader.add(dropDetails, BorderLayout.CENTER);
        add(dropHeader, BorderLayout.CENTER);
    }

}
