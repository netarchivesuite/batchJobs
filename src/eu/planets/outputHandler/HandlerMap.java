package eu.planets.outputHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for handling the output from batchjobs.
 * This should be of the type: <String, Integer>, where the String is the 
 * output type and the Integer is the count for that type.
 * 
 * @param <K> The keys.
 * @param <V> The values.
 */
public class HandlerMap<K, V> extends HashMap<K, V> {
    /**
     * The name of this map.
     */
    private String name;
    
    /**
     * Constructor.
     * 
     * @param name The name of this map.
     */
    public HandlerMap(String name) {
	super();
	
	this.name = name;
    }
    
    /**
     * Method for retrieving the name of this map.
     * 
     * @return The name of this map.
     */
    public String getName() {
	return name;
    }
    
    /**
     * Parses this map into a spreadsheet readable format.
     * Two columns separated by semicolon, ';'.
     * First line contains the name of the map, and an empty second column.
     * Then each entry in the map is presented, key in first column and value
     * in second column.
     * <br/>
     * <br/> E.g.: 
     * <br/> "myMap; "
     * <br/> "key1;value1"
     * <br/> "key2;value2"
     * <br/> ...
     * 
     * @return The map parsed into a spreadsheet readable format.
     */
    public String toString() {
	StringBuilder res = new StringBuilder();
	res.append(name + ";" + " " + "\n");
	
	for(Map.Entry<K, V> entry : entrySet()) {
	    res.append(entry.getKey() + ";" + entry.getValue() + "\n");
	}

	return res.toString();
    }
}
