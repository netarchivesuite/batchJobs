import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class LogPrinter {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        
        File directory;
        List<File> files = new ArrayList<File>();
        int linesPerSec = 10;
        
        if(args.length < 1) {
            System.err.println("This application takes a directory containing "
                    + "log files as argument.");
            System.exit(1);
        }
        if(args.length > 2) {
            System.out.println("This application can only handle one argument."
                    + " The others are ignored.");
        }
        if(args.length == 2) {
            linesPerSec = Integer.valueOf(args[1]);
        }
        directory = new File(args[0]);
        
        if(!directory.exists()) {
            System.err.println("The directory '" + directory.getAbsolutePath()
                    + "' does not exist.");
            System.exit(1);
        }
        if(!directory.isDirectory()) {
            System.err.println("The path '" + directory.getAbsolutePath()
                    + "' does not point to a directory but a file.");
            System.exit(1);
        }
        
        for(File fil : directory.listFiles()) {
            if(fil.exists() && fil.isFile()) {
                files.add(fil);
            }
        }
        
        while(true) {
            for(File fil : files) {
                try {
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new FileReader(fil));
                        String line = br.readLine();
                        while((line = br.readLine()) != null) {
                            waitCount(linesPerSec);
                            System.out.println(line);
                        }
                    } finally {
                        if(br != null) {
                            br.close();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Skipping file '" + fil.getPath() + "'"
                            + " because " + e.getClass().getName());
                    continue;
                }
            }
        }
    }

    private static void waitCount(int waitTime) {
        int total = 0;
        long time = new Date().getTime();
        while(time + (1000 / waitTime) > new Date().getTime()) {
            for(int i=0; i<100;i++) {
                total += i;
            }
        }
    }
}
