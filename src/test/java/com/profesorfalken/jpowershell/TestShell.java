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
}
