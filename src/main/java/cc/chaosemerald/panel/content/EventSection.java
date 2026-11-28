package cc.chaosemerald.panel.content;

import cc.chaosemerald.panel.data.EventRecord;
import cc.chaosemerald.panel.data.ImageLoader;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EventSection extends JPanel {

    public EventSection(ImageLoader imageLoader, EventRecord record){
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

        ZonedDateTime start = Instant.parse(record.getStartsAt()).atZone(ZoneId.systemDefault());
        int day = start.getDayOfMonth();
        String suffix;
        if(day >= 11 && day <= 13)
            suffix = "th";
        else if(day % 10 == 1)
            suffix = "st";
        else if(day % 10 == 2)
            suffix = "nd";
        else if(day % 10 == 3)
            suffix = "rd";
        else
            suffix = "th";

        String startsAtText = "Starts " + day + suffix + " "
                + start.format(DateTimeFormatter.ofPattern("MMM h:mma", Locale.ENGLISH));

        JLabel otwTimeLeft = new JLabel(startsAtText);
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
        add(eventHeader, BorderLayout.CENTER);
    }

}
