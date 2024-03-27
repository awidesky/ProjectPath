package io.github.awidesky.projectPath;

/**
 * Test code for README example
 * */
class Test {

  @org.junit.jupiter.api.Test
  void test() {
    /*
     * Actually calls JarPath.getProjectPath(JarPath.class)
     * May not work if ProjectPath library is in another location. 
     */
    System.out.println("Project path : "
        + JarPath.getProjectPath());
    /*
     * If Test.class is packaged into a .jar file,
     * JarPath will retrieve the name of .jar file that contains given class(Test.class),
     * and check the .jar file resides in one of the candidate paths.
     * 
     * If Test.class is not packaged into a .jar file
     * JarPath cannot make sure, so the most-likly candidate will be returned.
     * (will yield same results with JarPath.getProjectPath())
     */
    //System.out.println("JarPath.getProjectPath(Test.class) : "
    //    + JarPath.getProjectPath(Test.class));
    /*
     * In this case, JarPath does not check existence of .jar file.
     * Instead, it checks if given file resides in any of the candidate paths.
     * 
     * If the given file does not exists in any of the candidates,
     * JarPath cannot make sure, so the most-likly candidate will be returned.
     * (will yield same results with JarPath.getProjectPath())
     */
    //System.out.println("JarPath.getProjectPath(Test.class, \"myResource.txt\") : "
    //    + JarPath.getProjectPath(Test.class, "myResource.txt"));
	/*
	 * Get name of the jar file contains given class
	 */
	System.out.println("Name of jar file contains JarPath.class : "
			+ JarPath.getJarName(JarPath.class));
    /*
     * UserDataPath finds application local data folder.
     * Parameters(optional) will be resolved as subdirectories of the folder.
     * Note that resolved folder will not created.  
     */
    System.out.println("App data path : "
        + UserDataPath.appLocalFolder("myapp", "config"));
  }
}
