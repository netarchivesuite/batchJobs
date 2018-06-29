/* File:        $Id: RunBatch.java,v 1.8 2011/02/18 09:47:56 jolf Exp $
 * Revision:    $Revision: 1.8 $
 * Author:      $Author: jolf $
 * Date:        $Date: 2011/02/18 09:47:56 $
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
import java.io.IOException;
import java.security.AllPermission;
import java.security.Permissions;
import java.security.Policy;
import java.security.SecurityPermission;
import java.security.Policy.Parameters;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import sun.security.provider.PolicyFile;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.exceptions.IllegalState;
import dk.netarkivet.common.utils.ProcessUtils;
import dk.netarkivet.common.utils.batch.FileBatchJob;
import dk.netarkivet.common.utils.batch.LoadableFileBatchJob;
import dk.netarkivet.common.utils.batch.LoadableJarBatchJob;

/**
 * Program for running a batch job.
-J The jar file the the JarBatchJob.
-N The method for JarBatchJob.
-C The class file for FileBatchJob. 
-D The directory (directories are separated by '#').
-B Script to run after batch job finished.
-O Output file.
-E Error file.
-R (none. Either exists or not).
-P The pattern.
 */
public final class RunBatch {
/*
-C/home/jolf/batch/batchprogs/ChecksumJob.class
-S/home/jolf/batch/my_script/settings_local.xml
-D/home/jolf/filedir/.

-C/home/jolf/batch/batchprogs/ChecksumJob.class 
-D/home/jolf/filedir/arcs#/home/jolf/filedir/html 
-O/home/jolf/JOLF/output/out.tmp 
-E/home/jolf/JOLF/output/err.tmp

-Djava.security.manager
-Djava.security.policy=/home/jolf/JOLF/security.policy3
 */
    /** The batch job to run. Given as argument.*/
    private static FileBatchJob job;
    /** For handling the argument parameters.*/
    private static RunBatchParameters batchParms = new RunBatchParameters();
    /** The directory to run the batch job upon.*/
    private static List<File> dirs;
    /** The bash file to run after batch job finished.*/
    private static File endScript;
    /** The output file, if any.*/
    private static File outputFile;
    /** The error output file.*/
    private static File errorFile;
    /** The pattern for matching the files in the directories.*/
    private static String pattern;
    /** Whether sub-directories should be run recursively.*/
    private static boolean recursively; 
    
    /**
     * Dummy Constructor.
     */
    private RunBatch() {}
    
