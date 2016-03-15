/*
 * Copyright 2016 Javier Garcia Alonso.
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
 * Processor used to send commands to PowerShell console.<p^>
 * It works as an independent thread and its results are collected using the
 * Future interface.
 *
 * @author Javier Garcia Alonso
 */
class PowerShellCommandProcessor implements Callable {

    private static final String CRLF = "\r\n";

    private final BufferedReader reader;
    private final String name;

    private boolean closed = false;

    /**
     * Constructor that takes the output and the input of the PowerShell session
     *
     * @param commandWriter the input to the PowerShell console
     * @param inputStream the stream needed to read the command output
     */
    public PowerShellCommandProcessor(String name, InputStream inputStream) {
        this.reader = new BufferedReader(new InputStreamReader(
                inputStream));
        this.name = name;
    }

    /**
     * Calls the command and returns its output
     *
     * @return
     * @throws IOException
     */
    @Override
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
            powerShellOutput.append(line).append(CRLF);
            try {
                if (!continueReading() || this.closed) {
                    break;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(PowerShellCommandProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //Checks when we can start reading the output. Timeout if it takes too long in order to avoid hangs
    private boolean startReading() throws IOException, InterruptedException {
        int timeWaiting = 0;

        while (!this.reader.ready()) {
            Thread.sleep(PowerShell.WAIT_PAUSE);
            timeWaiting += PowerShell.WAIT_PAUSE;
            if ((timeWaiting > PowerShell.MAX_WAIT) || this.closed) {
                return false;
            }
        }
        return true;
    }

    //Checks when we the reader can continue to read.
    private boolean continueReading() throws IOException, InterruptedException {
        Thread.sleep(PowerShell.WAIT_PAUSE);
        return this.reader.ready();
    }

    /**
     * Closes the command processor, cancelling the current work if not finish
     */
    public void close() {
        this.closed = true;
    }

    /**
     * Return the given name of the command processor
     *
     * @return name of the command processor
     */
    public String getName() {
        return this.name;
    }
}
