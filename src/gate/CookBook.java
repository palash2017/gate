/*
 *	CookBook.java
 *
 *	Hamish Cunningham, 16/Feb/2000
 *
 *	$Id$
 */

package gate;

import java.util.*;
import java.net.*;
import java.io.*;
import junit.framework.*;

import gate.*;


/**
<P>
This class provides examples of using the GATE APIs.
Read this documentation along with a copy of the
<A HREF=CookBook.txt>source code</A>.

<P>
The CookBook is set up as
part of the GATE test suite (using the JUnit framework), so there's an easy
way to run the examples (viz.,
<A HREF=../gate/TestGate.html>gate.TestGate.main</A>, which will invoke the
JUnit test runner). Also, we can use JUnit's assert methods, e.g.
<PRE>
assert(corpus.isEmpty());
</PRE>
tests that a corpus object is empty, and creates a test failure report if
this is not the case.

<P>
Programming to the GATE Java API involves manipulating the classes and
interfaces in the <A HREF=package-summary.html>gate package</A>. These are
mainly interfaces; the classes that do exist are mainly to do with getting
access to objects that implement the interfaces (without exposing those
implementations). In other words, it's an interface-based design.

<P>
Two classes take care of instantiating objects that implement the interfaces:
<TT>DataStore</TT> and <TT>Transients</TT>.

<TT>DataStore</TT> allows the creation of objects that are stored in databases
(NOT IMPLEMENTED YET!!!).

<TT>Transients</TT> provides static methods that construct new transient
objects, i.e. objects whose lifespan is bounded by the current invocation of
the program.

<P>
The <A HREF=Corpus.html>Corpus interface</A> represents collections of
<A HREF=Document.html>Documents</A> (and takes the place of the old TIPSTER
<TT>Collection</TT> class). The
<A HREF=#testCorpusConstruction()>testCorpusConstruction</A> method gives
an example of how to create a new transient Corpus object.
**/
public class CookBook extends TestCase
{
  /** Constructing a corpus */
  public void testCorpusConstruction() {

    // corpus constructors require a name
    Corpus corpus = Transients.newCorpus("My example corpus");

    // the corpus interface inherits all the sorted set methods
    assert(corpus.isEmpty());

  } // testCorpusConstruction

  /** Constructing a corpus */
  public void testAddingDocuments() {
    Corpus corpus = Transients.newCorpus("My example corpus");

    // document constructors may take a URL; if so you have
    // to deal with URL and net-related exceptions:
    URL u = null;
    try {
      u = new URL("http://derwent.dcs.shef.ac.uk:8000/tests/doc0.html");
      Document doc1 = Transients.newDocument(u);
      Document doc2 = Transients.newDocument(u);
    } catch(IOException e) {
      fail(e.toString());   // fail the test, give up, go home, go to sleep
    }

    // some set methods
    Iterator iter = corpus.iterator();
    while(iter.hasNext()) {
      Document doc = (Document) iter.next();
      assert(u.equals(doc.getSourceURL()));
    } // while

  } // testAddingDocuments




  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Construction */
  public CookBook(String name) { super(name); }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(CookBook.class);
  } // suite

  
} // class CookBook
