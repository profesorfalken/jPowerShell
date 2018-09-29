package com.profesorfalken.jpowershell;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tests for jPowerShell
 *
 * @author Javier Garcia Alonso
 */
public class PowerShellTest {

    private static final String CRLF = "\r\n";

    /**
     * Test of openSession method, of class PowerShell.
     *
     * @throws java.lang.Exception
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
     * Test of openSession method, of class PowerShell.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSimpleListDir() throws Exception {
        System.out.println("start testListDir");
        if (OSDetector.isWindows()) {
            PowerShellResponse response = PowerShell.executeSingleCommand("dir");

            System.out.println("List Directory:" + response.getCommandOutput());

            Assert.assertTrue(response.getCommandOutput().contains("LastWriteTime"));
            System.out.println("end testListDir");
        }
    }

    /**
     * Test of openSession method, of class PowerShell.
     *
     * @throws java.lang.Exception
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
     * Test of openSession method, of class PowerShell.
     *
     * @throws java.lang.Exception
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
     * Test of empty response
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCheckEmptyResponse() throws Exception {
        System.out.println("testCheckEmptyResponse");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = powerShell.executeCommand("Get-WmiObject Win32_1394Controller");
            System.out.println("Empty response:" + response.getCommandOutput());

            Assert.assertTrue("".equals(response.getCommandOutput()));

            powerShell.close();
        }
    }

    /**
     * Test of long command
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testLongCommand() throws Exception {
        System.out.println("testLongCommand");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = powerShell
                    .executeCommand("Get-WMIObject -List | Where{$_.name -match \"^Win32_\"} | Sort Name");
            System.out.println("Long list:" + response.getCommandOutput());

            Assert.assertTrue(response.getCommandOutput().length() > 1000);

            powerShell.close();
        }
    }

    /**
     * Test error case.
     *
     * @throws java.lang.Exception
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
     * Test of openSession method, of class PowerShell.
     *
     * @throws java.lang.Exception
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
            Assert.assertTrue("Cannot find SMBIOSBIOSVersion",
                    response.getCommandOutput().contains("SMBIOSBIOSVersion"));

            powerShell.close();
        }
    }

    /**
     * Test github example.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExample() throws Exception {
        System.out.println("testExample");
        PowerShell powerShell = null;
        try {
            // Creates PowerShell session (we can execute several commands in
            // the same session)
            powerShell = PowerShell.openSession();

            // Execute a command in PowerShell session
            PowerShellResponse response = powerShell.executeCommand("Get-Process");

            // Print results
            System.out.println("List Processes:" + response.getCommandOutput());

            // Execute another command in the same PowerShell session
            response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");

            // Print results
            System.out.println("BIOS information:" + response.getCommandOutput());
        } catch (PowerShellNotAvailableException ex) {
            // Handle error when PowerShell is not available in the system
            // Maybe try in another way?
            // Assert.assertNull("PowerShellNotAvailableException", ex); //Commented to let
            // Travis pass the tests
        } finally {
            // Always close PowerShell session to free resources.
            if (powerShell != null) {
                powerShell.close();
            }
        }
    }

    /**
     * Test other executable from default one
     *
     * @throws java.lang.Exception
     */
    @Test(expected = PowerShellNotAvailableException.class)
    public void testOtherExecutablePath() throws Exception {
        PowerShell powerShell = null;
        if (OSDetector.isWindows()) {
            try {
                powerShell = PowerShell.openSession("powerShell2.exe");

                PowerShellResponse response = powerShell.executeCommand("Get-Process");
            } finally {
                // Always close PowerShell session to free resources.
                if (powerShell != null) {
                    powerShell.close();
                }
            }
        }
    }

