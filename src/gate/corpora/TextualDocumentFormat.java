/*
 *  TextualDocumentFormat.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 26/May/2000
 *
 *  $Id$
 */

package gate.corpora;

import java.util.*;
import java.net.*;

import gate.util.*;
import gate.*;
import gate.creole.*;

import org.w3c.www.mime.*;

/** The format of Documents. Subclasses of DocumentFormat know about
  * particular MIME types and how to unpack the information in any
  * markup or formatting they contain into GATE annotations. Each MIME
  * type has its own subclass of DocumentFormat, e.g. XmlDocumentFormat,
  * RtfDocumentFormat, MpegDocumentFormat. These classes register themselves
  * with a static index residing here when they are constructed. Static
  * getDocumentFormat methods can then be used to get the appropriate
  * format class for a particular document.
  */
public class TextualDocumentFormat extends DocumentFormat
{

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Default construction */
  public TextualDocumentFormat() { super(); }

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException{
    // Register plain text mime type
    MimeType mime = new MimeType("text","plain");
    // Register the class handler for this mime type
    mimeString2ClassHandlerMap.put(mime.getType()+ "/" + mime.getSubtype(),
                                                                          this);
    // Register the mime type with mine string
    mimeString2mimeTypeMap.put(mime.getType() + "/" + mime.getSubtype(), mime);
    // Register file sufixes for this mime type
    suffixes2mimeTypeMap.put("txt",mime);
    suffixes2mimeTypeMap.put("text",mime);
    // Set the mimeType for this language resource
    setMimeType(mime);
    return this;
  } // init()

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */
  public void unpackMarkup(Document doc) throws DocumentFormatException{
  }//unpackMarkup

  public void unpackMarkup(Document doc,
                                    String  originalContentFeatureType)
                                              throws DocumentFormatException{

  }//unpackMarkup
  public DataStore getDataStore(){ return null;}

} // class TextualDocumentFormat
