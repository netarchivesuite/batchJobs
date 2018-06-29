/* File:        $Id: Constants.java,v 1.8 2009/06/03 11:46:54 jolf Exp $
 * Revision:    $Revision: 1.8 $
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

import dk.netarkivet.common.exceptions.ArgumentNotValid;

/**
 * Contain all the constants for running and processing the batch jobs.
 */
public final class Constants {
    /** Dummy constructor.*/
    private Constants() {}
    // characters.
    /** The dot character: '.' .*/
    public static final String DOT = ".";
    /** The at character: '@' .*/
    public static final String AT = "@";
    /** The star character: '*' .*/
    public static final String STAR = "*";
    /** The new line character: '\n' .*/
    public static final String NEWLINE = "\n";
    /** The tab character: '\t' .*/
    public static final String TAB = "\t";
    /** The hash character: '#' .*/
    public static final String HASH = "#";
    /** The space character: ' ' .*/
    public static final String SPACE = " ";
    /** The dash character: '-' .*/
    public static final String DASH = "-";
    /** The underscore character: '_' .*/
    public static final String UNDERSCORE = "_";
    /** The start square bracket character: '[' .*/
    public static final String SQUARE_BRACKET_BEGIN = "[";
    /** The end square bracket character: ']' .*/
    public static final String SQUARE_BRACKET_END = "]";
    /** The slash character: '/' .*/
    public static final String SLASH = "/";
    /** The backslash character: '\\' .*/
    public static final String BACKSLASH = "\\";
    /** The and character: '&' .*/
    public static final String AND = "&";
    /** The quotation mark character: '\"' .*/
    public static final String QUOTATION_MARK = "\"";
    /** The semicolon character: ';' .*/
    public static final String SEMICOLON = ";";
    /** The colon character: ':' .*/
    public static final String COLON = ":";
    /** The exclamation mark character: '!' .*/
    public static final String EXCLAMATION_MARK = "!";

    // Text constants.
    /** The character used for separating directories in the arguments
     *  for 'RunBatch'.
     */
    public static final String DIRECTORY_SEPARATOR = HASH;
    /** The regular expression for the directory separator.*/
    public static final String REGEX_DIRECTORY_SEPARATOR = SQUARE_BRACKET_BEGIN
        + DIRECTORY_SEPARATOR + SQUARE_BRACKET_END;
    /** The regular expression for the linux directory character.*/
    public static final String REGEX_LINUX_DIRECTORY = SQUARE_BRACKET_BEGIN 
        + SLASH + SQUARE_BRACKET_END;
    /** The java string.*/
    public static final String JAVA = "java";
    /** The class path argument string.*/
    public static final String CLASS_PATH_ARGUMENT = "cp";
    /** The value of the operating system for windows machines.*/
    public static final String OPERATING_SYSTEM_WINDOWS = "windows";
    /** The value of the operating system for linux machines.*/
    public static final String OPERATING_SYSTEM_LINUX = "linux";
    /** The dashes separator to be echoed.*/
    public static final String ECHO_DASHES = "echo "
        + "-------------------------------------" + NEWLINE;
    /** The command for the C language.*/
    public static final String LANGUAGE_C = "LANG=C";
    /** bash.*/
    public static final String BASH = "bash";
    /** cmd /r .*/
    public static final String CMD_R = "cmd /r";
    /** ssh.*/
    public static final String SSH = "ssh";
    /** scp.*/
    public static final String SCP = "scp";
    /** /etc/profile .*/
    public static final String ETC_PROFILE = "/etc/profile";
    /** chmod 764.*/
    public static final String CHMOD_764 = "chmod 764";
    /** cd.*/
    public static final String CD = "cd";
    /** End java call for linux.*/
    public static final String JAVA_CALL_LINUX_SUFFIX = 
        " < /dev/null > batch.log 2>&1 &";
    /** End java call for windows.*/
    public static final String JAVA_CALL_WINDOWS_SUFFIX = " 2> batch.log &";
    /** if.*/
    public static final String IF = "if";
    /** fi.*/
    public static final String FI = "fi";
    /** then.*/
    public static final String THEN = "then";
    /** the bash command for 'make directory'.*/
    public static final String BASH_MAKE_DIRECTORY = "mkdir";
    /** the bash command for 'is directory'.*/
    public static final String BASH_IS_DIRECTORY = DASH + "d";
    /** The command to call scp in cygwin from windows.*/
    public static final String WINDOWS_CYGWIN_SCP = 
        "\"C:\\cygwin\\bin\\scp.exe\"";
    /** The 'not' command for batch in windows.*/
    public static final String BATCH_NOT = "not";
    /** The 'exist' command for batch in windows.*/
    public static final String BATCH_EXIST = "exist";

