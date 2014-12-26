package com.soundcenter.soundcenter.client.gui.tabs;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.soundcenter.soundcenter.client.gui.actions.StationsTabActions;
import com.soundcenter.soundcenter.client.util.GuiUtil;
import com.soundcenter.soundcenter.lib.data.Station;

@SuppressWarnings("serial")
public class StationsTab extends JPanel {

	public JComboBox<String> playerComboBox = new JComboBox<String>();
	public JComboBox<String> typeComboBox = new JComboBox<String>(new String[]{"Areas", "Boxes", "Biomes", "Worlds", "WorldGuard Regions"});
	
	public JList<Station> stationList = new JList<Station>();
	
	public JCheckBox muteCheckBox = new JCheckBox("Mute this station");
	public JButton addButton = new JButton("Add...");
	public JButton deleteButton = new JButton("Delete");
	public JButton editButton = new JButton("Edit...");
	
	public StationsTab() {
		
		/* initialize components */		
		stationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		stationList.setLayoutOrientation(JList.VERTICAL);
		stationList.setVisibleRowCount(-1);
		JScrollPane stationScroller = new JScrollPane(stationList);
		stationScroller.setPreferredSize(new Dimension(450, 280));
		stationScroller.setMinimumSize(new Dimension(200,80));
		
		muteCheckBox.setEnabled(false);
		muteCheckBox.setSelected(false);
		
		GuiUtil.setFixedSize(playerComboBox, new Dimension(150, 30));
		GuiUtil.setFixedSize(typeComboBox, new Dimension(150, 30));		
		
		addButton.setEnabled(false);
		deleteButton.setEnabled(false);
		editButton.setEnabled(false);
		
		//Actions
		ActionListener selectionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StationsTabActions.stationChooserSelected();
			}
		};
		playerComboBox.addActionListener(selectionListener);
		typeComboBox.addActionListener(selectionListener);
		
		stationList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				StationsTabActions.listSelectionChanged();
			}
		});
		
		muteCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				StationsTabActions.muteCheckBoxSelected();
			}
		});
		
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StationsTabActions.editStationButtonPressed();
			}
		});
		
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StationsTabActions.addStationButtonPressed();
			}
		});
		
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StationsTabActions.deleteStationButtonPressed();
			}
		});

		/* build gui */
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		
		Box choosersBox = Box.createVerticalBox();
			Box playerChooserBox = Box.createHorizontalBox();
				JLabel playerLabel = new JLabel("Player:");
				playerLabel.setPreferredSize(new Dimension(40, 30));
				playerChooserBox.add(playerLabel);
				playerChooserBox.add(Box.createRigidArea(new Dimension(20,0)));
				playerChooserBox.add(playerComboBox);
				playerChooserBox.add(Box.createHorizontalGlue());
			choosersBox.add(playerChooserBox);
			
			choosersBox.add(Box.createRigidArea(new Dimension(0, 20)));
			
			Box typeChooserBox = Box.createHorizontalBox();
				JLabel typeLabel = new JLabel("Type:");
				typeLabel.setPreferredSize(new Dimension(40, 30));
				typeChooserBox.add(typeLabel);
				typeChooserBox.add(Box.createRigidArea(new Dimension(20,0)));
				typeChooserBox.add(typeComboBox);
				typeChooserBox.add(Box.createHorizontalGlue());
			choosersBox.add(typeChooserBox);
			
			choosersBox.add(Box.createVerticalGlue());
			Box hBox1 = Box.createHorizontalBox();
				hBox1.add(muteCheckBox);
				hBox1.add(Box.createHorizontalGlue());
			choosersBox.add(hBox1);
		add(choosersBox);
		
		add(Box.createRigidArea(new Dimension(10, 0)));
		JSeparator vseparator1 = new JSeparator(JSeparator.VERTICAL);
		vseparator1.setPreferredSize(new Dimension(15, 320));
		add(vseparator1);
		
		Box listBox = Box.createVerticalBox();
			listBox.add(stationScroller);
			
			listBox.add(Box.createRigidArea(new Dimension(0, 10)));
			
			Box buttonsBox = Box.createHorizontalBox();
				buttonsBox.add(addButton);
				buttonsBox.add(Box.createRigidArea(new Dimension(20, 0)));
				buttonsBox.add(deleteButton);
				buttonsBox.add(Box.createHorizontalGlue());
				buttonsBox.add(editButton);
			listBox.add(buttonsBox);
			
			listBox.add(Box.createVerticalGlue());
		add(listBox);		
	}
	
}
