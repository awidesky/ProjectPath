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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/***
 * Provides location of the running .jar file, or project's root folder(if run on IDE).
 * {@code JarPath} needs a proper {@code Class} instance of a class that's packaged into
 * the running .jar file to find the location of it.
 * If none was provided, {@code JarPath} will use {@code JarPath.class}, which means
 * it considers the {@code JarPath} class is packaged into the main .jar file.<p>
 * 
 * Return value of last call is cached; {@code JarPath#getProjectPath()}
 * will return the cached value without re-evalueation if present.<p>
 * 
 * <ul>
 * There are two main "approaches" to find the path.
 * 	<li>"working directory" approach</li>
 * 	<ul>
 * 		<li>Tries to get the working directory.</il>
 * 		<li>It works at most cases, but not when running the jar by command prompt
 * 			whose working directory is not where jar file located.</li>
 * 	</ul>
 * 	<li>"class file path" approach</li>
 * 	<ul>
 * 		<li>Tries to get project path by searching path of the classes inside of jar/project</il>
 * 		<li>Evaluates system property {@code java.class.path} or absolute path of {@code new File("")} </il>
 * 		<li>Doesn't work in IDE(points bin folder of target/classes).</li>
 * 	</ul>
 * </ul>
 * The "working directory" approach is used first, and this behavior can change via
 * {@link JarPath#setClassPathSearchFirst(boolean)}.
 */
public class JarPath {

	private static boolean classPathSearchFirst = false;
	private static String jarPath = null;
	private static boolean debug = false;
	
	/**
	 * Returns {@code true} if debug mode.
	 * In debug mode, exception message is printed for each candidate.
	 * <br>If {@link JarPath#getProjectPath()} returns invalid path or fails,
	 * try setting debug mode and call {@link JarPath#getProjectPath(Class)}
	 * again to see why candidates fail.
	 *  
	 * @return {@code true} if debug mode.
	 */
	public static boolean isDebug() {
		return debug;
	}
	
	/**
	 * Sets debug mode. Default is {@code false}.
	 * 
	 * @param debug new debug mode.
	 */
	public static void setDebug(boolean debug) {
		JarPath.debug = debug;
	}
	
	/**
	 * Checks if "class file path approach" is tried before "working directory approach".<br>
	 * The default value is {@code false}.
	 * @return {@code true} if "class file path approach" is tried before "working directory approach".
	 */
	public static boolean isClassPathSearchFirst() {
		return classPathSearchFirst;
	}
	/**
	 * Sets whether "class file path approach" is tried before "working directory approach" or not.<br>
	 * The default value is {@code false}.
	 */
	public static void setClassPathSearchFirst(boolean classFileSearchFirst) {
		JarPath.classPathSearchFirst = classFileSearchFirst;
	}
	
	/***
	 * Return the location of jar file or project path(if run on IDE).<p>
	 * If it's evaluated before, return the value.
	 * If not, generate new value.<p>
	 * 
	 * Note: when generating new value, this method calls
	 * {@code JarPath#getProjectPath(Class, String)} with {@code JarPath.class},
	 * which means that the returned path points to the directory of a .jar file
	 * that includes {@code JarPath} class.
	 * 
	 * @return a path to the directory of .jar file (or project) that contains this class 
	 */
	public static String getProjectPath() {
		if(jarPath != null) return jarPath;
		return getProjectPath(JarPath.class);
	}
	/***
	 * Return the location of jar file or project path(if run on IDE).<p>
	 * If it's evaluated before, return the value.
	 * If not, generate new value.<p>
	 * 
	 * @param c a class whose .class file resides inside of running .jar
	 * @return a path to the directory of .jar file (or project) that contains given class 
	 */
	public static String getProjectPath(Class<?> c) {
		return (jarPath = generateProjectPath(c, getJarName(c)));
	}
	
