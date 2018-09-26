![](https://img.shields.io/maven-central/v/com.profesorfalken/jPowerShell.svg)
![](https://img.shields.io/github/license/profesorfalken/jPowerShell.svg)
![](https://travis-ci.org/profesorfalken/jPowerShell.svg)

# jPowerShell

Simple Java API to interact with PowerShell console

## Installation ##

To install jPowerShell you can add the dependecy to your software project management tool: http://mvnrepository.com/artifact/com.profesorfalken/jPowerShell/2.1.1

For example, for Maven you have just to add to your pom.xml: 

      <dependency>
	        <groupId>com.profesorfalken</groupId>
	        <artifactId>jPowerShell</artifactId>
	        <version>2.1.1</version>
        </dependency>

Instead, you can direct download the JAR file and add it to your classpath. 
https://repo1.maven.org/maven2/com/profesorfalken/jPowerShell/2.1.1/jPowerShell-2.1.1.jar

## Basic Usage ##

The best way to document is providing a good example:

#### Single command execution ####

```java
   //Execute a command in PowerShell session
   PowerShellResponse response = PowerShell.executeSingleCommand("Get-Process");
   
   //Print results
   System.out.println("List Processes:" + response.getCommandOutput());
```


#### Executing one or multiple commands using the same PowerShell session ####

```java
   PowerShell powerShell = null;
   try {
       //Creates PowerShell session (we can execute several commands in the same session)
       powerShell = PowerShell.openSession();
       
       //Execute a command in PowerShell session
       PowerShellResponse response = powerShell.executeCommand("Get-Process");
       
       //Print results
       System.out.println("List Processes:" + response.getCommandOutput());
       
       //Execute another command in the same PowerShell session
       response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");
       
       //Print results
       System.out.println("BIOS information:" + response.getCommandOutput());
   } catch(PowerShellNotAvailableException ex) {
       //Handle error when PowerShell is not available in the system
       //Maybe try in another way?
   } finally {
       //Always close PowerShell session to free resources.
       if (powerShell != null)
         powerShell.close();
   }
```

#### Configure jPowerShell Session ####

We can easily configure the jPowerShell session:

* *By project* creating a _jpowershell.properties_ file in the classpath of your project and settings the variables you want to override.
* *By call*, using a map that can be chained to powershell call.
 
For example: 

```java
    //Set the timeout when waiting for command to terminate to 30 seconds instead of 10 (default)
    Map<String, String> myConfig = new HashMap<>();
    myConfig.put("maxWait", "30000");
    response = powerShell.configuration(myConfig).executeCommand("Get-WmiObject Win32_BIOS");
```

The three variables that can be configured in jPowerShell are: 

*maxThreads*: the maximum number of thread to use in pool. 3 is an optimal and default value

*waitPause*: the pause in ms between each loop pooling for a response. Default value is 10

*maxWait*: the maximum wait in ms for the command to execute. Default value is 10000

*remoteMode*: it should be true when we are executing a command in remote. Otherwise the execution will finish in timeout.

#### Setting the PowerShell executable path ####

If the PowerShell executable has a different name/path on your system, you can change it when opening a new session: 

```java
   PowerShellResponse response = null;
   try {
       //Creates PowerShell session
       PowerShell powerShell = PowerShell.openSession("myCustomPowerShellExecutable.exe");
       [...]
```       

#### Executing PowerShell Script ####

In order to execute a PowerShell Script it is recommended to use the executeScript() method instead of executeCommand():

```java
   PowerShellResponse response = null;
   try {
       //Creates PowerShell session
       PowerShell powerShell = PowerShell.openSession();
       //Increase timeout to give enough time to the script to finish
       Map<String, String> config = new HashMap<String, String>();
       config.put("maxWait", "80000");
       
       //Execute script
       response = powerShell.configuration(config).executeScript("./myPath/MyScript.ps1");
       
       //Print results if the script
       System.out.println("Script output:" + response.getCommandOutput());
   } catch(PowerShellNotAvailableException ex) {
       //Handle error when PowerShell is not available in the system
       //Maybe try in another way?
   } finally {
       //Always close PowerShell session to free resources.
       if (powerShell != null)
         powerShell.close();
   }
```
#### Executing PowerShell Scripts packaged inside jar ####

In order to execute a PowerShell Script that is bundled inside a jar you must use a BufferedReader to load the resource:

```java
    PowerShell powerShell = PowerShell.openSession();
    String script = "resourcePath/MyScript.ps1"
    String scriptParams = "-Parameter value"

    //Read the resource
    BufferedReader srcReader = new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream(script)));

    if (scriptParams != null && !scriptParams.equals("")) {
        response = powerShell.executeScript(srcReader, scriptParams);
    } else {
        response =  powerShell.executeScript(srcReader);
    }
```
