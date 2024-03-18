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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserDataPath {
	
	private static final String home = System.getProperty("user.home");
	private static final String os = System.getProperty("os.name").toLowerCase();
	
	/***
	 * Returns application data folder.
	 * The parameters will be the subfolders under the the user specific local application data folder.<p>
	 * <ul>
	 * <li>In MacOS : {@code /Users/(username)/Library/Application Support/subfolders[0]/subfolders[1]/.../subfolders[n]}</li>
	 * <li>In Windows : {@code C:\Users\(username)\AppData\local\subfolders[0]\subfolders[1]\...\subfolders[n]}</li>
	 * <li>In Linux : {@code /home/(username)/.local/share/subfolders[0]/subfolders[1]/.../subfolders[n]}</li>
	 * </ul>
	 * @param pathNames
	 * @return
	 */
	public static String appLocalFolder(String... subfolders) {
		String projectPath;
		if (os.startsWith("mac")) {
			projectPath = home + "/Library/Application Support".replace('/', File.separatorChar);
		} else if (os.startsWith("windows")) {
			projectPath = System.getenv("LOCALAPPDATA");
		} else {
			// Assume linux.
			projectPath = home + "/.local/share".replace('/', File.separatorChar);
		}
		projectPath += File.separator + Stream.of(subfolders).collect(Collectors.joining(File.separator));
		new File(projectPath).mkdirs();
		return projectPath;
	}
	
}
