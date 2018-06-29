package batchtesting;

import dk.netarkivet.archive.bitarchive.distribute.BatchMessage;
import dk.netarkivet.common.distribute.ChannelID;
import dk.netarkivet.common.distribute.Channels;
import dk.netarkivet.common.distribute.JMSConnection;
import dk.netarkivet.common.distribute.JMSConnectionFactory;
import dk.netarkivet.common.utils.batch.ChecksumJob;

import dk.netarkivet.common.utils.batch.FileBatchJob;

/**
 * Program that sends a lot of batch messages to bitarchives
 * to provoke OOM errors.
 * Used for testing a production bug in 3.12.1
 */
public class SendChecksumMessage {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println(
                    "arguments missing. Expected <repId> <numberOfmessages>");
            System.exit(1);
        }
        String repId = args[0];
        long messagesToSend = Long.parseLong(args[1]);        
        ChannelID destination = Channels.getAllBa();
        ChannelID bamon = Channels.getBaMonForReplica(repId);
        JMSConnection con = JMSConnectionFactory.getInstance();
        for (long current=0; current < messagesToSend; current++) {
            String filename = "TESTFILE-" 
                + System.currentTimeMillis() + "." + current + ".arc";
            System.out.println("Sending checksum-message (" + current 
                    + ") for presumed nonexisting file " + filename);
            FileBatchJob job = new ChecksumJob();
            job.processOnlyFileNamed(filename);
            BatchMessage msg = new BatchMessage(destination, bamon,
                    job, repId);
            con.send(msg);
        }        
	con.cleanup();
    }

}
