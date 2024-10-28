package io.github.awidesky.projectPath;

import org.junit.jupiter.api.Test;

class JarPathTest {

	@Test
	void test() {
		JarPath.setDebug(false);
		System.out.println("[TEST.JarPathTest] ProjectPath with JarPath.class");
		System.out.println("[TEST.JarPathTest] \t" + JarPath.getProjectPath(JarPath.class));
		System.out.println("[TEST.JarPathTest] ProjectPath with JarPathTest.class");
		System.out.println("[TEST.JarPathTest] \t" + JarPath.getProjectPath(JarPathTest.class));
		System.out.println("[TEST.JarPathTest] JarName with JarPath.class");
		System.out.println("[TEST.JarPathTest] \t" + JarPath.getJarName(JarPath.class));
		System.out.println("[TEST.JarPathTest] JarName with JarPathTest.class");
		System.out.println("[TEST.JarPathTest] \t" + JarPath.getJarName(JarPathTest.class));
		System.out.println("[TEST.JarPathTest] isClassPathSearchFirst : " + JarPath.isClassPathSearchFirst());
		System.out.println();
		
		JarPath.setDebug(true);
		JarPath.getCandidates(JarPath.class).forEach(c -> {
			System.out.println("[TEST.JarPathTest] " + c.getDescription() + " :");
			System.out.println("[TEST.JarPathTest] \t" + c.generatePath());
		});
		System.out.println();
	}

}
