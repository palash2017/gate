package gate.creole.tokeniser;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.event.*;
import java.util.*;

/**
 * A composed tokeniser containing a {@link SimpleTokeniser} and a
 * {@link gate.creole.Transducer}.
 * The simple tokeniser tokenises the document and the transducer processes its
 * output.
 */
public class DefaultTokeniser extends AbstractLanguageAnalyser {

  public DefaultTokeniser() {
  }


  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException{
    try{
      //init super object
      super.init();
      //create all the componets
      FeatureMap params;
      FeatureMap features;
      Map listeners = new HashMap();
      listeners.put("gate.event.StatusListener", new StatusListener(){
        public void statusChanged(String text){
          fireStatusChanged(text);
        }
      });

      //tokeniser
      fireStatusChanged("Creating a tokeniser");
      params = Factory.newFeatureMap();
      if(tokeniserRulesURL != null) params.put("rulesURL",
                                               tokeniserRulesURL);
      params.put("encoding", encoding);
      if(DEBUG) Out.prln("Parameters for the tokeniser: \n" + params);
      features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);
      tokeniser = (SimpleTokeniser)Factory.createResource(
                    "gate.creole.tokeniser.SimpleTokeniser",
                    params, features, listeners, null);
      tokeniser.setName("Tokeniser " + System.currentTimeMillis());

      fireProgressChanged(50);

      //transducer
      fireStatusChanged("Creating a Jape transducer");
      params.clear();
      if(transducerGrammarURL != null) params.put("grammarURL",
                                                  transducerGrammarURL);
      params.put("encoding", encoding);
      if(DEBUG) Out.prln("Parameters for the transducer: \n" + params);
      features.clear();
      Gate.setHiddenAttribute(features, true);
      listeners.put("gate.event.ProgressListener",
                    new IntervalProgressListener(51, 100));
      transducer = (Transducer)Factory.createResource("gate.creole.Transducer",
                                                      params, features,
                                                      listeners, null);
      fireProgressChanged(100);
      fireProcessFinished();
      transducer.setName("Transducer " + System.currentTimeMillis());
    }catch(ResourceInstantiationException rie){
      throw rie;
    }catch(Exception e){
      throw new ResourceInstantiationException(e);
    }
    return this;
  }

  public void execute() throws ExecutionException{
    interrupted = false;
    //set the parameters
    try{
      FeatureMap params = Factory.newFeatureMap();
      fireProgressChanged(0);
      //tokeniser
      params.put("document", document);
      params.put("annotationSetName", annotationSetName);
      tokeniser.setParameterValues(params);

      //transducer
      params.clear();
      params.put("document", document);
      params.put("inputASName", annotationSetName);
      params.put("outputASName", annotationSetName);
      transducer.setParameterValues(params);
    }catch(ResourceInstantiationException rie){
      throw new ExecutionException(rie);
    }

    ProgressListener pListener = null;
    StatusListener sListener = null;
    fireProgressChanged(5);
    pListener = new IntervalProgressListener(5, 50);
    sListener = new StatusListener(){
      public void statusChanged(String text){
        fireStatusChanged(text);
      }
    };

    //tokeniser
    if(isInterrupted()) throw new ExecutionInterruptedException(
        "The execution of the \"" + getName() +
        "\" tokeniser has been abruptly interrupted!");
    tokeniser.addProgressListener(pListener);
    tokeniser.addStatusListener(sListener);
    try{
      tokeniser.execute();
    }catch(ExecutionInterruptedException eie){
      throw new ExecutionInterruptedException(
        "The execution of the \"" + getName() +
        "\" tokeniser has been abruptly interrupted!");
    }
    tokeniser.removeProgressListener(pListener);
    tokeniser.removeStatusListener(sListener);

  //transducer
    if(isInterrupted()) throw new ExecutionInterruptedException(
        "The execution of the \"" + getName() +
        "\" tokeniser has been abruptly interrupted!");
    pListener = new IntervalProgressListener(50, 100);
    transducer.addProgressListener(pListener);
    transducer.addStatusListener(sListener);

    transducer.execute();
    transducer.removeProgressListener(pListener);
    transducer.removeStatusListener(sListener);
  }//execute


  /**
   * Notifies all the PRs in this controller that they should stop their
   * execution as soon as possible.
   */
  public synchronized void interrupt(){
    interrupted = true;
    tokeniser.interrupt();
    transducer.interrupt();
  }

  public void setTokeniserRulesURL(java.net.URL tokeniserRulesURL) {
    this.tokeniserRulesURL = tokeniserRulesURL;
  }
  public java.net.URL getTokeniserRulesURL() {
    return tokeniserRulesURL;
  }
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
  public String getEncoding() {
    return encoding;
  }
  public void setTransducerGrammarURL(java.net.URL transducerGrammarURL) {
    this.transducerGrammarURL = transducerGrammarURL;
  }
  public java.net.URL getTransducerGrammarURL() {
    return transducerGrammarURL;
  }
 // init()

  private static final boolean DEBUG = false;

  /** the simple tokeniser used for tokenisation*/
  protected SimpleTokeniser tokeniser;

  /** the transducer used for post-processing*/
  protected Transducer transducer;
  private java.net.URL tokeniserRulesURL;
  private String encoding;
  private java.net.URL transducerGrammarURL;
  private String annotationSetName;


  public void setAnnotationSetName(String annotationSetName) {
    this.annotationSetName = annotationSetName;
  }
  public String getAnnotationSetName() {
    return annotationSetName;
  }/////////class CustomProgressListener implements ProgressListener
}