package batchprogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;

import uk.gov.nationalarchives.droid.AnalysisController;
import uk.gov.nationalarchives.droid.FileFormatHit;
import uk.gov.nationalarchives.droid.IdentificationFile;
import uk.gov.nationalarchives.droid.binFileReader.AbstractByteReader;
import uk.gov.nationalarchives.droid.binFileReader.ByteReader;
import uk.gov.nationalarchives.droid.signatureFile.FFSignatureFile;

import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.StreamUtils;
import dk.netarkivet.common.utils.arc.ARCBatchJob;
import dk.netarkivet.common.utils.FileUtils;

/**
 * The output will be given in the following format:
 * ClassificationText + number of hits + first file format hit + 
 * first file format version + mimetype + date \n
 * 
 * For DROID to classify arc records, it has to load the record into an 
 * InputStreamByteReader. The ByteReader reads from the System.in stream, which
 * means that the arc record has to be directed over to the system input stream.
 * 
 */
public class DroidArcJob extends ARCBatchJob {
    // Retrieve the constant values for separation and finish of output.
    private final String STREAM_SEPARATOR = eu.planets.batchprogs.Constants.STREAM_SEPARATOR;
    private final String STREAM_DONE = eu.planets.batchprogs.Constants.STREAM_DONE;

    // The signature file, which is used for the classification.
    private FFSignatureFile sigFile;
    // The analysis controller, used for parsing the signature file.
    private AnalysisController ac;

    // The original system input stream. Used for resetting during finish.
    private InputStream origSysIn;

    @Override
    public void finish(OutputStream os) {
	try {
	    // reset the system input stream.
	    System.setIn(origSysIn);
	} catch (Exception e) {
	    System.err.println(e);
	}
    }

    @Override
    /**
     * The function initialises the necessary for running the batch job. This
     * involves extracting the following files from the classpath:
     * DROID_SignatureFile_V18.xml and log4j.properties.
     * 
     * The log4j.properties need to have the name 'log4j.properties', and it is
     * only extracted if it is missing. It does not override existing a file.
     */
    public void initialize(OutputStream os) {
	try {
	    
	    // save the original system input stream.
	    origSysIn = System.in;
	    
	    File test = new File("test.tmp");
	    FileUtils.copyFile(getResourceFileFromClassPath("dk.netarkivet.archive.settings.xml"), test);
	    System.out.println(FileUtils.readListFromFile(test));
	    
	    // create the log4j.properties file, if it does not already exists.
	    File log4jProperties = new File("log4j.properties");
	    if (!log4jProperties.exists()) {
		FileUtils.copyFile(
			getResourceFileFromClassPath("log4j.properties"),
			log4jProperties);
	    }
	    
	    // initialise the AnalysisController and extract
	    // the configuration and signature files (Version 18).
	    ac = new AnalysisController();
	    File signatureFile = getResourceFileFromClassPath(
		    "DROID_SignatureFile_V18.xml");
	    // parse the signature file
	    sigFile = ac.parseSigFile(signatureFile.getAbsolutePath());

	} catch (Exception e) {
	    throw new IOFailure("Cannot invoke the AnalysisController.", e);
	}
    }

    @Override
    public void processRecord(ARCRecord record, OutputStream os) {
	try {
	    // Extract the metadata
	    ARCRecordMetaData arcRMD = record.getMetaData();
	    // Send the record to the system input stream, since the
	    // InputStreamByteReader only reads the System.in stream.
	    System.setIn(record);
	    // Identify the file. Must be "-" for stream.
	    IdentificationFile idFile = new IdentificationFile("-");
	    // Extract the byte reader, which should be a InputStreamByteReader.
	    ByteReader br = AbstractByteReader.newByteReader(idFile);
	    // Set the path of the file to the url of the record. This must be 
	    // done after the bytestream reader is initialised, otherwise a
	    // URLbytestream reader is initialised and the online object is
	    // attempted to be validated instead of the one in the record.
	    idFile.setFilePath(arcRMD.getUrl());

	    // Run the identification on the file.
	    sigFile.runFileIdentification(br);

	    // Write the classification text.
	    String res = idFile.getClassificationText() + STREAM_SEPARATOR;
	    // Write the number of hits.
	    res += idFile.getNumHits() + STREAM_SEPARATOR;

	    // Write the specific output (format and version) or nulls.
	    if (idFile.getNumHits() > 0) {
		FileFormatHit ffh = idFile.getHit(0);
		// write the fileformat
		res += ffh.getFileFormatName() + STREAM_SEPARATOR;
		// write the file format version
		res += ffh.getFileFormatVersion() + STREAM_SEPARATOR;
		// write the file format mimetype
		res += ffh.getMimeType() + STREAM_SEPARATOR;
	    } else { 
		// Write null for the file format name.
		res += "null" + STREAM_SEPARATOR;
		// write null for the file format version.
		res += "null" + STREAM_SEPARATOR;
		// write the arc record mimetype.
		res += arcRMD.getMimetype() + STREAM_SEPARATOR;
	    }

	    // Write the date of harvest and end the result string with done!
	    res += arcRMD.getDate() + STREAM_DONE;

	    // Write to output stream.
	    os.write(res.getBytes());
	} catch (Exception e) {
	    // write out any error to the system error stream.
	    System.err.println(e.getMessage());
	}
    }

    /**
     * Loads an file from the class path (for retrieving a file from '.jar').
     * 
     * @param filePath
     *            The path of the file.
     * @return The file from the class path.
     * @throws IOFailure
     *             If resource cannot be retrieved from the class path.
     */
    public static File getResourceFileFromClassPath(String filePath)
	    throws IOFailure {
	ArgumentNotValid.checkNotNullOrEmpty(filePath,
		"String defaultClasspathSettingsPath");
	try {
	    // retrieve the file as a stream from the classpath.
	    InputStream stream = Thread.currentThread().getContextClassLoader()
		    .getResourceAsStream(filePath);
	    
	    if (stream != null) {
		// Make stream into file, and return it.
		File tmpFile = File.createTempFile("tmp", "tmp");
		StreamUtils.copyInputStreamToOutputStream(stream,
			new FileOutputStream(tmpFile));
		return tmpFile;
	    } else {
		String msg = "The resource was not retrieved correctly from"
			+ " the class path: '" + filePath + "'" +
		ClassLoader.getSystemClassLoader();
		throw new IOFailure(msg);
	    }
	} catch (IOException e) {
	    String msg = "Problems making stream of resource in class path "
		    + "into a file. Filepath: '" + filePath + "'";
	    throw new IOFailure(msg, e);
	}
    }
}
