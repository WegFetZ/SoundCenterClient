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

import com.soundcenter.soundcenter.lib.data.Region;
import com.soundcenter.soundcenter.lib.data.Station;

public class RegionListCellRenderer extends JPanel implements ListCellRenderer {
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {

		Station region = (Station) value;

		this.removeAll();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.add(Box.createRigidArea(new Dimension(0, 3)));
		Box layout = Box.createHorizontalBox();
			layout.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			layout.add(new JLabel("  Name: " + region.getName()));
			layout.add(Box.createRigidArea(new Dimension(20,0)));
			layout.add(new JLabel("ID: " + region.getId()));
			layout.add(Box.createRigidArea(new Dimension(20,0)));
			layout.add(new JLabel("Priority: " + region.getPriority()));
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
