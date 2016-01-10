package org.profesorfalken.jpowershell;

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
     * Test of openSession method, of class PowerShell.
     */
    @Test
    public void testListDir() throws Exception {
        System.out.println("testListDir");
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("dir");                
        
        System.out.println("List Directory:" + response.getCommandOutput());
        
        Assert.assertTrue(response.getCommandOutput().contains("LastWriteTime"));
        
        powerShell.close();
    }

    /**
     * Test of openSession method, of class PowerShell.
     */
    @Test
    public void testListProcesses() throws Exception {
        System.out.println("testListProcesses");
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("Get-Process");         

        System.out.println("List Processes:" + response.getCommandOutput());
        
        Assert.assertTrue(response.getCommandOutput().contains("powershell"));
        
        powerShell.close();
    }

    /**
     * Test of openSession method, of class PowerShell.
     */
    @Test
    public void testCheckBIOSByWMI() throws Exception {
        System.out.println("testCheckBIOSByWMI");
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");
        System.out.println("Check BIOS:" + response.getCommandOutput());   
        
        Assert.assertTrue(response.getCommandOutput().contains("SMBIOSBIOSVersion"));
        
        powerShell.close();
    }
    
    @Test
    public void testMultipleCalls() throws Exception {
        System.out.println("testMultiple");
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("dir");        
        System.out.println("First call:" + response.getCommandOutput());
        Assert.assertTrue(response.getCommandOutput().contains("LastWriteTime"));
        response = powerShell.executeCommand("Get-Process"); 
        System.out.println("Second call:" + response.getCommandOutput());
        Assert.assertTrue(response.getCommandOutput().contains("powershell"));
        response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");
        System.out.println("Third call:" + response.getCommandOutput());
        Assert.assertTrue(response.getCommandOutput().contains("SMBIOSBIOSVersion"));
        
        powerShell.close();       
    }
}
