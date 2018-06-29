/* $Id: JhoveArcJob.java,v 1.5 2009/09/17 10:08:40 jolf Exp $
 * $Date: 2009/09/17 10:08:40 $
 * $Revision: 1.5 $
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

import java.io.OutputStream;

import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;

import dk.netarkivet.common.utils.arc.ARCBatchJob;
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
 * Delivers the output in the format:
 * valid + ## + wellformed + ## + format + ## + version + ## + mod-name 
 * + ## + mimetype + ## + harvest-date \n
 *
 */
public class JhoveArcJob extends ARCBatchJob {
    private static final String STREAM_SEPARATOR = "##";
    private static final String STREAM_DONE = "\n";

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
        // TODO Auto-generated method stub
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
    public void processRecord(ARCRecord record, OutputStream os) {
        try {
            ARCRecordMetaData arcRMD = record.getMetaData();
            
            // ignore records larger than 100 MB
            if(arcRMD.getLength() > 100000000) {
                // valid + ## + wellformed + ## + format + ## + version + ## +  
                // mod-name + ## + arc-mimetype + ## + harvest-date \n
        	os.write(new String(
        		"Too Big" + STREAM_SEPARATOR 
            	    + "null" + STREAM_SEPARATOR 
        	    + "null" + STREAM_SEPARATOR 
        	    + "null" + STREAM_SEPARATOR
        	    + "null" + STREAM_SEPARATOR
                    + arcRMD.getMimetype() + STREAM_SEPARATOR
                    + arcRMD.getDate() + STREAM_DONE).getBytes());
        	return;
            }
            
            // RepInfo = the representation information. 
            // E.g. the results of an identification.
            RepInfo repinfo = new RepInfo(arcRMD.getUrl());
            
            // find file format from url-extension.
            Module mod = FindModuleFromUrl(arcRMD.getUrl());

            // if no proper url-extension, try mimetype
            if (mod == null) {
                mod = FindModuleFromMimetype(arcRMD.getMimetype());

                if (mod == null) {
                    // valid + ## + wellformed + ## + format + ## + version + ## +  
                    // mod-name + ## + arc-mimetype + ## + harvest-date \n
                    os.write(new String(
                	    "null" + STREAM_SEPARATOR 
                	    + "null" + STREAM_SEPARATOR 
                	    + "null" + STREAM_SEPARATOR 
                	    + "null" + STREAM_SEPARATOR
                	    + "null" + STREAM_SEPARATOR
                            + arcRMD.getMimetype() + STREAM_SEPARATOR
                            + arcRMD.getDate() + STREAM_DONE).getBytes());
                    return;
                }
            }

            // Is this necessary?
            mod.resetParams();
            // validate the arc-record
            mod.checkSignatures(null, record, repinfo);

            // write out results in the string format:
            // valid + ## + wellformed + ## + format + ## + version + ## +  
            // mod-name + ## + rep-mimetype + ## + harvest-date \n
            os.write(new String((repinfo.getValid() == RepInfo.TRUE ? 
        	    "Valid" : "Invalid") + STREAM_SEPARATOR 
        	    + (repinfo.getWellFormed() == RepInfo.TRUE ? "Wellformed" 
        		    : "Not wellformed")
        	    + STREAM_SEPARATOR + repinfo.getFormat()
        	    + STREAM_SEPARATOR + repinfo.getVersion()
        	    + STREAM_SEPARATOR + mod.getName()
        	    + STREAM_SEPARATOR + repinfo.getMimeType()
        	    + STREAM_SEPARATOR + arcRMD.getDate()
        	    + STREAM_DONE).getBytes());
        } catch (Exception e) {
            // Handle the exception. Send it to error stream.
            System.err.println("Error: " + e);
            return;
        }
    }


    /**
     * This function finds a module corresponding to the extension of a url.
     * If unknown extension, return null.
     * 
     * Modules | Extensions
     * AiffModule: aiff, aif, aifc
     * GifModule: gif, gfa
     * HtmlModule: htm, html
     * JpegModule: jpg, jpeg, jpe, jif, jfif, jfi
     * Jpeg2000Module: jp2, j2c, jpc, j2k, jpx
     * PdfModule: pdf, epdf
     * TiffModule:tiff, tif
     * WaveModule:wav
     * XmlModule: xml
     * 
     * @param url The url of the file
     * @return The module corresponding to the extension of the filename
     */
    private Module FindModuleFromUrl(String url) {

        if (url.endsWith(".aiff") || url.endsWith(".aif")
                || url.endsWith(".aifc")) {
            return aifMod;
        } 
        if (url.endsWith(".gif") || url.endsWith(".gfa")) {
            return gifMod;
        } 
        if (url.endsWith(".html") || url.endsWith(".htm")) {
            return htmlMod;
        } 
        if (url.endsWith(".jpg") || url.endsWith(".jpeg")
                || url.endsWith(".jpe") || url.endsWith(".jif")
                || url.endsWith(".jfif") || url.endsWith(".jfi")) {
            return jpegMod;
        } 
        if (url.endsWith(".jp2") || url.endsWith(".j2c")
                || url.endsWith(".jpc") || url.endsWith(".j2k")
                || url.endsWith(".jpx")) {
            return jpeg2000Mod;
        } 
        if (url.endsWith(".pdf") || url.endsWith(".epdf")) {
            return pdfMod;
        } 
        if (url.endsWith(".tiff") || url.endsWith(".tif")) {
            return tiffMod;
        } 
        if (url.endsWith(".wav")) {
            return waveMod;
        } 
        if (url.endsWith(".xml")) {
            return xmlMod;
        }

        return null;
    }

    /**
     * This function finds a module corresponding to the mimetype.
     * If unknown extension, return null.
     * 
     * Modules |        Mimetype
     * AiffModule:      N/A
     * GifModule:       image/gif
     * HtmlModule:      text/html
     * JpegModule:      image/jpeg
     * Jpeg2000Module:  N/A
     * PdfModule:       application/pdf
     * TiffModule:      N/A
     * WaveModule:      audio/x-wav
     * XmlModule:       text/xml, application/xml
     * 		suffix: '-xml'
     * 		* (application/atom+xml, application/rss+xml, 
     * 			application/xml...)
     *          * XML mimetypes ends with 'xml'.
     * 
     * Mimetype found but not handled by individual module:
     * text/plain
     * text/dns
     * text/css
     * text/javascript
     * image/png
     * image/x-icon
     * application/x-javascript
     * application/vnd.ms-powerpoint
     * application/x-cdx
     * application/x-shockwave-flash
     * application/x-component
     * application/json
     * application/x-gzip
     * application/x-java-archive
     * 
     * @param name The name of the file
     * @return The module corresponding to the extension of the filename.
     * If no module was found, then null is returned.
     */
    private Module FindModuleFromMimetype(String mimetype){

        if (mimetype.equalsIgnoreCase("text/html")) {
            return htmlMod;
        } 
        if (mimetype.equalsIgnoreCase("image/jpeg")) {
            return jpegMod;
        } 
        if (mimetype.equalsIgnoreCase("image/gif")) {
            return gifMod;
        } 
        if (mimetype.equalsIgnoreCase("application/pdf")) {
            return pdfMod;
        } 
        if (mimetype.equalsIgnoreCase("audio/x-wav")) {
            return waveMod;
        } 
        // The mimetype of XML files ends with 'xml'.
        if (mimetype.endsWith("xml")) {
            return xmlMod;
        }

        return null;
    }
}
