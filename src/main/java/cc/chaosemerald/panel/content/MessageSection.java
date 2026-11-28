package cc.chaosemerald.panel.content;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MessageSection extends JPanel {

    public MessageSection(String message, String icon){
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);

        add(messageLabel, BorderLayout.NORTH);
    }

}
