/*
 *  Main.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 1/Nov/00
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.awt.Dimension;
import java.awt.Toolkit;

import gnu.getopt.*;

import gate.util.*;
import gate.gui.*;


/** Top-level entry point for the GATE command-line and GUI interfaces.
  * <P>
  */
public class Main {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Status flag for normal exit. */
  private static final int STATUS_NORMAL = 0;

  /** Status flag for error exit. */
  private static final int STATUS_ERROR = 1;

  /** Main routine for GATE.
    * Command-line arguments:
    * <UL>
    * <LI>
    * <B>-h</B> display a short help message
    * </UL>
    */
  public static void main(String[] args) throws GateException {
    Gate.init();
    // process command-line options
    processArgs(args);

    // run the interface or do batch processing
    if(batchMode) {
      if(DEBUG) Out.prln("running batch process");
      batchProcess();
    }
    else {
      if(DEBUG) Out.prln("constructing GUI");
      // run the GUI
      MainFrame frame = new MainFrame();
      //Validate frames that have preset sizes
      frame.validate();
      //Center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = frame.getSize();
      if (frameSize.height > screenSize.height) {
        frameSize.height = screenSize.height;
      }
      if (frameSize.width > screenSize.width) {
        frameSize.width = screenSize.width;
      }
      frame.setLocation((screenSize.width - frameSize.width) / 2,
                        (screenSize.height - frameSize.height) / 2);
      frame.setVisible(true);
    }

    // shut down with normal exit status
    //System.exit(STATUS_NORMAL);

  } // main


/**

<BR>
<B>Options processing: </B>

<BR>
<TABLE>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -a annotator arg(s)
    </TH>
    <TH ALIGN=left>
    A CREOLE annotator to run on the collection, with zero or more
    arguments. The set of such annotators will be run in the sequence
    they appear in the arguments list. The arguments list must end with the
    start of another option; otherwise add a "-" after the arguments to
    terminate the list.
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -b
    </TH>
    <TH ALIGN=left>
    Batch mode. Don't start the GUI, just process options and exit after
    any actions (e.g. running annotators).
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -c collname
    </TH>
    <TH ALIGN=left>
    Name of the collection to use. If the collection already exists then
    it will be used as it stands, otherwise it will be created. See also
    -f.
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -d
    </TH>
    <TH ALIGN=left>
    Destroy the collection after use. (The default is to save it to
    disk.)
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -f file(s)
    </TH>
    <TH ALIGN=left>
    One or more files to create a collection with. If the collection
    being used (see -c) already exists, these files are ignored.
    Otherwise they are used to create the collection.
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -h
    </TH>
    <TH ALIGN=left>
    Print a usage message and exit.
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -p creolepath
    </TH>
    <TH ALIGN=left>
    Sets the search path for CREOLE modules.
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -v classname(s)
    </TH>
    <TH ALIGN=left>
    Verbose: turns on debugging output. Takes zero or more class names
    to debug.
    </TH>
  </TR>
</TABLE>

*/
  /** Name of the collection we were asked to process. */
  private static String collName;

  /** Search path for CREOLE modules. */
  private static String creolePath;

  /** List of files we were asked to build a collection from. */
  private static List fileNames = new ArrayList();

  /** List of annotators we were asked to run on the collection. */
  private static List annotatorNames = new ArrayList();

  /** Map of annotator arguments. */
  private static Map annotatorArgsMap = new HashMap();

  /** List of classes we were asked to debug. */
  private static List debugNames = new ArrayList();

  /** Are we in batch mode? */
  private static boolean batchMode = false;

  /** Don't save collection after batch? */
  private static boolean destroyColl = false;

  /** Verbose? */
  private static boolean verbose = false;


  /** Process arguments and set up member fields appropriately.
    * Will shut down the process (via System.exit) if there are
    * incorrect arguments, or if the arguments ask for something
    * simple like printing the help message.
    */
  public static void processArgs(String[] args) {

    Getopt g = new Getopt("GATE main", args, "hc:ba:df:p:v::");
    int c;
    while( (c = g.getopt()) != -1 )
      switch(c) {
        // -h
        case 'h':
          help();
          usage();
          System.exit(STATUS_NORMAL);
          break;


/*
        // -c collname
        case '-c':
          collName = g.getOptarg();
          break;

        // -b
        case '-b':
          batchMode = true;
          break;

        // -a annotator(s)
        case '-a':
          if(++i == args.length) { usage(); return; }
          String annotatorName = g.getOptarg();
          annotatorNames.add(annotatorName);
// collect any args for the annotator
          break;

        // -d
        case '-d':
          destroyColl = true;
          break;

        // -f file(s)
        case '-f':
          while(++i < args.length)
            if(args[i].toCharArray()[0] == '-') { // start of another option
              i--;
              break;
            }
            else
              fileNames.add(args[i]);
          break;

        // -p creolepath
        case '-p':
          if(++i < args.length)
            creolePath = args[i];
          else
            { usage(); return; }
          break;

        // -v classname(s)
        case '-v':
          verbose = true;
          Debug.setDebug(true);
          while(++i < args.length) {
            if(args[i].toCharArray()[0] == '-') { // start of another option
              i--;
              break;
            }
            else
              debugNames.add(args[i]);
          } // while
          break;
*/

        case '?':
          // leave the warning to getopt
	        System.exit(STATUS_ERROR);
          break;

        default:
          // shouldn't happen!
          Err.prln("getopt() returned " + c + "\n");
	        System.exit(STATUS_ERROR);
          break;
      } // getopt switch

  } // processArgs()