    /**
     * The main method. To be called from console/terminale.
     * 
     * @param args The arguments to run a batch job.
     */
    public static void main(String[] args) {
        // Catch any exception.
        try {
            // Make sure the arguments can be parsed.
            if(!batchParms.parseParameters(args)) {
                System.err.print(Constants.ERROR_MSG_PARSE_ARGUMENTS);
                System.out.println(batchParms.listArguments());
                System.exit(0);
            }

            // Check arguments
            if(batchParms.getCommandLine().getOptions().length
                    < Constants.CONST_RUN_BATCH_ARGS) {
                System.err.print(Constants.ERROR_MSG_NOT_ENOUGH_ARGUMENTS);
                System.out.println("\n");
                System.out.println("Use with following arguments:");
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
            initialise();
            
            // run the batch job.
            run();

            // run the end script.
            runEndScript();
        } catch (Exception e) {
            throw new IllegalState(Constants.ERROR_MSG_RUNBATCH 
        	    + Constants.NEWLINE + e.getMessage(), e);
        }
    }
    
    /**
     * Calls the methods for initialising the variables.
     */
    private static void initialise() {
        initJob();
        initDirectory();
        initEndScript();
        initOutputFiles();
        initPattern();
        initRecursively();
    }
    
    /**
     * Initialises the directory.
     * Exits if wrong argument.
     */
    private static void initDirectory() {
        String option = batchParms.getCommandLine()
            .getOptionValue(Constants.DIRECTORY_OPTION_KEY);
        
        if(option == null || option.isEmpty()) {
            dirs = new ArrayList<File>(1);
            dirs.add(new File(Constants.DEFAULT_TMPDIR));
            System.out.println(Constants.WARN_MSG_NO_DIRECTORIES_RUNBATCH 
                    + dirs.get(0).getAbsolutePath());
        } else {
            // extract the directories from the argument (separated by '#')
            String[] options = option.split(Constants
                    .REGEX_DIRECTORY_SEPARATOR);
            
            // make list of directory names into list of directories.
            dirs = new ArrayList<File>(options.length);
            for(String dirName : options) {
                File dir = new File(dirName);
                if(!dir.exists() || !dir.isDirectory()) {
                    System.err.println(Constants.ERROR_MSG_DIR_NOT_ACCESSIBLE
                            + dir.getAbsolutePath());
                    System.out.println(batchParms.listArguments());
                    System.exit(0);
                }
                dirs.add(dir);
            }
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
            File fil = new File(option);

            job = new LoadableFileBatchJob(fil, new ArrayList<String>());
            succes = true;
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
                    System.err.println("No method defined.");
                    succes = false;
                } else {
                    File fil = new File(option);

                    job = new LoadableJarBatchJob(method, 
                            new ArrayList<String>(), fil);
                    succes = true;
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
     * Retrieves the script to run after batch job completion.
     */
    private static void initEndScript() {
        // get end script option value.
        String option = batchParms.getCommandLine().getOptionValue(
                Constants.SCRIPT_AFTER_RUN_OPTION_KEY);

        // If no such argument, then just return.
        if(option == null || option.isEmpty()) {
            return;
        }

        // get file.
        endScript = new File(option);

        // check that file is ok.
        if(endScript == null || !endScript.exists()) {
            System.out.println(Constants.WARN_MSG_NO_END_SCRIPT_FILE);
            endScript = null;
            return;
        }
    }
    
    /**
     * Retrieves the arguments for the output files, and initialises them.
     */
    private static void initOutputFiles() {
        // initialise the option variable.
        String option;
        try {
            // get the output file.
            option = batchParms.getCommandLine().getOptionValue(
                    Constants.OUTPUT_FILE_OPTION_KEY);
            if(option == null || option.isEmpty()) {
                outputFile = null;
            } else {
                outputFile = new File(option);
                // tests if file works.
                if(outputFile == null || (!outputFile.exists() 
                        && !outputFile.createNewFile())) {
                    // TODO decide whether a 'throw IOFailure' should occur.
                    System.err.println(Constants.WARN_MSG_CANT_USE_OUTPUT_FILE);
                    outputFile = null;
                }
            }
        } catch (IOException e) {
            throw new IOFailure(Constants.ERROR_MSG_INIT_OUTPUT_FILE, e);
        }

        try {
            // get the error output file.
            option = batchParms.getCommandLine().getOptionValue(
                    Constants.ERROR_FILE_OPTION_KEY);
            if(option == null || option.isEmpty()) {
                errorFile = null;
            } else {
                errorFile = new File(option);
                // tests if file works.
                if(errorFile == null || (!errorFile.exists() 
                        && !errorFile.createNewFile())) {
                    // TODO decide whether a 'throw IOFailure' should occur.
                    System.err.println(Constants.WARN_MSG_CANT_USE_ERROR_FILE);
                    errorFile = null;
                }
            }
        } catch (IOException e) {
            throw new IOFailure(Constants.ERROR_MSG_INIT_ERROR_FILE, e);
        }
    }
    
    /**
     * Method for extracting the pattern from the arguments.
     */
    private static void initPattern() {
        String option = batchParms.getCommandLine().getOptionValue(
                Constants.REGEX_PATTERN_OPTION_KEY);

        if(option == null || option.isEmpty()) {
            pattern = Constants.DEFAULT_REGEXP;
        } else {
            pattern = option;
        }
    }
    
    /**
     * Method for extracting whether the batch job should run recursively 
     * on the sub-directories.
     */
    private static void initRecursively() {
        recursively = batchParms.getCommandLine().hasOption(
                Constants.RECURSIVE_OPTION_KEY);
    }
    
    /**
     * Runs the given batch job upon the given directory.
     */
    private static void run() {
        ProcessBatch pb = new ProcessBatch(job, outputFile, errorFile, pattern,
                recursively);
        pb.initialise();
        for(File dir : dirs) {
            pb.process(dir);
        }
        pb.finalize();
    }
    
    /**
     * If an end-script defined, then run it.
     */
    private static void runEndScript() {
        // only run end-script if it exists.
        if(endScript != null) {
            ProcessUtils.runProcess(new String[] {Constants.LANGUAGE_C},
                Constants.BASH, endScript.getAbsolutePath());
        }
    }
    
    /** 
     * Type to encapsulate parameters defined by options to batchjob 
     * based on apache.commons.cli.
     */
    private static class RunBatchParameters {
        /** Options object for parameters.*/
        private Options options = new Options();
        /** The command line parser.*/
        private CommandLineParser parser = new PosixParser();
        /** The command line.*/
        private CommandLine cmd;
        //HelpFormatter only prints directly, thus this is not used at
        //the moment. Instead the method usage is implemented
        // in the listArguments() method.
        // TODO Use the HelpFormatter class to print out Usage information.
        
        /**
         * Initialize options by setting legal parameters for batch jobs.
         * Note that all our options has arguments.
         */
        public RunBatchParameters() {
            final boolean hasArg = true;
            final boolean hasNoArg = false;
            options.addOption(Constants.CLASSFILE_OPTION_KEY, hasArg,
                    "Class file to be run");
            options.addOption(Constants.JARFILE_OPTION_KEY, hasArg,
                    "Jar file to be run (required if class file "
                            + "is in jar file)");
            options.addOption(Constants.CLASSNAME_OPTION_KEY, hasArg,
                    "Name of the primary class to be run. Only "
                            + "needed when using the Jar-file option");
            options.addOption(Constants.DIRECTORY_OPTION_KEY, hasArg,
                    "The directory ");
            options.addOption(Constants.SCRIPT_AFTER_RUN_OPTION_KEY, hasArg, 
                    "The bash script to run after batch job finish.");
            options.addOption(Constants.OUTPUT_FILE_OPTION_KEY, hasArg,
                    "The output file argument.");
            options.addOption(Constants.ERROR_FILE_OPTION_KEY, hasArg,
                    "The error output file argument.");
            options.addOption(Constants.REGEX_PATTERN_OPTION_KEY, hasArg,
                    "The regular expressions for the files in the directory.");
            options.addOption(Constants.RECURSIVE_OPTION_KEY, hasNoArg, 
                    "Whether to go through sub-directories.");
        }
        
        /**
         * Parsing the input arguments.
         * 
         * @param args The input arguments.
         * @return Whether it parsed correctly or not.
         */
        public Boolean parseParameters(String[] args) {
            try {
                // parse the command line arguments
                cmd = parser.parse(options, args);
            } catch(ParseException exp) {
                System.out.println("Parsing error: " + exp 
                        + "\n with message: " + exp.getMessage());
                return false;
            }
            return true;
        }
        
        /**
         * Get the list of possible arguments with their description.
         * 
         * @return The list describing the possible arguments.
         */
        public String listArguments() {
            StringBuilder res = new StringBuilder();
            res.append("\n");
            res.append("Init arguments.");
            // add options
            for (Object o: options.getOptions()) {
                Option op = (Option) o;
                res.append("\n");
                res.append("-");
                res.append(op.getOpt());
                res.append(" ");
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
