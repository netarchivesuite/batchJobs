/* $Id: JhoveBatchJob.java,v 1.3 2009/09/17 10:08:40 jolf Exp $
 * $Date: 2009/09/17 10:08:40 $
 * $Revision: 1.3 $
 * $Author: jolf $
 *
 * The Netarchive Suite - Software to harvest and preserve websites
 * Copyright 2004-2007 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package eu.planets.batchprogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import dk.netarkivet.common.utils.batch.FileBatchJob;
import edu.harvard.hul.ois.jhove.Module;
import edu.harvard.hul.ois.jhove.RepInfo;
import edu.harvard.hul.ois.jhove.module.AiffModule;
import edu.harvard.hul.ois.jhove.module.GifModule;
import edu.harvard.hul.ois.jhove.module.HtmlModule;
import edu.harvard.hul.ois.jhove.module.Jpeg2000Module;
import edu.harvard.hul.ois.jhove.module.JpegModule;
import edu.harvard.hul.ois.jhove.module.PdfModule;
import edu.harvard.hul.ois.jhove.module.TiffModule;
import edu.harvard.hul.ois.jhove.module.WaveModule;
import edu.harvard.hul.ois.jhove.module.XmlModule;

/**
 * 
 * @author jolf
 *
 * Delivers the output in the format:
 * valid + ## + wellformed + ## + format + ## + version + ## + mod-name 
 * + ## + mimetype / extension + ## + harvest-date \n
 */
public class JhoveBatchJob extends FileBatchJob {

    private static final String STREAM_SEPARATOR = Constants.STREAM_SEPARATOR;
    private static final String STREAM_DONE = Constants.STREAM_DONE;

    private static Module aifMod;
    private static Module gifMod;
    private static Module htmlMod;
    private static Module jpegMod;
    private static Module jpeg2000Mod;
    private static Module pdfMod;
    private static Module tiffMod;
    private static Module waveMod;
    private static Module xmlMod;
    
    @Override
    public void finish(OutputStream os) {
        // TODO Auto-generated method stub
    }

    @Override
    public void initialize(OutputStream os) {
	aifMod = new AiffModule();
	gifMod = new GifModule();
	htmlMod = new HtmlModule();
	jpegMod = new JpegModule();
	jpeg2000Mod = new Jpeg2000Module();
	pdfMod = new PdfModule();
	tiffMod = new TiffModule();
	waveMod = new WaveModule();
	xmlMod = new XmlModule();
    }

    @Override
    public boolean processFile(File file, OutputStream os) {
        try {
            // ignore records larger than 100 MB
            if(file.length() > 100000000) {
                // valid + ## + wellformed + ## + format + ## + version + ## +  
                // mod-name + ## + arc-mimetype + ## + harvest-date \n
        	os.write(new String(
        		"Too Big" + STREAM_SEPARATOR 
            	    + "null" + STREAM_SEPARATOR 
        	    + "null" + STREAM_SEPARATOR 
        	    + "null" + STREAM_SEPARATOR
        	    + "null" + STREAM_SEPARATOR
                    + file.getName() + STREAM_SEPARATOR
                    + file.lastModified() + STREAM_DONE).getBytes());
        	return true;
            }

            // load file, initialize representation tool and an output handler
            FileInputStream fis = new FileInputStream(file);
            RepInfo repinfo = new RepInfo(file.getName());

            // retrieve the extension of the file.
            String[] split = file.getName().split("[.]");
            String extension = split[ split.length - 1 ]; 

            // find a module from the extension (in lower case)
            Module mod = FindModule(extension.toLowerCase());
            
            // check if a module was found
            if (mod == null) {
                    os.write(new String(
                	    "null" + STREAM_SEPARATOR 
                	    + "null" + STREAM_SEPARATOR 
                	    + "null" + STREAM_SEPARATOR 
                	    + "null" + STREAM_SEPARATOR
                	    + "null" + STREAM_SEPARATOR
                            + extension + STREAM_SEPARATOR
                            + file.lastModified() + STREAM_DONE).getBytes());
                    return true;
            }

            // reset module.
            mod.resetParams();

            // parse file
            mod.parse(fis, repinfo, 0);

            // write out results in the string format:
            // valid + ## + wellformed + ## + format + ## + version + ## +  
            // mod-name + ## + rep-mimetype + ## + 'last modified'-date \n
            os.write(new String((repinfo.getValid() == RepInfo.TRUE ? "Valid" : "Invalid")
        	    + STREAM_SEPARATOR 
        	    + (repinfo.getWellFormed() == RepInfo.TRUE ? "Wellformed" : "Not wellformed")
        	    + STREAM_SEPARATOR + repinfo.getFormat()
        	    + STREAM_SEPARATOR + repinfo.getVersion()
        	    + STREAM_SEPARATOR + mod.getName()
        	    + STREAM_SEPARATOR + repinfo.getMimeType()
        	    + STREAM_SEPARATOR + file.lastModified()
        	    + STREAM_DONE).getBytes());

            return true;
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + file.getName() 
        	    + "\n" + e);
            return false;
        } catch (IOException e) {
            System.err.println("Input/output error: " + file.getName() 
        	    + "\n" + e);
            return false;
        } catch (Exception e) {
            System.err.println("Unknown error: " + file.getName() 
        	    + "\n" + e);
            return false;
        }
    }

    /**
     * This function makes a module corresponding to the extension of a file.
     * If unknown extension, return null.
     * 
     * Modules | Extensions
     * AiffModule:  aiff, aif, aifc
     * GifModule:   gif, gfa
     * HtmlModule:  htm, html
     * JpegModule:  jpg, jpeg, jpe, jif, jfif, jfi
     * Jpeg2000Module: jp2, j2c, jpc, j2k, jpx
     * PdfModule:  pdf, epdf
     * TiffModule: tiff, tif
     * WaveModule: wav
     * XmlModule:  xml
     * 
     * @param ext The extension to the name of the file.
     * @return The module corresponding to the extension of the filename.
     */
    private Module FindModule(String ext) {
        if (ext.equals("aiff") || ext.equals("aif") || ext.equals("aifc")) {
            return aifMod;
        } 
        if (ext.equals("gif") || ext.equals("gfa")) {
            return gifMod;
        } 
        if (ext.equals("html") || ext.equals("htm")) {
            return htmlMod;
        } 
        if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("jpe") 
        	|| ext.equals("jif") || ext.equals("jfif") 
        	|| ext.equals("jfi")) {
            return jpegMod;
        } 
        if (ext.equals("jp2") || ext.equals("j2c") || ext.equals("jpc") 
        	|| ext.equals("j2k") || ext.equals("jpx")) {
            return jpeg2000Mod;
        } 
        if (ext.equals("pdf") || ext.equals("epdf")) {
            return pdfMod;
        } 
        if (ext.equals("tiff") || ext.equals("tif")) {
            return tiffMod;
        } 
        if (ext.equals("wav")) {
            return waveMod;
        } 
        if (ext.equals("xml")) {
            return xmlMod;
        }

        return null;
    }
}
