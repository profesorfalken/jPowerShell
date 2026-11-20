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

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Behaviour of the end-of-response marker, exercised with a fake shell so the scenarios
 * that used to fail can be reproduced without PowerShell installed.
 *
 * @author Javier Garcia Alonso
 */
public class MarkerProtocolTest {

    private File shell;

    @BeforeClass
    public static void requireUnix() {
        Assume.assumeFalse("Relies on a shell script", OSDetector.isWindows());
        Assume.assumeTrue("bash is not available", new File("/bin/bash").canExecute());
    }

    @Before
    public void createFakeShell() throws IOException {
        shell = File.createTempFile("jpowershell_marker_", ".sh");
        String script = "#!/bin/bash\n"
                + "skip=0\n"
                + "failed=0\n"
                + "while IFS= read -r line; do\n"
                + "  case \"$line\" in\n"
                + "    '$pid') echo 4242 ;;\n"
                + "    *JPWR-STATUS:*)\n"
                + "        if [ \"$failed\" = \"1\" ]; then echo 'JPWR-STATUS:False'; else echo 'JPWR-STATUS:True'; fi\n"
                + "        failed=0 ;;\n"
                + "    'Write-Output \"JPWS-'*)\n"
                + "        marker=${line#Write-Output \\\"}\n"
                + "        marker=${marker%\\\"}\n"
                + "        if [ \"$skip\" = \"1\" ]; then skip=0; else echo \"$marker\"; fi ;;\n"
                + "    exit) exit 0 ;;\n"
                + "    incompleto) skip=1 ;;\n"
                + "    falla) echo boom; failed=1 ;;\n"
                + "    nada) : ;;\n"
                + "    lento) echo l1; sleep 0.15; echo l2; sleep 0.15; echo l3 ;;\n"
                // executeScript appends the parameters, so the line may end with a space
                + "    *ps1*) echo 'hola desde script'; exit 0 ;;\n"
                + "    *) echo \"OUT[$line]\" ;;\n"
                + "  esac\n"
                + "done\n";
        Files.write(shell.toPath(), script.getBytes(StandardCharsets.UTF_8));
        Assert.assertTrue("Cannot make the fake shell executable", shell.setExecutable(true));
    }

    @After
    public void deleteFakeShell() {
        if (shell != null) {
            Assert.assertTrue("Cannot delete the fake shell", shell.delete());
        }
    }

    private PowerShell openSession(long maxWait) {
        Map<String, String> config = new HashMap<>();
        config.put("maxWait", String.valueOf(maxWait));
        return PowerShell.openSession(shell.getAbsolutePath()).configuration(config);
    }

    /**
     * A command that writes nothing used to consume the whole maxWait and come back as a
     * timeout (issue #11).
     */
    @Test
    public void testCommandWithoutOutputFinishesImmediately() {
        try (PowerShell powerShell = openSession(5000)) {
            long start = System.currentTimeMillis();
            PowerShellResponse response = powerShell.executeCommand("nada");
            long elapsed = System.currentTimeMillis() - start;

            Assert.assertFalse("A command without output should not time out", response.isTimeout());
            Assert.assertEquals("", response.getCommandOutput());
            Assert.assertTrue("It should not wait for the full timeout: " + elapsed + " ms", elapsed < 2000);
        }
    }

    /**
     * Output written in several chunks used to be cut when the stream went quiet.
     */
    @Test
    public void testSlowOutputIsComplete() {
        try (PowerShell powerShell = openSession(5000)) {
            PowerShellResponse response = powerShell.executeCommand("lento");

            Assert.assertFalse("Should not time out", response.isTimeout());
            Assert.assertEquals("l1\nl2\nl3", response.getCommandOutput());
        }
    }

    /**
     * The status travels in the same response, so the marker line cannot make a failed
     * command look successful.
     */
    @Test
    public void testLastCommandInErrorUsesTheReportedStatus() {
        try (PowerShell powerShell = openSession(5000)) {
            powerShell.executeCommand("falla");
            Assert.assertTrue("The failed command should be reported", powerShell.isLastCommandInError());

            powerShell.executeCommand("eco");
            Assert.assertFalse("The next command succeeded", powerShell.isLastCommandInError());
        }
    }

    /**
     * A script that calls exit closes the process before the marker. Its output has to be
     * returned instead of waiting for the timeout (issue #87).
     */
    @Test
    public void testScriptThatExitsReturnsItsOutput() throws IOException {
        File script = File.createTempFile("jpowershell_exit_", ".ps1");
        try {
            Files.write(script.toPath(), "Write-Output 'irrelevant'\n".getBytes(StandardCharsets.UTF_8));

            try (PowerShell powerShell = openSession(5000)) {
                long start = System.currentTimeMillis();
                PowerShellResponse response = powerShell.executeScript(script.getAbsolutePath());
                long elapsed = System.currentTimeMillis() - start;

                Assert.assertFalse("A dead process should not be reported as a timeout", response.isTimeout());
                Assert.assertEquals("hola desde script", response.getCommandOutput());
                Assert.assertTrue("It should not wait for the full timeout: " + elapsed + " ms", elapsed < 2000);
            }
        } finally {
            Assert.assertTrue("Cannot delete the script", script.delete());
        }
    }

    /**
     * A command whose marker never arrives is a command that never finished. The session
     * cannot be trusted afterwards, so it fails fast instead of returning the output of
     * the next command (issues #54 and #91).
     */
    @Test
    public void testUnfinishedCommandBreaksTheSession() {
        PowerShell powerShell = openSession(1500);
        try {
            PowerShellResponse response = powerShell.executeCommand("incompleto");
            Assert.assertTrue("Should time out", response.isTimeout());

            try {
                powerShell.executeCommand("eco");
                Assert.fail("The session should not accept more commands after a timeout");
            } catch (IllegalStateException expected) {
                Assert.assertTrue(expected.getMessage().contains("no longer usable"));
            }
        } finally {
            powerShell.close();
        }
    }
}
