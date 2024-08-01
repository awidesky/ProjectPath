package io.github.awidesky.projectPath;

import org.junit.jupiter.api.Test;

class UserDataPathTest {

	@Test
	void test() {
		System.out.println();
		System.out.println("[TEST.UserDataPathTest] OS name : " + OS.CURRUNTOS.name());
		System.out.println("[TEST.UserDataPathTest] appdata root : " + OS.CURRUNTOS.appLocalRoot());
		System.out.println("[TEST.UserDataPathTest] appLocalFolder(\"1\", \"2\", \"3\") : "
			+ UserDataPath.appLocalFolder("1", "2", "3"));
		System.out.println("[TEST.WindowsAppdataRoamingTest] Windows Appdata Roaming(\"1\", \"2\", \"3\") : "
			+ UserDataPath.getWindowsAppdataRoamingFolder("1", "2", "3"));
		System.out.println();
	}

}
