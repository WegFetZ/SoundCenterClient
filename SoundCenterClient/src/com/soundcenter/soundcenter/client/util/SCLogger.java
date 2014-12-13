package com.soundcenter.soundcenter.client.util;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.SwingUtilities;

import com.soundcenter.soundcenter.client.AppletStarter;
import com.soundcenter.soundcenter.client.gui.UserInterface;

public class AppletLogger {

	private Logger log = null;
	private UserInterface gui = null;
	
	public AppletLogger(Logger log, UserInterface gui) {
		this.log = log;
		this.gui = gui;
		
		/*
			//Redirect console streams to logarea
			TextAreaOutputStream logOut = new TextAreaOutputStream(gui.generalTab.logArea);
			System.setOut(new PrintStream(logOut));
			System.setErr(new PrintStream(logOut));
		*/
		
		try {
			FileHandler fileHandler = new FileHandler(AppletStarter.dataFolder + "sc_log%g.txt", 1024 * 512, 3);
			fileHandler.setFormatter(new SimpleFormatter());
			log.setUseParentHandlers(false);
			log.addHandler(fileHandler);
		} catch (Exception e) {
			w("Could not create FileHandler for the logger:", e);
		}
		
		log.setLevel(Level.INFO);
	}
	
	public synchronized void d(String msg, Exception e) {
		if (gui.controller.isDebugActive()) {
			log.info("[DEBUG] " + msg);
			appendMessage("[DEBUG] " + msg);
			if (e != null) {
				log.log(Level.INFO, "\n[DEBUG]" + e.getMessage(), e);
				appendStackTrace(e);
			}
		}
	}
	
	public synchronized void i(String msg, Exception e) {
		log.info(msg);
		appendMessage("[INFO] " + msg);
		if (gui.controller.isDebugActive() && e != null) {
			log.log(Level.INFO, "\n" + e.getMessage(), e);
			appendStackTrace(e);
		}
	}
	
	public synchronized void w(String msg, Exception e) {
		log.warning(msg);
		appendMessage("[WARNING] " + msg);
		if (e != null) {
			log.log(Level.WARNING, "\n" + e.getMessage(), e);
			appendStackTrace(e);
		}
	}
	
	public synchronized void s(String msg, Exception e) {
		log.severe(msg);
		appendMessage("[SEVERE] " + msg);
		if (e != null) {
			log.log(Level.SEVERE, "\n" + e.getMessage(), e);
			appendStackTrace(e);
		}
	}
	
	public synchronized void lineBreak(int count) {
		for (int i = 0; i<count; i++) {
			appendMessage("\n");
		}
	}
	
	private synchronized void appendMessage(final String msg) {
		Runnable  runnable = new Runnable() {
            public void run(){
            	gui.generalTab.logArea.append(msg + "\n");
        		gui.generalTab.logArea.setCaretPosition(gui.generalTab.logArea.getDocument().getLength());
            }
        };
        SwingUtilities.invokeLater(runnable);
	}
	
	private synchronized void appendStackTrace(final Exception e) {
		Runnable  runnable = new Runnable() {
            public void run(){
            	gui.generalTab.logArea.append(e.getMessage() + "\n");
        		for (StackTraceElement element : e.getStackTrace()) {
        			gui.generalTab.logArea.append("\t" + element.toString() + "\n");
        		}
        		gui.generalTab.logArea.setCaretPosition(gui.generalTab.logArea.getDocument().getLength());
            }
        };
        SwingUtilities.invokeLater(runnable);
	}
	
	/*
		private class TextAreaOutputStream extends OutputStream {
			
			private JTextArea textArea;
		     
		    private TextAreaOutputStream(JTextArea textArea) {
		        this.textArea = textArea;
		    }
		     
		    @Override
		    public void write(final int b) throws IOException {
		    	
		    	Runnable  runnable = new Runnable() {
		            public void run(){
		            	textArea.append(String.valueOf((char) b));
		    	        textArea.setCaretPosition(textArea.getDocument().getLength());
		            }
		        };
		        SwingUtilities.invokeLater(runnable);
		    }
			
		}
	*/
	
}
