package com.soundcenter.soundcenter.client.util;

import java.awt.Dimension;
import javax.swing.JComponent;

public class GuiUtil {

	public static void setFixedSize(JComponent component, Dimension size) {
		
		component.setMinimumSize(size);
		component.setMaximumSize(size);
		component.setPreferredSize(size);
		
	}
}
