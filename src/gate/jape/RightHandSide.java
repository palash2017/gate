/*
 *  RightHandSide.java - transducer class
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 24/07/98
 *
 *  $Id$
 */


package gate.jape;

import gate.AnnotationSet;
import gate.Document;
import gate.Gate;
import gate.creole.ontology.Ontology;
import gate.util.Err;
import gate.util.GateRuntimeException;
import gate.util.Strings;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
  * The RHS of a CPSL rule. The action part. Contains an inner class
  * created from the code in the grammar RHS.
  */
public class RightHandSide implements JapeConstants, java.io.Serializable
{
  private static final long serialVersionUID = -4359589687308736378L;
  
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** An instance of theActionClass. */
  transient private Object theActionObject;

  /** The string we use to create the action class. */
  private StringBuffer actionClassString;

  /** The bytes of the compiled action class. */
  private byte[] actionClassBytes;

  /** The name of the action class. */
  private String actionClassName;

  /** Package name for action classes. It's called a "dir name" because
    * we used to dump the action classes to disk and compile them there.
    */
  static private String actionsDirName = "japeactionclasses";

  /** The qualified name of the action class. */
  private String actionClassQualifiedName;

  /** Name of the .java file for the action class. */
  private String actionClassJavaFileName;

  /** Name of the .class file for the action class. */
  private String actionClassClassFileName;
  
  /** A list of source info object for mapping between Java and Jape. */
  private transient List<SourceInfo> sourceInfo;

  /** Cardinality of the action class set. Used for ensuring class name
    * uniqueness.
    */
  private static AtomicInteger actionClassNumber = new AtomicInteger();

  /** The set of block names.
    * Used to ensure we only get their annotations once in the action class.
    */
  private HashSet blockNames;

  /** Returns the string for the java code */
  public String getActionClassString() { return actionClassString.toString(); }

  public String getActionClassName() { return actionClassQualifiedName; }

  /** The LHS of our rule, where we get bindings from. */
  private LeftHandSide lhs;

  /** A list of the files and directories we create. */
  static private ArrayList tempFiles = new ArrayList();

  /** Local fashion for newlines. */
  private final String nl = Strings.getNl();

  /** Debug flag. */
  static final boolean debug = false;
  private String phaseName;
  private String ruleName;
  
  private static Set<StackTraceElement> warnings = new HashSet<StackTraceElement>();

  /** Construction from the transducer name, rule name and the LHS. */
  public RightHandSide(
    String transducerName,
    String ruleName,
    LeftHandSide lhs,
    String importblock
  ) {
    // debug = true;
    this.lhs = lhs;
    this.phaseName = transducerName;
    this.ruleName = ruleName;
    actionClassName = new String(
      transducerName + ruleName + "ActionClass" + actionClassNumber.getAndIncrement()
    );
    blockNames = new HashSet();
    sourceInfo = new ArrayList<SourceInfo>();

    actionClassString = new StringBuffer(
      "// " + actionClassName + nl +
      "package " + actionsDirName + "; " + nl +
      importblock + nl +
      "public class " + actionClassName + nl +
      "implements java.io.Serializable, RhsAction { " + nl +
      "  private ActionContext ctx;"+nl+
      "  public void setActionContext(ActionContext ac) { ctx = ac; }"+nl+
      "  public ActionContext getActionContext() { return ctx; }"+nl+
      "  public void doit(gate.Document doc, " + nl +
      "                   java.util.Map<java.lang.String, gate.AnnotationSet> bindings, " + nl +
      "                   gate.AnnotationSet annotations, " + nl +
      "                   gate.AnnotationSet inputAS, gate.AnnotationSet outputAS, " + nl +
      "                   gate.creole.ontology.Ontology ontology) throws gate.jape.JapeException {" + nl
    );

    // initialise various names
    actionClassJavaFileName =
      actionsDirName +  File.separator +
      actionClassName.replace('.', File.separatorChar) + ".java";
    actionClassQualifiedName =
      actionsDirName.
      replace(File.separatorChar, '.').replace('/', '.').replace('\\', '.') +
      "." + actionClassName;
    actionClassClassFileName =
      actionClassQualifiedName.replace('.', File.separatorChar) + ".class";
  } // Construction from lhs

  /** Add an anonymous block to the action class */
  public void addBlock(String anonymousBlock) {
    actionClassString.append(nl);
    actionClassString.append("if (true) {");
    actionClassString.append(nl);
    actionClassString.append(storeSourceInfo(anonymousBlock));
    actionClassString.append(nl);
    actionClassString.append("}");
    actionClassString.append(nl);
  } // addBlock(anon)

