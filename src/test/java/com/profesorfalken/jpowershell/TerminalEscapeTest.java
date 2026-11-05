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
 * Regression test for terminal escape sequences in the PowerShell output.
 * <p>
 * When the PowerShell console host detects a terminal it writes ANSI control
 * sequences around the real output, and the last one arrives <b>without a trailing
 * newline</b>. The reader used to call {@code readLine()}, so after reading the
 * result it blocked forever waiting for that newline and the command timed out
 * with an empty response.
 * <p>
 * The fake shell used here emits exactly that byte sequence, so the test does not
 * need PowerShell installed. It is skipped on Windows because it relies on a shell
 * script.
 *
 * @author Javier Garcia Alonso
 */
public class TerminalEscapeTest {

    private static final String ESC = "\u001B";

    private File shell;

    @BeforeClass
    public static void requireUnix() {
        Assume.assumeFalse("Relies on a shell script", OSDetector.isWindows());
        Assume.assumeTrue("bash is not available", new File("/bin/bash").canExecute());
    }

    @Before
    public void createFakeShell() throws IOException {
        shell = File.createTempFile("jpowershell_fake_", ".sh");
        String script = "#!/bin/bash\n"
                + "while IFS= read -r line; do\n"
                + "  case \"$line\" in\n"
                + "    '$pid') printf '\\033[?1h\\033[?1l4242\\n\\033[?1h' ;;\n"
                + "    exit)   exit 0 ;;\n"
                + "    *)      printf '\\033[?1h\\033[?1lOUT[%s]\\n\\033[?1h' \"$line\" ;;\n"
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

    private PowerShell openSession() {
        Map<String, String> config = new HashMap<>();
        config.put("maxWait", "5000");
        return PowerShell.openSession(shell.getAbsolutePath()).configuration(config);
    }

    /**
     * The escape sequences never reach the caller.
     */
    @Test
    public void testEscapeSequencesAreStripped() {
        try (PowerShell powerShell = openSession()) {
            PowerShellResponse response = powerShell.executeCommand("dir");

            Assert.assertFalse("Should not time out", response.isTimeout());
            Assert.assertEquals("OUT[dir]", response.getCommandOutput());
            Assert.assertFalse("Output still contains escape sequences",
                    response.getCommandOutput().contains(ESC));
        }
    }

    /**
     * PowerShell writes its escape sequences first, pauses, and only then the real
     * output. Reading just the escapes used to end the command with an empty response.
     */
    @Test
    public void testOutputArrivingAfterEscapeSequences() throws IOException {
        File delayed = File.createTempFile("jpowershell_delayed_", ".sh");
        try {
            String script = "#!/bin/bash\n"
                    + "while IFS= read -r line; do\n"
                    + "  case \"$line\" in\n"
                    + "    '$pid') printf '\\033[?1h\\033[?1l'; sleep 0.15; printf '4242\\n\\033[?1h' ;;\n"
                    + "    exit)   exit 0 ;;\n"
                    + "    *)      printf '\\033[?1h\\033[?1l'; sleep 0.15; printf 'OUT[%s]\\n\\033[?1h' \"$line\" ;;\n"
                    + "  esac\n"
                    + "done\n";
            Files.write(delayed.toPath(), script.getBytes(StandardCharsets.UTF_8));
            Assert.assertTrue("Cannot make the delayed shell executable", delayed.setExecutable(true));

            Map<String, String> config = new HashMap<>();
            config.put("maxWait", "5000");
            try (PowerShell powerShell = PowerShell.openSession(delayed.getAbsolutePath()).configuration(config)) {
                PowerShellResponse response = powerShell.executeCommand("dir");

                Assert.assertFalse("Should not time out", response.isTimeout());
                Assert.assertEquals("OUT[dir]", response.getCommandOutput());
            }
        } finally {
            Assert.assertTrue("Cannot delete the delayed shell", delayed.delete());
        }
    }

    /**
     * A trailing escape sequence without newline used to leave the reader blocked,
     * so the following commands returned nothing. It also catches the race where a
     * command finishes before the caller checks the result: the output used to be
     * discarded when the future was already done.
     */
    @Test
    public void testConsecutiveCommandsDoNotHang() {
        try (PowerShell powerShell = openSession()) {
            for (int i = 0; i < 20; i++) {
                PowerShellResponse response = powerShell.executeCommand("eco" + i);

                Assert.assertFalse("Should not time out on iteration " + i, response.isTimeout());
                Assert.assertEquals("Wrong output on iteration " + i,
                        "OUT[eco" + i + "]", response.getCommandOutput());
            }
        }
    }
}