    // script messages.
    /** The script message for stating local.*/
    public static final String SCRIPT_ECHO_STARTING_LOCAL = 
        "echo Starting local.";
    /** The script message for starting machine.*/
    public static final String SCRIPT_ECHO_STARTING_MACHINE = 
        "echo Starting machine: ";
    /** The script message for installing at machine.*/
    public static final String SCRIPT_ECHO_INSTALLING_MACHINE = 
        "echo Installing at: ";

    // Integer constants
    /** The minimum number of arguments for running Batch.*/
    public static final int CONST_BATCH_ARGS = 3;
    /** The minimum number of arguments for running RunBatch.*/
    public static final int CONST_RUN_BATCH_ARGS = 2;
    
    // default values
    /** Default regexp that matches everything. */
    public static final String DEFAULT_REGEXP = DOT + STAR;
    /** Default tmpdir to the current directory.*/
    public static final String DEFAULT_TMPDIR = DOT; 
    
    // Option keys.
    /** The jarfile option key. */
    public static final String JARFILE_OPTION_KEY = "J";
    /** The classname option key. */
    public static final String CLASSNAME_OPTION_KEY = "N";
    /** The classfile option key. */
    public static final String CLASSFILE_OPTION_KEY = "C";
    /** The settings file option key.*/
    public static final String SETTINGS_FILE_OPTION_KEY = "S";
    /** The directory option key.*/
    public static final String DIRECTORY_OPTION_KEY = "D";
    /** The batch package option key.*/
    public static final String BATCH_PACK_OPTION_KEY = "Z";
    /** The bash script option key.*/
    public static final String SCRIPT_AFTER_RUN_OPTION_KEY = "B";
    /** The output file option key.*/
    public static final String OUTPUT_FILE_OPTION_KEY = "O";
    /** The error output file option key.*/
    public static final String ERROR_FILE_OPTION_KEY = "E";
    /** The recursive go-through sub-directories option key.*/
    public static final String RECURSIVE_OPTION_KEY = "R";
    /** The pattern for the regular expression to test against files.*/
    public static final String REGEX_PATTERN_OPTION_KEY = "P";
    
    // file names
    /** The extension for the script files for linux.*/
    public static final String FILE_EXTENSION_LINUX = ".sh";
    /** The extension for the script files for Windows.*/
    public static final String FILE_EXTENSION_WINDOWS = ".bat";
    /** The file name of the installation script.*/
    public static final String FILE_NAME_INSTALL = "install";
    /** The file name for the installation script for linux.*/
    public static final String FILE_NAME_INSTALL_LINUX = 
        FILE_NAME_INSTALL + FILE_EXTENSION_LINUX;
    /** The file name for the installation script for windows.*/
    public static final String FILE_NAME_INSTALL_WINDOWS =
        FILE_NAME_INSTALL + FILE_EXTENSION_WINDOWS;
    /** The file name for the start_all script.*/
    public static final String FILE_NAME_START_ALL = "startall";
    /** The file name for the start all script for linux.*/
    public static final String FILE_NAME_START_ALL_LINUX = 
        FILE_NAME_START_ALL + FILE_EXTENSION_LINUX;
    /** The file name for the start all script for windows.*/
    public static final String FILE_NAME_START_ALL_WINDOWS =
        FILE_NAME_START_ALL + FILE_EXTENSION_WINDOWS;
    /** The file name for the start script.*/
    public static final String FILE_NAME_START = "start";
    /** The file name for the start script for linux.*/
    public static final String FILE_NAME_START_LINUX = 
        FILE_NAME_START + FILE_EXTENSION_LINUX;
    /** The file name for the start script for windows.*/
    public static final String FILE_NAME_START_WINDOWS =
        FILE_NAME_START + FILE_EXTENSION_WINDOWS;
    /** The file name of the send script.*/
    public static final String FILE_NAME_SEND = "send";
    /** The file name for the start script for linux.*/
    public static final String FILE_NAME_SEND_LINUX = 
        FILE_NAME_SEND + FILE_EXTENSION_LINUX;
    /** The file name for the start script for windows.*/
    public static final String FILE_NAME_SEND_WINDOWS =
        FILE_NAME_SEND + FILE_EXTENSION_WINDOWS;
    
    // Attributes
    /** The operating system attribute for the machines.*/
    public static final String ATTRIBUTE_MACHINE_OPERATING_SYSTEM = "os";
    /** The name attribute for the machines.*/
    public static final String ATTRIBUTE_MACHINE_NAME = "name";
    