  /** Add a named block to the action class */
  public void addBlock(String name, String namedBlock) {
    // is it really a named block?
    // (dealing with null name cuts code in the parser...)
    if(name == null) {
      addBlock(namedBlock);
      return;
    }

    if(blockNames.add(name)) // it wasn't already a member
      actionClassString.append(
        "    gate.AnnotationSet " + name + "Annots = bindings.get(\""
        + name + "\"); " + nl
      );

    actionClassString.append(
      "    if(" + name + "Annots != null && " + name +
      "Annots.size() != 0) { " + nl);
      
    actionClassString.append(storeSourceInfo(namedBlock));
      
   actionClassString.append(
      nl + "    }" + nl
    );
  } // addBlock(name, block)
  
  private String storeSourceInfo(String codeBlock) {
    if(!codeBlock.startsWith("  // JAPE Source:")) return codeBlock;

    String info = codeBlock.substring(18, codeBlock.indexOf("\n")).trim();
    String code = codeBlock.substring(codeBlock.indexOf("\n") + 1);

    String japeURL = info.substring(0, info.lastIndexOf(":"));
    int lineNumber = Integer
            .parseInt(info.substring(info.lastIndexOf(":") + 1));

    int startLine = actionClassString.toString().split("\n").length + 1;
    int endLine = startLine + code.split("\n").length;

    int startOffset = actionClassString.length();
    int endOfset = actionClassString.length() + code.length();
    
    sourceInfo.add(new SourceInfo(japeURL, lineNumber, startLine, endLine,
            startOffset, endOfset));

    return code;
  }
  
  /**
   * Enhances a Throwable by replacing mentions of Java code inside a
   * Jape RhsAction with a reference to the original Jape source where
   * available.
   * 
   * @param t the Throwable to enhance with Jape source information
   */
  private void enhanceTheThrowable(Throwable t) {
    if(t.getCause() != null) {
      enhanceTheThrowable(t.getCause());
    }

    List<StackTraceElement> stack = new ArrayList<StackTraceElement>();

    for(StackTraceElement ste : t.getStackTrace()) {
      if(ste.getClassName().equals(actionClassQualifiedName)) {

        StackTraceElement japeSTE = null;

        if(ste.getLineNumber() >= 0 && sourceInfo != null) {
          for(SourceInfo info : sourceInfo) {
            japeSTE = info.getStackTraceElement(ste.getLineNumber());

            if(japeSTE != null) break;
          }
        }
        else {
          // this will happen if we are running from a
          // serialised jape grammar as we don't keep the
          // source info
          japeSTE = new StackTraceElement(getPhaseName(), getRuleName(), null,
                  -1);
        }

        stack.add(japeSTE != null ? japeSTE : ste);
      }
      else {
        stack.add(ste);
      }
    }

    t.setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
  }

  /** Create the action class and an instance of it. */
  public void createActionClass() throws JapeException {
    // terminate the class string
    actionClassString.append("  }" + nl + "}" + nl);
//    try {
//      Javac.loadClass(actionClassString.toString(),
//                           actionClassJavaFileName);
//    } catch(GateException e) {
//      String nl = Strings.getNl();
//      String actionWithNumbers =
//        Strings.addLineNumbers(actionClassString.toString());
//      throw new JapeException(
//        "Couldn't create action class: " + nl + e + nl +
//        "offending code was: " + nl + actionWithNumbers + nl
//      );
//    }
//    instantiateActionClass();
  } // createActionClass

  /** Create an instance of the action class. */
  public void instantiateActionClass() throws JapeException {

    try {
      theActionObject = Gate.getClassLoader().
                        loadClass(actionClassQualifiedName).
                        newInstance();
    } catch(Exception e) {
      throw new JapeException(
        "couldn't create instance of action class " + actionClassName + ": "
        + e.getMessage()
      );
    }
  } // instantiateActionClass

  /** Remove class files created for actions. */
  public static void cleanUp() {
    if(tempFiles.size() == 0) return;

    // traverse the list in reverse order, coz any directories we
    // created were done first
    for(ListIterator i = tempFiles.listIterator(tempFiles.size()-1);
        i.hasPrevious();
       ) {
      File tempFile = (File) i.previous();
      tempFile.delete();
    } // for each tempFile

    tempFiles.clear();
  } // cleanUp

  private void writeObject(java.io.ObjectOutputStream out)
  throws IOException{
    out.defaultWriteObject();
    //now we need to save the class for the action
    try{
		Class class1 = Gate.getClassLoader().loadClass(actionClassQualifiedName);
		//System.out.println(class1.getName());
		out.writeObject(class1);
    }catch(ClassNotFoundException cnfe){
      throw new GateRuntimeException(cnfe);
    }
  }
  
