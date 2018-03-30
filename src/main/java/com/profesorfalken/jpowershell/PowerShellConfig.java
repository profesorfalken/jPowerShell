/*
 * Copyright 2016-2018 Javier Garcia Alonso.
 *
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

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Utility class that reads the content of the configuration file and convert it into
 * Properties.
 *
 * @author Javier Garcia Alonso
 */
final class PowerShellConfig {
    private static final String CONFIG_FILENAME = "jpowershell.properties";

    private static Properties config;

    public static Properties getConfig() {
        if (config == null) {
            config = new Properties();
            try {
                //load a properties file from class path, inside static method
                config.load(PowerShellConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILENAME));
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(PowerShell.class.getName())
                        .log(Level.SEVERE, "Cannot read config values from file:" + CONFIG_FILENAME, ex);
            }
        }

        return config;
    }
}
