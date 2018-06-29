package eu.planets.outputHandler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for handling the output file from the Jhove batch jobs,
 * whether it is JhoveArcJob or JhoveBatchJob.
 * 
 * It requires at least two arguments: the output file and an input file.
 * As many input files as wanted can be handled
 */
public class HandleJhoveOutput {
    public static final String SEPARATOR = "##";
    
    private static File outFile;
    private static List<File> inFiles;
    
    private static long lineEntryCount = 0;

    // Make the maps for each output argument.
    private static HandlerMap<String, Integer> validMap = new HandlerMap<String, Integer>("valid");
    private static HandlerMap<String, Integer> wellformedMap = new HandlerMap<String, Integer>("wellformed");
    private static HandlerMap<String, Integer> formatMap = new HandlerMap<String, Integer>("format");
    private static HandlerMap<String, Integer> versionMap = new HandlerMap<String, Integer>("version");
    private static HandlerMap<String, Integer> moduleMap = new HandlerMap<String, Integer>("module");
    private static HandlerMap<String, Integer> mimetypeMap = new HandlerMap<String, Integer>("mimetype");
    private static HandlerMap<String, Integer> dateMap = new HandlerMap<String, Integer>("date");
    
    // Make other maps
    // Map for correlation between validity and format.
    private static HandlerMap<String, Integer> validFormatMap = new HandlerMap<String, Integer>("valid-format");
    private static HandlerMap<String, Integer> validMimetypeMap = new HandlerMap<String, Integer>("valid-mimetype");
    private static HandlerMap<String, Integer> validModuleMap = new HandlerMap<String, Integer>("valid-module");
    private static HandlerMap<String, Integer> validDateMap = new HandlerMap<String, Integer>("valid-date");
    private static HandlerMap<String, Integer> validWellformedMap = new HandlerMap<String, Integer>("valid-wellformed");
    private static HandlerMap<String, Integer> wellformedFormatMap = new HandlerMap<String, Integer>("wellformed-format");
    private static HandlerMap<String, Integer> wellformedMimetypeMap = new HandlerMap<String, Integer>("wellformed-mimetype");
    private static HandlerMap<String, Integer> wellformedModuleMap = new HandlerMap<String, Integer>("wellformed-module");
    private static HandlerMap<String, Integer> wellformedDateMap = new HandlerMap<String, Integer>("wellformed-date");
    private static HandlerMap<String, Integer> wellformedValidMap = new HandlerMap<String, Integer>("wellformed-valid");
    
    // map for statistics on not-handled stuff.
    private static HandlerMap<String, Integer> invalidMimetypeMap = new HandlerMap<String, Integer>("invalid-mimetype");
    private static HandlerMap<String, Integer> unhandledMimetypeMap = new HandlerMap<String, Integer>("unhandled-mimetype");
    
    // List for containing the maps.
    private static List<HandlerMap<String, Integer>> mapList = new ArrayList<HandlerMap<String, Integer>>();
    
