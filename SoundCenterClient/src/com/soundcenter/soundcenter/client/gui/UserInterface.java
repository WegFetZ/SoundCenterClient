package com.soundcenter.soundcenter.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import com.soundcenter.soundcenter.client.gui.tabs.GeneralTab;
import com.soundcenter.soundcenter.client.gui.tabs.MusicTab;
import com.soundcenter.soundcenter.client.gui.tabs.StationsTab;

public class UserInterface extends JPanel {
	
	public GuiController controller = new GuiController(this);
	
	public JTabbedPane mainTabs = new JTabbedPane();
	public GeneralTab generalTab = new GeneralTab();
	public MusicTab musicTab = new MusicTab();
	public StationsTab stationsTab = new StationsTab();
	
	public JPanel glassPane = new DisabledGlassPane();
	
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
	}
	
	public void createGlassPane(JApplet applet) {
		glassPane = new DisabledGlassPane();
		
		applet.setGlassPane(glassPane);
	}
	
	public void createGlassPane(JFrame frame) {
		glassPane = new DisabledGlassPane();
		
		frame.setGlassPane(glassPane);
	}
	
	private class DisabledGlassPane extends JPanel {
		
		private ImageIcon loadingImage = new ImageIcon(getClass().getResource("/resources/images/ajax-loader.gif"));
		private JLabel loadingLabel = new JLabel("Loading data...", loadingImage, JLabel.CENTER);	
		
		private DisabledGlassPane() {
			
			this.setOpaque(false);
			this.setLayout(new BorderLayout());
			this.addMouseListener(new MouseAdapter() {});
			this.add(loadingLabel, BorderLayout.CENTER);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			g.setColor(new Color(255, 255, 255, 80));
			g.fillRect(0, 0, this.getSize().width, this.getSize().height);
		}
		
	}
}