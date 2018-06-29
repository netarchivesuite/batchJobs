/* File:        $Id: MachineLinux.java,v 1.5 2009/06/03 11:46:54 jolf Exp $
 * Revision:    $Revision: 1.5 $
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

import org.dom4j.Element;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.StringUtils;
import dk.netarkivet.deploy.XmlStructure;

/**
 * The Linux instance of a machine. Thus a machine running with a Unix-based 
 * operating system. (Should also work on Mac-OS 10+ and Solaris).
 */
public class MachineLinux extends Machine {

    /**
     * Constructor for the LoadableJarBatchJob case.
     * 
     * @param conf The configuration instance.
     * @param set The settings, inherited from the global.
     * @param batchPackage The package file containing RunBatch.
     * @param jarFile The jar file, which contains the method.
     * @param useClass The method to run for RunBatch.
     */
    public MachineLinux(Element conf, XmlStructure set, File batchPackage,
            String jarFile, String useClass) {
        super(conf, set, batchPackage, jarFile, useClass);

        // set operating system to LINUX.
        os = Constants.OPERATING_SYSTEM_LINUX;

        // initialise the content.
        initialise();
    }

    /**
     * Constructor for the LoadableJarBatchJob case.
     * 
     * @param conf The configuration instance.
     * @param set The settings, inherited from the global.
     * @param batchPackage The package file containing RunBatch.
     * @param batchClassFile The class file to run for RunBatch.
     */
    public MachineLinux(Element conf, XmlStructure set, File batchPackage,
            String batchClassFile) {
        super(conf, set, batchPackage, batchClassFile);

        // set operating system to LINUX.
        os = Constants.OPERATING_SYSTEM_LINUX;

        // initialise the content.
        initialise();
    }