	/***
	 * Returns the name of the .jar file that contains given class.
	 * 
	 * @param c a class whose .class file resides inside of running .jar
	 * @return the name of .jar file, or {@code null}
	 */
	public static String getJarName(Class<?> c) {
		String ret = null;
		try {
			ret = new File(classLocationBased(c).get()).getName();
			if(!ret.endsWith(".jar")) ret = null;
		} catch (Exception e) {
			if(isDebug()) e.printStackTrace();
		}
		return ret;
	}
	/**
	 * Return the location of jar file or project path(if run on IDE).<p>
	 * Given file used to check if it's the right location.
	 * In default({@code JarPath#getProjectPath(Class)}),
	 * the name of the jar file(or in IDE, name of {@code bin} or {@code classes} folder) is passed,
	 * so that we can check if the jar file resides in the one of the candidate paths.
	 * If there's none, of the pathname is invalid(or {@code null}, the first candidate,
	 * which is the first element of {@code JarPath#getCandidates(Class)} is returned.
	 * If all of the candidate is null(normally won't happen), an empty String is return`ed.
	 * <P>
	 * 
	 * Even if it's evaluated before, new value will be generate and stored.<p>
	 * After calling this method, prefer calling {@code JarPath#getProjectPath()}
	 * for getting the already generated data without re-generating it.
	 *
	 * @param c a class whose .class file resides inside of running .jar
	 * @param file a file that has to exist in desired directory
	 * @return a path to the directory of .jar file (or project) that contains this class 
	 */
	public static String getProjectPath(Class<?> c, String file) {
		return (jarPath = generateProjectPath(c, file));
	}
	
	
	private static String generateProjectPath(Class<?> c, String file) {
		List<CandidateEntry> list = getCandidates(c);
		return list.stream()
				.map(CandidateEntry::generatePath)
				.filter(Objects::nonNull)
				.map(File::new)
				.filter(File::exists)
				.filter(f -> file == null || new File(f, file).exists())
				.map(File::getAbsolutePath)
				.findFirst()
				.orElseGet(() -> {
					if(isDebug()) System.out.println("[JarPath|debug] Unable to find. just return the first non-null thing or empty String");
					return list.stream() //unable to find. just return the first non-null thing
							.map(CandidateEntry::generatePath)
							.filter(Objects::nonNull)
							.findFirst()
							.orElse("");
				});
	}
	
	/**
	 * Get list of candidates used to find jar path.
	 * 
	 * @param c
	 * @return
	 */
	public static List<CandidateEntry> getCandidates(Class<?> c) {
		LinkedList<CandidateEntry> ret = new LinkedList<>();		
		ret.add(new CandidateEntry("System property user.dir", JarPath::property_userdir)); //"working directory" approach #1
		ret.add(new CandidateEntry("new File(\"\")" , JarPath::fileBased)); //"working directory" approach #2
		
		ret.add(new CandidateEntry(c.getSimpleName() + "Class ProtectionDomain CodeSource location", JarPath.classLocationBased(c))); //"class file path" approach #1
		ret.add(new CandidateEntry("System property java.class.path", JarPath::property_javaclasspath)); //"class file path" approach #2

		if(classPathSearchFirst) Collections.reverse(ret);
		
		ret.add(0, new CandidateEntry("System property jpackage.app-path", JarPath::jpackage)); 
		
		return ret;
	}
	
	/**
	 * Stores the path candidate generator.
	 * Entry has the path generator method(the result can be acquired via
	 * {@link CandidateEntry#generatePath()}), and its description(
	 * {@link CandidateEntry#getDescription()}).
	 */
	public static class CandidateEntry {
		private final String description;
		private final Supplier<String> gen;

		public CandidateEntry(String description, Supplier<String> gen) {
			this.description = description;
			this.gen = () -> {
				String get = gen.get();
				if(get == null) {
					if(isDebug()) {
						System.out.println("[JarPath|debug] Candidate \"" + description + "\" returned null!");
					}
					return null;
				}
				File f = new File(get).getAbsoluteFile();
				while (!f.isDirectory()) f = f.getParentFile();
				String ret = f.getAbsolutePath();
				if (System.getProperty("jpackage.app-path") != null && !ret.endsWith("app")) {
					ret += File.separator + "app";
				}
				return ret;
			};
		}
		
