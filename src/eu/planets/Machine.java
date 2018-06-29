/* File:        $Id: Machine.java,v 1.6 2009/06/03 11:46:54 jolf Exp $
 * Revision:    $Revision: 1.6 $
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package eu.planets;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IllegalState;
import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.deploy.XmlStructure;

/**
 * The machine class. 
 */
public abstract class Machine {
    /** The name of the machine. This is the id on the network.*/
    protected String name;
    /** The operating system on the machine.*/
    protected String os;
    /** The settings for this machine. Inherited and overwritten.*/
    protected XmlStructure settings;
    /** The configuration for this machine.*/
    protected XmlStructure configuration;
    /** The host, for sending the output file.*/
    protected String host;
    /** The username for the host.*/
    protected String hostUserName;
    /** The directories upon which the batch job should be run.*/
    protected List<String> dirs;
    /** The login for the machine.*/
    protected String login;
    /** The installation directory.*/
    protected String installDir;
    /** The batch package file.*/
    protected File batchPack;
    /** The jar file for a LoadableJarBatchJob.*/
    protected String jarFile;
    /** The name of the class to run within the LoadableJarBatchJob.*/
    protected String className;
    /** The class file for at LoadableFileBatchJob.*/
    protected String classFile;
    /** The file to put the output stream.*/
    protected String outStreamFile;
    /** The file to put the error stream.*/
    protected String errStreamFile;
    /** The pattern for the files within the directories.*/
    protected String regexPattern;
    /** Whether to run upon the sub-directories.*/
    protected boolean subDir;

    /**
     * The constructor for LoadableJarBatchJob.
     * 
     * @param conf The configuration for this instance.
     * @param set The inherited settings. To be overwritten by the 
     * current configuration, if any.
     * @param batchPackage The batch package file. Used to run the batch job.
     * @param jarFile The jar file for the batch job.
     * @param jarClass The class name for the batch job within the jar file.
     */
    public Machine(Element conf, XmlStructure set, File batchPackage, 
            String jarFile, String jarClass) {
        ArgumentNotValid.checkNotNull(conf, "Element conf");
        ArgumentNotValid.checkNotNull(set, "XmlStructure set");
        ArgumentNotValid.checkNotNull(batchPackage, "File batchPackage");
        ArgumentNotValid.checkNotNullOrEmpty(jarFile, "String useClass");
        ArgumentNotValid.checkNotNullOrEmpty(jarClass, "String useClass");

        configuration = new XmlStructure(conf);
        settings = new XmlStructure(set.getRoot());

        // extract attributes. Except the OS attribute which defines 
        // the sub-class instance and is specified in its constructor.
        name = conf.attributeValue(Constants.ATTRIBUTE_MACHINE_NAME);
        
        this.batchPack = batchPackage;
        
        this.jarFile = jarFile;
        this.className = jarClass;
        this.classFile = null;

        // apply new settings
        Element newSettings = configuration.getChild(
                Constants.COMPLETE_SETTINGS_PATH);
        if(newSettings != null) {
            settings.overWrite(newSettings);
        }
    }
    
    /**
     * Constructor for LoadableFileBatchJob.
     * 
     * @param conf The configuration for this instance.
     * @param set The inherited settings. To be overwritten by the 
     * current configuration, if any.
     * @param batchPackage The batch package file. Used to run the batch job.
     * @param batchClassFile The class file for the batch job.
     */
    public Machine(Element conf, XmlStructure set, File batchPackage, 
            String batchClassFile) {
        ArgumentNotValid.checkNotNull(conf, "Element conf");
        ArgumentNotValid.checkNotNull(set, "XmlStructure set");
        ArgumentNotValid.checkNotNull(batchPackage, "File batchPackage");
        ArgumentNotValid.checkNotNullOrEmpty(batchClassFile, "String useClass");

        configuration = new XmlStructure(conf);
        settings = new XmlStructure(set.getRoot());

        // extract attributes. Except the OS attribute which defines 
        // the sub-class instance and is specified in its constructor.
        name = conf.attributeValue(Constants.ATTRIBUTE_MACHINE_NAME);
        
        this.batchPack = batchPackage;
        
        this.classFile = batchClassFile;
        this.className = null;
        this.jarFile = null;

        // apply new settings
        Element newSettings = configuration.getChild(
                Constants.COMPLETE_SETTINGS_PATH);
        if(newSettings != null) {
            settings.overWrite(newSettings);
        }
    }

