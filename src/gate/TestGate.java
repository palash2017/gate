/*
 *	TestGate.java
 *
 *	Hamish Cunningham, 21/Jan/00
 *
 *	$Id$
 */

package gate;

import oracle.jdbc.driver.OracleDriver;
import java.util.*;
import junit.framework.*;
import gate.annotation.*;
import gate.corpora.*;
import gate.util.*;
import gate.db.*;


/** Top-level entry point for GATE test suite.
  * "main" will run the JUnit test runner interface.
  * Use a "-t" flag to run the textual UI test runner (useful for
  * debugging, as there's less confusion to do with threads and
  * class loaders!).
  */
public class TestGate
{
  /** Main routine. */
  public static void main(String[] args) {
    String a[] = new String[1];
    a[0] = "gate.TestGate";
    // use the next line if you're running with output to console in text mode:
    // a[1] = "-wait";

    if(args.length > 0 && args[0].equals("-t")) // text runner mode
      junit.textui.TestRunner.main(a);
    else
      junit.ui.LoadingTestRunner.main(a);
  } // main

  /** GATE test suite. Every test case class has to be
    * registered here.
    */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(TestDocument.suite());
    suite.addTest(TestAnnotation.suite());
    suite.addTest(TestRBTreeMap.suite());
    suite.addTest(TestCorpus.suite());
    suite.addTest(CookBook.suite());
    suite.addTest(TestDB.suite());
    return suite;
  } // suite

} // class TestGate
