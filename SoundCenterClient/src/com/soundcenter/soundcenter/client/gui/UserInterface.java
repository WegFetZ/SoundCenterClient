package com.soundcenter.soundcenter.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.soundcenter.soundcenter.client.gui.actions.GeneralTabActions;
import com.soundcenter.soundcenter.client.gui.actions.MusicTabActions;
import com.soundcenter.soundcenter.client.gui.actions.StationsTabActions;
import com.soundcenter.soundcenter.client.gui.tabs.GeneralTab;
import com.soundcenter.soundcenter.client.gui.tabs.MusicTab;
import com.soundcenter.soundcenter.client.gui.tabs.StationsTab;

@SuppressWarnings("serial")
public class UserInterface extends JPanel {
	
	public GuiController controller = new GuiController(this);
	
	public JTabbedPane mainTabs = new JTabbedPane();
	public GeneralTab generalTab = new GeneralTab();
	public MusicTab musicTab = new MusicTab();
	public StationsTab stationsTab = new StationsTab();
	
	public JPanel appGlassPane = new LoadingGlassPane();
	
	public UserInterface() {
		super(new BorderLayout());
		
		initComponents();
	}
	
	private void initComponents() {
		setBorder(new EmptyBorder(new Insets(2,2,2,2)));
		this.setBackground(Color.WHITE);
		
		//set-up the components
		mainTabs.setPreferredSize(getSize());
		
		//add components to the mainPanel
		mainTabs.add("General", generalTab);
		mainTabs.add("Music", musicTab);
		mainTabs.add("Stations", stationsTab);
		add(mainTabs);
		
		mainTabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int index = mainTabs.getSelectedIndex();				
				switch(index) {
					case 0:
						GeneralTabActions.tabOpened();
						break;
					case 1:
						MusicTabActions.tabOpened();
						break;
					case 2:
						StationsTabActions.tabOpened();
				}
			}
		});
	}
	
	public void createGlassPane(JApplet applet) {
		appGlassPane = new LoadingGlassPane();
		
		applet.setGlassPane(appGlassPane);
	}
	public void createGlassPane(JFrame frame) {
		appGlassPane = new LoadingGlassPane();
		
		frame.setGlassPane(appGlassPane);
	}
}