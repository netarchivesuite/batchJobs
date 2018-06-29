package eu.planets;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import dk.netarkivet.common.exceptions.ArgumentNotValid;
import dk.netarkivet.common.exceptions.IOFailure;
import dk.netarkivet.common.exceptions.IllegalState;
import dk.netarkivet.common.utils.FileUtils;
import dk.netarkivet.deploy.XmlStructure;

/**
 * Class for handling the batch configuration.
 * Creates the script for distribution and running the batch job, 
 * based on a configuration file.
 */
public abstract class BatchConfig {
    /** The configuration file.*/
    protected XmlStructure configuration;
    /** The settings from the configuration file.*/
    protected XmlStructure settings;
    /** The directory to run the batch job upon.*/
    protected List<File> dirs;
    /** The pattern for the files within the directories.*/
    protected String regexPattern;
    /** Whether the sub-directories should be run upon.*/
    protected boolean subDir;
    /** The output file.*/
    protected String outputFileName;
    /** The error file.*/
    protected String errorFileName;
    /** The machines connected to this instance.*/
    protected List<Machine> macs;
    /** The local directory for the position of the scripts.*/
    protected File localDirectory;
    /** The jar file. Only used for running LoadableJarBatchJob.*/
    protected String jarFile;
    /** The class name. Used as class name for JarBatchJob and class file for 
     * LoadableFileBatchJob.*/
    protected String className;
    /** The batch package, which are to be used as argument when 
     * running the batch job.*/
    protected File batchPackage;
    /** Tells whether this instance has been initialised.*/
    protected boolean initialised;
    
    /** 
     * Constructor.
     * 
     * The BatchConfig has to be initialised before any usage.
     */
    public BatchConfig() { 
        initialised = false;

//      System.out.println("Making instance of: " + this.getClass().getName());
    }
    
    /**
     * The initialiser for instance with jar file and class method, 
     * not only a class file.
     * 
     * @param config The configuration file.
     * @param batchPack The package file for the batch module.
     * @param jarBatchFile The jar file.
     * @param classname The name of the class within the jar file.
     */
    public void initialise(File config, File batchPack, String jarBatchFile, 
            String classname) {
        ArgumentNotValid.checkNotNull(config, "File config");
        ArgumentNotValid.checkNotNull(batchPack, "File batchPack");
        ArgumentNotValid.checkNotNullOrEmpty(jarBatchFile, 
                "String jarBatchFile");
        ArgumentNotValid.checkNotNullOrEmpty(classname, "String classpath");

        // make sure, that it has not been initialised before.
        if(initialised){
            throw new IllegalState("ALREADY INITIALISED!");
        }
        initialised = true;
        // TODO the encoding "UTF-8" is not tested. This is just added to make it compile
        this.configuration = new XmlStructure(config, "UTF-8");
        this.jarFile = jarBatchFile;
        this.className = classname;
        this.batchPackage = batchPack;

        initVariables();
    }
    
    /**
     * Initialiser. Batch job with class file (not jar).
     * 
     * @param config The configuration file.
     * @param batchPack The package file for the batch module.
     * @param classfile The name of the class file.
     */
    public void initialise(File config, File batchPack, String classfile) {
        ArgumentNotValid.checkNotNull(config, "File config");
        ArgumentNotValid.checkNotNull(batchPack, "File batch");
        ArgumentNotValid.checkNotNullOrEmpty(classfile, "String classpath");

        // make sure, that it has not been initialised before.
        if(initialised){
            throw new IllegalState("ALREADY INITIALISED!");
        }
        initialised = true;
        // TODO the encoding "UTF-8" is not tested. This is just added to make it compile
        this.configuration = new XmlStructure(config, "UTF-8");
        this.jarFile = null;
        this.className = classfile;
        this.batchPackage = batchPack;

        initVariables();
    }
    
