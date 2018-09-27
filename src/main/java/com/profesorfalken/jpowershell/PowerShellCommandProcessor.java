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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processor used to send commands to PowerShell console.<p>
 * It works as an independent thread and its results are collected using the Future interface.
 *
 * @author Javier Garcia Alonso
 */
class PowerShellCommandProcessor implements Callable<String> {

    private static final String CRLF = "\r\n";

    private final BufferedReader reader;
    private final String name;

    private boolean closed = false;

    private boolean scriptMode = false;

    private final long maxWait;
    private final int waitPause;

    /**
     * Constructor that takes the output and the input of the PowerShell session
     *
     * @param name        the name of the CommandProcessor
     * @param inputStream the stream needed to read the command output
     * @param maxWait     long the max wait time in milliseconds
     * @param waitPause   long the wait pause in milliseconds
     * @param scriptMode  boolean indicates if the command executes a script
     */
    public PowerShellCommandProcessor(String name, InputStream inputStream, long maxWait, int waitPause, boolean scriptMode) {
        this.reader = new BufferedReader(new InputStreamReader(
                inputStream));
        this.name = name;
        this.maxWait = maxWait;
        this.waitPause = waitPause;
        this.scriptMode = scriptMode;
    }

    /**
     * Calls the command and returns its output
     *
     * @return String output of call
     * @throws IOException error when reading data
     */
    public String call() throws IOException, InterruptedException {
        StringBuilder powerShellOutput = new StringBuilder();

        try {
            if (startReading()) {
                readData(powerShellOutput);
            }
        } catch (IOException ioe) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Unexpected error reading PowerShell output", ioe);
            return ioe.getMessage();
        }

        return powerShellOutput.toString();
    }

    //Reads all data from output
    private void readData(StringBuilder powerShellOutput) throws IOException {
        String line;
        while (null != (line = this.reader.readLine())) {

            //In the case of script mode it finish when the last line is read
            if (this.scriptMode) {
                if (line.equals(PowerShell.END_SCRIPT_STRING)) {
                    break;
                }
            }

            powerShellOutput.append(line).append(CRLF);

            //When not in script mode, it exits when the command is finished
            if (!this.scriptMode) {
                try {
                    if (this.closed || !continueReading()) {
                        break;
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(PowerShellCommandProcessor.class.getName()).log(Level.SEVERE, "Error executing command and reading result", ex);
                }
            }
        }
    }

    //Checks when we can start reading the output. Timeout if it takes too long in order to avoid hangs
    private boolean startReading() throws IOException, InterruptedException {
        int timeWaiting = 0;

        while (!this.reader.ready()) {
            Thread.sleep(this.waitPause);
            timeWaiting += this.waitPause;
            if ((timeWaiting > this.maxWait) || this.closed) {
                return false;
            }
        }
        return true;
    }

    //Checks when we the reader can continue to read.
    private boolean continueReading() throws IOException, InterruptedException {
        Thread.sleep(this.waitPause);
        return this.reader.ready();
    }

    /**
     * Closes the command processor, canceling the current work if not finish
     */
    public void close() {
        this.closed = true;
    }
}
