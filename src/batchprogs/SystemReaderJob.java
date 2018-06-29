package batchprogs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.batch.FileBatchJob;

public class SystemReaderJob extends FileBatchJob {

    private static int fileCount = 0;
    
    @Override
    public void finish(OutputStream os) {
	try {
	    String res = "File count: " + fileCount + "\n";
	    res += "User: " + System.getProperty("user.name") + "\n";
	    
	    os.write(res.getBytes());
	} catch (IOException e) {
	    throw new IOFailure("Failed during finish!", e);
	}
    }

    @Override
    public void initialize(OutputStream os) {
	try {
	    String res = "System properties!" + "\n";
	    res += "java version: " + System.getProperty("java.version") + "\n";
	    res += "os name: " + System.getProperty("os.name") + "\n";
	    res += "os architecture: " + System.getProperty("os.arch") + "\n";
	    res += "os version: " + System.getProperty("os.version") + "\n";
	    
	    os.write(res.getBytes());
	} catch (IOException e) {
	    throw new IOFailure("Failed during initialise!", e);
	}
    }

    @Override
    public boolean processFile(File file, OutputStream os) {
	try {
	    fileCount++;
	    os.write(new String("File: " + file.getName() + "\n").getBytes());
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

}
