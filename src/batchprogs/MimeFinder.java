package batchprogs;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;

import dk.netarkivet.common.utils.arc.ARCBatchJob;


public class MimeFinder extends ARCBatchJob {
    /** The class log. */
    private static Log log = LogFactory.getLog(MimeFinder.class);
    
    private static final long MIN_RECORD_SIZE = 20000L;
    private static long totalCount = 0;
    private static long totalSize = 0;

    private static long pdfSmallCount = 0;
    private static long pdfSmallSize = 0;
    private static long docSmallCount = 0;
    private static long docSmallSize = 0;
    private static long imageSmallCount = 0;
    private static long imageSmallSize = 0;
    private static long videoSmallCount = 0;
    private static long videoSmallSize = 0;
    private static long audioSmallCount = 0;
    private static long audioSmallSize = 0;
    private static long textSmallCount = 0;
    private static long textSmallSize = 0;
    private static long octetstreamSmallCount = 0;
    private static long octetstreamSmallSize = 0;    

    private static long pdfLargeCount = 0;
    private static long pdfLargeSize = 0;
    private static long docLargeCount = 0;
    private static long docLargeSize = 0;
    private static long imageLargeCount = 0;
    private static long imageLargeSize = 0;
    private static long videoLargeCount = 0;
    private static long videoLargeSize = 0;
    private static long audioLargeCount = 0;
    private static long audioLargeSize = 0;
    private static long textLargeCount = 0;
    private static long textLargeSize = 0;
    private static long octetstreamLargeCount = 0;
    private static long octetstreamLargeSize = 0;    

    @Override
    public void finish(OutputStream out) {
        try {
            out.write(new String("Total: " + totalCount + " : " 
                    + totalSize + "\n").getBytes());
            out.write(new String("small text/*: " + textSmallCount + " : " 
                    + textSmallSize + "\n").getBytes());
            out.write(new String("large text/*: " + textLargeCount + " : " 
                    + textLargeSize + "\n").getBytes());
            out.write(new String("small image/*: " + imageSmallCount + " : " 
                    + imageSmallSize + "\n").getBytes());
            out.write(new String("large image/*: " + imageLargeCount + " : " 
                    + imageLargeSize + "\n").getBytes());
            out.write(new String("small video/*: " + videoSmallCount + " : " 
                    + videoSmallSize + "\n").getBytes());
            out.write(new String("large video/*: " + videoLargeCount + " : " 
                    + videoLargeSize + "\n").getBytes());
            out.write(new String("small audio/*: " + audioSmallCount + " : " 
                    + audioSmallSize + "\n").getBytes());
            out.write(new String("large audio/*: " + audioLargeCount + " : " 
                    + audioLargeSize + "\n").getBytes());
            out.write(new String("small application/msword: " + docSmallCount 
                    + " : " + docSmallSize + "\n").getBytes());
            out.write(new String("large application/msword: " + docLargeCount 
                    + " : " + docLargeSize + "\n").getBytes());
            out.write(new String("small application/pdf: " + pdfSmallCount 
                    + " : " + pdfSmallSize + "\n").getBytes());
            out.write(new String("large application/pdf: " + pdfLargeCount 
                    + " : " + pdfLargeSize + "\n").getBytes());
            out.write(new String("small application/octet-stream: " 
                    + octetstreamSmallCount + " : " + octetstreamSmallSize 
                    + "\n").getBytes());
            out.write(new String("large application/octet-stream: " 
                    + octetstreamLargeCount + " : " + octetstreamLargeSize 
                    + "\n").getBytes());
            out.write(new String("\n").getBytes());
        } catch (IOException e) {
            // ??
        }
    }