    /**
     * Creates the start script for the Linux machine.
     * 
     * @param outputDir The directory where the script is placed.
     */
    @Override
    protected void createStartScript(File outputDir) {
        try {
            // make the list of directories into a string, where they are
            // separated by '#'.
            String d = StringUtils.conjoin(Constants.DIRECTORY_SEPARATOR, dirs);

            // initialise the file.
            File startScript = new File(outputDir, 
                    Constants.FILE_NAME_START_LINUX);
            FileWriter fw = new FileWriter(startScript);
            
            // 'echo Starting at : login' 
            fw.write(Constants.SCRIPT_ECHO_STARTING_MACHINE);
            fw.write(login);
            fw.write(Constants.NEWLINE);
            
            // 'java -cp batchPack RunBatch' ...
            fw.write(Constants.JAVA);
            fw.write(Constants.SPACE);
            fw.write(Constants.DASH);
            fw.write(Constants.CLASS_PATH_ARGUMENT);
            fw.write(Constants.SPACE);
            fw.write(installDir);
            fw.write(Constants.SLASH);
            fw.write(batchPack.getName());
            fw.write(Constants.SPACE);
            fw.write(RunBatch.class.getName());
            
            // write FileBatchJob arguments.
            if(jarFile == null || jarFile.isEmpty()) {
                // ' -C' + classFile
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.CLASSFILE_OPTION_KEY);
                fw.write(installDir);
                fw.write(Constants.SLASH);
                fw.write(Constants.getFilenameFromPath(classFile));
            } else {
                // ' -J' + jarFile + ' -N' + methodName
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.JARFILE_OPTION_KEY);
                fw.write(installDir);
                fw.write(Constants.SLASH);
                fw.write(Constants.getFilenameFromPath(jarFile));
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.CLASSNAME_OPTION_KEY);
                fw.write(className);
            }
            
            // write directory argument. ' -D' + dir
            fw.write(Constants.SPACE);
            fw.write(Constants.DASH);
            fw.write(Constants.DIRECTORY_OPTION_KEY);
            fw.write(d);
            
            // write end script argument. ' -B' + path 
            fw.write(Constants.SPACE);
            fw.write(Constants.DASH);
            fw.write(Constants.SCRIPT_AFTER_RUN_OPTION_KEY);
            fw.write(installDir);
            fw.write(Constants.SLASH);
            fw.write(Constants.FILE_NAME_SEND_LINUX);
            
            // write output file argument. ' -O' + outputFile
            if(outStreamFile != null && !outStreamFile.isEmpty()) {
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.OUTPUT_FILE_OPTION_KEY);
                fw.write(Constants.getFilenameFromPath(outStreamFile));
            }
            
            // write error file argument. ' -E' + errorFile
            if(errStreamFile != null && !errStreamFile.isEmpty()) {
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.ERROR_FILE_OPTION_KEY);
                fw.write(Constants.getFilenameFromPath(errStreamFile));
            }
            
            // write recursive sub-directories argument. ' -R'
            if(subDir) {
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.RECURSIVE_OPTION_KEY);
            }

            // write pattern argument. ' -P' + pattern
            if(regexPattern != null && !regexPattern.isEmpty()) {
                fw.write(Constants.SPACE);
                fw.write(Constants.DASH);
                fw.write(Constants.REGEX_PATTERN_OPTION_KEY);
                fw.write(regexPattern);
            }
            
            // write linux suffix for calling java commands.
            fw.write(Constants.JAVA_CALL_LINUX_SUFFIX);
            fw.write(Constants.NEWLINE);
            fw.close();
        } catch (IOException e) {
            throw new IOFailure(Constants.ERROR_MSG_MACHINE_START_SCRIPT
                    + name, e);
        }
    }

    /**
     * Method for creating the send script, which send the results back to 
     * the host.
     * 
     * @param outputDir The directory where the send-script is created.
     */
    @Override
    protected void createSendResultScript(File outputDir) {
        try {
            // don't send back results, if no result files have been used.
            if(outStreamFile == null && errStreamFile == null) {
                System.out.println(Constants.WARN_MSG_MACHINE_NO_FILES + name); 
                System.out.println(Constants.WARN_MSG_EMPTY_SEND_SCRIPT);
            }

            // Initialise the file.
            File sendScript = new File(outputDir, 
                    Constants.FILE_NAME_SEND_LINUX);
            FileWriter fw = new FileWriter(sendScript);

            // handle output file.
            if(outStreamFile != null) {
                // 'scp outFileName hostLogin:outFilePath_machineName
                fw.write(Constants.SCP);
                fw.write(Constants.SPACE);
                fw.write(Constants.getFilenameFromPath(outStreamFile));
                fw.write(Constants.SPACE);
                fw.write(getHostLogin());
                fw.write(Constants.COLON);
                fw.write(outStreamFile);
                fw.write(Constants.UNDERSCORE);
                fw.write(name);
                fw.write(Constants.NEWLINE);
            }
            
            // handle error file.
            if(errStreamFile != null) {
                // 'scp errorFileName hostLogin:errorFilePath_machineName
                fw.write(Constants.SCP);
                fw.write(Constants.SPACE);
                fw.write(Constants.getFilenameFromPath(errStreamFile));
                fw.write(Constants.SPACE);
                fw.write(getHostLogin());
                fw.write(Constants.COLON);
                fw.write(errStreamFile);
                fw.write(Constants.UNDERSCORE);
                fw.write(name);
                fw.write(Constants.NEWLINE);
            }
            
            // close send script file after writing it.
            fw.close();
        } catch (IOException e) {
            throw new IOFailure(Constants.ERROR_MSG_SEND_SCRIPT + name, e);
        }
    }

    /**
     * Method for making the commands to install this machine.
     * This is called from the BatchConfig instance, and is put into the 
     * install script.
     * 
     * @param outputDir The directory where the script for this instance 
     * is placed.
     * @return The script to install this instance.
     */
    @Override
    protected String getInstallScript(File outputDir) {
        StringBuilder res = new StringBuilder();

        // echo installing at: machineName
        res.append(Constants.SCRIPT_ECHO_INSTALLING_MACHINE);
        res.append(getName());
        res.append(Constants.NEWLINE);
        
        // ssh user@machine "mkdir installDir"
        res.append(Constants.SSH);
        res.append(Constants.SPACE);
        res.append(getLogin());
        res.append(Constants.SPACE);
        res.append(Constants.QUOTATION_MARK);
        res.append(createDirectoryCommand(getInstallDir()));
        res.append(Constants.QUOTATION_MARK);
        res.append(Constants.NEWLINE);
        
        // scp batchpack user@machine:installDir
        res.append(Constants.SCP);
        res.append(Constants.SPACE);
        res.append(batchPack.getAbsolutePath());
        res.append(Constants.SPACE);
        res.append(getLogin());
        res.append(Constants.COLON);
        res.append(getInstallDir());
        res.append(Constants.SLASH);
        res.append(Constants.DOT);
        res.append(Constants.SPACE);
        res.append(Constants.NEWLINE);
        
        // scp BatchJobFile user@machine:installDir
        res.append(Constants.SCP);
        res.append(Constants.SPACE);
        if(jarFile == null || jarFile.isEmpty()) {
            // class file
            res.append(classFile);
        } else {
            // jar file
            res.append(jarFile);
        }
        res.append(Constants.SPACE);
        res.append(getLogin());
        res.append(Constants.COLON);
        res.append(getInstallDir());
        res.append(Constants.SLASH);
        res.append(Constants.DOT);
        res.append(Constants.SPACE);
        res.append(Constants.NEWLINE);
        
        // scp machineDir/* user@machine:installDir
        res.append(Constants.SCP);
        res.append(Constants.SPACE);
        res.append(outputDir.getAbsolutePath());
        res.append(Constants.SLASH);
        res.append(getName());
        res.append(Constants.SLASH);
        res.append(Constants.STAR);
        res.append(Constants.SPACE);
        res.append(getLogin());
        res.append(Constants.COLON);
        res.append(getInstallDir());
        res.append(Constants.SLASH);
        res.append(Constants.DOT);
        res.append(Constants.SPACE);
        res.append(Constants.NEWLINE);
        
        // ssh user@machine "chmod 764 installDir/*.sh"
        res.append(Constants.SSH);
        res.append(Constants.SPACE);
        res.append(getLogin());
        res.append(Constants.SPACE);
        res.append(Constants.QUOTATION_MARK);
        res.append(Constants.CHMOD_764);
        res.append(Constants.SPACE);
        res.append(getInstallDir());
        res.append(Constants.SLASH);
        res.append(Constants.STAR);
        res.append(Constants.FILE_EXTENSION_LINUX);
        res.append(Constants.SPACE);
        res.append(Constants.QUOTATION_MARK);
        res.append(Constants.NEWLINE);
        // end!

        return res.toString();
    }

    /**
     * Method for retrieving the script for calling the start script on this
     * machine.
     * 
     * @return The command to call the start script on this machine
     * from an external machine.
     */
    @Override
    protected String getScriptForStartAll() {
        StringBuilder res = new StringBuilder();
        // 'ssh login ". /ect/profile; cd installDir; ./start.sh"'
        res.append(Constants.SSH);
        res.append(Constants.SPACE);
        res.append(getLogin());
        res.append(Constants.SPACE);
        res.append(Constants.QUOTATION_MARK);
        res.append(Constants.DOT);
        res.append(Constants.SPACE);
        res.append(Constants.ETC_PROFILE);
        res.append(Constants.SEMICOLON);
        res.append(Constants.SPACE);
        res.append(Constants.CD);
        res.append(Constants.SPACE);
        res.append(getInstallDir());
        res.append(Constants.SEMICOLON);
        res.append(Constants.SPACE);
        res.append(Constants.DOT);
        res.append(Constants.SLASH);
        res.append(Constants.FILE_NAME_START_LINUX);
        res.append(Constants.QUOTATION_MARK);
        res.append(Constants.NEWLINE);
        return res.toString();
    }
    
    /**
     * Writes the command for creating a directory, 'dir', through
     * shell script, and check whether the directory already exists.
     * 
     * if [ ! -d dir ]; then mkdir dir; fi; .
     * 
     * @param dir The path of the directory to be created. 
     * @return The command for creating the directory.
     */
    public static final String createDirectoryCommand(String dir) {
        StringBuilder res = new StringBuilder();
        res.append(Constants.IF);
        res.append(Constants.SPACE);
        res.append(Constants.SQUARE_BRACKET_BEGIN);
        res.append(Constants.SPACE);
        res.append(Constants.EXCLAMATION_MARK);
        res.append(Constants.SPACE);
        res.append(Constants.BASH_IS_DIRECTORY);
        res.append(Constants.SPACE);
        res.append(dir);
        res.append(Constants.SPACE);
        res.append(Constants.SQUARE_BRACKET_END);
        res.append(Constants.SEMICOLON);
        res.append(Constants.SPACE);
        res.append(Constants.THEN);
        res.append(Constants.SPACE);
        res.append(Constants.BASH_MAKE_DIRECTORY);
        res.append(Constants.SPACE);
        res.append(dir);
        res.append(Constants.SEMICOLON);
        res.append(Constants.SPACE);
        res.append(Constants.FI);
        res.append(Constants.SEMICOLON);
        return res.toString();
    }
}
