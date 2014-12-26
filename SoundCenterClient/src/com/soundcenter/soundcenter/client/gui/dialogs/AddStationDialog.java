package com.soundcenter.soundcenter.client.gui.dialogs;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.gui.actions.StationsTabActions;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;

@SuppressWarnings("serial")
public class AddStationDialog extends JDialog {

	public byte type;
	private String typeString = "Stations";
	
	public JList<String> availableList = new JList<String>();
	public JButton addButton = new JButton("Add");
	public JButton cancelButton = new JButton("Cancel");
	
	public AddStationDialog(JFrame parent, byte type) {
		super(parent);
		this.setModal(true);
		this.setModalityType(DEFAULT_MODALITY_TYPE);
		
		this.type = type;
		
		addButton.setEnabled(false);
		
		availableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		availableList.setLayoutOrientation(JList.VERTICAL);
		availableList.setVisibleRowCount(-1);
		availableList.setModel(new DefaultListModel<String>());
		
		JScrollPane availableScroller = new JScrollPane(availableList);
		availableScroller.setPreferredSize(new Dimension(350, 160));

		
		//Actions
		final AddStationDialog frame = this;
		availableList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				StationsTabActions.addStationDialogListSelectionChanged(frame);
			}
		});
		
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				StationsTabActions.addStationDialogAddButtonPressed(frame);		
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();				
			}
		});		
		
		
		/* load station properties */
		loadProperties(type);
		
		
		/* build the gui */
		JComponent pane = (JComponent) getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		
		Box hBox1 = Box.createHorizontalBox();
			hBox1.add(new JLabel("Available " + typeString + ":"));
			hBox1.add(Box.createHorizontalGlue());
		pane.add(hBox1);
		
		pane.add(availableScroller);
		
		pane.add(Box.createRigidArea(new Dimension(0, 10)));
		pane.add(Box.createVerticalGlue());
		
		Box buttonBox = Box.createHorizontalBox();
			buttonBox.add(Box.createHorizontalGlue());
			buttonBox.add(addButton);
			buttonBox.add(Box.createRigidArea(new Dimension(20,0)));
			buttonBox.add(cancelButton);
		pane.add(buttonBox);
		
		pack();
		setLocationRelativeTo(parent);
		
	}
	
	private void loadProperties(byte type) {
		DefaultListModel<String> model = null;
		String title = "Add Station";
		
		if (type == GlobalConstants.TYPE_BIOME) {
			title = "Add Biome";
			typeString = "Biomes";
			model = Client.database.getAvailableBiomes();
			
		} else if (type == GlobalConstants.TYPE_WORLD) {
			title = "Add World";
			typeString = "Worlds";
			model = Client.database.getAvailableWorlds();
		}
		
		this.setTitle(title);
		if (model != null) {
			availableList.setModel(model);
		}
	}
	
}