    /**
     * Function for initialising the variables, based on the content of 
     * the configuration file.
     */
    private void initVariables() {
        // Load the settings from the configuration.
        settings = new XmlStructure(configuration.getChild(
                Constants.COMPLETE_SETTINGS_PATH));

        // variable for retrieving the content of the settings.
        String[] options;

        // initialise the output file.
        options = settings.getLeafValues(Constants.SETTINGS_OUTPUT_FILE_LEAF);
        if(options == null || options.length < 1) {
            outputFileName = null;
        } else {
            outputFileName = options[0];
            // make warning if more than one output file defined.
            if(options.length > 1) {
                System.out.println("WARNING! outputFileName : "
                        + outputFileName);
            }
        }
        
        // initialise the error file.
        options = settings.getLeafValues(Constants.SETTINGS_ERROR_FILE_LEAF);
        if(options == null || options.length < 1) {
            errorFileName = null;
        } else {
            errorFileName = options[0];
            // make warning if more than one error file defined.
            if(options.length > 1) {
                System.out.println("WARNING! errorFileName : "
                        + errorFileName);
            }
        }
        
        // initialise the pattern.
        options = settings.getLeafValues(Constants.SETTINGS_PATTERN_LEAF);
        if(options == null || options.length < 1) {
            regexPattern = null;
        } else {
            regexPattern = options[0];
            // make warning if more than one pattern defined.
            if(options.length > 1) {
                System.out.println("WARNING! regexPattern: "
                        + regexPattern);
            }
        }
        
        // initialise the sub-directory.
        options = settings.getLeafValues(Constants.SETTINGS_SUBDIR_LEAF);
        if(options == null || options.length < 1) {
            subDir = false;
        } else {
            subDir = true;
        }

        // Load the list of directories
        options = configuration.getLeafValues(
                Constants.COMPLETE_DIRECTORY_LEAFS);
        if(options == null || options.length < 1) {
            System.out.println(Constants.WARN_MSG_NO_DIRECTORIES);
            dirs = new ArrayList<File>(0);
        } else {
            dirs = new ArrayList<File>(options.length);
            for(String dirName : options) {
                File repository = new File(dirName);
                if(!repository.exists() || !repository.isDirectory()) {
                    String msg = Constants
                         .ERROR_MSG_DIRECTORY_DOES_NOT_EXIST + dirName;
                    System.err.println(msg);
                    throw new IOFailure(msg);
                }
                dirs.add(repository);
            }
        }

        // Get machines
        List<Element> macEl = configuration.getChildren(
                Constants.COMPLETE_MACHINE_PATH);
        if(macEl == null || macEl.size() < 1) {
            System.out.println(Constants.WARN_MSG_NO_MACHINES);
            macs = new ArrayList<Machine>(0);
        } else {
            macs = new ArrayList<Machine>(macEl.size());
            // make either windows or linux machine, dependent on 'os' value.
            for(Element machine : macEl) {
                String os = machine.attributeValue(
                        Constants.ATTRIBUTE_MACHINE_OPERATING_SYSTEM);
                if(os != null && os.equals(Constants
                        .OPERATING_SYSTEM_WINDOWS)) {
                    // handle whether jar batch job or file batch job.
                    if(jarFile == null || jarFile.isEmpty()) {
                        macs.add(new MachineWindows(machine, settings, 
                            batchPackage, className));
                    } else {
                        macs.add(new MachineWindows(machine, settings, 
                            batchPackage, jarFile, className));
                    }
                } else {
                    // handle whether jar batch job or file batch job.
                    if(jarFile == null || jarFile.isEmpty()) {
                        macs.add(new MachineLinux(machine, settings, 
                            batchPackage, className));
                    } else {
                        macs.add(new MachineLinux(machine, settings, 
                            batchPackage, jarFile, className));
                    }
                }
            }
        }

        // error if neither machines nor local directories are defined.
        if(dirs.isEmpty() && macs.isEmpty()) {
            System.err.println(Constants.ERROR_MSG_NO_MACS_NOR_DIRS_DEF);
            System.out.println(this.toString());
            throw new IllegalState(Constants.ERROR_MSG_NO_MACS_NOR_DIRS_DEF);
        }
        
        // get the local output directory for the scripts.
        String localDir = settings.getLeafValue(
                Constants.SETTINGS_SCRIPT_DIR_LEAF);

        // make sure, that local dir exists
        if(localDir == null || localDir.isEmpty()) {
            localDir = Constants.DEFAULT_TMPDIR;
        }

        // create directory.
        localDirectory = new File(localDir);
        FileUtils.createDir(localDirectory);
    }
    
    /**
     * Creates the scripts for handling the batch job.
     */
    public void createScripts() {
        // cannot use this function if this instance has not been initialised.
        if(!initialised) {
            throw new IllegalState("Not initialised yet!");
        }

        // Create local script
        if(dirs != null && dirs.size() > 0) {
            createLocalStartScript(localDirectory);
        }

        // Create scripts to be distributed 
        for(Machine mac : macs) {
            mac.createScripts(localDirectory);
        }
        
        // Create script to distribute other scripts.
        createInstallScript(localDirectory);

        // Create script to start batch job, locally and distributed.
        createStartAllScript(localDirectory);
    }
    
    /**
     * Method for creating installation script.
     * Installs the batch package and the scripts on each machine.
     *  
     * @param outputDir The directory where the installation script
     * should be created.
     */
    protected abstract void createInstallScript(File outputDir);
    
    /**
     * Creates the script for running all 'start' scripts.
     * 
     * @param outputDir The directory, where the scripts are placed.
     */
    protected abstract void createStartAllScript(File outputDir);
    
    /**
     * Creates the script for running the batch job on each directory.
     * 
     * @param outputDir The output directory, where the scripts are placed.
     */
    protected abstract void createLocalStartScript(File outputDir);
}
