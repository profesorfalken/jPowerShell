/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.profesorfalken.jpowershell;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * jPowerShell tests for Linux and macOS, executed against PowerShell 7 ({@code pwsh}).
 * <p>
 * They complement {@link PowerShellTest}, which only runs on Windows because it relies
 * on Windows PowerShell 5.1 ({@code dir}, {@code Get-WmiObject}, {@code LastWriteTime}).
 * Only cases with platform independent behaviour are included here; the Windows specific
 * ones stay in the other class, flagged with assumptions so JUnit reports them as
 * skipped instead of silently passing.
 * <p>
 * The executable is resolved by {@link TestShell}: <i>JPOWERSHELL_EXECUTABLE</i>
 * environment variable, usual locations or {@code pwsh} from the PATH.
 *
 * @author Javier Garcia Alonso
 */
public class PowerShellLinuxTest {

    private static final long MAX_WAIT = 10000L;

    @BeforeClass
    public static void requirePowerShell() {
        Assume.assumeFalse("Windows PowerShell is covered by PowerShellTest", OSDetector.isWindows());
        Assume.assumeTrue("pwsh is not installed or cannot be started", TestShell.available());
        Assume.assumeTrue("PowerShell (" + TestShell.executable()
                        + ") runs but does not answer commands through stdin",
                TestShell.canRunCommands());
    }

    private static PowerShell openSession() {
        Map<String, String> config = new HashMap<>();
        config.put("maxWait", String.valueOf(MAX_WAIT));
        return TestShell.open().configuration(config);
    }

    /**
     * A simple command returns its output.
     */
    @Test
    public void testSimpleCommand() {
        try (PowerShell powerShell = openSession()) {
            PowerShellResponse response = powerShell.executeCommand("Get-Date -Format 'yyyy-MM-dd'");

            Assert.assertFalse("Should not time out", response.isTimeout());
            Assert.assertFalse("Should not be in error", response.isError());
            Assert.assertTrue("Unexpected format: " + response.getCommandOutput(),
                    response.getCommandOutput().matches("\\d{4}-\\d{2}-\\d{2}"));
        }
    }

    /**
     * Multi line output is returned in full.
     */
    @Test
    public void testMultilineOutput() {
        try (PowerShell powerShell = openSession()) {
            PowerShellResponse response = powerShell.executeCommand("1..5 | ForEach-Object { \"line $_\" }");

            Assert.assertFalse("Should not time out", response.isTimeout());
            Assert.assertEquals("Unexpected number of lines: " + response.getCommandOutput(),
                    5, response.getCommandOutput().split("\n").length);
        }
    }

    /**
     * Non ASCII characters survive the round trip.
     */
    @Test
    public void testUtf8Output() {
        try (PowerShell powerShell = openSession()) {
            PowerShellResponse response = powerShell.executeCommand("Write-Output 'accents: áéíóú ñ'");

            Assert.assertTrue("Corrupted output: " + response.getCommandOutput(),
                    response.getCommandOutput().contains("áéíóú ñ"));
        }
    }

    /**
     * Long output is neither truncated nor causes a timeout.
     */
    @Test
    public void testLongOutput() {
        try (PowerShell powerShell = openSession()) {
            PowerShellResponse response = powerShell.executeCommand("1..300 | ForEach-Object { \"l$_\" }");

            Assert.assertFalse("Should not time out", response.isTimeout());
            Assert.assertEquals("Missing lines", 300, response.getCommandOutput().split("\n").length);
        }
    }

    /**
     * Several commands can be chained in the same session without losing sync.
     */
    @Test
    public void testSequentialCommands() {
        try (PowerShell powerShell = openSession()) {
            for (int i = 0; i < 10; i++) {
                String expected = "value" + i;
                PowerShellResponse response = powerShell.executeCommand("Write-Output '" + expected + "'");

                Assert.assertEquals("Crossed response on iteration " + i,
                        expected, response.getCommandOutput());
            }
        }
    }

