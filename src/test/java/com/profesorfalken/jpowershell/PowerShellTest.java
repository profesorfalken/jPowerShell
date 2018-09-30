package com.profesorfalken.jpowershell;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * Test of openSession method, of class PowerShell.
     *
     */
    @Test
    public void testListDir() {
        System.out.println("testListDir");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = powerShell.executeCommand("dir");

            System.out.println("List Directory:" + response.getCommandOutput());

            Assert.assertFalse(powerShell.isLastCommandInError());
            Assert.assertTrue(response.getCommandOutput().contains("LastWriteTime"));

            powerShell.close();
        }
    }

    /**
     * Test of openSession method, of class PowerShell.
     *
     */
    @Test
    public void testSimpleListDir() {
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
     */
    @Test
    public void testListProcesses() {
        System.out.println("testListProcesses");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = powerShell.executeCommand("Get-Process");

            System.out.println("List Processes:" + response.getCommandOutput());

            Assert.assertFalse(powerShell.isLastCommandInError());
            Assert.assertTrue(response.getCommandOutput().contains("powershell"));

            powerShell.close();
        }
    }

    /**
     * Test of openSession method, of class PowerShell.
     *
     */
    @Test
    public void testCheckBIOSByWMI() {
        System.out.println("testCheckBIOSByWMI");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");
            System.out.println("Check BIOS:" + response.getCommandOutput());

            Assert.assertFalse(powerShell.isLastCommandInError());
            Assert.assertTrue(response.getCommandOutput().contains("SMBIOSBIOSVersion"));

            powerShell.close();
        }
    }

    /**
     * Test of empty response
     *
     */
    @Test
    public void testCheckEmptyResponse() {
        System.out.println("testCheckEmptyResponse");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = powerShell.executeCommand("Get-WmiObject Win32_1394Controller");
            System.out.println("Empty response:" + response.getCommandOutput());

            Assert.assertFalse(powerShell.isLastCommandInError());
            Assert.assertTrue("".equals(response.getCommandOutput()));

            powerShell.close();
        }
    }

    /**
     * Test of long command
     *
     */
    @Test
    public void testLongCommand() {
        System.out.println("testLongCommand");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = powerShell
                    .executeCommand("Get-WMIObject -List | Where{$_.name -match \"^Win32_\"} | Sort Name");
            System.out.println("Long list:" + response.getCommandOutput());

            Assert.assertFalse(powerShell.isLastCommandInError());
            Assert.assertTrue(response.getCommandOutput().length() > 1000);

            powerShell.close();
        }
    }

    /**
     * Test error case.
     *
     */
    @Test
    public void testErrorCase() {
        System.out.println("testErrorCase");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = powerShell.executeCommand("sfdsfdsf");
            System.out.println("Error:" + response.getCommandOutput());

            Assert.assertTrue(response.getCommandOutput().contains("sfdsfdsf"));
            Assert.assertTrue(powerShell.isLastCommandInError());

            powerShell.close();
        }
    }

    /**
     * Test of openSession method, of class PowerShell.
     *
     */
    @Test
    public void testMultipleCalls() {
        System.out.println("testMultiple");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = powerShell.executeCommand("dir");
            System.out.println("First call:" + response.getCommandOutput());
            Assert.assertFalse(powerShell.isLastCommandInError());
            Assert.assertTrue("Cannot find LastWriteTime", response.getCommandOutput().contains("LastWriteTime"));
            response = powerShell.executeCommand("Get-Process");
            System.out.println("Second call:" + response.getCommandOutput());
            Assert.assertFalse(powerShell.isLastCommandInError());
            Assert.assertTrue("Cannot find powershell", response.getCommandOutput().contains("powershell"));
            response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");
            System.out.println("Third call:" + response.getCommandOutput());
            Assert.assertFalse(powerShell.isLastCommandInError());
            Assert.assertTrue("Cannot find SMBIOSBIOSVersion",
                    response.getCommandOutput().contains("SMBIOSBIOSVersion"));

            powerShell.close();
        }
    }

    /**
     * Test github example.
     *
     */
    @Test
    public void testExample() {
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
     * Test github example.
     *
     */
    @Test (expected = Test.None.class /* no exception expected */)
    public void testFunctionalExample() {
        System.out.println("testFunctionalExample");
        if (OSDetector.isWindows()) {
            PowerShell.openSession()
                    .executeCommandAndChain("Get-Process", (res -> System.out.println("List Processes:" + res.getCommandOutput())))
                    .executeCommandAndChain("Get-WmiObject Win32_BIOS", (res -> System.out.println("BIOS information:" + res.getCommandOutput())))
                    .close();
        }
    }

    /**
     * Test other executable from default one
     *
     */
    public void testOtherExecutablePath() {
        PowerShell powerShell = null;
        if (OSDetector.isWindows()) {
            try {
                powerShell = PowerShell.openSession("powerShell2.exe");

                //Should throw a PowerShellNotAvailableException
                exception.expect(PowerShellNotAvailableException.class);
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
     */
    @Test
    public void testComplexLoop() {
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
     */
    @Test
    public void testLoop() {
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
     */
    @Test
    public void testLongLoop() {
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
                    if (powerShell.isLastCommandInError()) {
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
     */
    @Test
    public void testTimeout() {
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

    //Activate only when having the right for remote execution
    //@Test
    public void testRemote() {
        System.out.println("testRemote");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = powerShell.executeCommand(
                    "Invoke-command -ComputerName localhost {Write-Host \"Test from Remote\"}");

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

    @Test
    public void testExecuteCommandAfterClose() {
        System.out.println("start testExecuteCommandAfterClose");
        if (OSDetector.isWindows()) {
            PowerShell powerShell = PowerShell.openSession();
            PowerShellResponse response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");
            System.out.println("Check BIOS:" + response.getCommandOutput());

            Assert.assertTrue(response.getCommandOutput().contains("SMBIOSBIOSVersion"));

            powerShell.close();

            //Should throw a RejectedExecutionException
            exception.expect(IllegalStateException.class);
            powerShell.executeCommand("Get-Process");
        }
    }

    /**
     * Test of configuration
     *
     */
    @Test
    public void testConfiguration() {
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
