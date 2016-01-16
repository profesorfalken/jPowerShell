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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows to open a session into PowerShell console and launch different
 * commands.<br>
 * This class cannot be directly instantiated. Instead, use the method
 * PowerShell.openSession and call the commands using the returned instance.
 * <p>
 * Once the session is finished, call close() method in order to free the
 * resources.
 *
 * @author Javier Garcia Alonso
 */
public class PowerShell {

    //Process to store PowerShell session
    private Process p;
    //Writer to send commands
    private PrintWriter commandWriter;
    
    //Threaded session variables
    private boolean closed = false;
    private ExecutorService threadpool;
    private static final int MAX_THREADS = 3; //standard output + error output + session close thread
    static final int WAIT_PAUSE = 3;
    static final int MAX_WAIT = 2000;

    //Private constructor.
    private PowerShell() {
    }

    //Initializes PowerShell console in which we will enter the commands
    private PowerShell initalize() throws PowerShellNotAvailableException {
        ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoExit", "-Command", "-");
        try {
            p = pb.start();
        } catch (IOException ex) {
            throw new PowerShellNotAvailableException(
                    "Cannot execute PowerShell.exe. Please make sure that it is installed in your system", ex);
        }

        commandWriter
                = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(p.getOutputStream())), true);

        return this;
    }

    /**
     * Creates a session in PowerShell console an returns an instance which
     * allows to execute commands in PowerShell context
     *
     * @return an instance of the class
     * @throws PowerShellNotAvailableException if PowerShell is not installed in
     * the system
     */
    public static PowerShell openSession() throws PowerShellNotAvailableException {
        PowerShell powerShell = new PowerShell();

        return powerShell.initalize();
    }

    /**
     * Launch a PowerShell command.<p>
     * This method launch a thread which will be executed in the already created
     * PowerShell console context
     *
     * @param command the command to call. Ex: dir
     * @return PowerShellResponse the information returned by powerShell
     */
    public PowerShellResponse executeCommand(String command) {
        Callable commandProcessor = new PowerShellCommandProcessor("standard", commandWriter, p.getInputStream(), true);
        Callable commandProcessorError = new PowerShellCommandProcessor("error", commandWriter, p.getErrorStream(), false);

        String commandOutput = "";
        boolean isError = false;

        this.threadpool = Executors.newFixedThreadPool(MAX_THREADS);
        Future<String> result = threadpool.submit(commandProcessor);
        Future<String> resultError = threadpool.submit(commandProcessorError);

        //Launch command
        commandWriter.println(command);

        try {
            while (!result.isDone() && !resultError.isDone()) {
                Thread.sleep(WAIT_PAUSE);
            }
            if (result.isDone()) {
                commandOutput = result.get();
            } else {
                isError = true;
                commandOutput = resultError.get();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Unexpected error when processing PowerShell command", ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Unexpected error when processing PowerShell command", ex);
        }

        return new PowerShellResponse(isError, commandOutput);
    }

    /**
     * Closes all the resources used to maintain the PowerShell context
     */
    public void close() {
        if (!this.closed) {
            try {
                Future<String> closeTask = threadpool.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        commandWriter.println("exit");                        
                        p.waitFor();                        
                        return "OK";
                    }
                });
                int closingTime = 0;
                while (!closeTask.isDone() && !closeTask.isDone()) {
                    if (closingTime > MAX_WAIT) {
                        Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Unexpected error when closing PowerShell: TIMEOUT!");
                        break;
                    }
                    Thread.sleep(WAIT_PAUSE);
                    closingTime += WAIT_PAUSE;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Unexpected error when pwhen closing PowerShell", ex);
            } finally {
                commandWriter.close();
                if (this.threadpool != null) {
                    this.threadpool.shutdownNow();

                    while (!this.threadpool.isTerminated()) {
                        //wait
                    }
                }
                this.closed = true;
            }
        }
    }

    /**
     * Try to close the PowerShell console if the object is collected by garbage
     * collector
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        if (!this.closed) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.WARNING, "Finalize executed because Powershell session was not properly closed!");
            close();
        }       
        super.finalize();
    }
}
