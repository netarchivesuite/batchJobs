/* File:        $Id: Batch.java,v 1.7 2009/06/03 11:46:54 jolf Exp $
 * Revision:    $Revision: 1.7 $
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
 *  USA
 */
package eu.planets;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.exceptions.IllegalState;

/**
 * The class for running making the scripts for deploying an running the batch
 * job upon a distributed system. 
 */
public final class Batch {
/*
-S/home/jolf/batch/conf/batch_config_test.xml
-S/home/jolf/batch/conf/batch_config_local.xml
-S/home/jolf/batch/conf/batch_config_machine.xml

-Z/home/jolf/batch/lib/batch.jar

-C/home/jolf/batch/batchprogs/ChecksumJob.class
-J/home/jolf/batch/batchprogs/eu.planets.batch.jar
-Neu.planets.CopyArcContent
 */
    /** For handling the argument parameters.*/
    private static BatchParameters batchParms = new BatchParameters();
    /** The configuration file.*/
    private static File config;
    /** The batch configuration instance.*/
    private static BatchConfig bc;
    /** 
     * The class name. Either the name of the method within the jar file, 
     * or the class file directly.
     */
    private static String classname;
    /** The name of the jar file.*/
    private static String jarfile;
    /** The package to distribute.*/
    private static File batchPack;

    /** DUMMY constructor.*/
    private Batch() {}
    
    /**
     * @param args The arguments for running this application.
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) {
        // Make sure the arguments can be parsed.
        if(!batchParms.parseParameters(args)) {
            System.err.print(Constants.ERROR_MSG_PARSE_ARGUMENTS);
            System.out.println(batchParms.listArguments());
            System.exit(0);
        }

        // Check arguments
        if(batchParms.getCommandLine().getOptions().length 
                < Constants.CONST_BATCH_ARGS) {
            System.err.print(Constants.ERROR_MSG_NOT_ENOUGH_ARGUMENTS);
            System.out.println(batchParms.listArguments());
            System.exit(0);
        }

        // test if more arguments than options is given 
        if (args.length > batchParms.getOptions().getOptions().size()) {
            System.err.print(Constants.ERROR_MSG_TOO_MANY_ARGUMENTS);
            System.out.println();
            System.out.println("Maximum " 
                    + batchParms.getOptions().getOptions().size() 
                    + "arguments.");
            System.exit(0);
        }

        // initialising.
        initializeVariables();
  
        // get the operating system, and make corresponding BatchConfig.
        String os = System.getProperty("os.name"); 
        if(os.startsWith(Constants.OPERATING_SYSTEM_PREFIX_LINUX)) {
            bc = new BatchConfigLinux();
        } else if(os.startsWith(Constants.OPERATING_SYSTEM_PREFIX_WINDOWS)){
            bc = new BatchConfigWindows();
        } else {
            throw new IllegalState(Constants
                    .ERROR_MSG_BATCHCONFIG_OPERATING_SYSTEM + os);
        }
        
        // Initialise configuration
        if(jarfile == null || jarfile.isEmpty()) {
            bc.initialise(config, batchPack, classname);
        } else {
            bc.initialise(config, batchPack, jarfile, classname);
        }

        // Create the scripts.
        bc.createScripts();
    }
    
    /**
     * Method for initialising variables.
     * Calls the other initialise functions.
     */
    private static void initializeVariables() {
        initConfig();
        initJob();
        initBatchPack();
    }
    
    /**
     * Retrieves the batchPack variable from the arguments.
     * Exits if the arguments are not correct (no arguments or no file).
     */
    private static void initBatchPack() {
        // Get the name of the batch package file.
        String option = batchParms.getCommandLine()
            .getOptionValue(Constants.BATCH_PACK_OPTION_KEY);

        if(option == null || option.isEmpty()) {
            System.err.println(Constants.ERROR_MSG_BATCHPACK_ARGUMENT);
            System.out.println(batchParms.listArguments());
            System.exit(0);
        }

        batchPack = new File(option);
        
        // make sure, that the batch pack exists.
        if(batchPack == null || !batchPack.exists()) {
            System.err.println(Constants.ERROR_MSG_BATCHPACK_FILE);
            System.out.println(batchParms.listArguments());
            System.exit(0);
        }
    }
    
    /**
     * Retrieves the configuration file, based on the arguments.
     * Exits if the arguments are not correct (no arguments or no file).
     */
    private static void initConfig() {
        // Get the name of the configuration file.
        String option = batchParms.getCommandLine()
            .getOptionValue(Constants.SETTINGS_FILE_OPTION_KEY);

        // check the configuration file argument.
        if(option == null || option.isEmpty()) {
            System.err.println(Constants.ERROR_MSG_CONFIG_ARGUMENT);
            System.out.println(batchParms.listArguments());
            System.exit(0);
        }

        config = new File(option);

        // check the configuration file.
        if(config == null || !config.exists()) {
            System.err.println(Constants.ERROR_MSG_CONFIG_FILE);
            System.out.println(batchParms.listArguments());
            System.exit(0);
        }
    }

