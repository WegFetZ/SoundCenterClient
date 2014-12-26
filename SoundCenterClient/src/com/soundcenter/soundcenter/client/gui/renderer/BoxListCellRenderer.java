package com.soundcenter.soundcenter.client.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import com.soundcenter.soundcenter.lib.data.SCLocation;

@SuppressWarnings("serial")
public class BoxListCellRenderer extends JPanel implements ListCellRenderer {
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {

		com.soundcenter.soundcenter.lib.data.Box box = (com.soundcenter.soundcenter.lib.data.Box) value;

		this.removeAll();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		SCLocation loc = box.getLocation();
		
		this.add(Box.createRigidArea(new Dimension(0, 3)));
		Box layout = Box.createHorizontalBox();
			layout.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			layout.add(new JLabel("  ID: " + box.getId()));
			layout.add(Box.createRigidArea(new Dimension(20,0)));
			layout.add(new JLabel("Range: " + box.getRange()));
			layout.add(Box.createRigidArea(new Dimension(20,0)));
			layout.add(new JLabel("Priority: " + box.getPriority()));
			layout.add(Box.createRigidArea(new Dimension(20,0)));
			layout.add(new JLabel("Loc.: " + loc.getPoint()));
			layout.add(Box.createRigidArea(new Dimension(10,0)));
			layout.add(new JLabel("World: " + box.getWorld()));
			layout.add(Box.createHorizontalGlue());
		this.add(layout);
		
		this.setOpaque(true);

		if (isSelected) {
			this.setForeground(UIManager.getColor("List.selectionForeground"));
			this.setBackground(UIManager.getColor("List.selectionBackground"));
		} else {
			this.setForeground(UIManager.getColor("List.foreground"));
			this.setBackground(UIManager.getColor("List.background"));
		}

		return this;
	}

}
