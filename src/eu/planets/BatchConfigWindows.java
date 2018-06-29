/* File:        $Id: BatchConfigWindows.java,v 1.1 2009/04/20 15:12:59 jolf 
 * Exp $
 * Revision:    $Revision: 1.2 $
 * Author:      $Author: jolf $
 * Date:        $Date: 2009/06/03 11:46:54 $
 *
 * The Netarchive Suite - Software to harvest and preserve websites
 * Copyright 2004-2007 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *   USA
 */
package eu.planets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.StringUtils;

/**
 * The windows instance of BatchConfig.
 */
public class BatchConfigWindows extends BatchConfig {

    /**
     * The constructor.
     * Only calls the constructor of the inherited class. 
     */
    public BatchConfigWindows() {
        super();
    }
    
    /**
     * Method for creating the installation script.
     * 
     * @param outputDir The directory where the installation script is created.
     */
    @Override
    protected void createInstallScript(File outputDir) {
        try {
            File installScript = new File(outputDir, Constants
                    .FILE_NAME_INSTALL_WINDOWS);
            FileWriter fw = new FileWriter(installScript);
            fw.write(Constants.ECHO_DASHES);
            // make installation commands for each machine.
            for(Machine mac : macs) {
//                fw.write("echo " + mac);
                fw.write(mac.getInstallScript(outputDir));
                fw.write(Constants.ECHO_DASHES);
            }
            fw.close();
        } catch (IOException e) {
            throw new IOFailure(Constants
                    .ERROR_MSG_CANNOT_CREATE_INSTALL_SCRIPT, e);
        }
    }

    /**
     * Method to create the start all script.
     * 
     * @param outputDir The directory where the start all script is created.
     */
    @Override
    protected void createStartAllScript(File outputDir) {
        // make local run script
        try {
            // initialise the start all script file.
            File localScript = new File(outputDir, 
                    Constants.FILE_NAME_START_ALL_WINDOWS);
            FileWriter fw = new FileWriter(localScript);
            
            // Run the local scripts.
            if(dirs != null && !dirs.isEmpty()) {
                // 'echo Starting local.'
                fw.write(Constants.SCRIPT_ECHO_STARTING_LOCAL);
                fw.write(Constants.NEWLINE);
                // 'cmd /r path\\start.bat &'
                fw.write(Constants.CMD_R);
                fw.write(Constants.SPACE);
                fw.write(outputDir.getAbsolutePath());
                fw.write(Constants.BACKSLASH);
                fw.write(Constants.FILE_NAME_START_WINDOWS);
                fw.write(Constants.SPACE);
                fw.write(Constants.AND);
                fw.write(Constants.NEWLINE);
            }
            
            // Run on the machines.
            for(Machine mac : macs) {
                // echo Starting machine: name
                fw.write(Constants.SCRIPT_ECHO_STARTING_MACHINE);
                fw.write(mac.getName());
                fw.write(Constants.NEWLINE);
                // machine start script.
                fw.write(mac.getScriptForStartAll());
            }
            
            // close the file after writing it.
            fw.close();
        } catch (IOException e) {
            throw new IOFailure(Constants.ERROR_MSG_START_ALL_SCRIPT, e);
        }
    }

    /**
     * Method to create the local start script.
     * 
     * @param outputDir The directory where the local start script is created.
     */
    @Override
    protected void createLocalStartScript(File outputDir) {
        // make local run script
        try {
            String directoriesFormatted = StringUtils.conjoin(
                    Constants.DIRECTORY_SEPARATOR, dirs);
            
            // initialising the file.
            File localScript = new File(outputDir, 
                    Constants.FILE_NAME_START_WINDOWS);
            FileWriter fw = new FileWriter(localScript);
            
            // java -cp batchpack RunBatch ...
            fw.write(Constants.JAVA);
            fw.write(Constants.SPACE);
            fw.write(Constants.DASH); 
            fw.write(Constants.CLASS_PATH_ARGUMENT);
            fw.write(Constants.SPACE);
            fw.write(batchPackage.getAbsolutePath());
            fw.write(Constants.SPACE);
            fw.write(RunBatch.class.getName());

            // add the FileBatchJob argument(s).
            if(jarFile == null || jarFile.isEmpty()) {
                // ' -C' + filename
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.CLASSFILE_OPTION_KEY);
                fw.write(className);
            } else {
                // ' -J' + filename + ' -N' + method name
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.JARFILE_OPTION_KEY);
                fw.write(jarFile);
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.CLASSNAME_OPTION_KEY);
                fw.write(className);
            }
            
            // add the directory arguments: ' -D' + directories (in format).
            fw.write(Constants.SPACE);
            fw.write(Constants.DASH);
            fw.write(Constants.DIRECTORY_OPTION_KEY);
            fw.write(directoriesFormatted);
            
            // add the recursive sub-directory argument?
            if(subDir){
                // ' -R'
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.RECURSIVE_OPTION_KEY);
            }
            
            // add the pattern argument?
            if(regexPattern != null && !regexPattern.isEmpty()) {
                // ' -P' + pattern
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.REGEX_PATTERN_OPTION_KEY);
                fw.write(regexPattern);
            }
            
            // add the outputFile argument?
            if(outputFileName != null && !outputFileName.isEmpty()) {
                // ' -O' + outputFileName
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.OUTPUT_FILE_OPTION_KEY);
                fw.write(outputFileName);            
            }
            
            // add the errorFile argument.
            if(outputFileName != null && !outputFileName.isEmpty()) {
                // ' -E' + errorFileName
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.ERROR_FILE_OPTION_KEY);
                fw.write(errorFileName);            
            }

            // Close the file after it has been written.
            fw.close();
        } catch (IOException e) {
            throw new IOFailure(Constants.ERROR_MSG_LOCAL_START_SCRIPT, e);
        }
    }
}
