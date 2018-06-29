import java.io.ByteArrayOutputStream;
import java.io.File;

import batchprogs.MimeSizeForBoth;

import dk.netarkivet.common.utils.batch.BatchLocalFiles;


public class BatchRunner {

    /**
     * @param args
     */
    public static void main(String[] args) {
        File f = new File(args[0]);
        File[] testFiles = new File[]{f};
        BatchLocalFiles blf = new BatchLocalFiles(testFiles);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        blf.run(new MimeSizeForBoth(), os);
        System.out.println(os.toString());
    }

}
