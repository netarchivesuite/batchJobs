package batchprogs;

import java.io.File;
import java.io.OutputStream;

import org.archive.io.arc.ARCRecord;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.arc.ARCBatchJob;

public class DigteFinder extends ARCBatchJob {

    /** Regex for finding the wanted pages at 'digte.dk'. */
    private static final String URL_REGEX = ".*digte[.]dk.*digt=.*Forfatter=.*";
    
    @Override
    public void finish(OutputStream arg0) {
        // DO NOTHING
    }

    @Override
    public void initialize(OutputStream arg0) {
        // DO NOTHING
    }

    @Override
    public void processRecord(ARCRecord record, OutputStream out) {
        try {
            String url = record.getMetaData().getUrl();
            if(url.matches(URL_REGEX)) {
                // extract the filename for the arc-file, and store it along 
                // with the URL. 
                File arcFile = new File(record.getMetaData().getArc());
                out.write(new String(url + " : " 
                        + arcFile.getName() + "\n").getBytes());
            }
        } catch (Exception e) {
            throw new IOFailure("Cannot handle record " + record, e);
        }
    }
}
