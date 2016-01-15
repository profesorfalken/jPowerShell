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
package org.profesorfalken.jpowershell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processor used to send commands to PowerShell console.<p^>
 * It works as an independent thread and its results are collected using 
 * the Future interface.
 * 
 * @author Javier Garcia Alonso
 */
class PowerShellCommandProcessor implements Callable {

    private static final String CRLF = "\r\n";
    private static final int WAIT_PAUSE = 3;
    private static final int MAX_WAIT = 2000;

    private final BufferedReader reader;
    private final boolean checkTimeout;

    /**
     * Constructor that takes the output and the input of the PowerShell session
     * 
     * @param commandWriter the input to the PowerShell console
     * @param inputStream the stream needed to read the command output
     */
    public PowerShellCommandProcessor(PrintWriter commandWriter, InputStream inputStream, boolean checkTimeout) {
        this.reader = new BufferedReader(new InputStreamReader(
                inputStream));
        this.checkTimeout = checkTimeout;
    }

    /**
     * Calls the command and returns its output
     * 
     * @return
     * @throws IOException 
     */
    @Override
    public String call() throws IOException, InterruptedException {
        String line;
        StringBuilder powerShellOutput = new StringBuilder();        

        if (startReading()) {
            while (null != (line = this.reader.readLine())) {
                powerShellOutput.append(line).append(CRLF);
                try {                    
                    if (!continueReading()) {
                        break;
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(PowerShellCommandProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }


        return powerShellOutput.toString();
    }
    
    private boolean startReading() throws IOException, InterruptedException {
        int timeWaiting = 0;
        
        while (!this.reader.ready()) {            
            Thread.sleep(WAIT_PAUSE);
            timeWaiting += WAIT_PAUSE;
            if (checkTimeout && timeWaiting > MAX_WAIT) {
                return false;
            }
        }
        return true;
    }
    
    private boolean continueReading() throws IOException, InterruptedException {
        Thread.sleep(WAIT_PAUSE);
        return this.reader.ready();
    }
}
