package com.soundcenter.soundcenter.client.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class SCFileFilter extends FileFilter {
	
	private String ext;
	private String desc;
	private boolean acceptDirs = true;


	public SCFileFilter(String ext) {
		this.ext = ext;
		this.desc = ext+"-Files ("+generateExtensionString(ext)+")";
	}

	public SCFileFilter(String ext, String desc){
    	this.ext = ext;
    	this.desc = desc + " ("+generateExtensionString(ext)+")";
	}

	public String getDescription() {
		return desc;
	}

	public boolean accept(File f) {
		if(f.isDirectory() && acceptDirs) {
			return true;
		}
		if(!isMultiExtensionString(ext)) {
			return f.getName().endsWith(ext);
		}
		String[] exts = ext.split(",");
		for(int i=0; i<exts.length; i++) {
			if(f.getName().endsWith(exts[i]))
				return true;
		}
		return false;
	}

	protected boolean isMultiExtensionString(String ext) {
		return ext.indexOf(',') >= 0;
	}

	protected String generateExtensionString(String ext) {
		String[] exts = ext.split(",");
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<exts.length; i++) {
			sb.append("*.");
        	sb.append(exts[i]);
        	if(i != exts.length-1)
        		sb.append(",");
		}
		return sb.toString();
	}

}
