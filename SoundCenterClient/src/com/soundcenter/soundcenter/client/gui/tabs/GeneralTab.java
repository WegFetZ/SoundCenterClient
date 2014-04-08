package com.soundcenter.soundcenter.client.gui.tabs;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.soundcenter.soundcenter.client.AppletStarter;
import com.soundcenter.soundcenter.client.gui.actions.GeneralTabActions;
import com.soundcenter.soundcenter.client.util.GuiUtil;

public class GeneralTab extends JPanel {
	
	public JTextArea logArea = new JTextArea();
	public JScrollPane logAreaScroller = new JScrollPane(logArea);
	
	public JTextField userNameField = new JTextField();
	public JTextField serverAddressField = new JTextField();
	public JTextField pluginPortField = new JTextField();
	
	public JCheckBox autoConnectCheckBox = new JCheckBox();
	public JCheckBox autoReconnectCheckBox = new JCheckBox();
	public JCheckBox debugCheckBox = new JCheckBox();
	
	public JButton connectButton = new JButton("Connect");
	public JToggleButton muteMusicButton = new JToggleButton("Music active");
	public JToggleButton muteVoiceButton = new JToggleButton("Voice active");
	
	public JSlider volumeSlider = new JSlider();
	public JLabel volumeLabel = new JLabel("100");
	
