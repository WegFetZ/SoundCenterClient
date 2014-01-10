package com.soundcenter.soundcenter.client.gui.dialogs;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

import com.soundcenter.soundcenter.client.network.tcp.UploadManager;

public class UploadSongDialog extends JDialog {
	
	//GUI
	public JLabel fileLabel = new JLabel();
	public JLabel progressLabel = new JLabel();
	public JLabel sizeLabel = new JLabel();
	public JLabel speedLabel = new JLabel("0");
	public JProgressBar progressBar = new JProgressBar();
	public JButton cancelButton = new JButton("Cancel");
	
	public UploadSongDialog(JFrame parent, UploadManager manager) {
		super(parent);
		this.setModal(true);
		this.setModalityType(DEFAULT_MODALITY_TYPE);
		
		//setup components
		progressBar.setPreferredSize(new Dimension(330,30));
		fileLabel.setPreferredSize(new Dimension(250, 25));
		
		
		final UploadManager uploadManager = manager;
		//Actions
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				uploadManager.cancelAll();
			}
		});
		
		//build gui
		JComponent contentPane = (JComponent) this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		contentPane.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		setPreferredSize(new Dimension(350, 200));
		
		Box hBox1 = Box.createHorizontalBox();
			hBox1.add(new JLabel("Uploading File: "));
			hBox1.add(fileLabel);
			hBox1.add(Box.createHorizontalGlue());
		contentPane.add(hBox1);
		
		contentPane.add(Box.createRigidArea(new Dimension(0, 10)));
		
		Box hBox2 = Box.createHorizontalBox();
			hBox2.add(progressLabel);
			hBox2.add(new JLabel("MB / "));
			hBox2.add(sizeLabel);
			hBox2.add(Box.createRigidArea(new Dimension(40,0)));
			hBox2.add(speedLabel);
			hBox2.add(new JLabel("KB/s"));
			hBox2.add(Box.createHorizontalGlue());
		contentPane.add(hBox2);
		
		contentPane.add(Box.createRigidArea(new Dimension(0,10)));
		
		contentPane.add(progressBar);
		
		contentPane.add(Box.createRigidArea(new Dimension(0, 20)));
		contentPane.add(Box.createVerticalGlue());
		
		Box hBox3 = Box.createHorizontalBox();
			hBox3.add(Box.createHorizontalGlue());
			hBox3.add(cancelButton);
		contentPane.add(hBox3);
		
		pack();
		setLocationRelativeTo(parent);
	}
	
	
	
	public void setStats(String mbRead, int percent, int speed) {
		setTitle("Uploading Song - " + percent + "%");
        progressLabel.setText(mbRead);
        progressBar.setValue(percent);
        speedLabel.setText(String.valueOf(speed));
	}
}
