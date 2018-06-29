package eu.planets.batchprogs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.archive.util.FileUtils;

import uk.gov.nationalarchives.droid.AnalysisController;
import uk.gov.nationalarchives.droid.FileFormatHit;
import uk.gov.nationalarchives.droid.IdentificationFile;
import uk.gov.nationalarchives.droid.binFileReader.AbstractByteReader;
import uk.gov.nationalarchives.droid.binFileReader.ByteReader;
import uk.gov.nationalarchives.droid.signatureFile.FFSignatureFile;

import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.utils.batch.FileBatchJob;
import dk.netarkivet.common.utils.StreamUtils;

/**
 * 
 * The output will be given in the following format: ClassificationText + number
 * of hits + first file format hit + first file format version + mimetype + date
 * \n
 * 
 * Where the File format and Format version is given only for the first hit. If
 * no hits, then File format and Format version are null.
 * 
 */
public class DroidBatchJob extends FileBatchJob {
    // Retrieve the constant values for separation and finish of output.
    private final String STREAM_SEPARATOR = Constants.STREAM_SEPARATOR;
    private final String STREAM_DONE = Constants.STREAM_DONE;

    // The analysis controller, used for parsing the signature file.
    private AnalysisController ac;

    public void finish(OutputStream os) {
	// do nothing at finish.
    }

    /**
     * The function initialises the necessary for running the batch job. This
     * involves extracting the following files from the classpath:
     * DROID_config.xml, DROID_SignatureFile_V18.xml and log4j.properties.
     * 
     * The log4j.properties need to have the name 'log4j.properties', and it is
     * only extracted if it is missing. It does not override existing a file.
     */
    public void initialize(OutputStream os) {
	try {
	    // create the log4j.properties file, if it does not already exists.
	    File log4jProperties = new File("log4j.properties");
	    if (!log4jProperties.exists()) {
		FileUtils.copyFile(
			getResourceFileFromClassPath("log4j.properties"),
			log4jProperties);
	    }
	    // initialise the AnalysisController and extract
	    // signature files (Version 18).
	    ac = new AnalysisController();
	    File signatureFile = getResourceFileFromClassPath("DROID_SignatureFile_V18.xml");
	    File configFile = getResourceFileFromClassPath("DROID_config.xml");
	    ac.readConfiguration(configFile.getAbsolutePath());
	    ac.readSigFile(signatureFile.getAbsolutePath());
	} catch (Exception e) {
	    throw new IOFailure("Cannot invoke the AnalysisController.", e);
	}
    }

    public boolean processFile(File file, OutputStream os) {
	try {
	    // Run the identification on the file.
	    IdentificationFile idFile = ac.performAnalysis(file);

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
		res += "null" + STREAM_SEPARATOR;
	    }

	    // Write the date of harvest and end the result string with done!
	    res += file.lastModified() + STREAM_DONE;

	    // Write to output stream.
	    os.write(res.getBytes());

	    // If this is reached the identification has been a success.
	    return true;
	} catch (Throwable e) {
	    e.printStackTrace();
	    
	    // If error, then the file is not successfully identified.
	    return false;
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
			+ " the class path: '" + filePath + "'";
		throw new IOFailure(msg);
	    }
	} catch (IOException e) {
	    String msg = "Problems making stream of resource in class path "
		    + "into a file. Filepath: '" + filePath + "'";
	    throw new IOFailure(msg, e);
	}
    }
}