    @Override
    public void initialize(OutputStream arg0) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public boolean postProcess(InputStream in, OutputStream out) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        
        log.info("Post processing the mimetypes.");
        try {
            long countTotal = 0L;
            long sizeTotal = 0L;
            long countSmallText = 0L;
            long sizeSmallText = 0L;
            long countLargeText = 0L;
            long sizeLargeText = 0L;
            long countSmallImage = 0L;
            long sizeSmallImage = 0L;
            long countLargeImage = 0L;
            long sizeLargeImage = 0L;
            long countSmallVideo = 0L;
            long sizeSmallVideo = 0L;
            long countLargeVideo = 0L;
            long sizeLargeVideo = 0L;
            long countSmallAudio = 0L;
            long sizeSmallAudio = 0L;
            long countLargeAudio = 0L;
            long sizeLargeAudio = 0L;
            long countSmallWord = 0L;
            long sizeSmallWord = 0L;
            long countLargeWord = 0L;
            long sizeLargeWord = 0L;
            long countSmallPdf = 0L;
            long sizeSmallPdf = 0L;
            long countLargePdf = 0L;
            long sizeLargePdf = 0L;
            long countSmallOctet = 0L;
            long sizeSmallOctet = 0L;
            long countLargeOctet = 0L;
            long sizeLargeOctet = 0L;
            String line;
            while((line = br.readLine()) != null) {
                // handle the first line 'Total'
                if(line.startsWith("Total")) {
                    String[] split = line.split(" ");
                    if(split.length >= 4) {
                        countTotal += Long.parseLong(split[1]);
                        sizeTotal += Long.parseLong(split[3]);
                    }
                } else if(line.startsWith("small text")) {
                    // Handle second line 'small text/*'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countSmallText += Long.parseLong(split[2]);
                        sizeSmallText += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("large text")) {
                    // Handle third line 'large text/*'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countLargeText += Long.parseLong(split[2]);
                        sizeLargeText += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("small image")) {
                    // Handle third line 'small image/*'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countSmallImage += Long.parseLong(split[2]);
                        sizeSmallImage += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("large image")) {
                    // Handle third line 'large image/*'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countLargeImage += Long.parseLong(split[2]);
                        sizeLargeImage += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("small video")) {
                    // Handle third line 'small video/*'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countSmallVideo += Long.parseLong(split[2]);
                        sizeSmallVideo += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("large video")) {
                    // Handle third line 'large video/*'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countLargeVideo += Long.parseLong(split[2]);
                        sizeLargeVideo += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("small audio")) {
                    // Handle third line 'small audio/*'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countSmallAudio += Long.parseLong(split[2]);
                        sizeSmallAudio += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("large audio")) {
                    // Handle third line 'large audio/*'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countLargeAudio += Long.parseLong(split[2]);
                        sizeLargeAudio += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("small application/msword")) {
                    // Handle third line 'small application/msword'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countSmallWord += Long.parseLong(split[2]);
                        sizeSmallWord += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("large application/msword")) {
                    // Handle third line 'large applciation/msword'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countLargeWord += Long.parseLong(split[2]);
                        sizeLargeWord += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("small application/pdf")) {
                    // Handle third line 'small application/pdf'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countSmallPdf += Long.parseLong(split[2]);
                        sizeSmallPdf += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("large application/pdf")) {
                    // Handle third line 'large application/pdf'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countLargePdf += Long.parseLong(split[2]);
                        sizeLargePdf += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("small application/octet-stream")) {
                    // Handle third line 'small application/octet-stream'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countSmallOctet += Long.parseLong(split[2]);
                        sizeSmallOctet += Long.parseLong(split[4]);
                    }
                } else if(line.startsWith("large application/octet-stream")) {
                    // Handle third line 'large application/octet-stream'
                    String[] split = line.split(" ");
                    if(split.length >= 5) {
                        countLargeOctet += Long.parseLong(split[2]);
                        sizeLargeOctet += Long.parseLong(split[4]);
                    }
                }
                
//                out.write(new String(line + "\n").getBytes());
            }
            
