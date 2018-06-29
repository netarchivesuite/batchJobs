package batchprogs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.ContentType;
import org.jwat.common.HttpHeader;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.archive.ArchiveBatchJob;
import dk.netarkivet.common.utils.archive.ArchiveHeaderBase;
import dk.netarkivet.common.utils.archive.ArchiveRecordBase;

/**
 * MimeSize batchjobs that handles both arc and warc batchjobs.
 * Ignores arc-records that are not 200-records
 * Ignores all but response-records in the warc-records.
 *
 */
public class MimeSizeForBoth extends ArchiveBatchJob {

    @Override
    public void finish(OutputStream arg0) {
    }

    @Override
    public void initialize(OutputStream arg0) {
    }

    @Override
    public void processRecord(ArchiveRecordBase record, OutputStream out) {
        
        if (record.bIsWarc) {
            processWarcRecord(record, out);
        } else if (record.bIsArc) {
            processArcRecord(record, out);
        }
    }
    private void processArcRecord(ArchiveRecordBase record, OutputStream out) {
        ArchiveHeaderBase header = record.getHeader();
        String resultString = "";
        if (header != null) {
            //String mimeType = header.getMimetype();
            //System.out.println("old mimetype: " + mimeType);
            //long length = header.getLength();
            String mime = getMimeString(record.getInputStream(), header);
            if (mime != null) {
                resultString = mime;
            }
                        
        }
        try {
            out.write(resultString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public void processWarcRecord(ArchiveRecordBase record, OutputStream out) {
        ArchiveHeaderBase header = record.getHeader();
        String mimeType = header.getMimetype();
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
                    String mime = getMimeString(record.getInputStream(), header);
                    if (mime != null) {
                        out.write(mime.getBytes());
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
                if (httpResponse.statusCode == 200) { 
                    
                    if (payloadContentType != null) {
                    resultString = payloadContentType.toStringShort() 
                            + "##" + httpResponse.getPayloadLength() + "\n";
                    } 
                } else {
                    return null;
                }
            }
        } catch (IOException e) {
            throw new IOFailure("Error reading WARC httpresponse header", e);
        }
        return resultString;
    }

}

