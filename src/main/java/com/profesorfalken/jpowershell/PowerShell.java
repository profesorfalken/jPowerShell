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

import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This API allows to open a session into PowerShell console and launch different commands.<br>
 * This class cannot be instantiated directly. Please use instead the method
 * PowerShell.openSession() and call the commands using the returned instance.
 * <p>
 * Once the session is finished it should be closed in order to free resources.
 * For doing that, you can either call manually close() or implement a try with resources as
 * it implements {@link AutoCloseable}.
 *
 * @author Javier Garcia Alonso
 */
public class PowerShell implements AutoCloseable {

    // Process to store PowerShell session
    private Process p;
    // Writer to send commands
    private PrintWriter commandWriter;

    // Threaded session variables
    private boolean closed = false;
    private ExecutorService threadpool;

    //Default PowerShell executable path
    private static final String DEFAULT_WIN_EXECUTABLE = "powershell.exe";
    private static final String DEFAULT_LINUX_EXECUTABLE = "powershell";

    // Config values
    private int maxThreads = 3;
    private int waitPause = 10;
    private long maxWait = 10000;
    private boolean remoteMode = false;

    // Variables used for script mode
    private boolean scriptMode = false;
    public static final String END_SCRIPT_STRING = "--END-JPOWERSHELL-SCRIPT--";

    // Private constructor. Instance using openSession method
    private PowerShell() {
    }