    // Operating system variables.
    /** The prefix for the operating system name for all Windows instances.*/
    public static final String OPERATING_SYSTEM_PREFIX_WINDOWS = "Windows";
    /** The prefix for the operating system name for all Linux instances.*/
    public static final String OPERATING_SYSTEM_PREFIX_LINUX = "Linux";
    
    // Paths to content in the configuration and settings,
    /** The path to the settings subtree from 'global'.*/
    public static final String COMPLETE_SETTINGS_PATH = "settings";
    /** The path to the pattern leafs from 'settings'.*/
    public static final String SETTINGS_PATTERN_LEAF = "pattern";
    /** The path to the sub-dir leafs from 'settings'.*/
    public static final String SETTINGS_SUBDIR_LEAF = "subdir";
    /** The path to the output file leaf from 'settings'.*/
    public static final String SETTINGS_OUTPUT_FILE_LEAF = "outputFile";
    /** The path to the error file leaf from 'settings'.*/
    public static final String SETTINGS_ERROR_FILE_LEAF = "errorFile";
    /** The path to the directory leafs from 'global'.*/
    public static final String COMPLETE_DIRECTORY_LEAFS = "directory";
    /** The path to the machine sub-trees from 'global'.*/
    public static final String COMPLETE_MACHINE_PATH = "machine";
    /** The path to the temporary directory leaf from 'settings'.*/
    public static final String SETTINGS_TMPDIR_LEAF = "tmpdir";
    /** The path to the script dir from 'settings'.*/
    public static final String SETTINGS_SCRIPT_DIR_LEAF = "scriptDir";
    /** The path to the user name from 'machine'.*/
    public static final String COMPLETE_USER_NAME_PATH = "machineUserName";
    /** The path to the installation directory from 'machine'.*/
    public static final String COMPLETE_INSTALL_DIR_PATH = "installDir";
    /** The path to the host machine from 'settings'.*/
    public static final String SETTINGS_HOST_LEAF = "host";
    /** The path to the username for the host machine from 'settings'.*/
    public static final String SETTINGS_HOST_USERNAME_LEAF = "hostUsername";

    
    /** The error message for error in parsing the arguments.*/
    public static final String ERROR_MSG_PARSE_ARGUMENTS = 
        "WARNING: wrong arguments given." + NEWLINE;
    /** The error message when too many arguments are given.*/
    public static final String ERROR_MSG_TOO_MANY_ARGUMENTS = 
        "Too many arguments given." + NEWLINE;
    /** The error message when not enough arguments are given.*/
    public static final String ERROR_MSG_NOT_ENOUGH_ARGUMENTS = 
        "Not enough arguments given." + NEWLINE;
    /** The error message when error during loading of a batch job.*/
    public static final String ERROR_MSG_CANNOT_LOAD_BATCH_JOB = 
        "Cannot load batch job file.";
    /** The error message when error during creation of settings file.*/
    public static final String ERROR_MSG_CANNOT_CREATE_SETTINGS = 
        "Cannot create settings file.";
    /** The error message when error during creation of local start script.*/
    public static final String ERROR_MSG_LOCAL_START_SCRIPT = 
        "Cannot create local start script.";
    /** The error message when error during creation of machine start script.*/
    public static final String ERROR_MSG_MACHINE_START_SCRIPT = 
        "Problems creating the start script on machine: ";
    /** The error message when no method defined for a jar-batch job.*/
    public static final String ERROR_MSG_NO_METHOD_DEFINED =
        "No method defined.";
    /** The error message when neither machines nor local directories has 
     *  been defined. 
     */
    public static final String ERROR_MSG_NO_MACS_NOR_DIRS_DEF = 
        "Neither machines nor local directories have been defined.";
    /** The error message when the configuration file cannot be loaded.*/
    public static final String ERROR_MSG_CONFIG_FILE = 
        "Error in loading the configuration file. It does not exist";
    /** The error message when no batch job is loaded.*/
    public static final String ERROR_MSG_INIT_BATCH_JOB = 
        "No proper batch job found.";
    /** 
     * The error message when a batch job is trying to be processed, 
     * without the 'ProcessBatch' instance has been initialised.
     */
    public static final String ERROR_MSG_PROCESS_BATCH_NOT_INITIALISED =
        "A batch job is trying to be processed, but the 'ProcessBatch'" 
        + " instance has not been initialised.";
    /** Error message for disc being full.*/
    public static final String ERROR_MSG_DISC_FULL =
        "The temporary area has been filled up.";
    /** Error message for problems with the arguments.*/
    public static final String ERROR_MSG_ARGUMENTS = "Error in arguments";
    /** Error message for problems with the configuration file argument.*/
    public static final String ERROR_MSG_CONFIG_ARGUMENT =
        "Error in arguments for Configuration file.";
    /** Error message for problems with BatchPack argument.*/
    public static final String ERROR_MSG_BATCHPACK_ARGUMENT = 
        "Error in arguments for BatchPack.";
    /** Error message when BatchPack file does not exist.*/
    public static final String ERROR_MSG_BATCHPACK_FILE =
        "BatchPack file does not exist.";
    /** The error message when the installation script cannot be created.*/
    public static final String ERROR_MSG_CANNOT_CREATE_INSTALL_SCRIPT =
        "Cannot create installation script.";
    /** The error message when an error occurs in RunBatch.*/
    public static final String ERROR_MSG_RUNBATCH = 
        "The following error occured:";
    /** The error message when directory is not accessible.*/
    public static final String ERROR_MSG_DIR_NOT_ACCESSIBLE = 
        "The following directory is not accessible: ";
    /** The error message when error during initialisation of output file.*/
    public static final String ERROR_MSG_INIT_OUTPUT_FILE =
        "Problems occured during initialisation of the output file.";
    /** The error message when error during initialisation of error file.*/
    public static final String ERROR_MSG_INIT_ERROR_FILE =
        "Problems occured during initialisation of the error file.";
    /** The error message when the output stream cannot be created.*/
    public static final String ERROR_MSG_OUTPUT_STREAM = 
        "Cannot create output stream.";
    /** The error message when the error stream cannot be created.*/
    public static final String ERROR_MSG_ERROR_STREAM = 
        "Cannot create error stream.";
    /** The error message when problems during finalising.*/
    public static final String ERROR_MSG_FINALISING = 
        "Problems during finalise of processing batch jobs.";
    /** The error message when problems during creation of startall script.*/
    public static final String ERROR_MSG_START_ALL_SCRIPT =
        "Cannot create start all script.";
    /** The error message when problems during creation of send-script.*/
    public static final String ERROR_MSG_SEND_SCRIPT = 
        "Cannot create send result script for machine: ";
    /** The error message when unknown operating system for BatchConfig.*/
    public static final String ERROR_MSG_BATCHCONFIG_OPERATING_SYSTEM = 
        "The operating system is not supported: ";
    /** The error message when a file is not readable by the system.*/
    public static final String ERROR_MSG_NO_READ_SYSTEM =
        "The following file is not readable by the filesystem as required: ";
    /** The error message when a file is not readable by the security.policy.*/
    public static final String ERROR_NO_READ_SEC_POLICY = "The following file "
        + "is not readable by the security.policy as required: ";
    /** The error message when a file is writable.*/
    public static final String ERROR_WRITABLE_FILE = "The following file is "
        + "writable by both the filesystem and the security.policy which it is "
        + "not allowed: ";
    /** The error message when a directory does not exist.*/
    public static final String ERROR_MSG_DIRECTORY_DOES_NOT_EXIST = 
        "The following directory does not exist: ";
    /** The error message when exception thrown during process of file.*/
    public static final String ERROR_MSG_PROCESS_FILE = 
	"The following file has given the error message below: ";
    /** The error message when exception thrown during process of directory.*/
    public static final String ERROR_MSG_PROCESS_DIR = 
	"Problems during processing of directory: ";
    /** The error message when no access to directory by security.policy.*/
    public static final String ERROR_MSG_NO_DIR_ACCESS = 
	"Security.policy has not given access to the following directory: ";

    
    // warning messages.
    /** The error message when no directories have been defined.*/
    public static final String WARN_MSG_NO_DIRECTORIES =
        "No directories defined.";
    /** The error message when no directories have been defined.*/
    public static final String WARN_MSG_NO_MACHINES =
        "No machines defined.";
    /** The warning when ProcessBatch is trying to be initialised twice.*/
    public static final String WARN_MSG_INITIALISE_TWICE =
        "This instance is being initialised more than once. "
        + "This is not allowed";
    /** The warning, when no pattern is defined.*/
    public static final String WARN_MSG_NO_PATTERN = "No pattern defined."
        + " The default is used: '" + DEFAULT_REGEXP + "'"; 
    /** The warning when no directories defined for RunBatch.*/
    public static final String WARN_MSG_NO_DIRECTORIES_RUNBATCH = 
        "No directory defined, using: ";
    /** The warning when the end script file does not exist.*/
    public static final String WARN_MSG_NO_END_SCRIPT_FILE = 
        "End-script file does not exist.";
    /** The warning when the output file cannot be used.*/
    public static final String WARN_MSG_CANT_USE_OUTPUT_FILE = 
        "Cannot use output file.";
    /** The warning when the error file cannot be used.*/
    public static final String WARN_MSG_CANT_USE_ERROR_FILE = 
        "Cannot use error file.";
    /** The warning when disc full about free space on disc.*/
    public static final String WARN_MSG_DISC_FULL_FREE_SPACE = "Free size: ";
    /** The warning when disc full about space usage by output files.*/
    public static final String WARN_MSG_DISC_FULL_USAGE = "Size used: ";
    /** The warning when no output or error file for machine.*/
    public static final String WARN_MSG_MACHINE_NO_FILES = 
        "No result file created, thus no file to send back, for machine: ";
    /** The warning when an empty send-script is being created.*/
    public static final String WARN_MSG_EMPTY_SEND_SCRIPT = 
        "Empty send script created.";
    
