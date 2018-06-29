/* File:        $Id: ProcessBatch.java,v 1.9 2010/03/11 13:49:53 jolf Exp $
 * Revision:    $Revision: 1.9 $
 * Author:      $Author: jolf $
 * Date:        $Date: 2010/03/11 13:49:53 $
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.regex.Pattern;

import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.exceptions.IllegalState;
import dk.netarkivet.common.utils.batch.FileBatchJob;

/**
 * Class for processing batch jobs.
 */
public class ProcessBatch {
    /** The regular expression for the file names.*/
    private String regexPattern;
    /** The output stream.*/
    private OutputStream outStream;
    /** The error stream.*/
    private OutputStream errorStream;
    /** The output file.*/
    private File outFile;
    /** The error output file.*/
    private File errFile;
    
    /** Whether the sub-directories also should be used.*/
    private boolean subDir;
    /** The batch job to run.*/
    private FileBatchJob job;
    
    // local variables.
    /** Whether this instance already has been initialised.*/
    private boolean initialised = false;
    /** The amount of free space on the disc.*/
    private long discFree;
    
    // old values, to be stored.
    /** Original output stream.*/
    private PrintStream origOutStream = System.out;
    /** Original error stream.*/
    private PrintStream origErrorStream = System.err;
    
    /**
     * Constructor.
     * 
     * @param fbj The FileBatchJob to run. 
     * @param outputFile The output file. If null then std.out is used.
     * @param errorFile The error file. If null then std.err is used.
     * @param pattern The pattern for the regular expression for the files.
     * @param recursively Whether the batch job should recursively run upon
     * the sub-directories. 
     */
    public ProcessBatch(FileBatchJob fbj, File outputFile, File errorFile, 
            String pattern, boolean recursively) {
        ArgumentNotValid.checkNotNull(fbj, "FileBatchJob fbj");
        this.job = fbj;
        this.outFile = outputFile;
        this.errFile = errorFile;
        this.regexPattern = pattern;
        this.subDir = recursively;
    }

    /**
     * Function for initialising all the variables, from the values
     * in the settings.
     */
    public void initialise() {
        // Only initialise once.
        if(initialised){
            System.err.println(Constants.WARN_MSG_INITIALISE_TWICE);
            return;
        }
        initialised = true;
        
        // Get space free.
        discFree = (new File(Constants.DEFAULT_TMPDIR)).getFreeSpace();

        // handle output stream.
        try {
            if(outFile == null || !outFile.canWrite()) {
                outStream = System.out;
            } else {
                outStream = new DataOutputStream(
                        new FileOutputStream(outFile));
            }
        } catch (IOException e) {
            throw new IOFailure(Constants.ERROR_MSG_OUTPUT_STREAM, e);
        }
        
        // handle error stream.
        try {
            if(errFile == null || !errFile.canWrite()) {
                errorStream = System.err;
            } else {
                errorStream = new DataOutputStream(
                        new FileOutputStream(errFile));
            }
        } catch (IOException e) {
            throw new IOFailure(Constants.ERROR_MSG_ERROR_STREAM, e);
        }
        
        // handle pattern
        if(regexPattern == null || regexPattern.isEmpty()) {
            regexPattern = Constants.DEFAULT_REGEXP;
        }
    }

    /**
     * Set the stream back to the original.
     */
    public void finalize() {
        try {
            // Set the out stream back.
            outStream.flush();
            if(outStream == System.out) {
                System.setOut(origOutStream);
            }

            // Set the error stream back.
            errorStream.flush();
            if(errorStream == System.err) {
                System.setErr(origErrorStream);
            }

            // go back to not initialised.
            initialised = false;

        } catch (IOException e) {
            throw new IOFailure(Constants.ERROR_MSG_FINALISING, e);
        }
    }
    
    /**
     * Method for running the batch job.
     * 
     * @param dir The directory to run the batch job upon.
     */
    public void process(File dir) {
        ArgumentNotValid.checkNotNull(dir, "File dir");

        // this has to be initialised before use.
        if(!initialised) {
            throw new IllegalState(Constants
                    .ERROR_MSG_PROCESS_BATCH_NOT_INITIALISED);
        }
        
        // Run the batch program on the directory.
        try {
            try {
        	job.initialize(outStream);
        	processDir(dir);
        	job.finish(outStream);
            } catch (AccessControlException e) {
        	String msg = Constants.ERROR_MSG_NO_DIR_ACCESS 
        	+ dir.getAbsolutePath() + Constants.NEWLINE;
        	errorStream.write(msg.getBytes());
        	e.printStackTrace();
            }
        } catch(IOException e) {
            throw new IOFailure(Constants.ERROR_MSG_PROCESS_DIR 
        	    + dir.getAbsolutePath(), e);
        }
    }
    