	public JLabel statusLabel = new JLabel("Disconnected.");
	
	
	public GeneralTab() {		
		setPreferredSize(super.getSize());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		//initialize components
		JLabel userNameLabel = new JLabel("Minecraft name:");
		userNameLabel.setPreferredSize(new Dimension(110, 30));
		userNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		
		JLabel serverAddressLabel = new JLabel("Server IP:");
		serverAddressLabel.setPreferredSize(new Dimension(110, 30));
		serverAddressField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		
		JLabel pluginPortLabel = new JLabel("SoundCenter Port:");
		pluginPortLabel.setPreferredSize(new Dimension(110, 30));
		pluginPortField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		
		autoConnectCheckBox.setText("Connect on startup.");
		autoReconnectCheckBox.setText("Reconnect on disconnect.");
		debugCheckBox.setText("Debug mode.");
		
		GuiUtil.setFixedSize(connectButton, new Dimension(100, 40));
		
		GuiUtil.setFixedSize(muteMusicButton, new Dimension(80, 20));
		muteMusicButton.setSelected(true);
		muteMusicButton.setBorder(null);
		
		GuiUtil.setFixedSize(muteVoiceButton, new Dimension(80, 20));
		muteVoiceButton.setSelected(true);
		muteVoiceButton.setBorder(null);
		
		volumeLabel.setPreferredSize(new Dimension(25,20));
		volumeSlider.setValue(100);
		
		logArea.setEditable(false);
		
		logAreaScroller.setPreferredSize(new Dimension(400, 340));
		logAreaScroller.setMinimumSize(new Dimension(200, 100));
		logAreaScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		logAreaScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		//ActionListeners
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GeneralTabActions.connectButtonPressed();
			}
		});
		
		volumeSlider.addChangeListener(new ChangeListener() {
	        @Override
	        public void stateChanged(ChangeEvent ce) {
	            AppletStarter.gui.controller.setMasterVolume(volumeSlider.getValue(), false);
	        }
	    });

		muteMusicButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GeneralTabActions.muteMusicButtonClicked();
			}
		});
		
		muteVoiceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GeneralTabActions.muteVoiceButtonClicked();
			}
		});
		
		//set up the layout		
		Box mainBox = Box.createHorizontalBox();
		mainBox.setBorder(new EmptyBorder(new Insets(10,10,10,10)));
			Box settingsBox = Box.createVerticalBox();
				Box hBox1 = Box.createHorizontalBox();
					hBox1.add(userNameLabel);
					hBox1.add(Box.createRigidArea(new Dimension(10, 0)));
					hBox1.add(userNameField);
				settingsBox.add(hBox1);
				
				settingsBox.add(Box.createRigidArea(new Dimension(0, 10)));
				
				Box hBox2 = Box.createHorizontalBox();
					hBox2.add(serverAddressLabel);
					hBox2.add(Box.createRigidArea(new Dimension(10, 0)));
					hBox2.add(serverAddressField);
				settingsBox.add(hBox2);
				
				settingsBox.add(Box.createRigidArea(new Dimension(0, 10)));
				
				Box hBox3 = Box.createHorizontalBox();
					hBox3.add(pluginPortLabel);
					hBox3.add(Box.createRigidArea(new Dimension(10, 0)));
					hBox3.add(pluginPortField);
				settingsBox.add(hBox3);
				
				settingsBox.add(Box.createRigidArea(new Dimension(0, 20)));
				JSeparator hseparator1 = new JSeparator(JSeparator.HORIZONTAL);
				hseparator1.setPreferredSize(new Dimension(266, 20));
				settingsBox.add(hseparator1);		
				
				Box checkBoxBox1 = Box.createHorizontalBox();
					checkBoxBox1.add(autoConnectCheckBox);
					checkBoxBox1.add(Box.createHorizontalGlue());
				settingsBox.add(checkBoxBox1);
				settingsBox.add(Box.createRigidArea(new Dimension(0, 10)));
				
				Box checkBoxBox2 = Box.createHorizontalBox();
					checkBoxBox2.add(autoReconnectCheckBox);
					checkBoxBox2.add(Box.createHorizontalGlue());
				settingsBox.add(checkBoxBox2);
				settingsBox.add(Box.createRigidArea(new Dimension(0, 20)));
				
				Box checkBoxBox3 = Box.createHorizontalBox();
					checkBoxBox3.add(debugCheckBox);
					checkBoxBox3.add(Box.createHorizontalGlue());
				settingsBox.add(checkBoxBox3);
			
				settingsBox.add(Box.createVerticalGlue());
			
			mainBox.add(settingsBox);
			mainBox.add(Box.createRigidArea(new Dimension(10, 0)));
			
			JSeparator vseparator1 = new JSeparator(JSeparator.VERTICAL);
			vseparator1.setPreferredSize(new Dimension(15, 290));
			mainBox.add(vseparator1);
			
			mainBox.add(logAreaScroller);
		
			
		JSeparator hseparator2 = new JSeparator(JSeparator.HORIZONTAL);
		hseparator2.setPreferredSize(new Dimension(776, 10));	
		
		Box controlBox = Box.createHorizontalBox();
			GuiUtil.setFixedSize(controlBox, new Dimension(Integer.MAX_VALUE, 60));
			controlBox.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
			
			controlBox.add(connectButton);
			
			controlBox.add(Box.createRigidArea(new Dimension(20,0)));
			JSeparator vseparator2 = new JSeparator(JSeparator.VERTICAL);
			vseparator2.setPreferredSize(new Dimension(10,60));
			controlBox.add(vseparator2);
			
			controlBox.add(statusLabel);
			
			controlBox.add(Box.createHorizontalGlue());
			
			controlBox.add(Box.createRigidArea(new Dimension(10,0)));
			JSeparator vseparator3 = new JSeparator(JSeparator.VERTICAL);
			vseparator3.setPreferredSize(new Dimension(10,60));
			controlBox.add(vseparator3);
						
			Box volumeBox = Box.createVerticalBox();
				Box volumeLabelBox = Box.createHorizontalBox();
					volumeLabelBox.add(new JLabel("Volume:"));
					volumeLabelBox.add(Box.createHorizontalGlue());
				volumeBox.add(volumeLabelBox);
				volumeBox.add(Box.createRigidArea(new Dimension(0,10)));
				Box volumeSliderBox = Box.createHorizontalBox();
					volumeSliderBox.add(volumeLabel);
					volumeSliderBox.add(Box.createRigidArea(new Dimension(5,0)));
					volumeSliderBox.add(volumeSlider);
				volumeBox.add(volumeSliderBox);
			controlBox.add(volumeBox);
			
			controlBox.add(Box.createRigidArea(new Dimension(20,0)));
			
			Box muteButtonsBox = Box.createVerticalBox();
				muteButtonsBox.add(muteMusicButton);
				muteButtonsBox.add(Box.createRigidArea(new Dimension(0,2)));
				muteButtonsBox.add(muteVoiceButton);
			controlBox.add(muteButtonsBox);			
			
		//put everything together
		add(mainBox);
		add(hseparator2);
		add(controlBox);				
	}

}
