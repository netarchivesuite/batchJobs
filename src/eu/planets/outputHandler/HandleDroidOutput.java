package eu.planets.outputHandler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for handling the output file from the Jhove batch jobs, whether it is
 * JhoveArcJob or JhoveBatchJob.
 * 
 * It requires at least two arguments: the output file and an input file. As
 * many input files as wanted can be handled
 */
public class HandleDroidOutput {
    public static final String SEPARATOR = "##";

    private static File outFile;
    private static List<File> inFiles;

    private static long lineEntryCount = 0;

    // Make the maps for each output argument.
    private static HandlerMap<String, Integer> classificationMap = new HandlerMap<String, Integer>(
	    "classification");
    private static HandlerMap<String, Integer> hitsMap = new HandlerMap<String, Integer>(
	    "hits");
    private static HandlerMap<String, Integer> formatMap = new HandlerMap<String, Integer>(
	    "format");
    private static HandlerMap<String, Integer> versionMap = new HandlerMap<String, Integer>(
	    "version");
    private static HandlerMap<String, Integer> mimetypeMap = new HandlerMap<String, Integer>(
	    "mimetype");
    private static HandlerMap<String, Integer> dateMap = new HandlerMap<String, Integer>(
	    "date");

    // Make other maps
    // Map for correlation between validity and format.
    private static HandlerMap<String, Integer> positiveHitsMap = new HandlerMap<String, Integer>(
	    "positive-hits");
    private static HandlerMap<String, Integer> positiveFormatMap = new HandlerMap<String, Integer>(
	    "positive-format");
    private static HandlerMap<String, Integer> positiveMimetypeMap = new HandlerMap<String, Integer>(
	    "positive-mimetype");
    private static HandlerMap<String, Integer> positiveDateMap = new HandlerMap<String, Integer>(
	    "positive-date");
    
    // statictics on tentative hits.
    private static HandlerMap<String, Integer> tentativeHitsMap = new HandlerMap<String, Integer>(
	    "tentative-hits");
    private static HandlerMap<String, Integer> tentativeFormatMap = new HandlerMap<String, Integer>(
	    "tentative-format");
    private static HandlerMap<String, Integer> tentativeMimetypeMap = new HandlerMap<String, Integer>(
	    "tentative-mimetype");
    private static HandlerMap<String, Integer> tentativeDateMap = new HandlerMap<String, Integer>(
	    "tentative-date");
    
    // statistics on not identified stuff
    private static HandlerMap<String, Integer> unknownMimetypeMap = new HandlerMap<String, Integer>(
	    "unknown-mimetype");
    private static HandlerMap<String, Integer> unknownDateMap = new HandlerMap<String, Integer>(
	    "unknown-date");

    // statistics on version
    private static HandlerMap<String, Integer> versionFormatMap = new HandlerMap<String, Integer>(
	    "version-format");
    private static HandlerMap<String, Integer> versionMimetypeMap = new HandlerMap<String, Integer>(
	    "version-mimetype");

    // List for containing the maps.
    private static List<HandlerMap<String, Integer>> mapList = new ArrayList<HandlerMap<String, Integer>>();

