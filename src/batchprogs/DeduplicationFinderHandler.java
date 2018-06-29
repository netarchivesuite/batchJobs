package batchprogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import eu.planets.outputHandler.HandlerMap;

public class DeduplicationFinderHandler {
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
        
        HandlerMap<String, Long> totalMap 
                = new HandlerMap<String, Long>("Total count");

        
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        
        String line;
        while((line = br.readLine()) != null) {
            try {
                String[] split = line.split(":");
                if(split.length != 2) {
                    continue;
                }
                
                if(split[0].contains("Total handled")) {
                    handleMap("Total handled", parseResults(split[1]), totalMap);
                } else if(split[0].contains("Duplicates found")){
                    handleMap("Duplicates found", parseResults(split[1]), totalMap);
                } else if(split[0].contains("Bytes total")){
                    handleMap("Bytes total", parseResults(split[1]), totalMap);
                } else if(split[0].contains("Bytes discarded")){
                    handleMap("Bytes discarded", parseResults(split[1]), totalMap);
                }
                // otherwise ignore!
                // System.out.println("ignored: " + line);

            } catch (Exception e) {
                System.out.println("Bad line: '" + line + "' gave error: "
                        + e.getMessage());
            }
        }
        // print out the results.
        System.out.println(totalMap.toString());
    }
    
    private static Long parseResults(String res) {
        String[] data = res.split(" ");
        boolean found = false;
        for(String s : data) {
            if(!found && !s.isEmpty()) {
                return Long.parseLong(s);
            }
        }
        
        return -1L;
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
