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

/**
 * Response of PowerShell command. This object encapsulate all the useful
 * returned information
 *
 * @author Javier Garcia Alonso
 */
public class PowerShellResponse {

    private final boolean error;
    private final String commandOutput;
    private final boolean timeout;

    PowerShellResponse(boolean isError, String commandOutput, boolean timeout) {
        this.error = isError;
        this.commandOutput = commandOutput;
        this.timeout = timeout;
    }

    /**
     * True if the command finished in error
     *
     * @return boolean value
     */
    public boolean isError() {
        return error;
    }

    /**
     * Retrieves the content returned by the executed command
     *
     * @return boolean value
     */
    public String getCommandOutput() {
        return commandOutput;
    }

    /**
     * True if the command finished in timeout
     *
     * @return boolean value
     */
    public boolean isTimeout() {
        return timeout;
    }
}
