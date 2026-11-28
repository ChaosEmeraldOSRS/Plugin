package cc.chaosemerald.panel.content;

import cc.chaosemerald.panel.data.ImageLoader;
import cc.chaosemerald.panel.data.OTWEventRecord;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;

public class OTWSection extends JPanel {

    public OTWSection(ImageLoader imageLoader, OTWEventRecord record){
        setLayout(new BorderLayout(7, 7));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JPanel eventHeader = new JPanel(new BorderLayout(7, 0));
        eventHeader.setOpaque(false);

        JPanel eventDetails = new JPanel(new BorderLayout());
        eventDetails.setOpaque(false);
        JLabel name = new JLabel(record.getTitle());
        name.setFont(FontManager.getRunescapeBoldFont());
        name.setForeground(ColorScheme.LIGHT_GRAY_COLOR.brighter());
        eventDetails.add(name, BorderLayout.CENTER);

        JLabel otwTimeLeft = new JLabel(record.getSubject() + " · " + record.getTimeLeft());
        otwTimeLeft.setForeground(ColorScheme.MEDIUM_GRAY_COLOR.brighter().brighter());
        eventDetails.add(otwTimeLeft, BorderLayout.SOUTH);

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

        eventHeader.add(avatarIcon, BorderLayout.WEST);
        eventHeader.add(eventDetails, BorderLayout.CENTER);
        add(eventHeader, BorderLayout.NORTH);

        // Top 3 Leaderboard
        JPanel leaderboard = new JPanel(new GridLayout(0, 1, 0, 3));
        leaderboard.setOpaque(false);

        for(OTWEventRecord.Standing row : record.getTop()){
            JPanel rowPanel = new JPanel(new BorderLayout());
            rowPanel.setBorder(new EmptyBorder(2, 4, 2, 4));
            rowPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

            JLabel rsn = new JLabel(row.getRank() + ". " + row.getRsn());
            rsn.setForeground(ColorScheme.LIGHT_GRAY_COLOR.brighter());

            JLabel gains = new JLabel(row.getGainedText());
            gains.setForeground(ColorScheme.MEDIUM_GRAY_COLOR.brighter().brighter());

            rowPanel.add(rsn, BorderLayout.WEST);
            rowPanel.add(gains, BorderLayout.EAST);

            leaderboard.add(rowPanel);
        }

        add(leaderboard, BorderLayout.CENTER);
    }

}