    /**
     * Allows to override jPowerShell configuration using a map of key/value <br>
     * Default values are taken from file <i>jpowershell.properties</i>, which can
     * be replaced just setting it on project classpath
     * <p>
     * The values that can be overridden are:
     * <ul>
     * <li>maxThreads: the maximum number of thread to use in pool. 3 is an optimal
     * and default value</li>
     * <li>waitPause: the pause in ms between each loop pooling for a response.
     * Default value is 10</li>
     * <li>maxWait: the maximum wait in ms for the command to execute. Default value
     * is 10000</li>
     * </ul>
     *
     * @param config map with the configuration in key/value format
     * @return instance to chain
     */
    public PowerShell configuration(Map<String, String> config) {
        try {
            this.maxThreads = Integer
                    .valueOf((config != null && config.get("maxThreads") != null) ? config.get("maxThreads")
                            : PowerShellConfig.getConfig().getProperty("maxThreads"));
            this.waitPause = Integer
                    .valueOf((config != null && config.get("waitPause") != null) ? config.get("waitPause")
                            : PowerShellConfig.getConfig().getProperty("waitPause"));
            this.maxWait = Long.valueOf((config != null && config.get("maxWait") != null) ? config.get("maxWait")
                    : PowerShellConfig.getConfig().getProperty("maxWait"));
            this.remoteMode = Boolean
                    .valueOf((config != null && config.get("remoteMode") != null) ? config.get("remoteMode")
                            : PowerShellConfig.getConfig().getProperty("remoteMode"));
        } catch (NumberFormatException nfe) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE,
                    "Could not read configuration. Using default values.", nfe);
        }
        return this;
    }

    /**
     * Creates a session in PowerShell console an returns an instance which allows
     * to execute commands in PowerShell context.<br>
     * It uses the default PowerShell installation in the system.
     *
     * @return an instance of the class
     * @throws PowerShellNotAvailableException if PowerShell is not installed in the system
     */
    public static PowerShell openSession() throws PowerShellNotAvailableException {
        return openSession(null);
    }

    /**
     * Creates a session in PowerShell console an returns an instance which allows
     * to execute commands in PowerShell context.<br>
     * This method allows to define a PowersShell executable path different from default
     *
     * @param customPowerShellExecutablePath the path of powershell executable. If you are using
     *                                       the default installation path, call {@link #openSession()} method instead
     * @return an instance of the class
     * @throws PowerShellNotAvailableException if PowerShell is not installed in the system
     */
    public static PowerShell openSession(String customPowerShellExecutablePath) throws PowerShellNotAvailableException {
        PowerShell powerShell = new PowerShell();

        // Start with default configuration
        powerShell.configuration(null);

        String powerShellExecutablePath = customPowerShellExecutablePath == null ? (OSDetector.isWindows() ? DEFAULT_WIN_EXECUTABLE : DEFAULT_LINUX_EXECUTABLE) : customPowerShellExecutablePath;

        return powerShell.initalize(powerShellExecutablePath);
    }

    // Initializes PowerShell console in which we will enter the commands
    private PowerShell initalize(String powerShellExecutablePath) throws PowerShellNotAvailableException {
        String codePage = PowerShellCodepage.getIdentifierByCodePageName(Charset.defaultCharset().name());
        ProcessBuilder pb = null;

        //Start powershell executable in process
        if (OSDetector.isWindows()) {
            pb = new ProcessBuilder("cmd.exe", "/c", "chcp", codePage, ">", "NUL", "&", powerShellExecutablePath,
                    "-ExecutionPolicy", "Bypass", "-NoExit", "-Command", "-");
        } else {
            pb =  new ProcessBuilder(powerShellExecutablePath,"-nologo","-noexit","-Command", "-");
        }

        //Merge standard and error streams
        pb.redirectErrorStream(true);

        try {
            //Launch process
            p = pb.start();

            if (p.waitFor(5, TimeUnit.SECONDS) && !p.isAlive()) {
                throw new PowerShellNotAvailableException(
                        "Cannot execute PowerShell. Please make sure that it is installed in your system. Errorcode:" + p.exitValue());
            }
        } catch (IOException | InterruptedException ex) {
            throw new PowerShellNotAvailableException(
                    "Cannot execute PowerShell. Please make sure that it is installed in your system", ex);
        }

        //Prepare writer that will be used to send commands to powershell
        this.commandWriter = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(p.getOutputStream())), true);

        //FIXME: is this really needed?
        // Init thread pool. 2 threads are needed: one to write and read console and the other to close it
        this.threadpool = Executors.newFixedThreadPool(this.maxThreads);

        return this;
    }

    /**
     * Execute a PowerShell command.
     * <p>
     * This method launch a thread which will be executed in the already created
     * PowerShell console context
     *
     * @param command the command to call. Ex: dir
     * @return PowerShellResponse the information returned by powerShell
     */
    public PowerShellResponse executeCommand(String command) {
        String commandOutput = "";
        boolean isError = false;
        boolean timeout = false;

        Callable<String> commandProcessor = new PowerShellCommandProcessor("standard", p.getInputStream(), this.maxWait,
                this.waitPause, this.scriptMode);
        Future<String> result = threadpool.submit(commandProcessor);

        if (this.remoteMode) {
            command = completeRemoteCommand(command);
        }

        // Launch command
        commandWriter.println(command);

        try {
            if (!result.isDone()) {
                try {
                    commandOutput = result.get(maxWait, TimeUnit.MILLISECONDS);
                } catch (TimeoutException timeoutEx) {
                    timeout = true;
                    //Interrupt command after timeout
                    result.cancel(true);
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE,
                    "Unexpected error when processing PowerShell command", ex);
        } finally {
            // issue #2. Close and cancel processors/threads - Thanks to r4lly
            // for helping me here
            ((PowerShellCommandProcessor) commandProcessor).close();
        }

        return new PowerShellResponse(isError, commandOutput, timeout);
    }

    /**
     * Execute a single command in PowerShell console and gets result
     *
     * @param command the command to execute
     * @return response with the output of the command
     */
    public static PowerShellResponse executeSingleCommand(String command) {
        PowerShellResponse response = null;

        try (PowerShell session = PowerShell.openSession()){
            response = session.executeCommand(command);
        } catch (PowerShellNotAvailableException ex) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "PowerShell not available", ex);
        }

        return response;
    }

    // Writes a temp powershell script file based on the srcReader
    private File createWriteTempFile(BufferedReader srcReader) {

        BufferedWriter tmpWriter = null;
        File tmpFile = null;

        try {

            tmpFile = File.createTempFile("psscript_" + new Date().getTime(), ".ps1");
            if (!tmpFile.exists()) {
                return null;
            }

            tmpWriter = new BufferedWriter(new FileWriter(tmpFile));
            String line;
            while (srcReader != null && (line = srcReader.readLine()) != null) {
                tmpWriter.write(line);
                tmpWriter.newLine();
            }

            // Add end script line
            tmpWriter.write("Write-Output \"" + END_SCRIPT_STRING + "\"");
        } catch (IOException ioex) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE,
                    "Unexpected error while writing temporary PowerShell script", ioex);
        } finally {
            try {
                if (srcReader != null) {
                    srcReader.close();
                }
                if (tmpWriter != null) {
                    tmpWriter.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE,
                        "Unexpected error when processing temporary PowerShell script", ex);
            }
        }

        return tmpFile;
    }

    /**
     * Executed the provided PowerShell script in PowerShell console and gets
     * result.
     *
     * @param scriptPath the full paht of the script
     * @return response with the output of the command
     */
    public PowerShellResponse executeScript(String scriptPath) {
        return executeScript(scriptPath, "");
    }

    /**
     * Executed the provided PowerShell script in PowerShell console and gets
     * result.
     *
     * @param scriptPath the full path of the script
     * @param params     the parameters of the script
     * @return response with the output of the command
     */
    public PowerShellResponse executeScript(String scriptPath, String params) {
        BufferedReader srcReader = null;

        try {
            srcReader = new BufferedReader(new FileReader(new File(scriptPath)));
        } catch (FileNotFoundException fnfex) {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE,
                    "Unexpected error when processing PowerShell script: file not found", fnfex);
            return new PowerShellResponse(true, "Wrong script path: " + scriptPath, false);
        }

        return executeScript(srcReader, params);
    }

    /**
     * Execute the provided PowerShell script in PowerShell console and gets
     * result.
     *
     * @param srcReader the script as BufferedReader (when loading File from jar)
     * @return response with the output of the command
     */
    public PowerShellResponse executeScript(BufferedReader srcReader) {
        return executeScript(srcReader, "");
    }

    /**
     * Execute the provided PowerShell script in PowerShell console and gets
     * result.
     *
     * @param srcReader the script as BufferedReader (when loading File from jar)
     * @param params    the parameters of the script
     * @return response with the output of the command
     */
    public PowerShellResponse executeScript(BufferedReader srcReader, String params) {

        if (srcReader != null) {
            File tmpFile = createWriteTempFile(srcReader);
            if (tmpFile != null) {
                this.scriptMode = true;
                return executeCommand(tmpFile.getAbsolutePath() + " " + params);
            } else {
                return new PowerShellResponse(true, "Cannot create temp script file!", false);
            }
        } else {
            Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE, "Script buffered reader is null!");
            return new PowerShellResponse(true, "Script buffered reader is null!", false);
        }

    }

    /**
     * Closes all the resources used to maintain the PowerShell context
     */
    @Override
    public void close() {
        if (!this.closed) {
            try {
                Future<String> closeTask = threadpool.submit(() -> {
                    commandWriter.println("exit");
                    p.waitFor();
                    return "OK";
                });
                waitUntilClose(closeTask);
            } catch (InterruptedException ex) {
                Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE,
                        "Unexpected error when when closing PowerShell", ex);
            } finally {
                commandWriter.close();
                try {
                    p.getInputStream().close();
                } catch (IOException ex) {
                    Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE,
                            "Unexpected error when when closing streams", ex);
                }
                if (this.threadpool != null) {
                    try {
                        this.threadpool.shutdownNow();
                        this.threadpool.awaitTermination(5, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE,
                                "Unexpected error when when shutting thread pool", ex);
                    }

                }
                this.closed = true;
            }
        }
    }

    private void waitUntilClose(Future<String> task) throws InterruptedException {
        int closingTime = 0;
        while (!task.isDone()) {
            if (closingTime > maxWait) {
                Logger.getLogger(PowerShell.class.getName()).log(Level.SEVERE,
                        "Unexpected error when closing PowerShell: TIMEOUT!");
                break;
            }
            Thread.sleep(this.waitPause);
            closingTime += this.waitPause;
        }
    }

    private String completeRemoteCommand(String command) {
        return command + ";Write-Output \"\"";
    }
}
