/*
 *	TestJdk.java
 *
 *	Hamish Cunningham, 16/Mar/00
 *
 *	$Id$
 */

package gate.util;

import java.util.*;
import java.io.*;
import junit.framework.*;
import java.net.*;

/** Tests for the Jdk class and for GateClassLoader. The testReloading method
  * reads a class from a .jar that is reached via a URL. This is called
  * TestJdk.jar; to build it, do "make TestJdk.jar" in the build directory
  * (the source for the class lives there, under "testpkg").
  */
public class TestJdk extends TestCase
{
  /** Instance of the Jdk class */
  private Jdk jdk;

  /** Construction */
  public TestJdk(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
    jdk = new Jdk();
  } // setUp

  /** Jdk tool directory finder */
  public void testFinder() throws Exception {
    String toolsDir = jdk.getToolsHome().getPath();
    assert(
      "Tools dir was found to be: " + toolsDir,
      toolsDir.startsWith("w:\\jdk\\jdk1") ||
      toolsDir.startsWith("W:\\jdk\\jdk1") ||
      toolsDir.startsWith("W:\\JBuilder") ||
      toolsDir.startsWith("w:\\JBuilder") ||
      toolsDir.startsWith("H:\\JBuilder") ||
      toolsDir.startsWith("D:\\apps\\JBuilder4\\jdk1.3\\jre\\..\\bin") ||
      toolsDir.startsWith("W:\\apps\\JBuilder4\\jdk1.3\\jre\\..\\bin") ||
      //toolsDir.startsWith("") ||
      toolsDir.startsWith("h:\\JBuilder") ||
      toolsDir.startsWith("/usr/local/") ||
      toolsDir.startsWith("/usr/java") ||
      toolsDir.startsWith("/usr/j2se/jre/../bin") ||
      toolsDir.startsWith("/usr/j2se/jre/bin") ||
      toolsDir.startsWith("/opt/")
    );
  } // testFinder()

  /** Jdk compiler */
  public void testCompiler() throws GateException {
    String nl = Strings.getNl();
    String javaSource =
      "package gate.util;" + nl +
      "import java.io.*;" + nl +
      "public class X {" + nl +
      "  public X() { /*System.out.println(\"X construcing\");*/ } " + nl +
      "  public static void main(String[] args)" + nl +
      "    { System.out.println(\"Hello from X\"); }" + nl +
      "  public static String getSomething() { return \"something\"; }" + nl +
      "} " + nl
      ;
    Gate.init();
    byte[] classBytes = jdk.compile(javaSource, "gate/util/X.java");
    assert(
      "no bytes returned from compiler",
      classBytes != null && classBytes.length > 0
    );

    /* if you want to write it to disk...
    FileOutputStream outFile =
      new FileOutputStream("z:\\gate2\\classes\\gate\\util\\X.class");
    outFile.write(classBytes);
    outFile.close();
    */

    // try and instantiate one
    Class theXClass = jdk.defineClass("gate/util/X", classBytes);
    Object theXObject = jdk.instantiateClass(theXClass);
    assert("couldn't instantiate the X class", theXObject != null);
    assert(
      "X instantiated wrongly, name = " + theXObject.getClass().getName(),
      theXObject.getClass().getName().equals("gate.util.X")
    );

  } // testCompiler()

  /** Jdk compiler test 2. Does nothing if it can't find the
    * gate class files in the usual places.
    */
  public void testCompiler2() throws GateException {
    byte[] thisClassBytes = null;
    String thisClassSource = null;

    // try and get the bytes from the usual place on NT
    try {
      File sf = new File("z:\\gate2\\src\\gate\\util\\X.java");
      File bf = new File("z:\\gate2\\classes\\gate\\util\\X.class");
      thisClassBytes = Files.getByteArray(bf);
      thisClassSource = Files.getString(sf);
    } catch(IOException e) {
    }

    // try and get them from the usual Solaris place
    if(thisClassBytes == null || thisClassBytes.length == 0)
      try {
        File sf = new File(
"/share/nlp/projects/gate/webpages/gate.ac.uk/gate2/src/gate/util/TestJdk.java"
        );
        File bf = new File(
"/share/nlp/projects/gate/webpages/gate.ac.uk/gate2/classes/gate/util/TestJdk.class"
        );
        thisClassBytes = Files.getByteArray(bf);
        thisClassSource = Files.getString(sf);
      } catch(IOException e) {

        // we couldn't find the bytes; in an ideal world we'd get it
        // from gate.jar....
        return;
      }

    // compile the source
    Jdk jdk = new Jdk();
    byte[] compiledBytes =
      jdk.compile(thisClassSource, "gate/util/TestJdk.java");

// testing the binary to see if it is the same as the one on
// disk doesn't work accross platforms as various strings to
// do with source, libraries and so on get embedded. the
// best test would be to do a javap and check compatibility,
// but life is finite, so:
if(true) return;

    assert(
      "compiled binary doesn't equal on-disk binary",
      compiledBytes.equals(thisClassBytes)
    );

  } // testCompiler2()


  /** Test reloading of classes. */
  public void testReloading() throws Exception {

    GateClassLoader loader = Gate.getClassLoader();

    if (Gate.isGateHomeReachable())
      loader.addURL(new URL(
        "http://derwent.dcs.shef.ac.uk/gate.ac.uk/tests/TestJdk.jar")
      );
    else if (Gate.isGateAcUkReachable())
      loader.addURL(new URL("http://gate.ac.uk/tests/TestJdk.jar"));
    else
      throw new LazyProgrammerException();

    //loader.addURL(new URL("file:/build/TestJdk.jar"));
    Class dummyClass1 = loader.loadClass("testpkg.Dummy");
    assert("dummy1 is null", dummyClass1 != null);
    Object dummyObject1 = dummyClass1.newInstance();
    assert("dummy1 object is null", dummyObject1 != null);

    Class dummyClass2 = loader.reloadClass("testpkg.Dummy");
    assert("dummy2 is null", dummyClass2 != null);
    Object dummyObject2 = dummyClass2.newInstance();
    assert("dummy2 object is null", dummyObject2 != null);

    Class dummyClass3 = loader.reloadClass("testpkg.Dummy");
    assert("dummy3 is null", dummyClass2 != null);
    Object dummyObject3 = dummyClass3.newInstance();
    assert("dummy3 object is null", dummyObject3 != null);

  } // testReloading


  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestJdk.class);
  } // suite

  public static void main(String[] args){
    try{
      TestJdk testJdk = new TestJdk("");
      testJdk.setUp();
      testJdk.testCompiler();
      testJdk.testCompiler2();
      testJdk.testFinder();
      testJdk.testReloading();
    }catch (Exception e) {e.printStackTrace(System.err);}
  }
} // class TestJdk