    /**
     * This method retrieves the arguments, and finds the corresponding 
     * batch job.
     * Check for either 'class file' batch job or 'jar file' batch job.
     * 
     * If not batch job options given, or wrong arguments in the batch job, 
     * then exit.
     */
    private static void initJob() {
        // keeping track of whether the initialisation was successfully. 
        boolean succes = false;

        // Check for 'class file batch job'.
        String option = batchParms.getCommandLine()
            .getOptionValue(Constants.CLASSFILE_OPTION_KEY);

        // Check whether the class file batch job option has arguments.
        if(option != null && !option.isEmpty()) {
            // Check that the file exists.
            File fil = new File(option);
            if(fil == null || !fil.exists()) {
                throw new IOFailure(Constants.ERROR_MSG_CANNOT_LOAD_BATCH_JOB);
            }

            // retrieve the batch job.
            succes = true;
            classname = fil.getAbsolutePath();
            jarfile = null;
        } else {
            // Check for 'jar file batch job'
            option = batchParms.getCommandLine().getOptionValue(
                    Constants.JARFILE_OPTION_KEY);
            if(option != null && !option.isEmpty()) {
                // get method
                String method = batchParms.getCommandLine().getOptionValue(
                        Constants.CLASSNAME_OPTION_KEY);
                // check method.
                if(method == null || method.isEmpty()) {
                    throw new IllegalState(
                            Constants.ERROR_MSG_NO_METHOD_DEFINED);
                } else {
                    // Check that the file exists.
                    File fil = new File(option);
                    if(fil == null || !fil.exists()) {
                        throw new IOFailure(
                                Constants.ERROR_MSG_CANNOT_LOAD_BATCH_JOB);
                    }

                    // retrieves the batch job.
                    succes = true;
                    classname = method;
                    jarfile = fil.getAbsolutePath();
                }
            }
        }

        // If improper arguments given, then give error message and exit.
        if(!succes) {
            // if no file batch job can be defined from the 
            System.err.println(Constants.ERROR_MSG_INIT_BATCH_JOB);
            System.out.println(batchParms.listArguments());
            System.exit(0);
        }
    }

    /** 
     * Type to encapsulate parameters defined by options to batchjob 
     * based on apache.commons.cli.
     */
    private static class BatchParameters {
        /** Options object for parameters. */
        private Options options = new Options();
        /** The command line parse.*/
        private CommandLineParser parser = new PosixParser();
        /** The command line.*/
        private CommandLine cmd;
        //HelpFormatter only prints directly, thus this is not used at
        //the moment. Instead the method usage is implemented
        // in the listArguments() method.
        
        /**
         * Initialize options by setting legal parameters for batch jobs.
         * Note that all our options has arguments.
         */
        BatchParameters() {
            final boolean hasArg = true;
            options.addOption(Constants.CLASSFILE_OPTION_KEY, hasArg,
                    "Class file to be run");
            options.addOption(Constants.JARFILE_OPTION_KEY, hasArg,
                    "Jar file to be run (required if class file "
                            + "is in jar file)");
            options.addOption(Constants.CLASSNAME_OPTION_KEY, hasArg,
                    "Name of the primary class to be run. Only "
                            + "needed when using the Jar-file option");
            options.addOption(Constants.SETTINGS_FILE_OPTION_KEY, hasArg, 
                    "The settings file.");
            options.addOption(Constants.BATCH_PACK_OPTION_KEY, hasArg,
                    "The batch package file (batch.jar)");
        }
        
        /**
         * Parsing the input arguments.
         * 
         * @param args The input arguments.
         * @return Whether it parsed correctly or not.
         */
        Boolean parseParameters(String[] args) {
            try {
                // parse the command line arguments
                cmd = parser.parse(options, args);
            } catch(ParseException exp) {
                System.out.println("Parsing error: " + exp);
                return false;
            }
            return true;
        }
        
        /**
         * Get the list of possible arguments with their description.
         * 
         * @return The list describing the possible arguments.
         */
        String listArguments() {
            StringBuilder res = new StringBuilder();
            res.append(Constants.NEWLINE);
            res.append("Init arguments.");
            // add options
            for (Object o: options.getOptions()) {
                Option op = (Option) o;
                res.append(Constants.NEWLINE);
                res.append(Constants.DASH);
                res.append(op.getOpt());
                res.append(Constants.SPACE);
                res.append(op.getDescription());
            }
            return res.toString();
        }
        
        /**
         * For retrieving the options.
         * 
         * @return The options.
         */
        public Options getOptions() {
            return options;
        }
        
        /**
         * For retrieving the commandLine.
         * 
         * @return The cmd.
         */
        public CommandLine getCommandLine() {
            return cmd;
        }
    }
}
