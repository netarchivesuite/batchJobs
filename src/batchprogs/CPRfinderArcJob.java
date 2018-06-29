package batchprogs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.archive.io.arc.ARCRecord;

import dk.netarkivet.common.utils.arc.ARCBatchJob;

public class CPRfinderArcJob extends ARCBatchJob {

    private final String cprRegex = "([0][1-9]|[12][0-9]|[3][01])"
        + "([0][1-9]|[1][0-2])" + "[0-9]{2}" 
        + "(([\\x2000]|\\p{Blank})*|-|~|-|_|-)" + "([0-9]{4})";

    @Override
    public void finish(OutputStream out) {
    }

    @Override
    public void initialize(OutputStream out) {
    }

    @Override
    public void processRecord(ARCRecord record, OutputStream out) {
        BufferedReader br = new BufferedReader(new InputStreamReader(record));

        try {
            Pattern regex = Pattern.compile(cprRegex);
            try {
                String line;

                while ((line = br.readLine()) != null) {
                    if(regex.matcher(line).find()) {
                        out.write(new String(record.getMetaData().getUrl() 
                                + "\n").getBytes());
                    }
                }
            } finally {
                br.close();
            }
        } catch (IOException e) {

        }
    }
}
