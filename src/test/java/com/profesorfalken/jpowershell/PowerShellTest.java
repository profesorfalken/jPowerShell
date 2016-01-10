package com.profesorfalken.jpowershell;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for jPowerShell
 * 
 * @author Javier Garcia Alonso
 */
public class PowerShellTest {

    public PowerShellTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of PowerShell command.
     */
    @Test
    public void testListDir() throws Exception {
        System.out.println("testListDir");
        if (OSDetector.isWindows()) {
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("dir");                
        
        System.out.println("List Directory:" + response.getCommandOutput());
        
        Assert.assertTrue(response.getCommandOutput().contains("LastWriteTime"));
        
        powerShell.close();
        }
    }

    /**
     * Test of PowerShell command.
     */
    @Test
    public void testListProcesses() throws Exception {
        System.out.println("testListProcesses");
        if (OSDetector.isWindows()) {
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("Get-Process");         

        System.out.println("List Processes:" + response.getCommandOutput());
        
        Assert.assertTrue(response.getCommandOutput().contains("powershell"));
        
        powerShell.close();
        }
    }

    /**
     * Test of PowerShell command.
     */
   @Test
    public void testCheckBIOSByWMI() throws Exception {
        System.out.println("testCheckBIOSByWMI");
        if (OSDetector.isWindows()) {
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");
        System.out.println("Check BIOS:" + response.getCommandOutput());   
        
        Assert.assertTrue(response.getCommandOutput().contains("SMBIOSBIOSVersion"));
        
        powerShell.close();
        }
    }
    
    /**
     * Test error case.
     */
    @Test
    public void testErrorCase() throws Exception {
        System.out.println("testErrorCase");
        if (OSDetector.isWindows()) {
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("sfdsfdsf");
        System.out.println("Error:" + response.getCommandOutput());          
        
        Assert.assertTrue(response.getCommandOutput().contains("sfdsfdsf"));
        
        powerShell.close();
        }
    }
    
    /**
     * Test of multiple PowerShell commands.
     */
    @Test
    public void testMultipleCalls() throws Exception {
        System.out.println("testMultiple");
        if (OSDetector.isWindows()) {
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("dir");        
        System.out.println("First call:" + response.getCommandOutput());
        Assert.assertTrue("Cannot find LastWriteTime", response.getCommandOutput().contains("LastWriteTime"));
        response = powerShell.executeCommand("Get-Process"); 
        System.out.println("Second call:" + response.getCommandOutput());
        Assert.assertTrue("Cannot find powershell", response.getCommandOutput().contains("powershell"));        
        response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");
        System.out.println("Third call:" + response.getCommandOutput());
        Assert.assertTrue("Cannot find SMBIOSBIOSVersion", response.getCommandOutput().contains("SMBIOSBIOSVersion"));
        
        powerShell.close();       
        }
    }
}