  private void readObject(java.io.ObjectInputStream in)
  throws IOException, ClassNotFoundException{
    in.defaultReadObject();
    //now read the class
	String className = getActionClassName();
	if(Gate.getClassLoader().findExistingClass(getActionClassName()) == null) {
		try{
			Map actionClasses = new HashMap();
			actionClasses.put(getActionClassName(), getActionClassString());
			gate.util.Javac.loadClasses(actionClasses);
		}catch(Exception e1){
			throw new GateRuntimeException (e1);
		}
	}
  }
  
  /** Makes changes to the document, using LHS bindings. */
  public void transduce(Document doc, java.util.Map<String, AnnotationSet> bindings,
                        AnnotationSet inputAS, final AnnotationSet outputAS,
                        Ontology ontology,
                        ActionContext actionContext)
                        throws JapeException {
    if(theActionObject == null) {
      instantiateActionClass();
    }

    // the 'annotations' parameter of the RhSAction.doIt method has been
    // deprecated. As there is no way in Java to deprecate a parameter
    // we will have to be a little clever/sneaky! We will create a proxy
    // around the outputAS and pass the proxy to the Jape RHS. If the RHS
    // code calls any method on the annotations proxy the following
    // handler will be called instead so we can warn about the
    // deprecation and then forward the method onwards to the outputAS
    // so that the JAPE code will still work.
    AnnotationSet annotations = (AnnotationSet)Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class[] {AnnotationSet.class},
            new InvocationHandler() {

              public Object invoke(Object proxy, Method method, Object[] args)
                      throws Throwable {

                StackTraceElement japeSTE = null;
                SourceInfo source = null;

                // find the stack trace element corresponding to the
                // call on the annotations proxy. This should always be
                // the third element but just to be on the safe side we
                // will find it by looping
                for(StackTraceElement ste : (new Throwable()).getStackTrace()) {
                  if(ste.getClassName().equals(actionClassQualifiedName)) {

                    if(ste.getLineNumber() >= 0 && sourceInfo != null) {
                      for(SourceInfo info : sourceInfo) {
                        japeSTE = info
                                .getStackTraceElement(ste.getLineNumber());

                        if(japeSTE != null) {
                          source = info;
                          break;
                        }
                      }
                    }
                    else {
                      // this will happen if we are running from a
                      // serialised jape grammar as we don't keep the
                      // source info
                      japeSTE = new StackTraceElement(getPhaseName(),
                              getRuleName(), null, -1);
                    }

                    break;
                  }
                }

                if(!warnings.contains(japeSTE)) {
                  // we only want to warn about each use once per
                  // invocation of GATE so we keep a cache of the stack
                  // trace elements we have already warned about
                  Err.println(nl + "WARNING: the JAPE 'annotations' parameter has been deprecated. Please use 'inputAS' or 'outputAS' instead.");
                  Err.println(japeSTE);
                  if(japeSTE.getLineNumber() >= 0)
                    Err.println("\t" + source.getSource(japeSTE.getLineNumber()).trim());

                  warnings.add(japeSTE);
                }

                //pass the method on so that the JAPE code still works
                return method.invoke(outputAS, args);
              }
            });
    
    // run the action class
    try {
      ((RhsAction) theActionObject).setActionContext(actionContext);
      ((RhsAction) theActionObject).doit(doc, bindings, annotations,
                                         inputAS, outputAS, ontology);
    } catch (NonFatalJapeException e) {
      // if the action class throws a non-fatal exception then respond by
      // dumping a whole bunch of useful debug information but then allow
      // processing to continue on as if nothing happened.
      Throwable t = e.getCause();
      Err.println("A non-fatal JAPE exception occurred while processing document '"+doc.getName()+"'.");
      Err.println("The issue occurred during execution of rule '"+getRuleName()+"' in phase '"+getPhaseName()+"':");
      if (t != null) {
        enhanceTheThrowable(t);
        t.printStackTrace(Err.getPrintWriter());
      } else {
        Err.println("Line number and exception details are not available!");
      }
    } catch (Throwable e) {
      // if the action class throws an exception, re-throw it with a
      // full description of the problem, inc. stack trace and the RHS
      // action class code
      enhanceTheThrowable(e);
      if(e instanceof Error) {
        throw (Error)e;
      }
      if(e instanceof JapeException) {
        throw (JapeException)e;
      }      
      if(e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      
      // shouldn't happen...
        throw new JapeException(
          "Couldn't run RHS action", e);
    }
  } // transduce

