package batchprogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import eu.planets.outputHandler.HandlerMap;

public class MimeSizeHandler {
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
        HandlerMap<String, Long> mimetypeSizeMap 
                = new HandlerMap<String, Long>("mimetype-size");
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        
        String line;
        while((line = br.readLine()) != null) {
            try {
                // Ignore empty lines
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] split = line.split("#");

                // Extract the variables.
                String mimetype = split[0].toLowerCase();
                Long size = Long.parseLong(split[split.length - 1]);
                
                // Put into the maps.
                handleMap(mimetype, mimetypeMap);
                handleMap(mimetype, size, mimetypeSizeMap);
            } catch (Exception e) {
                System.out.println("Bad line: '" + line + "' gave error: "
                        + e.getMessage());
            } finally {
                IOUtils.closeQuietly(br);
            }
            
        }
        
        System.out.println(mimetypeMap.toString());
        System.out.println();
        System.out.println();
        System.out.println(mimetypeSizeMap.toString());
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
    
    private static void handleMap(String s, Long l, HandlerMap<String, Long> m) {
        if(m.containsKey(s)) {
            Long count = m.get(s);
            m.put(s, count+l);
        } else {
            m.put(s, l);
        }
    }
}
