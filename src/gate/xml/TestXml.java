/*
 *	TestXml.java
 *
 *	Cristian URSU,  8/May/2000
 *
 *	$Id$
 */

package gate.xml;

import java.util.*;
import java.net.*;
import java.io.*;
import junit.framework.*;
import org.w3c.www.mime.*;
import gate.util.*;

/** Test class for XML facilities
  *
  */
public class TestXml extends TestCase
{
  /** Construction */
  public TestXml(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp


  public static void main(String args[]){
    TestXml app = new TestXml("TestXml");
    try{
      app.testSomething ();
    }catch (Exception e){
      e.printStackTrace (System.err);
    }
  }


  /** A test */
  public void testSomething() throws Exception{
    assert(true);

    // create the markupElementsMap map
    Map markupElementsMap = null;
    /*
    markupElementsMap = new HashMap();
    // populate it
    markupElementsMap.put ("S","Sentence");
    markupElementsMap.put ("s","Sentence");
    markupElementsMap.put ("W","Word");
    markupElementsMap.put ("w","Word");
    markupElementsMap.put ("p","Paragraph");
    markupElementsMap.put ("h1","Header 1");
    markupElementsMap.put ("H1","Header 1");
    markupElementsMap.put ("A","link");
    markupElementsMap.put ("a","link");
    */
    // create a new gate document
    //gate.Document doc = gate.Transients.newDocument(
    //          new URL("http://www.dcs.shef.ac.uk/~cursu/xml/input/bnc.xml")


    gate.Document doc = gate.Transients.newDocument(
             // new URL("http://redmires.dcs.shef.ac.uk/gate/tests/xml/xces/xces.xml")
             // new URL("http://www.dcs.shef.ac.uk/~cursu/xml/input/F8F.xml")
             // new URL("http://www.dcs.shef.ac.uk/~cursu/xml/input/bnc.xml")
                new URL("http://www.dcs.shef.ac.uk/~cursu/xml/input/xces/xces.xml")
             // new URL("http://redmires.dcs.shef.ac.uk/gate/tests/xml/bnc.xml")
    );


    /*
    gate.Document doc = gate.Transients.newDocument(
              new URL("http://www.dcs.shef.ac.uk/~cursu/xml/input/Sentence.xml")
    );
    */

    /*
    File f = Files.writeTempFile(Files.getResourceAsStream("texts/Sentence.xml"));
    URL u = f.toURL();
    gate.Document doc = gate.Transients.newDocument(u);
    f.delete ();
    */
   /*
    gate.Document doc = gate.Transients.newDocument(
      Files.getResourceAsString("texts/Sentence.xml")
    );
    */
    // get the docFormat that deals with it.
    // the parameter MimeType doesn't affect right now the behaviour
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat (
      new MimeType("text","xml")
    );

    // set's the map
    docFormat.setMarkupElementsMap(markupElementsMap);
    //*
    // timing the operation
    Date startTime = new Date();
      docFormat.unpackMarkup (doc,"DocumentContent");
    Date endTime = new Date();
    // get the size of the doc
    long  time1 = endTime.getTime () - startTime.getTime ();
    File f = Files.writeTempFile(doc.getSourceURL().openStream());
    long docSize = f.length();
    f.delete();
    System.out.println("unpacMarkup() time for " + doc.getSourceURL () + "(" +
      docSize/1024 + "." + docSize % 1024 + " K)" + "=" + time1 / 1000 + "." +
      time1 % 1000 + " sec," + " processing rate = " + docSize/time1*1000/1024 +
      "." + (docSize/time1*1000)%1024 + " K/second");
    //*/

    // graphic visualisation

    /*
    System.out.println("Timer started...");
    if (docFormat != null){
        // timing the operation
        Date startTime = new Date();

        docFormat.unpackMarkup (doc);

        Date endTime = new Date();
        long  time1 = endTime.getTime () - startTime.getTime ();
        System.out.println("unpacMarkup time for " + doc.getSourceURL () +
          ": " + time1 / 1000 + "." + time1 % 1000 + " seconds.");

        startTime = new Date();

        gate.jape.gui.JapeGUI japeGUI = new gate.jape.gui.JapeGUI();
        gate.Corpus corpus = gate.Transients.newCorpus("XML Test");
        corpus.add(doc);
        japeGUI.setCorpus(corpus);

        endTime = new Date();
        long time2 = endTime.getTime () - startTime.getTime ();
        System.out.println("Graphic initialization time : " + time2 / 1000 +
                            "." + time1 % 1000 + " seconds.");
        System.out.println("Total time : " + (time1 + time2) / 1000 + "." +
                            (time1 + time2) % 1000 + " seconds.");
    }
    */

  } // testSomething()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestXml.class);
  } // suite

} // class TestXml