  /** Create a string representation of the object. */
  public String toString() { return toString(""); }

  /** Create a string representation of the object. */
  public String toString(String pad) {
    String nl = Strings.getNl();
    StringBuffer buf = new StringBuffer(
      pad + "RHS: actionClassName(" + actionClassName + "); "
    );
    //buf.append("actionClassString(" + nl + actionClassString + nl);
    buf.append(
      "actionClassClassFileName(" + nl + actionClassClassFileName + nl
    );
    buf.append("actionClassJavaFileName(" + nl + actionClassJavaFileName + nl);
    buf.append(
      "actionClassQualifiedName(" + nl + actionClassQualifiedName + nl
    );

    buf.append("blockNames(" + blockNames.toString() + "); ");

    buf.append(nl + pad + ") RHS." + nl);

    return buf.toString();
  } // toString

  /** Create a string representation of the object. */
  public String shortDesc() {
    String res = "" + actionClassName;
    return res;
  }
  public void setPhaseName(String phaseName) {
    this.phaseName = phaseName;
  }
  public String getPhaseName() {
    return phaseName;
  }
  public void setRuleName(String ruleName) {
    this.ruleName = ruleName;
  }
  public String getRuleName() {
    return ruleName;
  } // toString
  
  /**
   * A simple class to store and use the mapping between Java and Jape
   * source code for error reporting.
   * 
   * @author Mark A. Greenwood
   */
  class SourceInfo implements java.io.Serializable {
    String japeURL;

    int japeLine;

    int startLine, endLine;

    int startOffset, endOffset;

    SourceInfo(String japeURL, int japeLine, int startLine, int endLine,
            int startOffset, int endOffset) {
      this.japeURL = japeURL;
      this.japeLine = japeLine;
      this.startLine = startLine;
      this.endLine = endLine;
      this.startOffset = startOffset;
      this.endOffset = endOffset;
    }

    public boolean contains(int lineNumber) {
      return (startLine <= lineNumber && lineNumber <= endLine);
    }

    public String getNumberedSource() {
      return Strings.addLineNumbers(getSource(), japeLine);
    }

    public String getSource(int line) {      
      String[] lines = getSource().split("\n");
      
      return lines[line-japeLine];
    }
    
    public int getJapeLineNumber(int javaLineNumber) {
      if(!contains(javaLineNumber)) return -1;

      return japeLine + (javaLineNumber - startLine);
    }

    public StackTraceElement getStackTraceElement(int javaLineNumber) {
      int japeLineNumber = getJapeLineNumber(javaLineNumber);

      if(japeLineNumber == -1) return null;

      return new StackTraceElement(RightHandSide.this.getPhaseName(),
              RightHandSide.this.getRuleName(), japeURL, japeLineNumber);
    }
    
    public String getSource() {
      return actionClassString.substring(startOffset, endOffset);
    }
  }
} // class RightHandSide


