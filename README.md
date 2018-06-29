# batchJobs
Batchjobs for Netarchivesuite.
See https://sbforge.org/display/NASDOC54/Tools+in+the+Archive+Module#ToolsintheArchiveModule-dk.netarkivet.archive.tools.RunBatch
for information on how to run these batchjobs.

This project stems fra old CVS project 'projects/webarkivering/batch' on kb-prod-udv-001.kb.dk


## Building the jarpackages
You can build the different jarfiles here: AllBatchProgs-1.0.0.jar  BatchJobs-1.0.0.jar  BatchProgs-1.0.0.jar

These jarfiles are build with the following commands, respectively
```
ant allprogsjar

ant batchjobsjar

ant batchprogsjar
```

Furthermore the command
```
ant jarfiles
```
will make all these files.