    /**
     * Initialises the variables.
     * Extracts the directories for the batch job and the user name 
     * for the login.
     */
    protected void initialise() {
        String[] options;

        // extract the directories
        options = configuration.getLeafValues(
                Constants.COMPLETE_DIRECTORY_LEAFS);
        if(options == null || options.length < 1) {
            System.err.println("No directories for machine: " + name);
            dirs = null;
        } else {
            dirs = new ArrayList<String>(options.length);
            for(String st : options) {
                dirs.add(st);
            }
        }

        // extract the user name for the login. Check max 1 user name.
        options = configuration.getLeafValues(
                Constants.COMPLETE_USER_NAME_PATH);
        if(options == null || options.length < 1) {
            // Assume, that no user name is required..
            login = name;
        } else {
            login = options[0];
            // Warn if more user names given.
            if(options.length > 1) {
                System.out.println("More than one user name given for "
                        + "machine: '" + name + "'. Using: " + login);
            }
            login += Constants.AT + name;
        }
        
        // extract the installation directory. Check max 1 install dir.
        options = configuration.getLeafValues(
                Constants.COMPLETE_INSTALL_DIR_PATH);
        if(options == null || options.length < 1) {
            // Assume no specific installation directory.
            installDir = ".";
        } else {
            installDir = options[0];
            // Warn if more installation directories given.
            if(options.length > 1) {
                System.out.println("More than one installation directory given"
                        + " for machine: '" + name + "'. Using: " + installDir);
            }
        }     
        
        // extract the host machine name.
        options = settings.getLeafValues(Constants.SETTINGS_HOST_LEAF);
        if(options == null || options.length < 1) {
            throw new IllegalState("Cannot distribute without a host.");
        } else {
            host = options[0];
            if(options.length > 1) {
                System.out.println("More than one host given for machine: '"
                        + name + "'. Using: " + host);
            }
        }
        
        // extract the username for the host machine.
        options = settings.getLeafValues(Constants.SETTINGS_HOST_USERNAME_LEAF);
        if(options == null || options.length < 1) {
            hostUserName = null;
        } else {
            hostUserName = options[0];
            if(options.length > 1) {
                System.out.println("More than one username for the host given"
                        + " for machine: '" + name + "'. Using: " 
                        + hostUserName);
            }
        }
        
        // extract whether the sub-dirs should recursively be run upon.
        options = settings.getLeafValues(Constants.SETTINGS_SUBDIR_LEAF);
        if(options == null || options.length < 1) {
            subDir = false;
        } else {
            subDir = true;
            if(options.length > 1) {
                System.out.println("The subDir argument has been defined "
                        +"more than once for machine: " + name);
            }
        }
        
        // extract the regular expression for the pattern.
        options = settings.getLeafValues(Constants.SETTINGS_PATTERN_LEAF);
        if(options == null || options.length < 1) {
            regexPattern = null;
        } else {
            regexPattern = options[0];
            if(options.length > 1) {
                System.out.println("More than one pattern given for machine: "
                        + name + ". Using: " + regexPattern);
            }
        }
        
        // extract the output file name.
        options = settings.getLeafValues(Constants.SETTINGS_OUTPUT_FILE_LEAF);
        if(options == null || options.length < 1) {
            outStreamFile = null;
        } else {
            outStreamFile = options[0];
            if(options.length > 1) {
                System.out.println("More than one output file given "
                        + "for machine: '" + name + "'. Using: " 
                        + outStreamFile);
            }
        }
        
        // extract the error file name.
        options = settings.getLeafValues(Constants.SETTINGS_ERROR_FILE_LEAF);
        if(options == null || options.length < 1) {
            errStreamFile = null;
        } else {
            errStreamFile = options[0];
            if(options.length > 1) {
                System.out.println("More than one error file given "
                        + "for machine: '" + name + "'. Using: " 
                        + errStreamFile);
            }
        }
    }
    
    /**
     * Method for retrieving the name of this machine.
     * 
     * @return The name for accessing the machine on a network.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Method for retrieving the operating system for this machine.
     * 
     * @return The operating system for the machine.
     */
    public String getOs() {
        return os;
    }
    
    /**
     * Method for retrieving the login for this machine.
     * 
     * @return The login for this machine. (user-name + @ + machine-name).
     */
    public String getLogin() {
        return login;
    }
    
    /**
     * Method for retrieving the installation directory for this machine.
     * 
     * @return The path to the installation directory on this machine.
     */
    public String getInstallDir() {
        return installDir;
    }
    
    /**
     * Method for retrieving the login for the host.
     * 
     * @return The host login.
     */
    protected String getHostLogin() {
        if(hostUserName == null) {
            return host;
        } else {
            return hostUserName + Constants.AT + host;
        }
    }
    
    /**
     * Method for creating all the scripts.
     * Starts by creating a specific directory for this machine, 
     * and then creates all the scripts within this directory.
     * 
     * Call the other 'create*' functions for creating the scripts.
     * 
     * @param parentDir The directory where the machine directory 
     * should be created.
     */
    public void createScripts(File parentDir) {
        File machineDir = new File(parentDir, name);
        FileUtils.createDir(machineDir);

        createStartScript(machineDir);
        createSendResultScript(machineDir);
    }

    /**
     * Creates the script for starting the process of running the batch job
     * upon the chosen directories.
     * 
     * @param outputDir The directory, where this script should be placed, 
     * before it is distributed out on the correct machine.
     */
    protected abstract void createStartScript(File outputDir);

    /**
     * Method for creating the script for sending the resulting file back 
     * to the machine, where the all the resulting files should be placed.
     * 
     * @param outputDir The directory where the send script should be created.
     */
    protected abstract void createSendResultScript(File outputDir);

    /**
     * Method for making and retrieving the script for installing the 
     * needed files for this machine.
     * 
     * @param outputDir the directory where the install script is created.
     * Used when sending the files to this machine.
     * @return The commands for the installing script.
     */
    protected abstract String getInstallScript(File outputDir);
    
    /**
     * Method for calling the start script for this machine.
     *  
     * @return The command for calling the start script on this machine.
     */
    protected abstract String getScriptForStartAll();
}
