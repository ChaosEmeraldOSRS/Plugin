package cc.chaosemerald.panel;

import cc.chaosemerald.ChaosEmeraldApiClient;
import cc.chaosemerald.panel.data.ImageLoader;
import cc.chaosemerald.panel.data.PanelRecord;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChaosEmeraldPanel extends PluginPanel {

    private final ImageLoader imageLoader;
    private final ChaosEmeraldApiClient apiClient;

    @Inject
    ChaosEmeraldPanel(ImageLoader imageLoader, ChaosEmeraldApiClient apiClient){
        super(false);

        this.imageLoader = imageLoader;
        this.apiClient = apiClient;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 0, 0));

        showLoading();
    }

    @Override
    public void onActivate(){
        showLoading();
        apiClient.fetchPanel(this::showPanel, this::showError);
    }

    private void setContent(JComponent content){
        removeAll();
        add(content, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void showLoading(){
        setContent(new JLabel("Loading..."));
    }

    private void showPanel(PanelRecord record){
        setContent(buildContent(record));
    }

    private void showError(){
        setContent(new JLabel("Couldn't reach the webserver."));
    }

    private JPanel buildContent(PanelRecord record){
        JPanel content = new JPanel(new BorderLayout());

        JPanel north = new JPanel(new BorderLayout());

        north.add(new ProfileSection(imageLoader, record.getProfile()), BorderLayout.NORTH);

        JPanel tabDisplay = new JPanel(new BorderLayout());
        MaterialTabGroup tabs = new MaterialTabGroup(tabDisplay);
        MaterialTab eventTab = new MaterialTab("Events", tabs, new EventsPanel(imageLoader, record));
        MaterialTab dropTab = new MaterialTab("Drops", tabs, new DropsPanel(imageLoader, record));
        tabs.addTab(eventTab);
        tabs.addTab(dropTab);
        tabs.select(eventTab);

        JButton reloadPanelButton = new JButton("Reload");
        reloadPanelButton.addActionListener(e -> onActivate());

        JPanel tabsButtonRow = new JPanel(new BorderLayout());
        tabsButtonRow.setBorder(new EmptyBorder(6, 6, 6, 6));

        tabs.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabsButtonRow.add(tabs, BorderLayout.CENTER);
        tabsButtonRow.add(reloadPanelButton, BorderLayout.EAST);

        north.add(tabsButtonRow, BorderLayout.SOUTH);

        content.add(north, BorderLayout.NORTH);

        JScrollPane scrollableSection = new JScrollPane(tabDisplay);
        scrollableSection.setBorder(null);
        scrollableSection.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableSection.getVerticalScrollBar().setUnitIncrement(16);

        content.add(scrollableSection, BorderLayout.CENTER);

        return content;
    }

}
