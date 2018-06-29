package batchprogs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;

import dk.netarkivet.common.exceptions.ArgumentNotValid;

public class PersonDataArcBatch extends dk.netarkivet.common.utils.arc.ARCBatchJob {

    private static List<String> regularStrings;
    
    private static final int MAX_OBJECT_SIZE = 1000000;
    
    private static int totalCount = 0;
    private static int textCount = 0;
    private static int sensitiveCount = 0;
    
    @Override
    public void finish(OutputStream os) {
	// DO NOTHING
	regularStrings.clear();
	try {
	    os.write(new String("Count: " + totalCount + "\n").getBytes());
	    os.write(new String("Text: " + textCount + "\n").getBytes());
	    os.write(new String("Sensitive personal data: " + sensitiveCount + "\n").getBytes());
	} catch (IOException e) {
	    return;
	}
    }

    @Override
    public void initialize(OutputStream os) {
	initRegExList();
    }

    @Override
    public void processRecord(ARCRecord record, OutputStream os) {
	try {
	    totalCount++;
	    
	    // check whether the arc-record is text. 
	    ARCRecordMetaData arcRMD = record.getMetaData();
	    if(!arcRMD.getMimetype().contains("text")) {
//		System.out.println(arcRMD.getMimetype());
		return;
	    }
	    if(arcRMD.getLength() > MAX_OBJECT_SIZE) {
		System.out.println("TEXT: " + arcRMD.getLength());
		// set to invalid.
		return;
	    }
	    textCount++;
	    
	    // Read the arc-record
	    String data = readStream(record);
	    
	    
	    for(String reg : regularStrings) {
		if(Pattern.matches(reg, data)) {
		    // CONTAGIOUS
		    criticalUrl(arcRMD, os);
		    return;
		}
	    }
	    
	} catch (Throwable e) {
	    // DO NOTHING!
	}
    }
    
    private void criticalUrl(ARCRecordMetaData arcRMD, OutputStream os) throws IOException {
//	os.write(new String(arcRMD.getUrl() + "\n").getBytes());
	sensitiveCount++;
    }

    private void initRegExList() {
	String ascii = "[\\p{ASCII}]*";
	regularStrings = new ArrayList<String>();
//	regularStrings.add(".*[\\d]{10}.*");
//	regularStrings.add(ascii + "[\\d]{10}" + ascii);
//	regularStrings.add(ascii + "[\\d]{6}-[\\d]{4}" + ascii);
//	regularStrings.add(ascii + "[0-3][0-9][01][0-9]{7}" + ascii);
//	regularStrings.add(ascii + "[0-3][0-9][01][0-9]{3}-[0-9]{4}" + ascii);
	regularStrings.add(ascii + "\\D([0][1-9]|[1-2][0-9]|[3][0-1])\\D" + ascii);
	regularStrings.add(ascii + "forum" + ascii);
	regularStrings.add(ascii + "blog" + ascii);
//	regularStrings.add(ascii);
    }
    
    private String readStream(InputStream is) throws IOException {
	ArgumentNotValid.checkNotNull(is, "InputStream is");
	StringBuffer sb = new StringBuffer();

	BufferedReader br = new BufferedReader(new InputStreamReader(is));

	try {
	    int i;

	    while ((i = br.read()) != -1) {
		sb.append((char) i);
	    }
	} finally {
	    br.close();
	}

	return sb.toString();

    }
}
