package batchprogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.netarkivet.common.utils.batch.FileBatchJob;

public class CPRfinderBatchJob extends FileBatchJob {

    private final String cprRegex = "\\D" + "([0][1-9]|[12][0-9]|[3][01])"
        + "([0][1-9]|[1][0-2])" + "[0-9]{2}" 
        + "(([\\x2000]|\\p{Blank})*|-|~|-|_|-)" + "([0-9]{4})" + "\\D";

    @Override
    public void finish(OutputStream arg0) {
    }

    @Override
    public void initialize(OutputStream arg0) {
    }

    @Override
    public boolean processFile(File in, OutputStream out) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(in));

            Pattern regex = Pattern.compile(cprRegex);
            try {
                String line;

                while ((line = br.readLine()) != null) {
                    Matcher match = regex.matcher(line);
                    if(match.find()) {
                        out.write(new String(in.getAbsolutePath() + " : " 
                                + line.substring(match.start(), match.end())
                                + " : " + line + "\n").getBytes());
                        return true;
                    }
                }
                out.write(new String("CLEAN FILE: " 
                        + in.getAbsolutePath() + "\n").getBytes());
            } finally {
                br.close();
            }
        } catch (IOException e) {
            try {
                out.write(new String("BAD FILE: " 
                        + in.getAbsolutePath() + "\n").getBytes());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            return false;
        }
        
        return true;
    }

}
