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

    /**
     * Test of openSession method, of class PowerShell.
     * @throws java.lang.Exception
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
     * @throws java.lang.Exception
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
     * @throws java.lang.Exception
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
    
    /**
     * Test of empty response
     * @throws java.lang.Exception
     */
    @Test
    public void testCheckEmptyResponse() throws Exception {
        System.out.println("testCheckEmptyResponse");
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("Get-WmiObject Win32_1394Controller");
        System.out.println("Empty response:" + response.getCommandOutput());   
        
        Assert.assertTrue("".equals(response.getCommandOutput()));
        
        powerShell.close();
    }
    
    /**
     * Test of long command
     * @throws java.lang.Exception
     */
    @Test
    public void testLongCommand() throws Exception {
        System.out.println("testLongCommand");
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("Get-WMIObject -List | Where{$_.name -match \"^Win32_\"} | Sort Name");
        System.out.println("Long list:" + response.getCommandOutput());   
        
        Assert.assertTrue(response.getCommandOutput().length() > 1000);
        
        powerShell.close();
    }
    
    /**
     * Test error case.
     * @throws java.lang.Exception
     */
    @Test
    public void testErrorCase() throws Exception {
        System.out.println("testErrorCase");
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("sfdsfdsf");
        System.out.println("Error:" + response.getCommandOutput());          
        
        Assert.assertTrue(response.getCommandOutput().contains("sfdsfdsf"));
        
        powerShell.close();
    }
    
    /**
     * Test of openSession method, of class PowerShell.
     * @throws java.lang.Exception
     */
    @Test
    public void testMultipleCalls() throws Exception {
        System.out.println("testMultiple");
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