            out.write(new String("\nPost Processing\n").getBytes());
            // write a new ending
            out.write(new String("\nAll Total: " + countTotal + " : " 
                    + sizeTotal).getBytes());
            out.write(new String("\nAll small text/*: " + countSmallText + " : " 
                    + sizeSmallText).getBytes());
            out.write(new String("\nAll large text/*: " + countLargeText + " : " 
                    + sizeLargeText).getBytes());
            out.write(new String("\nAll small image/*: " + countSmallImage + " : " 
                    + sizeSmallImage).getBytes());
            out.write(new String("\nAll large image/*: " + countLargeImage + " : " 
                    + sizeLargeImage).getBytes());
            out.write(new String("\nAll small video/*: " + countSmallVideo + " : " 
                    + sizeSmallVideo).getBytes());
            out.write(new String("\nAll large video/*: " + countLargeVideo + " : " 
                    + sizeLargeVideo).getBytes());
            out.write(new String("\nAll small audio/*: " + countSmallAudio + " : " 
                    + sizeSmallAudio).getBytes());
            out.write(new String("\nAll large audio/*: " + countLargeAudio + " : " 
                    + sizeLargeAudio).getBytes());
            out.write(new String("\nAll small application/msword: " 
                    + countSmallWord + " : " + sizeSmallWord).getBytes());
            out.write(new String("\nAll large application/msword: " 
                    + countLargeWord + " : " + sizeLargeWord).getBytes());
            out.write(new String("\nAll small application/pdf: " 
                    + countSmallPdf + " : " + sizeSmallPdf).getBytes());
            out.write(new String("\nAll large application/pdf: " 
                    + countLargePdf + " : " + sizeLargePdf).getBytes());
            out.write(new String("\nAll small application/octet-stream: " 
                    + countSmallOctet + " : " + sizeSmallOctet).getBytes());
            out.write(new String("\nAll large application/octet-stream: " 
                    + countLargeOctet + " : " + sizeLargeOctet).getBytes());
        } catch (IOException e) {
            log.warn("Unexpected error occured", e);
            return false;
        }
        
        return true;
    }

    @Override
    public void processRecord(ARCRecord record, OutputStream out) {
        ARCRecordMetaData metadata = record.getMetaData();
        long size = metadata.getLength();
        // increment the total count.
        totalCount++;
        totalSize += size;
        
        String mimetype = metadata.getMimetype();
        
        // check whether mimetype text/*
        if(mimetype.startsWith("text/")) {
            if(size < MIN_RECORD_SIZE) {
                textSmallCount++;
                textSmallSize += size;
            } else {
                textLargeCount++;
                textLargeSize += size;
            }
            return;
        }
        // check whether mimetype image/*
        if(mimetype.startsWith("image/")) {
            if(size < MIN_RECORD_SIZE) {
                imageSmallCount++;
                imageSmallSize += size;
            } else {
                imageLargeCount++;
                imageLargeSize += size;
            }
            return;
        }
        // check whether mimetype video/*
        if(mimetype.startsWith("video/")) {
            if(size < MIN_RECORD_SIZE) {
                videoSmallCount++;
                videoSmallSize += size;
            } else {
                videoLargeCount++;
                videoLargeSize += size;
            }
            return;
        }
        // check whether mimetype audio/*
        if(mimetype.startsWith("audio/")) {
            if(size < MIN_RECORD_SIZE) {
                audioSmallCount++;
                audioSmallSize += size;
            } else {
                audioLargeCount++;
                audioLargeSize += size;
            }
            return;
        }
        // check whether mimetype application/msword (word document).
        if(mimetype.startsWith("application/msword")) {
            if(size < MIN_RECORD_SIZE) {
                docSmallCount++;
                docSmallSize += size;
            } else {
                docLargeCount++;
                docLargeSize += size;
            }
            return;
        }
        // check whether mimetype application/pdf (pdf document).
        if(mimetype.startsWith("application/pdf")) {
            if(size < MIN_RECORD_SIZE) {
                pdfSmallCount++;
                pdfSmallSize += size;
            } else {
                pdfLargeCount++;
                pdfLargeSize += size;
            }
            return;
        }
        // check whether mimetype application/octet-stream (binaries).
        if(mimetype.startsWith("application/octet-stream")) {
            if(size < MIN_RECORD_SIZE) {
                octetstreamSmallCount++;
                octetstreamSmallSize += size;
            } else {
                octetstreamLargeCount++;
                octetstreamLargeSize += size;
            }
            return;
        }
    }
}