    /**
     * executeScript runs a .ps1 file and returns its output.
     */
    @Test
    public void testScriptFile() throws IOException {
        File script = File.createTempFile("jpowershell_test_", ".ps1");
        try {
            Files.write(script.toPath(), "Write-Output 'hello from script'".getBytes(StandardCharsets.UTF_8));

            try (PowerShell powerShell = openSession()) {
                PowerShellResponse response = powerShell.executeScript(script.getAbsolutePath());

                Assert.assertFalse("Should not time out", response.isTimeout());
                Assert.assertTrue("Unexpected output: " + response.getCommandOutput(),
                        response.getCommandOutput().contains("hello from script"));
            }
        } finally {
            Assert.assertTrue("Cannot delete the temporary script", script.delete());
        }
    }

    /**
     * isLastCommandInError reflects the outcome of the last executed command.
     */
    @Test
    public void testLastCommandInError() {
        try (PowerShell powerShell = openSession()) {
            powerShell.executeCommand("Get-Date");
            Assert.assertFalse("Get-Date should not report an error", powerShell.isLastCommandInError());

            powerShell.executeCommand("throw 'boom'");
            Assert.assertTrue("throw should report an error", powerShell.isLastCommandInError());
        }
    }

    /**
     * executeSingleCommand opens, executes and closes in a single call.
     * <p>
     * Note: it calls {@code PowerShell.openSession()} without a path, which on Linux
     * looks for a binary named {@code powershell}. As it does not exist, the exception
     * is logged and the method returns {@code null} instead of throwing it.
     */
    @Test
    @Ignore("executeSingleCommand cannot receive a custom executable: returns null on Linux")
    public void testExecuteSingleCommand() {
        PowerShellResponse response = PowerShell.executeSingleCommand("Write-Output 'single'");

        Assert.assertNotNull("Null response", response);
        Assert.assertTrue("Unexpected output: " + response.getCommandOutput(),
                response.getCommandOutput().contains("single"));
    }

    /**
     * Known bug (issue #84): opening a session takes ~5 s because
     * {@code initalize()} calls {@code p.waitFor(5, TimeUnit.SECONDS)}
     * and PowerShell, being alive, always waits the whole timeout.
     */
    @Test
    @Ignore("issue #84: openSession() blocks for 5 s in p.waitFor(5, TimeUnit.SECONDS)")
    public void testOpenSessionIsFast() {
        long start = System.currentTimeMillis();
        try (PowerShell powerShell = openSession()) {
            Assert.assertNotNull(powerShell);
        }
        long elapsed = System.currentTimeMillis() - start;

        Assert.assertTrue("openSession took " + elapsed + " ms", elapsed < 2000L);
    }

    /**jPowerShell feature/linux-tests-central-portal ✗ mvn -v | head -3
     /home/javier/Dev/github/.pwsh/pwsh -NoProfile -Command '$PSVersionTable.PSVersion.ToString(); $PWD'
     Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)
     Maven home: /usr/share/java/maven
     Java version: 27.0.1.0.2, vendor: Eclipse Adoptium, runtime: /home/javier/.local/share/mise/installs/java/27.0.1+0
     7.6.9

     Path
     ----
     /home/javier/Dev/github/jPowerShell
     * Known bug (issue #11): a command that writes nothing waits for the whole maxWait
     * and is reported as a timeout, even though it finished successfully.
     * <p>
     * Documented with @Ignore so the fix has a test waiting for it.
     */
    @Test
    @Ignore("issue #11: a command without output waits maxWait and returns timeout=true")
    public void testCommandWithoutOutput() {
        try (PowerShell powerShell = openSession()) {
            PowerShellResponse response = powerShell.executeCommand("$null = 1");

            Assert.assertFalse("A command without output should not time out", response.isTimeout());
            Assert.assertEquals("", response.getCommandOutput());
        }
    }
}
