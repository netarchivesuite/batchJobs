package batchprogs;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.ProcessUtils;
import dk.netarkivet.common.utils.batch.FileBatchJob;

/**
 * The idea of this job is to create a local file. This should not be allowed!
 */
public class MaliciousJob1 extends FileBatchJob {

    File myFile;
    
    @Override
    public void finish(OutputStream arg0) {
	// run the malicious shell script.
	ProcessUtils.runProcess("bash MALICIOUS.sh");
    }

    @Override
    public void initialize(OutputStream arg0) {
	// Create a malicious shell script.
	try {
	    myFile = new File("MALICIOUS.sh");
	    FileWriter fw = new FileWriter(myFile);
	    
	    fw.append("echo EVIL AND MALICIOUS BATCHJOB OUTPUT !!!! > evil.txt");
	    fw.flush();
	    fw.close();
	} catch (Throwable e) {
	    throw new IOFailure("Cannot create the malicious file.", e);
	}
    }

    @Override
    public boolean processFile(File arg0, OutputStream arg1) {
	return myFile.isFile();
    }
}