// $Log$
// Revision 1.31  2005/10/10 14:59:15  nirajaswani
// bug fixed - reenabled JAPE serialization
//
// Revision 1.30  2005/10/10 10:29:38  valyt
// Serialisatoin to savwe the RHS action class object as well.
//
// Revision 1.29  2005/09/30 16:01:04  valyt
// BUGFIX:
// RHS Java blocks now have braces around them (to reduce visibility of local variables)
//
// Revision 1.28  2005/01/11 13:51:36  ian
// Updating copyrights to 1998-2005 in preparation for v3.0
//
// Revision 1.27  2004/07/21 17:10:08  akshay
// Changed copyright from 1998-2001 to 1998-2004
//
// Revision 1.26  2004/03/25 13:01:14  valyt
// Imports optimisation throughout the Java sources
// (to get rid of annoying warnings in Eclipse)
//
// Revision 1.25  2002/05/14 09:43:17  valyt
//
// Ontology Aware JAPE transducers
//
// Revision 1.24  2002/02/27 15:11:16  valyt
//
// bug 00011:
// Jape access to InputAS
//
// Revision 1.23  2002/02/26 13:27:12  valyt
//
// Error messages from the compiler
//
// Revision 1.22  2002/02/26 10:30:07  valyt
//
// new compile solution
//
// Revision 1.21  2002/02/12 11:39:03  valyt
//
// removed sate and status members for Jape generated classes
//
// Revision 1.20  2002/02/04 13:59:04  hamish
// added status and state members to RhsAction
//
// Revision 1.19  2001/11/16 13:03:35  hamish
// moved line numbers method to Strings
//
// Revision 1.18  2001/11/16 10:29:45  hamish
// JAPE RHS compiler errors now include the RHS code; test added
//
// Revision 1.17  2001/11/15 14:05:09  hamish
// better error messages from JAPE RHS problems
//
// Revision 1.16  2001/11/01 15:49:09  valyt
//
// DEBUG mode for Japes
//
// Revision 1.15  2001/09/13 12:09:50  kalina
// Removed completely the use of jgl.objectspace.Array and such.
// Instead all sources now use the new Collections, typically ArrayList.
// I ran the tests and I ran some documents and compared with keys.
// JAPE seems to work well (that's where it all was). If there are problems
// maybe look at those new structures first.
//
// Revision 1.14  2000/11/08 16:35:03  hamish
// formatting
//
// Revision 1.13  2000/10/26 10:45:30  oana
// Modified in the code style
//
// Revision 1.12  2000/10/16 16:44:34  oana
// Changed the comment of DEBUG variable
//
// Revision 1.11  2000/10/10 15:36:36  oana
// Changed System.out in Out and System.err in Err;
// Added the DEBUG variable seted on false;
// Added in the header the licence;
//
// Revision 1.10  2000/07/04 14:37:39  valyt
// Added some support for Jape-ing in a different annotations et than the default one;
// Changed the L&F for the JapeGUI to the System default
//
// Revision 1.9  2000/06/12 13:33:27  hamish
// removed japeactionclasse create code (static init block
//
// Revision 1.8  2000/05/16 10:38:25  hamish
// removed printout
//
// Revision 1.7  2000/05/16 10:30:33  hamish
// uses new gate.util.Jdk compiler
//
// Revision 1.6  2000/05/05 12:51:12  valyt
// Got rid of deprecation warnings
//
// Revision 1.5  2000/05/05 10:14:09  hamish
// added more to toString
//
// Revision 1.4  2000/05/02 16:54:47  hamish
// porting to new annotation API
//
// Revision 1.3  2000/04/20 13:26:42  valyt
// Added the graph_drawing library.
// Creating of the NFSM and DFSM now works.
//
// Revision 1.2  2000/02/24 17:28:48  hamish
// more porting to new API
//
// Revision 1.1  2000/02/23 13:46:11  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:02  hamish
// added gate2
//
// Revision 1.21  1998/11/13 17:25:10  hamish
// stop it using sun.tools... when in 1.2
//
// Revision 1.20  1998/10/30 15:31:07  kalina
// Made small changes to make compile under 1.2 and 1.1.x
//
// Revision 1.19  1998/10/29 12:17:12  hamish
// use reflection when using sun compiler classes, so can compile without them
//
// Revision 1.18  1998/10/01 16:06:36  hamish
// new appelt transduction style, replacing buggy version
//
// Revision 1.17  1998/09/18 16:54:17  hamish
// save/restore works except for attribute seq
//
// Revision 1.16  1998/09/18 13:35:44  hamish
// refactored to split up createActionClass
//
// Revision 1.15  1998/09/18 12:15:40  hamish
// bugs fixed: anon block null ptr; no error for some non-existant labelled blocks
//
// Revision 1.14  1998/08/19 20:21:41  hamish
// new RHS assignment expression stuff added
//
// Revision 1.13  1998/08/17 10:43:29  hamish
// action classes have unique names so can be reloaded
//
// Revision 1.12  1998/08/12 15:39:42  hamish
// added padding toString methods
//
// Revision 1.11  1998/08/10 14:16:38  hamish
// fixed consumeblock bug and added batch.java
//
// Revision 1.10  1998/08/07 12:01:46  hamish
// parser works; adding link to backend
//
// Revision 1.9  1998/08/05 21:58:07  hamish
// backend works on simple test
//
// Revision 1.8  1998/08/04 12:42:56  hamish
// fixed annots null check bug
//
// Revision 1.7  1998/08/03 21:44:57  hamish
// moved parser classes to gate.jape.parser
//
// Revision 1.6  1998/08/03 19:51:26  hamish
// rollback added
//
// Revision 1.5  1998/07/31 16:50:18  hamish
// RHS compilation works; it runs - and falls over...
//
// Revision 1.4  1998/07/31 13:12:25  hamish
// done RHS stuff, not tested
//
// Revision 1.3  1998/07/30 11:05:24  hamish
// more jape
//
// Revision 1.2  1998/07/29 11:07:10  hamish
// first compiling version
//
// Revision 1.1.1.1  1998/07/28 16:37:46  hamish
// gate2 lives
