/*
 * Copyright 2016-2018 Javier Garcia Alonso.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.profesorfalken.jpowershell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Processor used to send commands to PowerShell console.<p>
 * It works as an independent thread and its results are collected using the Future interface.
 * <p>
 * The end of a response is marked by a known string instead of guessed from the silence of
 * the stream: the command is followed by a line that prints a unique marker, and the reader
 * stops when it finds it. Guessing was the source of the empty responses, the truncated
 * outputs and the mixing between commands.
 *
 * @author Javier Garcia Alonso
 */
class PowerShellCommandProcessor implements Callable<String> {

    /**
     * Terminal control sequences (cursor keys, colours, window title...) that PowerShell
     * emits when its console host detects a terminal. They carry no useful information.
     */
    private static final Pattern ANSI_ESCAPES = Pattern.compile(
            "\u001B\\[[0-9;?]*[A-Za-z]"          // CSI: ESC [ ... letra
                    + "|\u001B\\][^\u0007]*\u0007"    // OSC: ESC ] ... BEL
                    + "|\u001B[()][A-Za-z0-9]");      // juego de caracteres

    private final BufferedReader reader;

    private final Process process;

    private final String marker;

    private final int waitPause;

    private final boolean expectStatus;

    private volatile boolean closed = false;

    /**
     * Result of the last command, as reported by PowerShell right before the end marker,
     * or null when the backend did not report it.
     */
    private Boolean lastCommandSucceeded = null;

    /**
     * Constructor that takes the output and the input of the PowerShell session
     *
     * @param process      the PowerShell process, used to detect the end of the stream
     * @param waitPause    the wait pause in milliseconds between reads
     * @param marker       the string that marks the end of the response
     * @param expectStatus whether the response ends with a True/False status line
     */
    public PowerShellCommandProcessor(Process process, int waitPause, String marker, boolean expectStatus) {
        this.process = process;
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.waitPause = waitPause;
        this.marker = marker;
        this.expectStatus = expectStatus;
    }

    /**
     * Returns the result of the last command when PowerShell reported it.
     *
     * @return TRUE if the command succeeded, FALSE if it failed, null if not reported
     */
    public Boolean getLastCommandSucceeded() {
        return this.lastCommandSucceeded;
    }

    /**
     * Calls the command and returns its output
     *
     * @return String output of call, without the end marker
     * @throws InterruptedException error when reading data
     */
    public String call() throws InterruptedException {
        StringBuilder rawOutput = new StringBuilder();

        try {
            while (!this.closed) {
                if (this.reader.ready()) {
                    int read = this.reader.read();
                    if (read < 0) {
                        // The process closed its output: there will be no marker
                        break;
                    }
                    rawOutput.append((char) read);
                    if (rawOutput.indexOf(this.marker) >= 0) {
                        break;
                    }
                    continue;
                }

                if (!this.process.isAlive()) {
                    // Without a marker, whatever was collected is all the output there is
                    break;
                }

                Thread.sleep(this.waitPause);
            }
        } catch (IOException ioe) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE,
                    "Unexpected error reading PowerShell output", ioe);
        }

        String output = rawOutput.toString();

        // Everything from the marker on belongs to the protocol, not to the command
        int markerIndex = output.indexOf(this.marker);
        if (markerIndex >= 0) {
            output = output.substring(0, markerIndex);
        }

        output = ANSI_ESCAPES.matcher(output).replaceAll("");
        output = output.replaceAll("[\\r\\n]+$", "");

        if (this.expectStatus) {
            // The status is the last line printed before the marker
            int lastBreak = output.lastIndexOf('\n');
            String lastLine = (lastBreak >= 0) ? output.substring(lastBreak + 1) : output;

            if ("True".equals(lastLine) || "False".equals(lastLine)) {
                this.lastCommandSucceeded = Boolean.valueOf(lastLine);
                return (lastBreak >= 0) ? output.substring(0, lastBreak) : "";
            }
        }

        return output;
    }

    /**
     * Closes the command processor, canceling the current work if not finish
     */
    public void close() {
        this.closed = true;
    }
}
