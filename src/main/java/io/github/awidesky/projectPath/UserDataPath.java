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

/**
 * Generate user's application local data folder.
 * <p>
 * <ul>
 * <li>In MacOS : {@code /Users/(username)/Library/Application Support}</li>
 * <li>In Windows : {@code C:\Users\(username)\AppData\local}</li>
 * <li>In Linux : {@code /home/(username)/.local/share}</li>
 * </ul>
 * 
 */
public class UserDataPath {
	
	private static final String appLocalRoot = OS.CURRUNTOS.appLocalRoot();
	
	/***
	 * Returns application local data folder.
	 * The parameters will be the subfolders under the the user specific local application data folder.<p>
	 * <ul>
	 * <li>In MacOS : {@code /Users/(username)/Library/Application Support/subfolders[0]/subfolders[1]/.../subfolders[n]}</li>
	 * <li>In Windows : {@code C:\Users\(username)\AppData\local\subfolders[0]\subfolders[1]\...\subfolders[n]}</li>
	 * <li>In Linux : {@code /home/(username)/.local/share/subfolders[0]/subfolders[1]/.../subfolders[n]}</li>
	 * </ul>
	 *  
	 * @param subFolders the names of subdirectories under application local folder
	 */
	public static String appLocalFolder(String... subFolders) {
		return appLocalRoot + File.separator + Stream.of(subFolders).collect(Collectors.joining(File.separator));
	}

	/**
	 * Returns Windows-only appdata roaming folder.
	 * Such as : {@code C:\\Users\\username\\AppData\\Roaming}.
	 * <p>If the OS is not Windows, ther returned value will be same as {@code UserDataPath#appLocalRoot}.
	 * 
	 * @param subFolders the names of subdirectories under application local folder
	 */
	public static String getWindowsAppdataRoamingFolder(String... subFolders) {
		String ret = appLocalFolder(subFolders);
		if(OS.CURRUNTOS == OS.WINDOWS && ret.contains("AppData\\Local")) {
			ret = ret.replace("AppData\\Local", "AppData\\Roaming");
		}
		return ret;
	}
}
