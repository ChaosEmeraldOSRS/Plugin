package cc.chaosemerald.panel.content;

import cc.chaosemerald.panel.data.ImageLoader;
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

public class ProfileSection extends JPanel {

    public ProfileSection(ImageLoader imageLoader, ProfileRecord profile, String statusText){
        setLayout(new BorderLayout(7, 7));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel playerDetails = new JPanel(new BorderLayout());
        playerDetails.setOpaque(false);

        boolean profileFound = profile.isFound();

        JLabel name = new JLabel(profile.getRsn());
        name.setFont(FontManager.getRunescapeBoldFont());
        name.setForeground(ColorScheme.LIGHT_GRAY_COLOR.brighter());
        if(profileFound) {
            imageLoader.load(profile.getRankIcon(), image -> {
                BufferedImage resized = ImageUtil.resizeImage(image, 13, 13);
                BufferedImage raised = new BufferedImage(13, 13 + 2, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = raised.createGraphics();
                g2d.drawImage(resized, 0, 0, null);
                g2d.dispose();
                name.setIcon(new ImageIcon(raised));
                name.setIconTextGap(3);
            });
        }
        playerDetails.add(name, BorderLayout.CENTER);

        int favour = profile.getFavour();
        String currentRank = profile.getCurrentRank();
        int currentRankFavour = profile.getCurrentRankFavour();
        String nextRank = profile.getNextRank();
        int nextRankFavour = profile.getNextRankFavour();

        int percent = 0;
        if(profileFound && nextRank != null)
            percent = (favour - currentRankFavour) * 100 / (nextRankFavour - currentRankFavour);

        JLabel favourDetail;
        if(profileFound){
            favourDetail = new JLabel(favour + " Favour · " + (nextRankFavour - favour) + " to rank up");
        }else{
            favourDetail = new JLabel(statusText);
        }
        favourDetail.setForeground(ColorScheme.MEDIUM_GRAY_COLOR.brighter().brighter());
        playerDetails.add(favourDetail, BorderLayout.SOUTH);

        // Avatar Icon
        JLabel avatarIcon = new JLabel();
        avatarIcon.setOpaque(true);
        avatarIcon.setBackground(ColorScheme.DARK_GRAY_COLOR);
        avatarIcon.setPreferredSize(new Dimension(42, 42));

        BufferedImage defaultAvatar = ImageUtil.loadImageResource(ProfileSection.class, "/cc/chaosemerald/default-headshot.png");
        avatarIcon.setIcon(resizeAvatar(defaultAvatar));

        if(profile.getAvatarUrl() != null)
            imageLoader.load(profile.getAvatarUrl(), image -> avatarIcon.setIcon(resizeAvatar(image)));

        add(avatarIcon, BorderLayout.WEST);
        add(playerDetails, BorderLayout.CENTER);

        if(profileFound) {
            JPanel south = new JPanel(new BorderLayout(4, 4));
            south.setOpaque(false);
            ProgressBar bar = new ProgressBar();
            bar.setMaximumValue(24);
            bar.setValue(4);
            bar.setLeftLabel(currentRank);
            bar.setCenterLabel(percent + "%");
            bar.setRightLabel(nextRank);
            bar.setBackground(ColorScheme.DARK_GRAY_COLOR);
            bar.setForeground(new Color(18, 81, 23));

            south.add(bar, BorderLayout.NORTH);

            JButton saveOutfitButton = new JButton("Save worn gear to your web profile");
            saveOutfitButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
            south.add(saveOutfitButton, BorderLayout.SOUTH);

            add(south, BorderLayout.SOUTH);
        }
    }

    private static ImageIcon resizeAvatar(BufferedImage image){
        return new ImageIcon(new BaseMultiResolutionImage(
                ImageUtil.resizeImage(image, 42, 42),
                ImageUtil.resizeImage(image, 63, 63),
                ImageUtil.resizeImage(image, 84, 84)));
    }

}
