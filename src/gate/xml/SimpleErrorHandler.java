/*
 *	SimpleErrorHandle.java
 *
 *	Cristian URSU,  8/May/2000
 *  $id$
 */

package gate.xml;

import java.io.*;
import org.xml.sax.*;


public class SimpleErrorHandler implements ErrorHandler {
  /**
    * SimpleErrorHandler constructor comment.
    */
  public SimpleErrorHandler() {
	  super();
  }
  /**
    * error method comment.
    */
  public void error(SAXParseException ex) throws SAXException {
	  File fInput = new File (ex.getSystemId());
	  System.err.println("e: " + fInput.getPath() + ": line " + ex.getLineNumber() + ": " + ex);
    System.err.println("This is recoverable error. ");

  }
  /**
    * fatalError method comment.
    */
  public void fatalError(SAXParseException ex) throws SAXException {
	  File fInput = new File(ex.getSystemId());
	  System.err.println("E: " + fInput.getName() + ": line " + ex.getLineNumber() + ": " + ex);
    System.err.println("This is fatal error. ");
  }
  /**
    * warning method comment.
    */
  public void warning(SAXParseException ex) throws SAXException {
	  File fInput = new File(ex.getSystemId());
	  System.err.println("w: " + fInput.getName() + ": line " + ex.getLineNumber() + ": " + ex);
    System.err.println("This is just a warning. ");
  }
}