    /**
     * The main method.
     * 
     * @param args
     *            The arguments for running this method.
     */
    public static void main(String... args) {
	try {
	    // handle the input arguments
	    if (args.length < 2) {
		System.err.println("Please give at least two arguments: "
			+ "the path to the output file and the batch result "
			+ " file.");
		System.exit(0);
	    }

	    // Create the output file and ensure that it does not exist.
	    outFile = new File(args[0]);
	    if (outFile.exists()) {
		System.err.println("The output file at '" + outFile.getPath()
			+ "' already exists.");
		System.exit(0);
	    }

	    // Create the file and ensure that it is writable.
	    /*
	     * if(!outFile.createNewFile()) {
	     * System.err.println("Could not create the output file at '" +
	     * outFile.getCanonicalPath() + "'."); System.exit(0); }
	     * if(!outFile.canWrite()) {
	     * System.err.println("Cannot write to the output file at '" +
	     * outFile.getCanonicalPath() + "'."); System.exit(0); }
	     */
	    // initialise the map list.
	    init();

	    // create the list of input files.
	    inFiles = new ArrayList<File>(args.length - 1);
	    for (int i = 1; i < args.length; i++) {
		File inputFile = new File(args[i]);

		// ensure the current input file existence and is readable.
		if (!inputFile.exists()) {
		    System.out.println("The input file at '"
			    + inputFile.getPath() + "' does not exist.");
		    System.exit(0);
		}
		if (!inputFile.canRead()) {
		    System.out.println("The input file at '"
			    + inputFile.getPath() + "' is not readable.");
		    System.exit(0);
		}

		// add to the list of input files.
		inFiles.add(inputFile);
	    }

	    // Handle the input files.
	    for (File inputFile : inFiles) {
		handleFile(inputFile);
	    }

	    // write the output.
	    writeOutput();
	} catch (Exception e) {
	    System.err.println("The following exception was caught: \n" + e);
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    private static void init() {
	mapList.add(classificationMap);
	mapList.add(hitsMap);
	mapList.add(formatMap);
	mapList.add(versionMap);
	mapList.add(mimetypeMap);
	mapList.add(dateMap);
	
	// identification statistics
	mapList.add(positiveHitsMap);
	mapList.add(positiveFormatMap);
	mapList.add(positiveMimetypeMap);
	mapList.add(positiveDateMap);
	
	// tentative statistics
	mapList.add(tentativeHitsMap);
	mapList.add(tentativeFormatMap);
	mapList.add(tentativeMimetypeMap);
	mapList.add(tentativeDateMap);

	// unknown statistics
	mapList.add(unknownMimetypeMap);
	mapList.add(unknownDateMap);
	
	// other statistics
	mapList.add(versionFormatMap);
	mapList.add(versionMimetypeMap);
    }

    /**
     * Method for writing the output to the output file.
     */
    private static void writeOutput() throws IOException {
	FileWriter fw = new FileWriter(outFile);
	for(HandlerMap<String, Integer> map : mapList) {
	    fw.append(map.toString());
	    fw.append('\n');
	    fw.append('\n');
	}
	fw.flush();
	fw.close();
    }

    /**
     * Method for handling a file. Each line in the file is read and handled.
     * 
     * @param f
     *            The file to handle.
     */
    private static void handleFile(File f) {
	try {
	    FileReader fr = new FileReader(f);
	    LineNumberReader lnr = new LineNumberReader(fr);

	    String line = lnr.readLine();

	    while (line != null) {
		handleLine(line);
		line = lnr.readLine();
	    }
	} catch (IOException e) {
	    System.err.println("The file '" + f.getPath()
		    + "' could not be handled.");
	}
    }

    /**
     * Method for handling a line. Each of the seven arguments are retrieved and
     * put into their corresponding map.
     * 
     * @param line
     *            The line to handle.
     */
    private static void handleLine(String line) {
	String[] lineContent = line.split(SEPARATOR);
	if (lineContent.length != 6) {
	    System.err
		    .println("The line '"
			    + line
			    + "' does not have enough"
			    + " parameters. It should have: "
			    + "ClassificationText##number of hits##"
			    + "first file format hit##first file format version"
			    + "##mimetype##date");
	    return;
	}

	lineEntryCount++;
	
	String classification = lineContent[0];
	String hits = lineContent[1];
	String format = lineContent[2];
	String version = lineContent[3];
	String mimetype = lineContent[4];
	String usedDate = lineContent[5];
	if(usedDate.length() > 8) {
	    usedDate = usedDate.substring(0, 8);
	}

	// handle empty mimetype 
	if(mimetype.isEmpty()) {
	    mimetype = "null";
	}
	
	// handle the maps
	handleMap(classification, classificationMap);
	handleMap(hits, hitsMap);
	handleMap(format, formatMap);
	handleMap(version, versionMap);
	handleMap(mimetype, mimetypeMap);
	handleMap(usedDate, dateMap);
	
	if(classification.equalsIgnoreCase("positive")) {
	    // ??
	    handleMap(hits, positiveHitsMap);
	    handleMap(format, positiveFormatMap);
	    handleMap(mimetype, positiveMimetypeMap);
	    handleMap(usedDate, positiveDateMap);
	} else if(classification.equalsIgnoreCase("tentative")) {
	    // ??
	    handleMap(hits, tentativeHitsMap);
	    handleMap(format, tentativeFormatMap);
	    handleMap(mimetype, tentativeMimetypeMap);
	    handleMap(usedDate, tentativeDateMap);
	} else if (classification.equalsIgnoreCase("not identified")){
	    handleMap(mimetype, unknownMimetypeMap);
	    handleMap(usedDate, unknownDateMap);
	}
	
	// handle different formats
	if(!version.equalsIgnoreCase("null")) {
	    handleMap(format + ", '" + version + "'", versionFormatMap);
	    handleMap(mimetype + ", '" + version + "'", versionMimetypeMap);
	}
    }

    /**
     * If the map contains the string as a key, then the integer is increased.
     * Else the key is added to the map.
     * 
     * @param s
     *            The string, which is key in the map.
     * @param m
     *            The map.
     */
    private static void handleMap(String s, HandlerMap<String, Integer> m) {
	if (m.containsKey(s)) {
	    // Extract the integer and increment it and put it back in.
	    Integer i = m.get(s);
	    m.put(s, i + 1);
	} else {
	    m.put(s, 1);
	}
    }
}