		/**
		 * Get description of this path candidate generator method for debug.
		 * @return description of this path candidate generator method
		 */
		public String getDescription() {
			return description;
		}
		
		/**
		 * Evaluate the {@code Supplier} and return it.
		 * @return possible jar path
		 */
		public String generatePath() {
			String ret = null;
			try {
				ret = gen.get();
			} catch (Exception e) {
				if(isDebug()) e.printStackTrace();
			}
			return ret;
		}
		
		@Override
		public String toString() {
			return getDescription() + " : " + generatePath();
		}
	}
	
	/**
	 * Get project path by getting system property jpackage
	 * 
	 * doesn't work if the application is not packaged by jpackage
	 * */
	private static String jpackage() {
		return System.getProperty("jpackage.app-path");
	}
	/**
	 * Get project path by getting system property user.dir
	 * 
	 * This actually get a working directory.
	 * It works at most cases, but not when running the jar by command prompt
	 * whose working directory is not where jar file located.
	 * */
	private static String property_userdir() {
		return System.getProperty("user.dir");
	}
	/**
	 * Get project path by getting absolute path of new File("")
	 * 
	 * This actually get a working directory.
	 * It works at most cases, but not when running the jar by command prompt
	 * whose working directory is not where jar file located.   
	 * */
	private static String fileBased() {
		return new File("").getAbsolutePath();
	}
	/**
	 * Get project path by getting system property java.class.path
	 * 
	 * doesn't work in IDE(points bin folder of target/classes)
	 * */
	private static String property_javaclasspath() {
		return System.getProperty("java.class.path").split(File.pathSeparator)[0];
	}
	/**
	 * Get project path by find path of given {@code Class} as an URL and decode as string
	 * 
	 * Code from https://stackoverflow.com/a/12733172
	 * doesn't work in IDE(points bin folder of target/classes)
	 * */
	private static Supplier<String> classLocationBased(Class<?> cl) { 
		return () -> urlToFile(getLocation(cl)).getAbsolutePath();
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
	        if (codeSourceLocation != null && !codeSourceLocation.toExternalForm().startsWith("rsrc"))
	        	return codeSourceLocation;
	    }
	    catch (SecurityException | NullPointerException e) {
	    	if(isDebug()) e.printStackTrace();
	    }

	    // NB: The easy way failed, so we try the hard way. We ask for the class
	    // itself as a resource, then strip the class's path from the URL string,
	    // leaving the base path.

	    // get the class's raw resource path
	    final URL classResource = c.getResource(c.getSimpleName() + ".class");
	    if (classResource == null) {
	    	if(isDebug()) System.out.println("[JarPath|debug] JarPath.getLocation : Cannot find class resource");
	    	return null; // cannot find class resource
	    }

	    final String url = classResource.toString();
	    final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
	    if (!url.endsWith(suffix)) {
	    	if(isDebug()) System.out.println("[JarPath|debug] JarPath.getLocation : Weird URL");
	    	return null; // weird URL
	    }

	    // strip the class's path from the URL string
	    final String base = url.substring(0, url.length() - suffix.length());

	    String path = base;

	    // remove the "jar:" prefix and "!/" suffix, if present
	    if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

	    try {
	        return new URI(path).toURL();
	    } catch (MalformedURLException | URISyntaxException e) {
	    	if(isDebug()) e.printStackTrace();
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
	        return new File(new URI(path));
	    }
	    catch (final URISyntaxException e) {
	    	if(isDebug()) e.printStackTrace();
	    }
	    if (path.startsWith("file:")) {
	        // pass through the URL as-is, minus "file:" prefix
	        path = path.substring(5);
	        return new File(path);
	    }
	    if(isDebug()) System.out.println("[JarPath|debug] Invalid url : " + url);
	    return null;
	}
}
