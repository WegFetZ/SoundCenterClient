package com.soundcenter.soundcenter.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LoadingGlassPane extends JPanel {
	private ImageIcon loadingImage = new ImageIcon(getClass().getResource("/resources/images/ajax-loader.gif"));
	private JLabel loadingLabel = new JLabel("Loading...", loadingImage, JLabel.CENTER);	
	
	public LoadingGlassPane() {
		
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
