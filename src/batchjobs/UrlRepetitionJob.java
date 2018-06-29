/*
 * Default license stuff... 
 * All rights reserved.
 */
package batchjobs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.Resources;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.archive.ArchiveBatchJob;
import dk.netarkivet.common.utils.archive.ArchiveRecordBase;

/**
 * Batchjob for finding URLs with repetitions.
 */
@SuppressWarnings("serial")
@Resources(value = {
        @Resource(name="Repeats", description="The number of repeats a given "
            +"part of the url must have to be found. 1 does not make any "
            + "sense, whereas 3 would be good choice.", 
            type=java.lang.String.class),
        @Resource(description="This job is made for finding URLs which has "
            + "a single element in their URL repeated several times. E.g. "
            + "http://netarkivet.dk/img/img/img/img/test.gif, where the "
            + "'img' subfolder is repeated several times. "
            + "Or if the URL has nested repetitions, e.g. "
            + "http://netarkivet.dk/img/1/img/2/img/3/img/4/test.gif, where "
            + "'img' is repeated. \n"
            + "Both of these examples will be found if the number of repeats "
            + "are at most 4 (thus 1,2,3,4).", 
                type=batchjobs.UrlRepetitionJob.class)})
public class UrlRepetitionJob extends ArchiveBatchJob {
    /** The number of repetitions to find. */
    private final Integer repetitions;
    
    /**
     * Constructor.
     * @param number The number of URLs to find.
     */
    public UrlRepetitionJob(String number) {
        repetitions = Integer.parseInt(number);
    }
    
    @Override
    public void finish(OutputStream out) {
    }

    @Override
    public void initialize(OutputStream out) {
    }

    @Override
    public void processRecord(ArchiveRecordBase record, OutputStream out) {
        String url = record.getHeader().getUrl();
        
        // Retrieve all sub-parts of the URL
        String[] urlParts = url.split("/");
        Map<String, Integer> urlPartsCount = new HashMap<String, Integer>();
        
        // Count each part of the URL.
        for(String part : urlParts) {
            if(part.isEmpty()) {
                continue;
            }
            
            if(urlPartsCount.containsKey(part)) {
                Integer count = urlPartsCount.get(part);
                count++;
                urlPartsCount.put(part, count);
            } else {
                urlPartsCount.put(part, new Integer(1));
            }
        }
        
        // Find those parts larger than 'repetitions'.
        for(Map.Entry<String, Integer> entry : urlPartsCount.entrySet()) {
            if(entry.getValue() >= repetitions) {
                try {
                    out.write(new String(url + " : " + entry.getKey() + " : " 
                            + entry.getValue() + "\n").getBytes());
                } catch (IOException e) {
                    throw new IOFailure("Could not write for entry '" + url 
                            + " : " + entry.getKey() + " : " 
                            + entry.getValue(), e);
                }
            }
        }
    }

}
