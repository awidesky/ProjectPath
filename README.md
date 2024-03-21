# ProjectPath

## Overview

**ProjectPath**(inspired by [hawarta/AppDirs](https://github.com/harawata/appdirs)) provides paths for application to store user-specific data or find resources with plain Java

## Requirements

**ProjectPath** requires Java 8 or later, and it does not need any dependency.

## How to use

[Add dependency](https://central.sonatype.com/artifact/io.github.awidesky/ProjectPath) to your `pom.xml`  download `.jar` file from [release](https://github.com/awidesky/ProjectPath/releases) and add to classpath.

## Examples

Following classes are used to acquire  various paths :

* `JarPath.getProjectPath(...)` - the location of `.jar` file, or project's root folder(if run on IDE)

* `UserDataPath.appLocalFolder()` - location of application data folder of current user.

```java
import io.github.awidesky.projectPath.JarPath;
import io.github.awidesky.projectPath.UserDataPath;

public class Test {
  public static void main(String[] args) {
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
     * UserDataPath finds application local data folder.
     * Parameters(optional) will be resolved as subdirectories of the folder.
     * Note that resolved folder will not created.  
     */
    System.out.println("App data path : "
        + UserDataPath.appLocalFolder("myapp", "config"));
  }
}
```



### Results in MacOS

Run in eclipse :

```
Project path : /Users/username/eclipse-workspace/Test
App data path : /Users/username/Library/Application Support/myapp/config
```

Put `Test.jar` in Documents folder, and run with `java -jar Test.jar` 

```
Project path : /Users/username/Documents
App data path : /Users/username/Library/Application Support/myapp/config
```



### Results in Windows

Run in eclipse :

```
Project path : C:\Users\username\eclipse-workspace\Test
App data path : C:\Users\username\AppData\Local\myapp\config
```

Put `Test.jar` in Documents folder, and run with `java -jar Test.jar` 

```
Project path : C:\Users\username\Documents
App data path : C:\Users\username\AppData\Local\myapp\config
```



### Results in Linux

Put `Test.jar` in Documents folder, and run with `java -jar Test.jar` 

```
Project path : /home/username/Documents
App data path : /home/username/.local/share/myapp/config
```

