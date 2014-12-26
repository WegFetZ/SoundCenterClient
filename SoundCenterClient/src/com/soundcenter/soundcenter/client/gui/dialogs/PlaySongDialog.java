package com.soundcenter.soundcenter.client.gui.dialogs;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.gui.actions.MusicTabActions;
import com.soundcenter.soundcenter.lib.data.Song;

@SuppressWarnings("serial")
public class PlaySongDialog extends JDialog {
	
	public Song song;
	
	public JRadioButton selfButton = new JRadioButton("only for me.");
	public JRadioButton globalButton = new JRadioButton("for everyone on the server (globally).");
	public JRadioButton worldButton = new JRadioButton("for everyone in world:");
	public JButton playButton = new JButton("Play");
	public JButton cancelButton = new JButton("Cancel");
	public JTextField worldTextField = new JTextField();

	ButtonGroup buttonGroup = new ButtonGroup();
	
	public PlaySongDialog(JFrame parent, Song song, final boolean play) {
		super(parent);
		this.setModal(true);
		this.setModalityType(DEFAULT_MODALITY_TYPE);
		
		this.song = song;		
		
		//initialize components
		String actionTitle = "Play";
		if (!play) {
			actionTitle = "Stop";
		}
		setTitle(actionTitle + " song");
		playButton.setText(actionTitle);
		
		buttonGroup.add(selfButton);
		buttonGroup.add(globalButton);
		buttonGroup.add(worldButton);
		
		globalButton.setEnabled(Client.database.permissionGranted("sc.play.global"));
		worldButton.setEnabled(Client.database.permissionGranted("sc.play.world"));
		worldTextField.setEnabled(false);
		
		selfButton.setSelected(true);
		
		
		//Actions
		final PlaySongDialog frame = this;
		
		ActionListener radioButtonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MusicTabActions.playSongDialogRadioButtonSelected(frame);
			}
		};
		selfButton.addActionListener(radioButtonListener);
		globalButton.addActionListener(radioButtonListener);
		worldButton.addActionListener(radioButtonListener);
		
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MusicTabActions.playSongDialogPlayButtonPressed(frame, play);		
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
			Box vBox1 = Box.createVerticalBox();
				vBox1.add(new JLabel(actionTitle + " song"));
				vBox1.add(selfButton);
				vBox1.add(globalButton);
				vBox1.add(worldButton);
				vBox1.add(worldTextField);
			hBox1.add(vBox1);
			hBox1.add(Box.createHorizontalGlue());
		pane.add(hBox1);
		
		pane.add(Box.createRigidArea(new Dimension(0, 10)));
		pane.add(Box.createVerticalGlue());
		
		Box buttonBox = Box.createHorizontalBox();
			buttonBox.add(Box.createHorizontalGlue());
			buttonBox.add(playButton);
			buttonBox.add(Box.createRigidArea(new Dimension(20,0)));
			buttonBox.add(cancelButton);
		pane.add(buttonBox);
		
		pack();
		setLocationRelativeTo(parent);
	}
		
}
