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
import com.soundcenter.soundcenter.lib.data.Area;
import com.soundcenter.soundcenter.lib.data.SCLocation;

public class AreaListCellRenderer extends JPanel implements ListCellRenderer {
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {

		Area area = (Area) value;

		this.removeAll();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		SCLocation min = area.getMin();
		SCLocation max = area.getMax();
		
		this.add(Box.createRigidArea(new Dimension(0, 3)));
		Box layout = Box.createHorizontalBox();
			layout.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			layout.add(new JLabel("  ID: " + area.getId()));
			layout.add(Box.createRigidArea(new Dimension(20, 0)));
			layout.add(new JLabel("Priority: " + area.getPriority()));
			layout.add(Box.createRigidArea(new Dimension(20, 0)));
			layout.add(new JLabel("Corners: " + min.getPoint() + " - " + max.getPoint()));
			layout.add(Box.createRigidArea(new Dimension(10, 0)));
			layout.add(new JLabel("World: " + area.getWorld()));
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
