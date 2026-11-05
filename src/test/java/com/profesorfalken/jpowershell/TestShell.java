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

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * Test utilities to locate the PowerShell executable of the current platform.
 * <p>
 * On Windows the tests use {@code powershell.exe} (Windows PowerShell 5.1), which is
 * the executable the library expects by default. On Linux and macOS the PowerShell 7
 * binary is called {@code pwsh}, so its path can be set through the
 * <i>JPOWERSHELL_EXECUTABLE</i> environment variable or left to the usual locations.
 *
 * @author Javier Garcia Alonso
 */
final class TestShell {

    static final String ENV_VAR = "JPOWERSHELL_EXECUTABLE";

    private static final String[] UNIX_CANDIDATES = {
            "/usr/bin/pwsh",
            "/usr/local/bin/pwsh",
            "/snap/bin/pwsh",
            "/opt/microsoft/powershell/7/pwsh"
    };

    private TestShell() {
    }

    /**
     * Returns the PowerShell executable suitable for the current platform.
     *
     * @return path or name of the executable
     */
    static String executable() {
        String fromEnv = System.getenv(ENV_VAR);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        if (OSDetector.isWindows()) {
            return "powershell.exe";
        }
        for (String candidate : UNIX_CANDIDATES) {
            if (new File(candidate).canExecute()) {
                return candidate;
            }
        }
        return "pwsh";
    }

    /**
     * Opens a session using the detected executable.
     *
     * @return PowerShell session
     */
    static PowerShell open() {
        return PowerShell.openSession(executable());
    }

    /**
     * Checks that the executable exists and starts, so it can be used in an assumption.
     *
     * @return true if PowerShell can be executed
     */
    static boolean available() {
        try {
            Process process = new ProcessBuilder(executable(), "-NoProfile", "-Command", "exit 0")
                    .redirectErrorStream(true)
                    .start();
            if (!process.waitFor(20, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Checks that PowerShell actually answers a command sent through stdin, which is
     * how the library drives it. Some environments start the executable but never
     * return its output, and that has nothing to do with jPowerShell: in that case
     * the tests are skipped with a explicit message instead of failing seven times
     * with an empty response.
     *
     * @return true if a command written to stdin produces output
     */
    static boolean canRunCommands() {
        Process process = null;
        try {
            process = new ProcessBuilder(executable(), "-nologo", "-noexit", "-noninteractive",
                    "-noprofile", "-Command", "-")
                    .redirectErrorStream(true)
                    .start();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            writer.println("Write-Output jpowershell-probe");

            StringBuilder output = new StringBuilder();
            long deadline = System.currentTimeMillis() + 8000L;
            while (System.currentTimeMillis() < deadline) {
                if (reader.ready()) {
                    int read = reader.read();
                    if (read >= 0) {
                        output.append((char) read);
                        if (output.indexOf("jpowershell-probe") >= 0) {
                            return true;
                        }
                    }
                } else {
                    Thread.sleep(20L);
                }
            }
            return false;
        } catch (Exception ex) {
            return false;
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }
}