    /**
     * Method for running the batch job on every file within a directory.
     * If the batch job should run upon sub-directories, then this method
     * calls it self recursively upon these directories.
     * 
     * @param dir The directory to run the batch job on.
     * @throws IOException When writing to the output streams.
     */
    private void processDir(File dir) throws IOException {
        // Get all the files in the directory.
        File[] files = dir.listFiles();
        for(File fil : files) {
            // Check if disc is full, before running batch
            if(discFull()) {
        	System.err.println("The disc is full!");
                return;
            }
            // Handle the file accordingly if it is a sub-directory
            if(fil.isDirectory()) {
        	// Either go through the sub-directory, or write warning.
        	if(subDir) {
        	    processDir(fil);
        	} else {
        	    System.out.println(Constants.WARN_MSG_FILE_IS_A_DIRECTORY(
        		    fil.getAbsolutePath()));
        	}
            } else {
        	try {
        	    // process file, if it validates through the 
        	    // regular expression.
        	    if(Pattern.matches(regexPattern, fil.getName())) {
/*        		if(!job.processFile(fil, outStream)) {
        		    System.out.println("Cannot process file " + fil.getName());
        		}
*/
        		job.processFile(fil, outStream);
        	    }
        	} catch (Throwable e) {
        	    String msg = Constants.ERROR_MSG_PROCESS_FILE 
        	    + fil.getAbsolutePath() + Constants.NEWLINE 
        	    + e + Constants.NEWLINE;
        	    errorStream.write(msg.getBytes());
        	}
            }
            // */
        }
    }
    
    /**
     * Method for checking whether the disc is full.
     * Checks actually whether the streams uses more space than was left, when
     * the this instance was initialised.
     * 
     * @return Whether the disc is full.
     */
    private boolean discFull() {
        // Calculate used space.
        long sizeUsed = 0;
        if(outStream.getClass().getName() == DataOutputStream.class.getName()) {
            sizeUsed += ((DataOutputStream) outStream).size();
        }
        if(errorStream.getClass().getName() == DataOutputStream
                .class.getName()) {
            sizeUsed += ((DataOutputStream) errorStream).size();
        }

        // handle consequences when more space used than free
        if(sizeUsed > discFree) {
            System.err.println(Constants.ERROR_MSG_DISC_FULL);
            System.out.println(Constants.WARN_MSG_DISC_FULL_FREE_SPACE
                    + discFree);
            System.out.println(Constants.WARN_MSG_DISC_FULL_USAGE + sizeUsed);
            return true;
        }
        return false;
    }
    
    /**
     * Makes sure, that the file is read-only.
     * This also handles if the read-only property is set by a security.policy.
     * 
     * This does not concern executable. Only the read and write properties. 
     * 
     * @param f The file to check.
     * @return Returns true when readable and not writable. Otherwise false.
     * @throws IOException During writing to errorStream 
     * (e.g. disc-full, can't write to file or equivalent).
     */
    private boolean isReadOnly(File f) throws IOException {
        // Make sure, that the file is readable.
        try{
            // if not readable, then definitely not read-only
            if(!f.canRead()) {
        	String msg = Constants.ERROR_MSG_NO_READ_SYSTEM 
        	        + f.getAbsolutePath() + Constants.NEWLINE;
        	errorStream.write(msg.getBytes());
        	return false;
            }
        } catch (AccessControlException e) {
            // This error can occour at 'f.canRead()' and it indicates, 
            // that the file is defined as not readable in the security.policy
            // If not readable, then definitely not read-only.
            String msg = Constants.ERROR_NO_READ_SEC_POLICY 
                    + f.getAbsolutePath() + Constants.NEWLINE;
            errorStream.write(msg.getBytes());
            return false;
        }

        // Make sure that the file is not writable.
        try {
            // Return whether non-writable.
            if(f.canWrite()) {
        	String msg = Constants.ERROR_WRITABLE_FILE 
        	        + f.getAbsolutePath() + Constants.NEWLINE;
        	errorStream.write(msg.getBytes());
        	return false;
            } else {
        	// thus read-only by file-system.
        	return true;
            }
        } catch (AccessControlException e) {
            // This error can occour at 'f.canWrite()' and it indicates, 
            // that the file is defined as not writable in the security.policy
            // If not writable, then read-only by security.policy.
            return true;
        }
    }
    
    /**
     * Function for making files read-only.
     * 
     * @param f The file to make read-only.
     */
    private void makeReadOnly(File f) {
/*	
	f.setExecutable(false);
	f.setReadable(false);
	f.setWritable(false);

	// before
	System.out.print("Permissions: "
		+ (f.canExecute() ? "x" : " ")
		+ (f.canRead() ? "r" : " ")
		+ (f.canWrite() ? "w" : " "));
	
	f.setExecutable(true);
	f.setReadable(true);
	f.setWritable(true);

	System.out.println(" : "
		+ (f.canExecute() ? "x" : " ")
		+ (f.canRead() ? "r" : " ")
		+ (f.canWrite() ? "w" : " ")
		+ " " + f.getAbsolutePath());
// */
	
	if(!f.canRead() || f.canWrite()) {
	    f.setWritable(true);
	    f.setReadable(true, false);
	    f.setExecutable(false, false);
	    f.setWritable(false, false);
	}

    }
    
    private void printRights(File f) {
	try {
	System.out.println("Permissions: "
		+ (f.canExecute() ? "x" : " ")
		+ (f.canRead() ? "r" : " ")
		+ (f.canWrite() ? "w" : " "));
	} catch (Exception e) {
	    System.out.println("Some rights have been disabled by security policy.");
	}
    }
    
    private void setPolicyReadOnly(File f) {
	Permission fp = new FilePermission(f.getAbsolutePath(), "read");
	System.out.println(fp.toString());
    }
}
