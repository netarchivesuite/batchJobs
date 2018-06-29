package batchprogs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.archive.io.arc.ARCRecord;

import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.arc.ARCBatchJob;

public class DeduplicationFinder extends ARCBatchJob {

    @Override
    public void finish(OutputStream arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize(OutputStream arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void processRecord(ARCRecord record, OutputStream os) {
        String url = record.getMetaData().getUrl();
        try {
            if(url == null || url.isEmpty() ||
                    !url.startsWith("metadata://netarkivet.dk/crawl/reports/processors-report.txt")) {
                return;
            }
  
            String content = getInputStreamAsString(record);
            int start = content.indexOf("Processor: is.hi.bok.digest.DeDuplicator");
            int end = content.indexOf("[Host]", start);
            if(end <= start) {
                end = content.length()-1;
            }
            
            os.write(content.substring(start, end).getBytes());
            os.write(new String("\n").getBytes());
        } catch (IOException e) {
            throw new IOFailure("Could not dump the processor report!", e);
        }
    }

    /**
     * Reads an input stream and returns it as a string.
     * 
     * @param in The input stream.
     * @return The string content of the input stream in the UTF8-charset.
     * @throws ArgumentNotValid If the input stream is null.
     * @throws IOFailure If an IOException is caught while reading the 
     * inputstream. 
     */
    public static String getInputStreamAsString(InputStream in) 
            throws ArgumentNotValid, IOFailure {
        ArgumentNotValid.checkNotNull(in, "InputStream in");

        StringBuilder res = new StringBuilder();
        byte[] buf = new byte[dk.netarkivet.common.Constants.IO_BUFFER_SIZE];
        int read = 0;
        try {
            try {
                while ((read = in.read(buf)) != -1) {
                    res.append(new String(buf, "UTF-8"), 0, read);
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new IOFailure( "Trouble reading inputstream '" + in + "'", e);
        }
        
        return res.toString();
    }
}
