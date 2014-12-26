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
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.soundcenter.soundcenter.client.App;
import com.soundcenter.soundcenter.client.gui.actions.GeneralTabActions;
import com.soundcenter.soundcenter.client.util.GuiUtil;

@SuppressWarnings("serial")
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
	public JCheckBox stationsActiveCheckBox = new JCheckBox("Stations active");
	public JCheckBox voiceActiveCheckBox = new JCheckBox("Voice active");
	public JCheckBox singleSongsActiveCheckBox = new JCheckBox("Songs played with \"/sc play\" active");
	
	public JSlider volumeSlider = new JSlider();
	public JLabel volumeLabel = new JLabel("100");
	
	
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
		
		stationsActiveCheckBox.setSelected(true);
		singleSongsActiveCheckBox.setSelected(true);
		voiceActiveCheckBox.setSelected(true);
		
		volumeLabel.setPreferredSize(new Dimension(25,20));
		volumeSlider.setPreferredSize(new Dimension(100,20));
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
	            App.gui.controller.setMasterVolume((byte) volumeSlider.getValue(), false);
	        }
	    });

		stationsActiveCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GeneralTabActions.stationsActiveCheckBoxChanged();
			}
		});
		singleSongsActiveCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GeneralTabActions.singleSongsActiveCheckBoxChanged();
			}
		});
		voiceActiveCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GeneralTabActions.voiceActiveCheckBoxChanged();
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
				
				settingsBox.add(Box.createRigidArea(new Dimension(0, 10)));
				JSeparator hseparator1 = new JSeparator(JSeparator.HORIZONTAL);
				hseparator1.setPreferredSize(new Dimension(266, 10));
				settingsBox.add(hseparator1);		
				settingsBox.add(Box.createRigidArea(new Dimension(0, 10)));
				
				Box checkBoxBox1 = Box.createHorizontalBox();
					checkBoxBox1.add(autoConnectCheckBox);
					checkBoxBox1.add(Box.createHorizontalGlue());
				settingsBox.add(checkBoxBox1);
				
				Box checkBoxBox2 = Box.createHorizontalBox();
					checkBoxBox2.add(autoReconnectCheckBox);
					checkBoxBox2.add(Box.createHorizontalGlue());
				settingsBox.add(checkBoxBox2);
				settingsBox.add(Box.createRigidArea(new Dimension(0, 10)));
				
				Box checkBoxBox3 = Box.createHorizontalBox();
					checkBoxBox3.add(debugCheckBox);
					checkBoxBox3.add(Box.createHorizontalGlue());
				settingsBox.add(checkBoxBox3);
			
				settingsBox.add(Box.createVerticalGlue());
			
			mainBox.add(settingsBox);
			mainBox.add(Box.createRigidArea(new Dimension(10, 0)));
			
			JSeparator vseparator1 = new JSeparator(JSeparator.VERTICAL);
			vseparator1.setPreferredSize(new Dimension(15, 270));
			mainBox.add(vseparator1);
			
			mainBox.add(logAreaScroller);
		
			
		JSeparator hseparator2 = new JSeparator(JSeparator.HORIZONTAL);
		hseparator2.setPreferredSize(new Dimension(660, 2));	
		
		Box controlBox = Box.createHorizontalBox();
			controlBox.setBorder(new EmptyBorder(new Insets(5, 10, 10, 5)));
			
			Box vBox1 = Box.createVerticalBox();
				vBox1.add(Box.createVerticalGlue());
				vBox1.add(connectButton);
				vBox1.add(Box.createVerticalGlue());
			controlBox.add(vBox1);
			
			controlBox.add(Box.createRigidArea(new Dimension(20,0)));
			
			JSeparator vseparator3 = new JSeparator(JSeparator.VERTICAL);
			vseparator3.setPreferredSize(new Dimension(20,60));
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
			controlBox.add(Box.createHorizontalGlue());
			
			Box vBox2 = Box.createVerticalBox();
				vBox2.add(stationsActiveCheckBox);
				vBox2.add(singleSongsActiveCheckBox);
				vBox2.add(voiceActiveCheckBox);
				vBox2.add(Box.createVerticalGlue());
				
			controlBox.add(vBox2);			
			
		//put everything together
		add(mainBox);
		add(hseparator2);
		add(controlBox);				
	}

}
