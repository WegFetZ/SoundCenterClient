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

import com.soundcenter.soundcenter.lib.data.Song;

public class SongListCellRenderer extends JPanel implements ListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {

		Song song = (Song) value;
		String type = "   ";
		if (song.getDuration() == -1) {
			type = "   Radio: ";
		}

		this.removeAll();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.add(Box.createRigidArea(new Dimension(0, 3)));
		Box box = Box.createHorizontalBox();
			box.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			box.add(new JLabel(type));
			box.add(new JLabel(song.getTitle()));
			box.add(Box.createRigidArea(new Dimension(20,0)));
			box.add(new JLabel("URL: " + song.getUrl()));
			box.add(Box.createHorizontalGlue());
		this.add(box);
		
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
