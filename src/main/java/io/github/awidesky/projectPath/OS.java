package io.github.awidesky.projectPath;

import java.io.File;

/***
 * Indicate the type of current OS.
 * The local application data directory can be obtained by calling {@code OS#appLocalRoot()},
 * but prefer using {@code UserDataPath#appLocalFolder()}.
 */
public enum OS {
	WINDOWS(System.getenv("LOCALAPPDATA")),
	MACOS(System.getProperty("user.home") + "/Library/Application Support".replace('/', File.separatorChar)),
	LINUX(System.getProperty("user.home") + "/.local/share".replace('/', File.separatorChar));
	
	public static final OS CURRUNTOS = findOS();
	
	private String appLocalRoot;
	OS(String appLocalRoot) {
		this.appLocalRoot = appLocalRoot;
	}

	/***
	 * Returns the local application data directory.<p>
	 * This method returns same value with {@code OS#appLocalRoot()}
	 * method without any arguments.
	 */
	public String appLocalRoot() {
		return appLocalRoot;
	}

	private static OS findOS() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("mac")) {
			return MACOS;
		} else if (os.startsWith("windows")) {
			return WINDOWS;
		} else {
			// Assume linux.
			return LINUX;
		}
	}

}