  /** Run commands as a batch process. */
  private static void batchProcess() {
/*
    // turn debugging on where requested
    if(verbose) {
      for(ArrayIterator i = debugNames.begin(); ! i.atEnd(); i.advance()) {
        try { Debug.setDebug(Class.forName(((String) i.get())), true); }
        catch(ClassNotFoundException e) {
          System.err.println(
            "can't debug class " + (String) i.get() + ": " + e.toString()
          );
        }
      } // for
    } // debugging on

    // collection: does it exist and can we open it?
    if(collName == null) {
      System.err.println("no collection name given");
      usage();
      return;
    }
    File collDir = new File(collName);
    JdmCollection coll = null;
    if(collDir.exists()) { // open collection
      Debug.prnl("opening collection " + collName);
      try {
        coll = new JdmCollection(collName);
      } catch (JdmException e) {
        System.err.println(
          "Couldn't open collection " + collName + " " + e.toString()
        );
        return;
      }
    } else { // create collection and add documents
      Debug.prnl("creating collection " + collName);
      JdmAttributeSequence attrs = new JdmAttributeSequence();
      try {
        coll = new JdmCollection(collName, attrs);
      } catch (JdmException e) {
        System.err.println(
          "Couldn't create collection " + collName + " " + e.toString()
        );
        return;
      }

      // add the documents to the collection
      for(ArrayIterator i = fileNames.begin(); ! i.atEnd(); i.advance()) {
        Debug.prnl("adding document " + (String) i.get());
        try {
          JdmDocument doc = coll.createDocument(
            (String) i.get(),
            null,
            new JdmAnnotationSet(),
            new JdmAttributeSequence()
          );
        } catch (JdmException e) {
          System.err.println(
             "Can't add document " + (String) i.get() + ": " + e.toString()
          );
        } // catch
      } // for each filename
    } // collection create

    // run the annotators on each document in the collection
    // for each document
    JdmDocument doc = null;
    if(coll.length() > 0)
      try{ doc = coll.firstDocument(); } catch(JdmException e) { }
    for(int i = 0; i<coll.length(); i++) {
      if(doc == null) continue; // first and next doc shouldn't throw excptns!

      // for each annotator
      for(ArrayIterator j = annotatorNames.begin(); !j.atEnd(); j.advance()) {
        String annotatorName = (String) j.get();
        Debug.prnl(
          "calling annotator " + annotatorName + " on doc " + doc.getId()
        );

        // load the annotator class
        Annotator annotator = null;
        Class annotatorClass = null;
        try {
          // cheat and assume that all annotators are on CLASSPATH
          annotatorClass = Class.forName(annotatorName);
        } catch (Exception ex) {
          System.err.println(
            "Could load class for CREOLE object " + annotatorName + ": " +
            ex.toString()
          );
          continue;
        }

        // construct the annotator
        try {
          annotator = (Annotator) annotatorClass.newInstance();
        } catch (Throwable ex) { // naughty chap
          System.err.println(
            "Could create instance of CREOLE object " + annotatorName + ": " +
            ex.toString()
          );
          continue;
        }

        // annotate this document
        String[] args = (String[]) annotatorArgsMap.get(annotatorName);
        if(args == null) args = new String[0];
        annotator.annotate(doc, args);
      } // for each annotator

      doc = null;
      try { doc = coll.nextDocument(); } catch(JdmException e) { }
    } // for each doc, annotate

    // save collection?
    if(! destroyColl) {
      Debug.prnl("saving the collection");
      try {
        coll.sync();
      } catch (JdmException e) {
        System.err.println(
          "Can't save collection " + collName + ": " + e.toString()
        );
      }
    } else {
      Debug.prnl("destroying collection");
      try { coll.destroy(); } catch(JdmException e) {
        // if we didn't sync we can't destroy, but that's not an error
      }
    }

    Debug.prnl("done batch process");
*/
  } // batchProcess()

  /** Display a usage message */
  public static void usage() {
    Out.prln(
      "Usage: java gate.Main " +
      "[ -h [-a annotator(s)] -b -c collname [-d] " +
      "[-f file(s)] [-v [classname(s)]] -p creolepath]"
    );
  } // usage()

  /** Display a help message */
  public static void help() {
    String nl = Strings.getNl();
    Out.prln(
      "For help on command-line options and other information " + nl +
      "see the user manual in your GATE distribution or at " + nl +
      "http://gate.ac.uk/gate/doc/userguide.html"
    );
  } // help()

} // class Main
