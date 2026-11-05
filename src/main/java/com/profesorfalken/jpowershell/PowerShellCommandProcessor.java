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
 *
 * @author Javier Garcia Alonso
 */
class PowerShellCommandProcessor implements Callable<String> {

    private static final String CRLF = "\r\n";

    /**
     * Terminal control sequences (cursor keys, colours, window title...) that PowerShell
     * emits when its console host detects a terminal. They carry no useful information
     * and, worse, the last one usually arrives without a trailing newline.
     */
    private static final Pattern ANSI_ESCAPES = Pattern.compile(
            "\u001B\\[[0-9;?]*[A-Za-z]"          // CSI: ESC [ ... letra
                    + "|\u001B\\][^\u0007]*\u0007"    // OSC: ESC ] ... BEL
                    + "|\u001B[()][A-Za-z0-9]");      // juego de caracteres

    private final BufferedReader reader;

    private final Process process;

    private boolean closed = false;

    private final boolean scriptMode;

    private final int waitPause;

    /**
     * Constructor that takes the output and the input of the PowerShell session
     *
     * @param name       the name of the CommandProcessor
     * @param process    the PowerShell process, used to detect the end of the stream
     * @param waitPause  long the wait pause in milliseconds
     * @param scriptMode boolean indicates if the command executes a script
     */
    public PowerShellCommandProcessor(String name, Process process, int waitPause, boolean scriptMode) {
        this.process = process;
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.waitPause = waitPause;
        this.scriptMode = scriptMode;
    }

    /**
     * Calls the command and returns its output
     *
     * @return String output of call
     * @throws InterruptedException error when reading data
     */
    public String call() throws InterruptedException {
        StringBuilder powerShellOutput = new StringBuilder();

        try {
            if (startReading()) {
                readData(powerShellOutput);
            }
        } catch (IOException ioe) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Unexpected error reading PowerShell output", ioe);
            return ioe.getMessage();
        }

        String output = powerShellOutput.toString();

        // In script mode the end marker is only a protocol detail, never part of the result
        if (this.scriptMode) {
            int markerIndex = output.indexOf(PowerShell.END_SCRIPT_STRING);
            if (markerIndex >= 0) {
                output = output.substring(0, markerIndex);
            }
        }

        // Terminal control sequences are noise, not output
        output = ANSI_ESCAPES.matcher(output).replaceAll("");

        // Remove last CRLF from result
        return output.replaceAll("\\s+$", "");
    }

    /**
     * Waits until the first byte of the response is available.
     * <p>
     * It never blocks on a read: it polls {@link BufferedReader#ready()} and gives up
     * when the process is no longer alive.
     *
     * @return true if there is data to read
     * @throws InterruptedException thread interrupted while waiting
     */
    private boolean startReading() throws InterruptedException {
        while (!this.closed) {
            try {
                if (this.reader.ready()) {
                    return true;
                }
                if (!this.process.isAlive()) {
                    return false;
                }
            } catch (IOException ioe) {
                return false;
            }
            Thread.sleep(this.waitPause);
        }
        return false;
    }

    /**
     * Reads the output of the command character by character.
     * <p>
     * Reading is only attempted when {@link BufferedReader#ready()} says that data is
     * available, so a read never blocks. This matters: PowerShell emits terminal escape
     * sequences without a trailing newline, and a blocking {@code readLine()} waiting
     * for that newline kept the processor stuck until the command timed out.
     *
     * @param powerShellOutput buffer where the output is collected
     * @throws IOException          error reading data
     * @throws InterruptedException thread interrupted while waiting for data
     */
    private void readData(StringBuilder powerShellOutput) throws IOException, InterruptedException {
        while (!this.closed) {

            if (this.reader.ready()) {
                int read = this.reader.read();
                if (read < 0) {
                    return;
                }
                powerShellOutput.append((char) read);

                // In the case of script mode it finishes when the end marker is read
                if (this.scriptMode
                        && powerShellOutput.indexOf(PowerShell.END_SCRIPT_STRING) >= 0) {
                    return;
                }
                continue;
            }

            // Nothing buffered: if the process is gone there will be no more data
            if (!this.process.isAlive()) {
                return;
            }

            if (this.scriptMode) {
                // A script can stay silent for a while, so wait for the marker
                Thread.sleep(this.waitPause);
            } else if (!canContinueReading()) {
                return;
            }
        }
    }

    //Checks when we the reader can continue to read.
    private boolean canContinueReading() throws IOException, InterruptedException {
        //If the reader is not ready, gives it some milliseconds
        //It is important to do that, because the ready method guarantees that the readline will not be blocking
        if (!this.reader.ready()) {
            Thread.sleep(this.waitPause);
        }

        //If not ready yet, wait a moment to make sure it is finished
        if (!this.reader.ready()) {
            Thread.sleep(50);
        }

        return this.reader.ready();
    }

    /**
     * Closes the command processor, canceling the current work if not finish
     */
    public void close() {
        this.closed = true;
    }
}
