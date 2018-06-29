package batchprogs;

import java.io.OutputStream;

import org.archive.io.arc.ARCRecord;

import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.arc.ARCBatchJob;

public class MimeSize extends ARCBatchJob {

    @Override
    public void finish(OutputStream arg0) {
    }

    @Override
    public void initialize(OutputStream arg0) {
    }

    @Override
    public void processRecord(ARCRecord record, OutputStream out) {
        try {
            out.write(new String(record.getMetaData().getMimetype() 
                    + "##" + record.getMetaData().getLength() 
                    + "\n").getBytes());
        } catch (Throwable e) {
            throw new IOFailure("Bad record.", e);
        }
    }
}
