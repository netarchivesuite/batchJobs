package batchprogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import eu.planets.outputHandler.HandlerMap;

public class MimetypesHandler {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.err.println("Takes a single argument: "
                    + "The file to be handled!");
            System.exit(1);
        }
        
        File inputFile = new File(args[0]);
        
        if(!inputFile.isFile()) {
            System.err.println("The file '" + inputFile.getAbsolutePath() 
                    + "' is not valid!");
            System.exit(1);
        }
        
        HandlerMap<String, Integer> mimetypeMap 
                = new HandlerMap<String, Integer>("mimetypes");
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        
        String line;
        while((line = br.readLine()) != null) {
            handleMap(line.toLowerCase(), mimetypeMap);
        }
        
        System.out.println(mimetypeMap.toString());
        IOUtils.closeQuietly(br);
    }

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
