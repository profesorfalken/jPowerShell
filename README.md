![](https://img.shields.io/maven-central/v/com.profesorfalken/jPowerShell.svg)
![](https://img.shields.io/github/license/profesorfalken/jPowerShell.svg)

# jPowerShell

Simple Java API to interact with PowerShell console

## Installation ##

To install jPowerShell you can add the dependecy to your software project management tool: http://mvnrepository.com/artifact/com.profesorfalken/jPowerShell/1.3.2

For example, for Maven you have just to add to your pom.xml: 

      <dependency>
	        <groupId>com.profesorfalken</groupId>
	        <artifactId>jPowerShell</artifactId>
	        <version>1.3.2</version>
        </dependency>

Instead, you can direct download the JAR file and add it to your classpath. 
https://repo1.maven.org/maven2/com/profesorfalken/jPowerShell/1.3.2/jPowerShell-1.3.2.jar

## Basic Usage ##

The best way to document is providing a good example:

#### Single command execution ####

```java
   //Execute a command in PowerShell session
   PowerShellResponse response = powerShell.executeSingleCommand("Get-Process");
   
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
