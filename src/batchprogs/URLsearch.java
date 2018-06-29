package batchprogs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.annotation.Resources;

import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.arc.ARCBatchJob;

@SuppressWarnings("serial")
@Resources(value = {
        @Resource(name="urlPattern", description="The regular expression for the "
            + "urls.", type=java.lang.String.class), 
        @Resource(description="Batchjob for finding URLs which matches a given"
            + " regular expression.", 
                type=batchprogs.URLsearch.class)})
public class URLsearch extends ARCBatchJob {

    private String urlPattern;
    private long findings = 0L;
    private long total = 0L;
    
    public URLsearch(String urlPattern) {
        this.urlPattern = urlPattern;
    }
    
    @Override
    public void finish(OutputStream out) {
        String res = "Found " + findings + " urls matching the pattern " 
                + urlPattern + " out of " + total + " urls.";
        try {
            out.write(new String("\n").getBytes());
            out.write(res.getBytes());
        } catch (IOException e) {
            new IOFailure("Could not write string: " + res, e);
        }
    }

    @Override
    public void initialize(OutputStream arg0) {
        // Do nothing
    }

    @Override
    public void processRecord(ARCRecord record, OutputStream out) {
        total++;
        ARCRecordMetaData metadata = record.getMetaData();
        try {
            if(Pattern.matches(urlPattern, metadata.getUrl())) {
                findings++;
                out.write(new String("\n" + metadata.getUrl()).getBytes());
            }
        } catch (IOException e) {
            throw new IOFailure("Could not handle url'" + metadata.getUrl() 
                    + "' and pattern '" + urlPattern + "'.", e);
        }
    }

    @Override
    public boolean postProcess(InputStream input, OutputStream output) {
        try {
            
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(input));
            
            List<String> urls = new ArrayList<String>();
            
            String line = "";
            // collect all the urls, and print the other stuff.
            while((line = br.readLine()) != null) {
                if(line.startsWith("http://")) {
                    urls.add(line);
                } else if(!line.isEmpty()) {
                    output.write(new String(line + "\n").getBytes());
                }
            }
            
            output.write(new String("\n").getBytes());
            // print the urls at the end.
            for(String url : urls) {
                output.write(new String(url + "\n").getBytes());
            }
            
            return true;
        } catch (Exception e) {
            
            return false;
        }
    }
    
}