    /**
     * The main method.
     *  
     * @param args The arguments for running this method.
     */
    public static void main(String ... args) {
	try {
	    // handle the input arguments
	    if(args.length < 2) {
		System.err.println("Please give at least two arguments: "
			+ "the path to the output file and the batch result "
			+ " file.");
		System.exit(0);
	    }

	    // Create the output file and ensure that it does not exist.
	    outFile = new File(args[0]);
	    if(outFile.exists()) {
		System.err.println("The output file at '" + outFile.getPath() 
			+ "' already exists.");
		System.exit(0);
	    }
	    
	    // Create the file and ensure that it is writable.
	    if(!outFile.createNewFile()) {
		System.err.println("Could not create the output file at '"
			+ outFile.getCanonicalPath() + "'.");
		System.exit(0);
	    }
	    if(!outFile.canWrite()) {
		System.err.println("Cannot write to the output file at '"
			+ outFile.getCanonicalPath() + "'.");
		System.exit(0);
	    }

	    // initialise the map list.
	    init();

	    // create the list of input files.
	    inFiles = new ArrayList<File>(args.length - 1);
	    for(int i = 1; i < args.length; i++) {
		File inputFile = new File(args[i]);
		
		// ensure the current input file existence and is readable.
		if(!inputFile.exists()) {
		    System.out.println("The input file at '" + inputFile.getPath() 
			    + "' does not exist.");
		    System.exit(0);
		}
		if(!inputFile.canRead()) {
		    System.out.println("The input file at '" + inputFile.getPath()
			    + "' is not readable.");
		    System.exit(0);
		}

		// add to the list of input files.
		inFiles.add(inputFile);
	    }
	    
	    // Handle the input files.
	    for(File inputFile : inFiles) {
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
	    mapList.add(validMap);
	    mapList.add(wellformedMap);
	    mapList.add(formatMap);
	    mapList.add(versionMap);
	    mapList.add(moduleMap);
	    mapList.add(mimetypeMap);
	    mapList.add(dateMap);
	    mapList.add(validFormatMap);
	    mapList.add(validMimetypeMap);
	    mapList.add(validModuleMap);
	    mapList.add(validDateMap);
	    mapList.add(validWellformedMap);
	    mapList.add(wellformedFormatMap);
	    mapList.add(wellformedMimetypeMap);
	    mapList.add(wellformedModuleMap);
	    mapList.add(wellformedDateMap);
	    mapList.add(wellformedValidMap);
	    
	    mapList.add(invalidMimetypeMap);
	    mapList.add(unhandledMimetypeMap);
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
     * Method for handling a file.
     * Each line in the file is read and handled. 
     * 
     * @param f The file to handle.
     */
    private static void handleFile(File f) {
	try {
	    FileReader fr = new FileReader(f);
	    LineNumberReader lnr = new LineNumberReader(fr);
	    
	    String line = lnr.readLine();
	    
	    while(line != null) {
		handleLine(line);
		line = lnr.readLine();
	    }
	    	    
	} catch (IOException e) {
	    System.err.println("The file '" + f.getPath() 
		    + "' could not be handled.");
	}
    }
    
    /**
     * Method for handling a line.
     * Each of the seven arguments are retrieved and put into their 
     * corresponding map.
     * 
     * @param line The line to handle.
     */
    private static void handleLine(String line) {
	String[] lineContent = line.split(SEPARATOR);
	if(lineContent.length != 7) {
	    System.err.println("The line '" + line + "' does not have enough"
		    + " parameters. It should have: "
		    + "Valid##Wellformed##Format##Version##Module##Mimetype##Date");
	    return;
	}
	
	lineEntryCount++;
	
	// extract the arguments from the line.
	String valid = lineContent[0];
	String wellformed = lineContent[1];
	String format = lineContent[2];
	String version = lineContent[3];
	String module = lineContent[4];
	String mimetype = lineContent[5];
	// retrieve only the year, month and day part of the date (ignore the
	// hour and minute etc.).
	String usedDate = lineContent[6];
	if(usedDate.length() > 8) {
	    usedDate = usedDate.substring(0, 8);
	}
	
	// extract the data and put it into relative maps.
	handleMap(valid, validMap);
	handleMap(wellformed, wellformedMap);
	handleMap(format, formatMap);
	handleMap(version, versionMap);
	handleMap(module, moduleMap);
	handleMap(mimetype, mimetypeMap);
	handleMap(usedDate, dateMap);
	
	// -----------------------
	// Handle other maps
	// -----------------------
	// Put format into valid format map, if valid
	if(valid.equalsIgnoreCase("valid")) {
	    handleMap(format, validFormatMap);
	    handleMap(mimetype, validMimetypeMap);
	    handleMap(module, validModuleMap);
	    handleMap(usedDate, validDateMap);
	    handleMap(wellformed, validWellformedMap);
	} else if(valid.equalsIgnoreCase("invalid")) {
	    handleMap(mimetype, invalidMimetypeMap);
	} else {
	    handleMap(mimetype, unhandledMimetypeMap);
	}
	
	// put format into wellformed format map, if wellformed.
	if(wellformed.equalsIgnoreCase("wellformed")) {
	    handleMap(format, wellformedFormatMap);
	    handleMap(mimetype, wellformedMimetypeMap);
	    handleMap(module, wellformedModuleMap);
	    handleMap(usedDate, wellformedDateMap);
	    handleMap(valid, wellformedValidMap);
	}
    }
    
    /**
     * If the map contains the string as a key, then the integer is increased.
     * Else the key is added to the map.
     * 
     * @param s The string, which is key in the map.
     * @param m The map.
     */
    private static void handleMap(String s, HandlerMap<String, Integer> m) {
	if(m.containsKey(s)) {
	    // Extract the integer and increment it and put it back in.
	    Integer i = m.get(s);
	    m.put(s, i + 1);
	} else {
	    m.put(s, 1);
	}
    }
}
