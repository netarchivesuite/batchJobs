package batchjobs;

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
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.ContentType;
import org.jwat.common.HttpHeader;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.archive.ArchiveBatchJob;
import dk.netarkivet.common.utils.archive.ArchiveHeaderBase;
import dk.netarkivet.common.utils.archive.ArchiveRecordBase;

/**
 * Batchjob for performing text-searching through the content of arc-records.
 * Should only be used on records with text mimetype (e.g. matching text/.*). 
 * @author jolf
 */
@SuppressWarnings("serial")
@Resources(value = {
        @Resource(name="MimePattern", description="The regular expression for "
            + "the mimetype.", type=java.lang.String.class),
        @Resource(name="TextPattern", description="The regular expression for "
            + "matching text.", type=java.lang.String.class),
        @Resource(description="Batchjob for performing text-searching through "
            + "the content of arc-records. Should only be used on records with"
            + "text mimetype (e.g. matching text/.*). An example could be the"
            + " regex for CPR-numbers: ([0][1-9]|[12][0-9]|[3][01])"
            + "([0][1-9]|[1][0-2])" + "[0-9]{2}" 
            + "(([\\x2000]|\\p{Blank})*|-|~|-|_|-)" + "([0-9]{4})", 
                type=batchjobs.ContentSearch.class)})
public class ContentSearch extends ArchiveBatchJob {

    private String mimePattern;
    private String textPattern;
    private long findings = 0L;
    private long total = 0L;

    public ContentSearch(String mimePattern, String textPattern) {
        this.mimePattern = mimePattern;
        this.textPattern = textPattern;
    }

    @Override
    public void finish(OutputStream out) {
        String res = "Found " + findings + " urls which matches the "
                + "text-pattern '" + textPattern + "', which also matches the "
                + "mimetype pattern " + mimePattern + " out of " + total 
                + " urls.";
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

   
    public void processArcRecord(ArchiveRecordBase record, OutputStream out) {
        total++;
        ArchiveHeaderBase metadata = record.getHeader();
       
        try {
            if(Pattern.matches(mimePattern, metadata.getMimetype())) {
                // load content and match for text-pattern.
                BufferedReader br = new BufferedReader(new InputStreamReader(record.getInputStream()));
                Pattern regex = Pattern.compile(textPattern);
                try {
                    String line;

                    while ((line = br.readLine()) != null) {
                        if(regex.matcher(line).find()) {
                            findings++;
                            out.write(new String("\n" 
                                    + metadata.getUrl()).getBytes());
                            return;
                        }
                    }
                } finally {
                    br.close();
                }
            }
        } catch (IOException e) {
            throw new IOFailure("Could not handle url '" + metadata.getUrl() 
                    + "' with mimetype '" + metadata.getMimetype() + " for "
                    + "mimetype pattern '" + mimePattern + "'.", e);
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
    
    @Override
    public void processRecord(ArchiveRecordBase record, OutputStream out) {
        total++;
        if (record.bIsWarc) {
            processWarcRecord(record, out);
        } else if (record.bIsArc) {
            processArcRecord(record, out);
        }
    }
    
    public void processWarcRecord(ArchiveRecordBase record, OutputStream out) {  
        ArchiveHeaderBase header = record.getHeader();
        String mimeType = header.getMimetype();
        String url = header.getUrl();
        String msgType;
        ContentType recordContentType = ContentType.parseContentType(mimeType);
        try {
            if (recordContentType != null) {
                if ("application".equals(recordContentType.contentType)
                    && "http".equals(recordContentType.mediaType)) {
                msgType = recordContentType.getParameter("msgtype");
                    if (!"response".equals(msgType)) {
                        // don't do anything
                        return;
                    }
                    String payloadMime = getMimeString(record.getInputStream(), header);
                    if (Pattern.matches(mimePattern, payloadMime)) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(record.getInputStream()));
                        Pattern regex = Pattern.compile(textPattern);
                        try {
                            String line;

                            while ((line = br.readLine()) != null) {
                                if(regex.matcher(line).find()) {
                                    findings++;
                                    out.write(new String("\n" + url).getBytes());
                                    return;
                                }
                            }
                        } finally {
                            br.close();
                        }
                    }
                     
                } else {
                    // don't do anything
                    return;
                }
            }
        } catch (Throwable e) {
            throw new IOFailure("Bad record.", e);
        }
    }

    private String getMimeString(InputStream sar, ArchiveHeaderBase header) { 
        ByteCountingPushBackInputStream pbin = new ByteCountingPushBackInputStream(sar, 8192);
        HttpHeader httpResponse = null;
        ContentType payloadContentType = null;
        String resultString = "\n";
        try {
            httpResponse = HttpHeader.processPayload(HttpHeader.HT_RESPONSE,
                    pbin, header.getLength(), null);     
            if (httpResponse != null && httpResponse.contentType != null) {
                payloadContentType = ContentType.parseContentType(httpResponse.contentType);
                if (payloadContentType != null) {
                    resultString = payloadContentType.toStringShort() 
                            + "##" + httpResponse.getPayloadLength() + "\n";
                } 
            }
        } catch (IOException e) {
            throw new IOFailure("Error reading WARC httpresponse header", e);
        }
        return resultString;
    }
    
}
