package batchjobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.annotation.Resource;
import javax.annotation.Resources;

import dk.netarkivet.common.Constants;
import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.archive.ArchiveBatchJob;
import dk.netarkivet.common.utils.archive.ArchiveRecordBase;
import dk.netarkivet.common.utils.batch.ArchiveBatchFilter;

/**
* Batchjob that extracts lines from a crawl log matching a regular expression 
* The batch job should be restricted to run on metadata files for a specific
* job only, using the {@link #processOnlyFilesMatching(String)} construct.
*/
@SuppressWarnings("serial")
@Resources(value = {
        @Resource(name="regexp", description="The regular expression we want to match "
            + " the crawllog lines", type=java.lang.String.class), 
        @Resource(description="Batchjob for finding crawllog lines which matches a given"
            + " regular expression.", 
                type=batchjobs.CrawlLogLinesMatchingRegexp.class)})
 
public class CrawlLogLinesMatchingRegexp extends ArchiveBatchJob {
        
        /** Metadata URL for crawl logs. */
        private static String SETUP_URL_FORMAT
            = "metadata://netarkivet.dk/crawl/logs/crawl.log";

        /** The regular expression to match in the crawl.log line. */
        private String regexp;

        /**
         * Initialise the batch job.
         *
         * @param regexp The regexp to match in the crawl.log lines.
         */
        public CrawlLogLinesMatchingRegexp(String regexp) {
            ArgumentNotValid.checkNotNullOrEmpty(regexp, "regexp");
            this.regexp = regexp;

            /**
            * One week in milliseconds.
            */
            batchJobTimeout = 7* Constants.ONE_DAY_IN_MILLIES;
        }

        /**
         * Does nothing, no initialisation is needed.
         * @param os Not used.
         */
        @Override
        public void initialize(OutputStream os) {
        }
        
        @Override
        public ArchiveBatchFilter getFilter() {
            return new ArchiveBatchFilter("OnlyCrawlLog") {
                public boolean accept(ArchiveRecordBase record) {
                    String URL = record.getHeader().getUrl(); 
                    if (URL == null) {
                        return false;
                    } else {
                        return URL.startsWith(SETUP_URL_FORMAT);
                    }
                }
            };
        }

        /**
         * Process a record on crawl log concerning the given domain to result.
         * @param record The record to process.
         * @param os The output stream for the result.
         *
         * @throws ArgumentNotValid on null parameters
         * @throws IOFailure on trouble processing the record.
         */
        @Override
        public void processRecord(ArchiveRecordBase record, OutputStream os) {
            ArgumentNotValid.checkNotNull(record, "ArchiveRecordBase record");
            ArgumentNotValid.checkNotNull(os, "OutputStream os");
            BufferedReader arcreader
                    = new BufferedReader(new InputStreamReader(record.getInputStream()));
            try {
                for(String line = arcreader.readLine(); line != null;
                    line = arcreader.readLine()) {
                    if (line.matches(regexp)) {
                        os.write(line.getBytes("UTF-8"));
                        os.write('\n');
                    }

                }
            } catch (IOException e) {
                throw new IOFailure("Unable to process (w)arc record", e);
            } finally {
                try {
                    arcreader.close(); 
                } catch (IOException e) {
                   e.printStackTrace();
                }
            }
        }

        /**
         * Does nothing, no finishing is needed.
         * @param os Not used.
         */
        @Override
        public void finish(OutputStream os) {
        }

        @Override
        public String toString() {
            return getClass().getName() + ", with arguments: Regexp = " 
                + regexp + ", Filter = " + getFilter();
        }
}