    /**
     * Test complex loop example.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testComplexLoop() throws Exception {
        System.out.println("testExample");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = null;
            try {
                powerShell = PowerShell.openSession();

                for (int i = 0; i < 10; i++) {
                    PowerShellResponse response = powerShell.executeCommand("Get-Process");

                    System.out.println("List Processes:" + response.getCommandOutput());

                    response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");

                    System.out.println("BIOS information:" + response.getCommandOutput());

                    response = powerShell.executeCommand("sfdsfdsf");

                    System.out.println("Error:" + response.getCommandOutput());

                    response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");

                    System.out.println("BIOS information:" + response.getCommandOutput());
                }
            } finally {
                if (powerShell != null) {
                    powerShell.close();
                }
            }

            try {
                // Creates PowerShell session (we can execute several commands in
                // the same session)
                powerShell = PowerShell.openSession();

                // Execute a command in PowerShell session
                PowerShellResponse response = powerShell.executeCommand("Get-Process");

                // Print results
                System.out.println("List Processes:" + response.getCommandOutput());

                // Execute another command in the same PowerShell session
                response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");

                // Print results
                System.out.println("BIOS information:" + response.getCommandOutput());
            } catch (PowerShellNotAvailableException ex) {
                // Handle error when PowerShell is not available in the system
                // Maybe try in another way?
            } finally {
                // Always close PowerShell session to free resources.
                if (powerShell != null) {
                    powerShell.close();
                }
            }
        }
    }

    /**
     * Test loop.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testLoop() throws Exception {
        System.out.println("testLoop");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = null;
            try {
                powerShell = PowerShell.openSession();
                for (int i = 0; i < 10; i++) {
                    System.out.print("Cycle: " + i);
                    // Thread.sleep(3000);

                    String output = powerShell.executeCommand("date").getCommandOutput().trim();

                    System.out.println("\t" + output);
                }
            } catch (PowerShellNotAvailableException ex) {
                // Handle error when PowerShell is not available in the system
                // Maybe try in another way?
            } finally {
                // Always close PowerShell session to free resources.
                if (powerShell != null) {
                    powerShell.close();
                }
            }
        }
    }

    /**
     * Test long loop.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testLongLoop() throws Exception {
        System.out.println("testLongLoop");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = null;
            try {
                powerShell = PowerShell.openSession();
                for (int i = 0; i < 100; i++) {
                    System.out.print("Cycle: " + i);

                    // Thread.sleep(100);
                    PowerShellResponse response = powerShell.executeCommand("date"); // Line
                    // 17
                    // (see
                    // exception
                    // below)

                    if (response.isError()) {
                        System.out.println("error"); // never called
                    }

                    String output = "<" + response.getCommandOutput().trim() + ">";

                    System.out.println("\t" + output);
                }
            } catch (PowerShellNotAvailableException ex) {
                // Handle error when PowerShell is not available in the system
                // Maybe try in another way?
            } finally {
                // Always close PowerShell session to free resources.
                if (powerShell != null) {
                    powerShell.close();
                }
            }
        }
    }

    /**
     * Test of timeout
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testTimeout() throws Exception {
        System.out.println("testTimeout");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = null;
            try {
                response = powerShell.executeCommand("Start-Sleep -s 15");
            } finally {
                powerShell.close();
            }

            Assert.assertNotNull(response);
            Assert.assertTrue("PS error should finish in timeout", response.isTimeout());

        }
    }

    // @Test
    public void testRemote() throws Exception {
        System.out.println("testRemote");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            Map<String, String> config = new HashMap<>();
            config.put("remoteMode", "true");
            PowerShellResponse response = powerShell.configuration(config).executeCommand(
                    "Invoke-command -ComputerName leon {(Get-Service W32Time).WaitForStatus('Running','02:00:00')}");

            System.out.println("Output:" + response.getCommandOutput());

            Assert.assertFalse(response.isError());

            powerShell.close();
        }
    }

    @Test
    public void testScript() throws Exception {
        System.out.println("testScript");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            Map<String, String> config = new HashMap<>();
            PowerShellResponse response = null;

            StringBuilder scriptContent = new StringBuilder();
            scriptContent.append("Write-Host \"First message\"").append(CRLF);

            try {
                response = powerShell.configuration(config).executeScript(generateScript(scriptContent.toString()));
            } finally {
                powerShell.close();
            }

            Assert.assertNotNull("Response null!", response);
            if (!response.getCommandOutput().contains("UnauthorizedAccess")) {
                Assert.assertFalse("Is in error!", response.isError());
                Assert.assertFalse("Is timeout!", response.isTimeout());
            }
            System.out.println(response.getCommandOutput());
        }
    }

    @Test
    public void testScriptByBufferedReader() throws Exception {
        System.out.println("testScriptByBufferedReader");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            Map<String, String> config = new HashMap<>();
            PowerShellResponse response = null;

            StringBuilder scriptContent = new StringBuilder();
            scriptContent.append("Write-Host \"First message\"").append(CRLF);

            BufferedReader srcReader = null;
            try {
                srcReader = new BufferedReader(new FileReader(generateScript(scriptContent.toString())));
            } catch (FileNotFoundException fnfex) {
                Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE,
                        "Unexpected error when processing PowerShell script: file not found", fnfex);
            }

            Assert.assertNotNull("Cannot create reader from temp file", srcReader);

            try {
                response = powerShell.configuration(config).executeScript(srcReader);
            } finally {
                powerShell.close();
            }

            Assert.assertNotNull("Response null!", response);
            if (!response.getCommandOutput().contains("UnauthorizedAccess")) {
                Assert.assertFalse("Is in error!", response.isError());
                Assert.assertFalse("Is timeout!", response.isTimeout());
            }
            System.out.println(response.getCommandOutput());
        }
    }

    @Test
    public void testLongScript() throws Exception {
        System.out.println("testLongScript");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            Map<String, String> config = new HashMap<>();
            config.put("maxWait", "80000");
            PowerShellResponse response = null;

            StringBuilder scriptContent = new StringBuilder();
            scriptContent.append("Write-Host \"First message\"").append(CRLF);
            scriptContent.append("$output = \"c:\\10meg.test\"").append(CRLF);
            scriptContent.append(
                    "(New-Object System.Net.WebClient).DownloadFile(\"http://ipv4.download.thinkbroadband.com/10MB.zip\",$output)")
                    .append(CRLF);
            scriptContent.append("Write-Host \"Second message\"").append(CRLF);
            scriptContent.append(
                    "(New-Object System.Net.WebClient).DownloadFile(\"http://ipv4.download.thinkbroadband.com/10MB.zip\",$output)")
                    .append(CRLF);
            scriptContent.append("Write-Host \"Finish!\"").append(CRLF);

            try {
                response = powerShell.configuration(config).executeScript(generateScript(scriptContent.toString()));
            } finally {
                powerShell.close();
            }

            Assert.assertNotNull("Response null!", response);
            if (!response.getCommandOutput().contains("UnauthorizedAccess")) {
                Assert.assertFalse("Is in error!", response.isError());
                Assert.assertFalse("Is timeout!", response.isTimeout());
            }
            System.out.println(response.getCommandOutput());
        }
    }

    @Test (expected = java.util.concurrent.RejectedExecutionException.class)
    public void testExecuteCommandAfterClose() throws Exception {
        System.out.println("start testExecuteCommandAfterClose");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");
            System.out.println("Check BIOS:" + response.getCommandOutput());

            Assert.assertTrue(response.getCommandOutput().contains("SMBIOSBIOSVersion"));

            powerShell.close();

            //Should throw a RejectedExecutionException
            response = powerShell.executeCommand("Get-Process");
        }

    }

    /**
     * Test of configuration
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testConfiguration() throws Exception {
        System.out.println("testConfiguration");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            Map<String, String> config = new HashMap<>();
            config.put("maxWait", "1000");
            PowerShellResponse response = null;
            try {
                response = powerShell.configuration(config).executeCommand("Start-Sleep -s 10; Get-Process");
            } finally {
                powerShell.close();
            }

            Assert.assertNotNull(response);
            Assert.assertTrue("PS error should finish in timeout", response.isTimeout());

        }
    }

    private static String generateScript(String scriptContent) throws Exception {
        File tmpFile = null;
        FileWriter writer = null;

        try {
            tmpFile = File.createTempFile("psscript_" + new Date().getTime(), ".ps1");
            writer = new FileWriter(tmpFile);
            writer.write(scriptContent);
            writer.flush();
            writer.close();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return tmpFile.getAbsolutePath();
    }
}