    /**
     * Method for creating a warning about too many patterns in the settings.
     * 
     * @param length The amount of patterns defined. This has to 
     * be more than one.
     * @param value The first value, which is chosen. 
     * The other values are ignored.
     * @return The warning.
     */
    public static final String WARN_MSG_TOO_MANY_PATTERNS(int length, 
            String value) {
        ArgumentNotValid.checkNotNegative(length, "int length");
        ArgumentNotValid.checkNotNullOrEmpty(value, "String value");

        return "Only one regular expression pattern allowed. " + length
            + " patterns was given. The first found is used: '" + value + "'"; 
    }

    /**
     * Method for creating a warning about too many output files 
     * in the settings.
     * 
     * @param length The amount of output files defined. This has to 
     * be more than one.
     * @param value The first value, which is chosen. 
     * The other values are ignored.
     * @return The warning.
     */
    public static final String WARN_MSG_TOO_MANY_OUTPUT_FILES(int length,
            String value) {
        ArgumentNotValid.checkNotNegative(length, "int length");
        ArgumentNotValid.checkNotNullOrEmpty(value, "String value");

        return "Only one output file allowed. " + length + " files was given. "
            + "The first found is used: '" + value + "'";
    }
    
    /**
     * Method for creating a warning about too many error files 
     * in the settings.
     * 
     * @param length The amount of error files defined. This has to 
     * be more than one.
     * @param value The first value, which is chosen. 
     * The other values are ignored.
     * @return The warning.
     */
    public static final String WARN_MSG_TOO_MANY_ERROR_FILES(int length,
            String value) {
        ArgumentNotValid.checkNotNegative(length, "int length");
        ArgumentNotValid.checkNotNullOrEmpty(value, "String value");

        return "Only one error file allowed. " + length + " files was given. "
            + "The first found is used: '" + value + "'";
    }
    
