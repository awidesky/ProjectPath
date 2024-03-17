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
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class JarPath {

	public static String jarPath = null;
	
	/***
	 * Return the location of jar file.<p>
	 * If it's evaluated before, return the value.
	 * If not, generate new value.<p>
	 * 
	 * Note: when generating new value, this method calls
	 * {@code JarPath#getProjectPath(Class, String)} with {@code JarPath.class} and empty String,
	 * which means that the returning path might not precise.
	 * Prefer calling {@code JarPath#getProjectPath(Class, String)} at the first time.
	 * @return
	 */
	public static String getProjectPath() {
		if(jarPath != null) return jarPath;
		return (jarPath = generateProjectPath(JarPath.class, null));
	}
	public static String getProjectPath(Class<?> c) {
		return (jarPath = generateProjectPath(c, classLocationBased(c).get()));
	}
	/**
	 * Return the location of jar file..<p>
	 * Path of given class is checked for searching the location.
	 * If is hard to know exactly where the jar file resides,
	 * since various candidate returns different values, and it's unavailable
	 * to check if //TODO
	 * And given file should exist in the location.<P>
	 * Even if it's evaluated before, new value will be generate and stored.<p>
	 * After calling this method, prefer calling {@code JarPath#getProjectPath()}
	 * for getting the already generated data without re-generating it.
	 * 
	 * @param c
	 * @param file
	 * @return
	 */
	public static String getProjectPath(Class<?> c, String file) {
		return (jarPath = generateProjectPath(c, file));
	}
	
	
	private static String generateProjectPath(Class<?> c, String file) {
		List<Supplier<String>> projectPathCandidates = List.of(
				JarPath::jpackage, // System property jpackage.app-path
				JarPath::propertyBased, // System property java.class.path
				JarPath.classLocationBased(c), // c.class location
				JarPath::fileBased // new File("")
		);
		return projectPathCandidates.stream().filter(Objects::nonNull).map(candidate -> {
			String ret = candidate.get();
			File f = new File(ret).getAbsoluteFile();
			if(!f.isDirectory()) ret = f.getParentFile().getAbsolutePath();
			if (System.getProperty("jpackage.app-path") != null && !ret.endsWith("app")) {
				ret += File.separator + "app";
			}
			return ret + file != null ? File.separator + file : "";
		}).map(File::new).filter(File::exists).map(File::getParentFile).map(File::getAbsolutePath).findFirst().orElse(null);
	}
	
	/**
	 * Get project path by getting system property jpackage
	 * 
	 * doesn't work if the application is not packaged by jpackage
	 * */
	public static String jpackage() {
		return System.getProperty("jpackage.app-path");
	}
	/**
	 * Get project path by getting system property java.class.path
	 * 
	 * doesn't work in IDE(points bin folder of project root)
	 * */
	public static String propertyBased() {
		return System.getProperty("java.class.path");
	}
	/**
	 * Get project path by find path of given {@code Class} as an URL and decode as string
	 * 
	 * Code from https://stackoverflow.com/a/12733172
	 * doesn't work in IDE(points bin folder of project root)
	 * */
	public static Supplier<String> classLocationBased(Class<?> cl) {
		return urlToFile(getLocation(cl))::getAbsolutePath;
	}
	/**
	 * Get project path by getting absolute path of new File("")
	 * 
	 * This actually get a working directory, not a path of actual working directory.
	 * It works at most cases, but not when running the jar by command prompt whose working directory is not where jar file located.   
	 * */
	public static String fileBased() {
		return new File("").getAbsolutePath();
	}
	
	
	
	
	/**
	 * Gets the base location of the given class.
	 * <p>
	 * If the class is directly on the file system (e.g.,
	 * "/path/to/my/package/MyClass.class") then it will return the base directory
	 * (e.g., "file:/path/to").
	 * </p>
	 * <p>
	 * If the class is within a JAR file (e.g.,
	 * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
	 * path to the JAR (e.g., "file:/path/to/my-jar.jar").
	 * </p>
	 *
	 * @param c The class whose location is desired.
	 * @see FileUtils#urlToFile(URL) to convert the result to a {@link File}.
	 */
	private static URL getLocation(final Class<?> c) {
	    if (c == null) return null; // could not load the class

	    // try the easy way first
	    try {
	        final URL codeSourceLocation =
	            c.getProtectionDomain().getCodeSource().getLocation();
	        if (codeSourceLocation != null) return codeSourceLocation;
	    }
	    catch (SecurityException | NullPointerException e) {
	    	e.printStackTrace();
	    }

	    // NB: The easy way failed, so we try the hard way. We ask for the class
	    // itself as a resource, then strip the class's path from the URL string,
	    // leaving the base path.

	    // get the class's raw resource path
	    final URL classResource = c.getResource(c.getSimpleName() + ".class");
	    if (classResource == null) return null; // cannot find class resource

	    final String url = classResource.toString();
	    final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
	    if (!url.endsWith(suffix)) return null; // weird URL

	    // strip the class's path from the URL string
	    final String base = url.substring(0, url.length() - suffix.length());

	    String path = base;

	    // remove the "jar:" prefix and "!/" suffix, if present
	    if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

	    try {
	        return new URL(path);
	    }
	    catch (final MalformedURLException e) {
	    	e.printStackTrace();
	        return null;
	    }
	} 

	/**
	 * Converts the given {@link URL} to its corresponding {@link File}.
	 * <p>
	 * This method is similar to calling {@code new File(url.toURI())} except that
	 * it also handles "jar:file:" URLs, returning the path to the JAR file.
	 * </p>
	 * 
	 * @param url The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException if the URL does not correspond to a file.
	 */
	private static File urlToFile(final URL url) {
	    return url == null ? null : urlToFile(url.toString());
	}

	private static final Pattern FILEURLPATTERN = Pattern.compile("file:[A-Za-z]:.*");
	/**
	 * Converts the given URL string to its corresponding {@link File}.
	 * 
	 * @param url The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException if the URL does not correspond to a file.
	 */
	private static File urlToFile(final String url) {
	    String path = url;
	    if (path.startsWith("jar:")) {
	        // remove "jar:" prefix and "!/" suffix
	        final int index = path.indexOf("!/");
	        path = path.substring(4, index);
	    }
	    try {
	        if (System.getProperty("os.name").startsWith("Windows") && FILEURLPATTERN.matcher(path).matches()) {
	            path = "file:/" + path.substring(5);
	        }
	        return new File(new URL(path).toURI());
	    }
	    catch (final MalformedURLException | URISyntaxException e) {
	    	e.printStackTrace();
	    }
	    if (path.startsWith("file:")) {
	        // pass through the URL as-is, minus "file:" prefix
	        path = path.substring(5);
	        return new File(path);
	    }
	    return null;
	}
}
