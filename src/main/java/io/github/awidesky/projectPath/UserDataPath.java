/*
 * Copyright (c) 2024 Eugene Hong
 *
 * This software is distributed under license. Use of this software
 * implies agreement with all terms and conditions of the accompanying
 * software license.
 * Please refer to LICENSE
 * */

package io.github.awidesky.projectPath;

import java.io.File;

public class UserDataPath {

	
	public static String appLocalFolder() {
		String home = System.getProperty("user.home");
		String os = System.getProperty("os.name").toLowerCase();
		String projectPath;
		if (os.startsWith("mac")) {
			projectPath = home + "/Library/Application Support/awidesky/YoutubeClipboardAutoDownloader";
		} else if (os.startsWith("windows")) {
			projectPath = System.getenv("LOCALAPPDATA") + "\\awidesky\\YoutubeClipboardAutoDownloader";
		} else {
			// Assume linux.
			projectPath = home + "/.local/share/awidesky/YoutubeClipboardAutoDownloader";
		}
		File f = new File(projectPath);
		f.mkdirs();
		if (!f.exists()) {
			SwingDialogs.error("Cannot detect appdata directory!", projectPath + "\nis not a valid data directory!", null, true);
			Main.kill(ExitCodes.PROJECTPATHNOTFOUND);
		}
		return projectPath;
	}
	
}
