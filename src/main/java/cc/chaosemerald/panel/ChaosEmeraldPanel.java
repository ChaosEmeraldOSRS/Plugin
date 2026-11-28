package cc.chaosemerald.panel;

import cc.chaosemerald.ChaosEmeraldApiClient;
import cc.chaosemerald.panel.content.DropsPanel;
import cc.chaosemerald.panel.content.EventsPanel;
import cc.chaosemerald.panel.content.MessageSection;
import cc.chaosemerald.panel.content.ProfileSection;
import cc.chaosemerald.panel.data.ImageLoader;
import cc.chaosemerald.panel.data.PanelRecord;
import cc.chaosemerald.panel.data.ProfileRecord;
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

    private final JPanel profileContainer = new JPanel(new BorderLayout());
    private final JPanel eventsContainer = new JPanel(new BorderLayout());
    private final JPanel dropsContainer = new JPanel(new BorderLayout());

    private ProfileRecord lastFoundProfile;
    private final JButton reloadButton = new JButton("Reload");

    @Inject
    ChaosEmeraldPanel(ImageLoader imageLoader, ChaosEmeraldApiClient apiClient){
        super(false);

        this.imageLoader = imageLoader;
        this.apiClient = apiClient;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 0, 0));

        buildPanel();
        showPanelLoading();
    }

    @Override
    public void onActivate(){
        showTabsLoading();
        reloadPanel();
    }

    private void reloadPanel(){
        apiClient.fetchPanel(this::showPanel, this::showError);
    }

    private void updateContainer(JPanel container, JComponent content){
        container.removeAll();
        container.add(content, BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
    }

    private void showPanelLoading(){
        ProfileRecord loadingRecord = ProfileRecord.empty("Loading...");
        updateContainer(profileContainer, new ProfileSection(imageLoader, loadingRecord, "Please wait"));

        showTabsLoading();
    }

    private void showTabsLoading(){
        reloadButton.setEnabled(false);
        updateContainer(eventsContainer, new MessageSection("Loading events...", ""));
        updateContainer(dropsContainer, new MessageSection("Loading drops...", ""));
    }

    private void showPanel(PanelRecord record){
        reloadButton.setEnabled(true);
        reloadButton.setText("Reload");

        updateContainer(eventsContainer, new EventsPanel(imageLoader, record));
        updateContainer(dropsContainer, new DropsPanel(imageLoader, record));

        ProfileRecord profile = record.getProfile();
        if(profile != null) {
            if(!profile.equals(lastFoundProfile)) {
                updateContainer(profileContainer, new ProfileSection(imageLoader, profile, "Clan profile not found."));
                this.lastFoundProfile = profile;
            }
        }else if(lastFoundProfile == null){
            updateContainer(profileContainer, new ProfileSection(imageLoader, ProfileRecord.empty("Not logged in"), "Log in to see your clan profile"));
        }
    }

    private void showError(){
        reloadButton.setEnabled(true);
        reloadButton.setText("Reload");

        updateContainer(eventsContainer, new MessageSection("Couldn't reach the configured website.", ""));
        updateContainer(dropsContainer, new MessageSection("Couldn't reach the configured website.", ""));

        if(lastFoundProfile == null)
            updateContainer(profileContainer, new ProfileSection(imageLoader, ProfileRecord.empty("Couldn't reach website"), "Reload to try again."));
    }

    private void buildPanel(){
        JPanel north = new JPanel(new BorderLayout());

        north.add(profileContainer, BorderLayout.NORTH);

        JPanel tabDisplay = new JPanel(new BorderLayout());
        MaterialTabGroup tabs = new MaterialTabGroup(tabDisplay);

        MaterialTab eventTab = new MaterialTab("Events", tabs, eventsContainer);
        eventTab.setOnSelectEvent(() -> {
            reloadPanel();
            return true;
        });
        tabs.addTab(eventTab);

        MaterialTab dropTab = new MaterialTab("Drops", tabs, dropsContainer);
        dropTab.setOnSelectEvent(() -> {
            reloadPanel();
            return true;
        });
        tabs.addTab(dropTab);

        tabs.select(eventTab);

        reloadButton.addActionListener(e -> {
            reloadButton.setEnabled(false);
            reloadButton.setText("Loading...");
            reloadPanel();
        });

        JPanel tabsButtonRow = new JPanel(new BorderLayout());
        tabsButtonRow.setBorder(new EmptyBorder(6, 6, 6, 6));

        tabs.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabsButtonRow.add(tabs, BorderLayout.CENTER);
        tabsButtonRow.add(reloadButton, BorderLayout.EAST);

        north.add(tabsButtonRow, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);

        JScrollPane scrollableSection = new JScrollPane(tabDisplay);
        scrollableSection.setBorder(null);
        scrollableSection.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollableSection.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollableSection, BorderLayout.CENTER);
    }

}
