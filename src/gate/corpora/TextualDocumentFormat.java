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
    if (doc == null || doc.getContent() == null) return;
    // Create paragraph annotations in the default annotation set
    String annotSetName = null;
    int endOffset = doc.getContent().toString().length();
    int startOffset = 0;
    annotateParagraphs(doc,startOffset,endOffset,annotSetName);
  }//unpackMarkup

  /** This method annotates paragraphs in a GATE document. The investigated text
    * spans beetween start and end offsets and the paragraph annotations are
    * created in the annotSetName. If annotSetName is null then they are creted
    * in the default annotation set.
    * @param aDoc is the gate document on which the paragraph detection would
    *  be performed.If it is null or its content it's null then the method woul
    *  simply return doing nothing.
    * @param startOffset is the index  form the document content from which the
    * paragraph detection will start
    * @param endOffset is the offset where the detection will end.
    * @param annotSetName is the name of the set in which paragraph annotation
    * would be created.The annotation type created will be "paragraph"
    */
  public void annotateParagraphs(Document aDoc,int startOffset,int endOffset,
                            String annotSetName)throws DocumentFormatException{
    // Simply return if the document is null or its content
    if (aDoc == null || aDoc.getContent() == null) return;
    // Simply return if the start is > than the end
    if (startOffset > endOffset) return;
    // Decide where to put the newly detected annotations
    AnnotationSet annotSet = null;
    if (annotSetName == null)
      annotSet = aDoc.getAnnotations();
    else
      annotSet = aDoc.getAnnotations(annotSetName);
    // Extract the document content
    String content = aDoc.getContent().toString();
    // This is the offset marking the start of a para
    int startOffsetPara = startOffset;
    // This marks the ned of a para
    int endOffsetPara = endOffset;
    // The initial sate of the FSA
    int state = 1;
    // This field marks that a BR entity was read
    // A BR entity can be NL or NL CR, depending on the operating system (UNIX
    // or DOS)
    boolean readBR = false;
    int index = startOffset;
    while (index < endOffset){
      // Read the current char
      char ch = content.charAt(index);
      // Test if a BR entity was read
      if (ch =='\n'){
        readBR = true;
        // If \n is followed by a \r then advance the index in order to read a
        // BR entity
        while ((index+1 < endOffset) && (content.charAt(index+1) == '\r'))
          index ++;
      }// End if
      switch(state){
        // It is the initial and also a final state
        // Stay in state 1 while it reads whitespaces
        case 1:{
          // If reads a non whitespace char then move to state 2 and record
          // the beggining of a paragraph
          if (!Character.isWhitespace(ch)){
            state = 2;
            startOffsetPara = index;
          }// End if
        }break;
        // It can be also a final state.
        case 2:{
          // Stay in state 2 while reading chars != BR entities
          if (readBR){
            // If you find a BR char go to state 3. The possible end of the para
            // can be index. This will be confirmed by state 3. So, this is why
            // the end of a para is recorded here.
            readBR = false;
            endOffsetPara = index;
            state = 3;
          }// End if
        }break;
        // It can be also a final state
        // From state 3 there are only 2 possible ways: (state 2 or state1)
        // In state 1 it needs to read a BR
        // For state 2 it nead to read something different then a BR
        case 3:{
          if (readBR){
            // A BR was read. Go to state 1
            readBR = false;
            state = 1;
            // Create an annotation type paragraph
            try{
              annotSet.add( new Long(startOffsetPara),
                            new Long(endOffsetPara),
                            "paragraph",
                            Factory.newFeatureMap());
            } catch (gate.util.InvalidOffsetException ioe){
              throw new DocumentFormatException("Coudn't create a paragraph"+
              " annotation",ioe);
            }// End try
          }else{
            // Go to state 2 an keep reading chars
            state = 2;
          }// End if
        }break;
      }// End switch
      // Prepare to read the next char.
      index ++;
    }// End while
    endOffsetPara = index;
    // Investigate where the finite automata has stoped
    if ( state==2 || state==3 ){
      // Create an annotation type paragraph
      try{
        annotSet.add( new Long(startOffsetPara),
                      // Create the final annotation using the endOffset
                      new Long(endOffsetPara),
                      "paragraph",
                      Factory.newFeatureMap());
      } catch (gate.util.InvalidOffsetException ioe){
              throw new DocumentFormatException("Coudn't create a paragraph"+
              " annotation",ioe);
      }// End try
    }// End if
  }// End annotateParagraphs();

  public void unpackMarkup(Document doc,
                                    String  originalContentFeatureType)
                                              throws DocumentFormatException{
     FeatureMap fm = doc.getFeatures ();

     if (fm == null)
        fm = Factory.newFeatureMap();

     fm.put(originalContentFeatureType, doc.getContent().toString());
     doc.setFeatures(fm);
     unpackMarkup(doc);
  }//unpackMarkup
  public DataStore getDataStore(){ return null;}

} // class TextualDocumentFormat
