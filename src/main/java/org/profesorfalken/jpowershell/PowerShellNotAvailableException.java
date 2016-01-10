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

import java.io.IOException;

/**
 * Custom checked exception produced when the Powershell executable cannot 
 * be found
 * 
 * @author Javier Garcia Alonso
 */
public class PowerShellNotAvailableException extends IOException{

    public PowerShellNotAvailableException() {
    }

    public PowerShellNotAvailableException(String message) {
        super(message);
    }

    public PowerShellNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public PowerShellNotAvailableException(Throwable cause) {
        super(cause);
    }

}