    /**
     * Method for creating a warning about file being directory, and
     * thus cannot be processed by a batch job. 
     * This can only happen when 'subdir = false'.
     * 
     * @param path The path to the file, which is a directory.
     * @return The warning.
     */
    public static final String WARN_MSG_FILE_IS_A_DIRECTORY(String path) {
        return "Cannot process the file " + path + ", since it is a directory.";
    }
    
    /**
     * Method for creating a warning about too many directories given.
     * 
     * @param length The amount of tmpdirs defined in the configuration file.
     * @param value The first value found, and thus the value chosen.
     * @return The warning.
     */
    public static final String WARN_MSG_TOO_MANY_TMPDIRS(int length,
            String value) {
        ArgumentNotValid.checkNotNegative(length, "int length");
        ArgumentNotValid.checkNotNullOrEmpty(value, "String value");

        return "Only one tmpdir allowed. " + length + " directories was given. "
            + "The first found is used: '" + value + "'";
    }
    
    // functions for multiple class usage.
    /**
     * This function retrieves the filename from the total path to the file.
     * It handles both linux and windows.
     * 
     * On linux the path contains a '/', and on windows a '\\'.
     * 
     * @param path The path to the file.
     * @return The name of the file, without the path to the directory.1
     */
    public static String getFilenameFromPath(String path) {
        ArgumentNotValid.checkNotNullOrEmpty(path, "String path");

        if(path.contains("/")) {
            // handle linux case.
            String[] tmp = path.split("[/]");
            return tmp[ tmp.length -1 ];
        } else if(path.contains("\\")){
            // handle windows case.
            String[] tmp = path.split("[\\]");
            return tmp[ tmp.length -1 ];
        }

        // if no path given, then the path must be the filename, 
        // which is then returned.
        return path;
    }
}
