package io.github.awidesky.projectPath;

import org.junit.jupiter.api.Test;

class JarPathTest {

	@Test
	void test() {
		System.out.println();
		JarPath.getCandidates(JarPath.class).entrySet().forEach(e -> {
			System.out.println("[TEST.JarPathTest] " + e.getKey() + " :");
			System.out.println("[TEST.JarPathTest] \t" + e.getValue().get());
		});
		System.out.println();
	}

}
