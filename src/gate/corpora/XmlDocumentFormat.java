/*
 *	XmlDocumentFormat.java
 *
 *	Cristian URSU, 26/May/2000
 *
 *	$Id$
 */

package gate.corpora;

import java.util.*;
import java.io.*;
import java.net.*;
import org.w3c.www.mime.*;
import gate.util.*;
import gate.*;

// xml tools
import javax.xml.parsers.*;
import org.xml.sax.*;

/** The format of Documents. Subclasses of DocumentFormat know about
  * particular MIME types and how to unpack the information in any
  * markup or formatting they contain into GATE annotations. Each MIME
  * type has its own subclass of DocumentFormat, e.g. XmlDocumentFormat,
  * RtfDocumentFormat, MpegDocumentFormat. These classes register themselves
  * with a static index residing here when they are constructed. Static
  * getDocumentFormat methods can then be used to get the appropriate
  * format class for a particular document.
  */
public class XmlDocumentFormat extends TextualDocumentFormat
{

  /** Default construction */
  public XmlDocumentFormat() { super(); }

  /** Construction with a map of what markup elements we want to
    * convert when doing unpackMarkup(), and what annotation types
    * to convert them to.
    */
  public XmlDocumentFormat(Map markupElementsMap) {
    super(markupElementsMap);
  } // construction with map

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */
  public void unpackMarkup(Document doc){
	  try {

		  // Get a parser factory.
		  SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		  // Set up the factory to create the appropriate type of parser

      // non validating one
		  saxParserFactory.setValidating(false);
      // non namesapace aware one
		  saxParserFactory.setNamespaceAware(false);

      // create it
		  SAXParser parser = saxParserFactory.newSAXParser();

      // use it
		  //parser.parse(new File("D:\\CURSU\\jw.xml"),
      //     new CustomDocumentHandler("file:///D://CURSU//jw.xml"));

      if (null != doc){
        // parse and construct the gate annotations
		    //parser.parse(new InputSource(in),new gate.xml.CustomDocumentHandler(doc));
        try{
          parser.parse(doc.getSourceURL().toString(),
                       new gate.xml.CustomDocumentHandler(doc, this.markupElementsMap));
        } catch (NullPointerException e){
          e.printStackTrace();

        }
      }
		  //parser.parse("http://www.dcs.shef.ac.uk/~cursu/xml/input/bnc.xml",
      //     new CustomDocumentHandler("http://www.dcs.shef.ac.uk/~cursu/xml/input/bnc.xml"));


	  } catch (Exception ex) {
		  System.err.println("Exception : " + ex);
      ex.printStackTrace();
		  //System.exit(2);
	  }
  }

} // class XmlDocumentFormat
