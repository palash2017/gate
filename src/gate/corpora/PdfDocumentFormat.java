/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  PdfDocumentFormat.java
 *
 *  Ting Wang, Valentin Tablan, 14-Feb-2005, 
 *
 *  $Id$
 */

package gate.corpora;

import java.net.URL;
import gate.*;
import gate.Document;
import gate.DocumentFormat;
import gate.creole.ResourceInstantiationException;
import gate.util.DocumentFormatException;

import org.pdfbox.pdfparser.*;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.*;
import java.io.*;

/**
 */
public class PdfDocumentFormat extends DocumentFormat{


  /**
   * Initialise this resource, and return it.
   * Registers this format unpacker with the system.
   */
  public Resource init() throws ResourceInstantiationException{
    // Register plain text mime type
    MimeType mime = new MimeType("application","pdf");
    // Register the class handler for this mime type
    mimeString2ClassHandlerMap.put(mime.getType()+ "/" + mime.getSubtype(),
                                                                          this);
    // Register the mime type with mine string
    mimeString2mimeTypeMap.put(mime.getType() + "/" + mime.getSubtype(), mime);
    // Register file sufixes for this mime type
    suffixes2mimeTypeMap.put("pdf",mime);
    // Set the mimeType for this language resource
    setMimeType(mime);
    return this;
  } // init()


  /**
   * The PDF Document Format does not support repositioning info.
   * @return false.
   */
  public Boolean supportsRepositioning() {
    return new Boolean(false);
  } // supportsRepositioning


  /** Unpack the markup in the document. This converts markup from the
   * native format (e.g. XML, RTF) into annotations in GATE format.
   * Uses the markupElementsMap to determine which elements to convert, and
   * what annotation type names to use.
   */
  public void unpackMarkup(Document doc) throws DocumentFormatException{
    //get the original file
    URL fileURL = doc.getSourceUrl();
    if(fileURL == null) throw new DocumentFormatException(
            "Unpacking PDF files requires an URL to the original content!");
    
    //Implement the PDF unpacking.
    try {
      // get an Input stream from the gate document
      InputStream in = fileURL.openStream();
      // create a PDF Text Stripper
      PDFTextStripper pdfStripper = new PDFTextStripper();
      
      PDDocument document = PDDocument.load(in);
      
      String extractedContent = pdfStripper.getText(document);
      
      document.close();
      in.close();
      //set the content on the document
      doc.setContent(new DocumentContentImpl(extractedContent));
    } catch (IOException e){
      throw new DocumentFormatException("I/O exception for " +
                                        doc.getSourceUrl().toExternalForm(), 
                                        e);
    }
  }


  public void unpackMarkup(Document doc, RepositioningInfo repInfo,
                                       RepositioningInfo ampCodingInfo)
                                     throws DocumentFormatException{
    unpackMarkup(doc);
  }


}
