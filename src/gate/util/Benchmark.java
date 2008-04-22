/*
 *  Benchmark.java
 *
 *  Copyright (c) 1998-2008, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 */


package gate.util;

import java.io.File;

import gate.FeatureMap;
import gate.Gate;

import org.apache.log4j.Logger;

/**
 * This class provides methods for making entries in the shared log
 * maintained by the GATE system. User should use various methods
 * provided by this class and as described in the following example.
 * 
 * <p>
 * 
 * TODO: Provide example here.
 * 
 * </p>
 * 
 * @author niraj
 */
public class Benchmark {

  // initialize the log4J configuration
  static {
    File log4jFile =
      new File(new File(Gate.getGateHome(), "bin"), "gateLog4j.properties");
    if(System.getProperties().get("log4j.configuration") == null)
      System.getProperties().put(
        "log4j.configuration", log4jFile.toURI().toString()
      );
  }
  
  /**
   * This code appears in the log to indicate the check point entry
   */
  public final static String CHECK_POINT_CODE = "CP";
  
  /**
   * This code appears in the log to indicate finishing of the process.
   */
  public final static String PROCESS_FINISH_CODE = "PF";
  
  /**
   * This feature in the FeatureMap indicates the process was interrupted.
   */
  public final static String PROCESS_INTERRUPTED_FEATURE = "PROCESS_INTERRUPTED";
  
  /**
   * Corpus name feature
   */
  public final static String CORPUS_NAME_FEATURE = "CORPUS_NAME";

  /**
   * Corpus name feature
   */
  public final static String APPLICATION_NAME_FEATURE = "APPLICATION_NAME";
  
  /**
   * Document name feature
   */
  public final static String DOCUMENT_NAME_FEATURE = "DOCUMENT_NAME";
  
  /**
   * Processing resource name feature
   */
  public final static String PR_NAME_FEATURE = "PR_NAME";
  
  /**
   * Message feature
   */
  public final static String MESSAGE_FEATURE = "MESSAGE";
  
  
  /**
   * Static shared logger used for logging.
   */
  public static Logger logger = Logger.getLogger(Benchmark.class);

  /**
   * This returns the current system time.
   * 
   * @return
   */
  public static long startPoint() {
    return System.currentTimeMillis();
  }

  /**
   * This method is responsible for making entries into the log.
   * @param processStartTime - when the process started. This
   *          value should be the value obtained from the
   *          Benchmark.startPoint() method invoked at the begining of
   *          the process.
   * @param benchmarkID - a unique ID of the resource that should be
   *          logged with this message.
   * @param objectInvokingThisCheckPoint - the benchmarkable object that
   *          invokes this method.
   * @param message - general message.
   * @param features - any features (key-value pairs) that should be
   *          reported in the log message. toString() method will be
   *          invoked on the objects.
   */
  public static void checkPoint(
    String benchmarkID, Object objectInvokingThisCheckPoint, String message,
    FeatureMap benchmarkingFeatures
  ) {
    // build the string to be logged
    StringBuilder messageToLog = new StringBuilder();
    messageToLog.append(
      CHECK_POINT_CODE  + " " + benchmarkID + " " +
      objectInvokingThisCheckPoint.getClass().getName() + " \\\n"
    );
    
    log(messageToLog, message, benchmarkingFeatures);
  }

  /**
   * Helper method
   * @param messageToLog
   * @param userMessage
   */
  private static void log(
    StringBuilder messageToLog, String userMessage, FeatureMap features
  ) {
    messageToLog.append(featureMapToString(features, userMessage)).append("\n");
    logger.info(messageToLog.toString());
  }
  
  /**
   * This method is responsible for making entries into the log.
   * 
   * @param processStartTime - when did the actual process started. This
   *          value should be the value obtained by
   *          Benchmark.startPoint() method invoked at the begining of
   *          the process.
   * @param benchmarkID - a unique ID of the resource that should be
   *          logged with this message.
   * @param objectInvokingThisCheckPoint - The benchmarkable object that
   *          invokes this method.
   * @param features - any features (key-value pairs) that should be
   *          reported in the log message. toString() method will be
   *          invoked on the objects.
   */
  public static void finish(
    long processStartTime, String benchmarkID,
    Object objectInvokingThisCheckPoint, String message,
    FeatureMap benchmarkingFeatures
  ) {
    // we calculate processEndTime here as we don't want to consider
    // the time to convert featureMapToString
    long processingTime = System.currentTimeMillis() - processStartTime;
    
    // finally build the string to be logged
    StringBuilder messageToLog = new StringBuilder();
    messageToLog.append(
      PROCESS_FINISH_CODE + " " + processingTime  + " " + benchmarkID + " " +
      objectInvokingThisCheckPoint.getClass().getName() + " \\\n"
    );

    log(messageToLog, message, benchmarkingFeatures);
  }
  
  /**
   * A utility method to convert the featureBearer into a string
   * representation.
   * 
   * @param features
   * @return
   */
  protected static String featureMapToString(
    FeatureMap features, String userMessage
  ) {

    StringBuilder toReturn = new StringBuilder();
    toReturn.append('[');

    if(userMessage != null) {
      userMessage =
        userMessage.replaceAll("(,)","\\,").replaceAll("(:)", "\\:");
      toReturn.append(Benchmark.MESSAGE_FEATURE+":"+userMessage);
    }
    
    if(features == null || features.isEmpty())
      return toReturn.append("]").toString();

    synchronized(features) {
      for(Object key : features.keySet()) {
        if(toReturn.length() != 1) {
          toReturn.append(", ");
        }

        Object value = features.get(key);
        toReturn.append(
          key.toString().replaceAll("(:)", "\\:").replaceAll("(,)", "\\,") +
          ":"
        );
        toReturn.append(
          value.toString().replaceAll("(:)", "\\:").replaceAll("(,)", "\\,")
        );
      }
      features.notifyAll();
      toReturn.append("]");
      return toReturn.toString();
    }
  }
  
  /**
   * Helper method to generate the benchmark ID.
   * @param resourceName
   * @param parentBenchmarkID
   * @return
   */
  public static String createBenchmarkID(
    String resourceName, String parentBenchmarkID
  ) {
    if(parentBenchmarkID != null) {
      if(resourceName != null) {
        return (parentBenchmarkID + "." + resourceName).replaceAll("[ ]+", "_");
      } else {
        return (parentBenchmarkID + ".null").replaceAll("[ ]+", "_");
      }
    } else {
      if(resourceName != null) {
        return resourceName.replaceAll("[ ]+", "_");
      } else {
        return "null";
      }
    }
    
  }
}
