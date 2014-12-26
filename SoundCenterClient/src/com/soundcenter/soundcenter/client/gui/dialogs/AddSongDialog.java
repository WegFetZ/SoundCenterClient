package com.soundcenter.soundcenter.client.gui.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.soundcenter.soundcenter.client.gui.actions.MusicTabActions;

@SuppressWarnings("serial")
public class AddSongDialog extends JDialog {
	
	public JTextField titleTextField = new JTextField();
	public JTextField urlTextField = new JTextField();
	public JCheckBox radioCheckBox = new JCheckBox("This song is a radio-stream.");
	public JButton addButton = new JButton("Add");
	public JButton cancelButton = new JButton("Cancel");
	private JTextArea helpLabel = new JTextArea("You can add URLs to audio files on any public webserver. You can also use a filehoster like Dropbox or Google Drive to upload your music. Please don't violate copyright laws. Webradio stations will work as well, if you use an URL which is directly pointing to an audio stream in mp3 or ogg format.");
	
	public AddSongDialog(JFrame parent) {
		super(parent);
		this.setModal(true);
		this.setModalityType(DEFAULT_MODALITY_TYPE);
				
		//initialize components
		setTitle("Add a new song");
		
		titleTextField.setPreferredSize(new Dimension(400, 25));
		urlTextField.setPreferredSize(new Dimension(400, 25));
		helpLabel.setPreferredSize(new Dimension(420, 100));
		helpLabel.setBackground(new Color(0, 0, 0, 0));
		helpLabel.setLineWrap(true);
		helpLabel.setWrapStyleWord(true);
		
		
		//Actions
		final AddSongDialog frame = this;
		
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MusicTabActions.addSongDialogAddButtonPressed(frame);		
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();				
			}
		});		
				
		
		/* build the gui */
		JComponent pane = (JComponent) getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		
		Box hBox1 = Box.createHorizontalBox();
			hBox1.add(new JLabel("Title: "));
			hBox1.add(titleTextField);
			hBox1.add(Box.createHorizontalGlue());
		pane.add(hBox1);
		
		pane.add(Box.createRigidArea(new Dimension(0, 10)));
		
		Box hBox2 = Box.createHorizontalBox();
			hBox2.add(new JLabel("URL: "));
			hBox2.add(urlTextField);
			hBox2.add(Box.createHorizontalGlue());
		pane.add(hBox2);
		
		pane.add(Box.createRigidArea(new Dimension(0, 10)));
		
		Box hBox3 = Box.createHorizontalBox();
			hBox3.add(radioCheckBox);
			hBox3.add(Box.createHorizontalGlue());
		pane.add(hBox3);
		
		pane.add(Box.createRigidArea(new Dimension(0, 10)));
		
		pane.add(helpLabel);
		
		pane.add(Box.createRigidArea(new Dimension(0,15)));
		
		Box buttonBox = Box.createHorizontalBox();
			buttonBox.add(Box.createHorizontalGlue());
			buttonBox.add(addButton);
			buttonBox.add(Box.createRigidArea(new Dimension(20,0)));
			buttonBox.add(cancelButton);
		pane.add(buttonBox);
		
		pack();
		setLocationRelativeTo(parent);
	}
		
}
