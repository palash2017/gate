/*
 *	TestEmail.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *	Cristian URSU,  7/Aug/2000
 *
 *	$Id$
 */

package gate.email;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.util.*;
import gate.gui.*;
import gate.email.*;

import junit.framework.*;
import org.w3c.www.mime.*;


/**
  * Test class for Email facilities
  */
public class TestEmail extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestEmail(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp


  public static void main(String args[]){
    TestEmail app = new TestEmail("TestEmail");
    try {
      app.testUnpackMarkup();
      app.testEmail();
    } catch (Exception e) {
      e.printStackTrace (Err.getPrintWriter());
    }
  }

  /** A test */
  public void testUnpackMarkup() throws Exception{
    assert(true);

    // create the markupElementsMap map
    Map markupElementsMap = null;
    gate.Document doc = null;
    Gate.init();
    doc = gate.Factory.newDocument(Gate.getUrl("tests/email/test.eml"));

    // get a document format that deals with e-mails
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat(
      doc, doc.getSourceUrl()
    );
    assert(docFormat instanceof gate.corpora.EmailDocumentFormat);
    //docFormat.unpackMarkup (doc,"DocumentContent");
    docFormat.unpackMarkup(doc);
  } // testUnpackMarkup()

  /**
    * final test
    */
  public void testEmail(){
    EmailDocumentHandler emailDocumentHandler = new EmailDocumentHandler();
    emailDocumentHandler.testSelf();
  }// testEmail

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestEmail.class);
  } // suite

} // class TestEmail