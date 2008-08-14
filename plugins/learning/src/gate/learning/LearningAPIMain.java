/*
 *  LearningAPIMain.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: LearningAPIMain.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learning;

import gate.Document;
import gate.Factory;
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.Benchmark;
import gate.util.Benchmarkable;
import gate.util.GateException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * The main object of the ML Api. It does initialiation, read parameter values
 * from GUI, and run the selected learning mode. It can also be called by java
 * code, as an API (an GATE class), for using this learning api.
 */
public class LearningAPIMain extends AbstractLanguageAnalyser
                                                             implements
                                                             ProcessingResource,
                                                             Benchmarkable {
  /** This is where the model(s) should be saved */
  private URL configFileURL;
  /**
   * Name of the AnnotationSet contains annotations specified in the DATASET
   * element of configuration file.
   */
  private String inputASName;
  /**
   * The annotationSet for the resulting annotations by application of models.
   */
  private String outputASName;
  /**
   * Run-time parameter learningMode, having three modes: training, application,
   * and evaluation.
   */
  private RunMode learningMode;
  private RunMode learningModeAppl;
  private RunMode learningModeMiTraining;
  private RunMode learningModeVIEWSVMMODEL;
  private RunMode learningModeSelectingDocs;
  /** Learning settings specified in the configuration file. */
  private LearningEngineSettings learningSettings;
  /**
   * The lightweight learning object for getting the features, training and
   * application.
   */
  LightWeightLearningApi lightWeightApi = null;
  /** The File for NLP learning Log. */
  private File logFile;
  /** Used by lightWeightApi, specifying training or application. */
  private boolean isTraining;
  /** Subdirectory for storing the data file produced by learning api. */
  private File wdResults = null;
  /** Doing evaluation. */
  private EvaluationBasedOnDocs evaluation;
  /** The MI learning information object. */
  MiLearningInformation miLearningInfor = null;
  /** The three counters for batch application. */
  int startDocIdApp;
  int endDocIdApp;
  int maxNumApp;

  /** Trivial constructor. */
  public LearningAPIMain() {
    // do nothing
  }

  // featureMap that is used for exporting log messages
  protected java.util.Map benchmarkingFeatures = new HashMap();

  /** Initialise this resource, and return it. */
  public gate.Resource init() throws ResourceInstantiationException {
    fireStatusChanged("Checking and reading learning settings!");
    // here all parameters are needs to be checked
    // check for the model storage directory
    if(configFileURL == null)
      throw new ResourceInstantiationException(
        "WorkingDirectory is required to store the learned model and cannot be null");
    // it is not null, check it is a file: URL
    if(!"file".equals(configFileURL.getProtocol())) { throw new ResourceInstantiationException(
      "WorkingDirectory must be a file: URL"); }

    // Get the working directory which the configuration
    // file reside in.
    File wd = null;
    try {
      wd = new File(configFileURL.toURI()).getParentFile();
    }
    catch(URISyntaxException use) {
      wd = new File(configFileURL.getFile()).getParentFile();
    }

    // it must be a directory
    if(!wd.isDirectory()) { throw new ResourceInstantiationException(wd
      + " must be a reference to directory"); }

    if(LogService.minVerbosityLevel > 0)
      System.out.println("Configuration File=" + configFileURL.toString());

    try {

      if(!new File(configFileURL.toURI()).exists()) {

      throw new ResourceInstantiationException(
        "Error: the configuration file specified does not exist!!"); }

    }
    catch(URISyntaxException e1) {
      e1.printStackTrace();
      throw new ResourceInstantiationException(e1);
    }

    miLearningInfor = new MiLearningInformation();
    try {
      // Load the learning setting file
      // by reading the configuration file
      learningSettings =
        LearningEngineSettings.loadLearningSettingsFromFile(configFileURL);
    }
    catch(Exception e) {
      throw new ResourceInstantiationException(e);
    }

    try {
      // Creat the sub-directory of the workingdirectroy where the data
      // files will be stored in
      if(LogService.minVerbosityLevel > 0) {
        System.out.println("\n\n*************************");
        System.out.println("A new session for NLP learning is starting.\n");
      }

      wdResults =
        new File(wd, gate.learning.ConstantParameters.SUBDIRFORRESULTS);
      wdResults.mkdir();

      logFile =
        new File(new File(wd, ConstantParameters.SUBDIRFORRESULTS),
          ConstantParameters.FILENAMEOFLOGFILE);
      LogService.init(logFile, true, learningSettings.verbosityLogService);

      StringBuffer logMessage = new StringBuffer();
      logMessage.append("\n\n*************************\n");
      logMessage.append("A new session for NLP learning is starting.\n");

      // adding WorkingDirectory parameter in the benchmarkingFeatures
      benchmarkingFeatures.put("workingDirectory", wd.getAbsolutePath());

      logMessage.append("The initiliased time of NLP learning: "
        + new Date().toString() + "\n");
      logMessage.append("Working directory: " + wd.getAbsolutePath() + "\n");
      logMessage.append("The feature files and models are saved at: "
        + wdResults.getAbsolutePath() + "\n");

      // Call the lightWeightLearningApi
      lightWeightApi = new LightWeightLearningApi(wd);

      // more initialisation
      lightWeightApi.furtherInit(wdResults, learningSettings);

      // adding WorkingDirectory parameter in the benchmarkingFeatures
      // benchmarkingFeatures.put("LearnerName",
      // learningSettings.learnerSettings.getLearnerName());
      // benchmarkingFeatures.put("LearnerNickName",
      // learningSettings.learnerSettings.getLearnerNickName());
      // benchmarkingFeatures.put("SurroundMode", learningSettings.surround);

      logMessage.append("Learner name: "
        + learningSettings.learnerSettings.getLearnerName() + "\n");
      logMessage.append("Learner nick name: "
        + learningSettings.learnerSettings.getLearnerNickName() + "\n");
      logMessage.append("Learner parameter settings: "
        + learningSettings.learnerSettings.learnerName + "\n");
      logMessage.append("Surroud mode (or chunk learning): "
        + learningSettings.surround);
      LogService.logMessage(logMessage.toString(), 1);
      LogService.close();
    }
    catch(Exception e) {
      throw new ResourceInstantiationException(e);
    }
    learningModeAppl = RunMode.APPLICATION;
    maxNumApp = learningSettings.docNumIntevalApp;
    learningModeMiTraining = RunMode.MITRAINING;
    learningModeVIEWSVMMODEL = RunMode.VIEWPRIMALFORMMODELS;
    learningModeSelectingDocs = RunMode.RankingDocsForAL;
    fireProcessFinished();
    return this;
  } // init()

  /**
   * Run the resource.
   * 
   * @throws ExecutionException
   */
  public void execute() throws ExecutionException {

    // mode in which the PR is executed
    benchmarkingFeatures.put("learningMode", learningMode);

    if(learningMode.equals(learningModeVIEWSVMMODEL)) {
      if(corpus == null || corpus.size() == 0 || corpus.indexOf(document) == 0)
        lightWeightApi.viewSVMmodelsInNLPFeatures(new File(wdResults,
          ConstantParameters.FILENAMEOFModels), learningSettings);
      return;
    }
    if(learningMode.equals(learningModeSelectingDocs)) {
      // for ordering and selecting the documents for ative learning
      if(corpus == null || corpus.size() == 0 || corpus.indexOf(document) == 0) {
        // ranking the documents
        lightWeightApi.orderDocsWithModels(wdResults, learningSettings);
        // selecting the document
        // lightWeightApi.selectDocForAL()
      }
      return;
    }

    // now we need to see if the corpus is provided
    if(corpus == null)
      throw new ExecutionException("Provided corpus is null!");

    if(corpus.size() == 0)
      throw new ExecutionException("No Document found in corpus!");

    // set benchmark ID on the lightWeightApi
    String oldLightWeightApiId = null;
    if(lightWeightApi instanceof Benchmarkable) {
      oldLightWeightApiId = lightWeightApi.getBenchmarkId();
      lightWeightApi.setBenchmarkId(Benchmark.createBenchmarkId("LightWeightLearningAPI", getBenchmarkId()));
    }

    // first, get the NLP features from the documents, according to the
    // feature types specified in DataSetDefinition file
    int positionDoc = corpus.indexOf(document);

    // first document in the corpus
    if(positionDoc == 0) {
      lightWeightApi.inputASName = inputASName;
      lightWeightApi.outputASName = outputASName;

      /** Obtain the MI learning information of the last time learning. */
      if(learningMode.equals(this.learningModeMiTraining)) {
        miLearningInfor = new MiLearningInformation();
        File miLeFile =
          new File(wdResults, ConstantParameters.FILENAMEOFMILearningInfor);

        long startTime = Benchmark.startPoint();
        benchmarkingFeatures.put("miLearningInformationFile", miLeFile
          .getAbsolutePath());

        miLearningInfor.readDataFromFile(miLeFile);

        Benchmark.checkPoint(startTime, getBenchmarkId() + "."
          + Benchmark.READING_LEARNING_INFO, this, benchmarkingFeatures);
        benchmarkingFeatures.remove("miLearningInformationFile");
      }

      /** Set the information for batch application. */
      startDocIdApp = 0;
      endDocIdApp = 0;

      if(LogService.minVerbosityLevel > 0)
        System.out.println("Pre-processing the " + corpus.size()
          + " documents...");
      try {
        // PrintWriter logFileIn = new PrintWriter(new FileWriter(logFile,
        // true));
        LogService.init(logFile, true, learningSettings.verbosityLogService);
        LogService.logMessage("\n*** A new run starts.", 1);
        LogService.logMessage(
          "\nThe execution time (pre-processing the first document): "
            + new Date().toString(), 1);
        if(LogService.minVerbosityLevel > 0) {
          System.out.println("Learning starts.");
          System.out
            .println("For the information about this learning see the log file "
              + wdResults.getAbsolutePath()
              + File.separator
              + ConstantParameters.FILENAMEOFLOGFILE);
        }
        LogService.close();
        // logFileIn.println("EvaluationMode: " + evaluationMode);
        // logFileIn.println("TrainingMode: " + trainingMode);
        // logFileIn.println("InputAS: " + inputASName);
      }
      catch(IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    // Apply the model to a bunch of documents
    if(learningMode.equals(learningModeAppl)) {
      ++endDocIdApp;
      if(endDocIdApp - startDocIdApp == maxNumApp) {
        try {
          // first checking if the model file is available or not
          String modelFileName =
            wdResults.toString() + File.separator
              + ConstantParameters.FILENAMEOFModels;
          if(!new File(modelFileName).exists()) {
            System.out
              .println("Warning: the model is not available at the moment!!");
            return;
          }
          BufferedWriter outNLPFeatures = null;
          BufferedReader inNLPFeatures = null;
          BufferedWriter outFeatureVectors = null;
          // EvaluationBasedOnDocs.emptyDatafile(wdResults, false);
          if(LogService.minVerbosityLevel > 0)
            System.out.println("** " + "Application mode for document from "
              + startDocIdApp + " to " + endDocIdApp + "(not included):");

          LogService.logMessage("** Application mode for document from "
            + startDocIdApp + " to " + endDocIdApp + "(not included):", 1);
          isTraining = false;
          String classTypeOriginal =
            learningSettings.datasetDefinition.getClassAttribute().getType();

          outNLPFeatures =
            new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream(new File(wdResults,
                ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
          int numDoc;
          numDoc = endDocIdApp - startDocIdApp;

          long startTime = Benchmark.startPoint();
          benchmarkingFeatures.put("numDocs", "" + numDoc);

          for(int i = startDocIdApp; i < endDocIdApp; ++i) {
            Document toProcess = (Document)corpus.get(i);
            lightWeightApi.annotations2NLPFeatures(toProcess,
              i - startDocIdApp, outNLPFeatures, isTraining, learningSettings);
            if(toProcess.getDataStore() != null
              && corpus.getDataStore() != null) {// (isDatastore)
              corpus.getDataStore().sync(corpus);
              Factory.deleteResource(toProcess);
            }
          }
          outNLPFeatures.flush();
          outNLPFeatures.close();

          lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
            learningSettings);

          Benchmark.checkPoint(startTime, getBenchmarkId() + "."
            + Benchmark.ANNOTS_TO_NLP_FEATURES, this, benchmarkingFeatures);

          startTime = Benchmark.startPoint();

          /** Open the normal NLP feature file. */
          inNLPFeatures =
            new BufferedReader(new InputStreamReader(
              new FileInputStream(new File(wdResults,
                ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
          outFeatureVectors =
            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
              new File(wdResults,
                ConstantParameters.FILENAMEOFFeatureVectorDataApp)), "UTF-8"));

          lightWeightApi.nlpfeatures2FVs(wdResults, inNLPFeatures,
            outFeatureVectors, numDoc, isTraining, learningSettings);
          inNLPFeatures.close();
          outFeatureVectors.flush();
          outFeatureVectors.close();

          Benchmark.checkPoint(startTime, getBenchmarkId() + "."
            + Benchmark.NLP_FEATURES_TO_FVS, this, benchmarkingFeatures);

          // Applying th model
          String fvFileName =
            wdResults.toString() + File.separator
              + ConstantParameters.FILENAMEOFFeatureVectorDataApp;

          startTime = Benchmark.startPoint();

          lightWeightApi.applyModelInJava(corpus, startDocIdApp, endDocIdApp,
            classTypeOriginal, learningSettings, fvFileName);

          Benchmark.checkPoint(startTime, getBenchmarkId() + "."
            + Benchmark.MODEL_APPLICATION, this, benchmarkingFeatures);
          benchmarkingFeatures.remove("numDocs");

          startDocIdApp = endDocIdApp;
        }
        catch(IOException e) {
          e.printStackTrace();
        }
        catch(GateException e) {
          e.printStackTrace();
        }
      }
    }

    // we've reached the last document
    if(positionDoc == corpus.size() - 1) {
      // first select the training data and test data according to the
      // learning setting
      // set the inputASName in here, because it is a runtime parameter
      int numDoc = corpus.size();

      try {
        LogService.init(logFile, true, learningSettings.verbosityLogService);
        LogService.logMessage("The learning start at " + new Date().toString(),
          1);
        LogService.logMessage("The number of documents in dataset: " + numDoc,
          1);
        // Open the NLP feature file for storing the NLP feature vectors
        BufferedWriter outNLPFeatures = null;
        BufferedReader inNLPFeatures = null;
        BufferedWriter outFeatureVectors = null;
        // if only need the feature data
        switch(learningMode){
          case ProduceFeatureFilesOnly:
            // if only want feature data
            EvaluationBasedOnDocs.emptyDatafile(wdResults, true);
            if(LogService.minVerbosityLevel > 0)
              System.out.println("** Producing the feature files only!");
            LogService.logMessage("** Producing the feature files only!", 1);

            long startTime = Benchmark.startPoint();
            benchmarkingFeatures.put("numDocs", numDoc);

            isTraining = true;
            outNLPFeatures =
              new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(wdResults,
                  ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
            for(int i = 0; i < numDoc; ++i) {
              Document toProcess = (Document)corpus.get(i);
              lightWeightApi.annotations2NLPFeatures(toProcess, i,
                outNLPFeatures, isTraining, learningSettings);
              if(toProcess.getDataStore() != null && corpus.getDataStore() != null)
                Factory.deleteResource(toProcess);
            }
            outNLPFeatures.flush();
            outNLPFeatures.close();

            lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
              learningSettings);
            Benchmark.checkPoint(startTime, getBenchmarkId() + "."
              + Benchmark.ANNOTS_TO_NLP_FEATURES, this, benchmarkingFeatures);

            /** Open the normal NLP feature file. */
            inNLPFeatures =
              new BufferedReader(new InputStreamReader(new FileInputStream(
                new File(wdResults,
                  ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
            outFeatureVectors =
              new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(wdResults,
                  ConstantParameters.FILENAMEOFFeatureVectorData)), "UTF-8"));

            startTime = Benchmark.startPoint();

            lightWeightApi.nlpfeatures2FVs(wdResults, inNLPFeatures,
              outFeatureVectors, numDoc, isTraining, learningSettings);
            inNLPFeatures.close();
            outFeatureVectors.flush();
            outFeatureVectors.close();
            Benchmark.checkPoint(startTime, getBenchmarkId() + "."
              + Benchmark.NLP_FEATURES_TO_FVS, this, benchmarkingFeatures);

            // produce the ngram language model from feature list
            if(LogService.minVerbosityLevel > 0)
              System.out
                .println("Write the language model in N-grams into the file "
                  + ConstantParameters.FILENAMEOFNgramLM + "!");
            LogService.logMessage(
              "Write the language model in N-grams into the file "
                + ConstantParameters.FILENAMEOFNgramLM + "!", 1);

            if(learningSettings.datasetDefinition.getNgrams().size() >= 1) {

              startTime = Benchmark.startPoint();

              lightWeightApi.featureList2LM(wdResults,
                ((Ngram)learningSettings.datasetDefinition.getNgrams().get(0))
                  .getNumber());

              Benchmark.checkPoint(startTime, getBenchmarkId() + "."
                + Benchmark.WRITING_NGRAM_MODEL, this, benchmarkingFeatures);

              // produce the term-frequency matrix
              if(LogService.minVerbosityLevel > 0)
                System.out
                  .println("Write the term-document statistics into the file "
                    + ConstantParameters.FILENAMEOFTermFreqMatrix + "!");
              LogService.logMessage(
                "Write the term-document statistics into the file "
                  + ConstantParameters.FILENAMEOFTermFreqMatrix + "!", 1);

              startTime = Benchmark.startPoint();

              lightWeightApi.termfrequenceMatrix(wdResults, numDoc);

              Benchmark.checkPoint(startTime, getBenchmarkId() + "."
                + Benchmark.TERM_DOC_STATS, this, benchmarkingFeatures);

            }
            else {
              System.out
                .println("!! Warning: cannot produce N-gram data because there is no Ngram "
                  + "defintion in the configuration file!");
            }

            benchmarkingFeatures.remove("numDocs");

            // Write the name of documents and total number of them into a file
            BufferedWriter outDocsName =
              new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(wdResults, ConstantParameters.FILENAMEOFDocsName)),
                "UTF-8"));
            outDocsName.append("##totalDocs=" + numDoc);
            outDocsName.newLine();
            for(int i = 0; i < numDoc; ++i) {
              String docN = ((Document)corpus.get(i)).getName();
              if(docN.contains("_"))
                docN = docN.substring(0, docN.lastIndexOf("_"));
              outDocsName.append(docN);
              outDocsName.newLine();
            }
            outDocsName.flush();
            outDocsName.close();

            // Create the document for storing the names of selected documents
            // if it doesn't exist.
            File selectedFile =
              new File(wdResults, ConstantParameters.FILENAMEOFSelectedDOCForAL);
            if(!selectedFile.exists()) selectedFile.createNewFile();

            if(LogService.minVerbosityLevel > 0) displayDataFilesInformation();
            break;
          case TRAINING:
            // empty the data file
            Long tm1,
            tm2,
            tm3;
            if(LogService.DEBUG > 1) {
              tm1 = new Date().getTime();
            }
            EvaluationBasedOnDocs.emptyDatafile(wdResults, true);
            if(LogService.minVerbosityLevel > 0)
              System.out.println("** Training mode:");
            LogService.logMessage("** Training mode:", 1);

            startTime = Benchmark.startPoint();
            benchmarkingFeatures.put("numDocs", "" + numDoc);

            isTraining = true;
            outNLPFeatures =
              new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(wdResults,
                  ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
            for(int i = 0; i < numDoc; ++i) {
              Document toProcess = (Document)corpus.get(i);
              lightWeightApi.annotations2NLPFeatures(toProcess, i,
                outNLPFeatures, isTraining, learningSettings);
              if(toProcess.getDataStore() != null && corpus.getDataStore() != null)
                Factory.deleteResource(toProcess);
            }
            outNLPFeatures.flush();
            outNLPFeatures.close();
            lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
              learningSettings);

            Benchmark.checkPoint(startTime, getBenchmarkId() + "."
              + Benchmark.ANNOTS_TO_NLP_FEATURES, this, benchmarkingFeatures);

            if(LogService.DEBUG > 1) {
              tm2 = new Date().getTime();
              tm3 = tm2 - tm1;
              tm3 /= 1000;
              System.out.println("time for NLP features: " + tm3);
            }
            /** Open the normal NLP feature file. */
            inNLPFeatures =
              new BufferedReader(new InputStreamReader(new FileInputStream(
                new File(wdResults,
                  ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
            outFeatureVectors =
              new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(wdResults,
                  ConstantParameters.FILENAMEOFFeatureVectorData)), "UTF-8"));

            startTime = Benchmark.startPoint();

            lightWeightApi.nlpfeatures2FVs(wdResults, inNLPFeatures,
              outFeatureVectors, numDoc, isTraining, learningSettings);

            inNLPFeatures.close();
            outFeatureVectors.flush();
            outFeatureVectors.close();

            Benchmark.checkPoint(startTime, getBenchmarkId() + "."
              + Benchmark.NLP_FEATURES_TO_FVS, this, benchmarkingFeatures);

            if(LogService.DEBUG > 1) {
              tm1 = new Date().getTime();
              tm3 = tm1 - tm2;
              tm3 /= 1000;
              System.out.println("time for fv: " + tm3);
            }
            // if fitering the training data
            if(learningSettings.fiteringTrainingData
              && learningSettings.filteringRatio > 0.0) {

              startTime = Benchmark.startPoint();

              lightWeightApi.FilteringNegativeInstsInJava(corpus.size(),
                learningSettings);

              Benchmark.checkPoint(startTime, getBenchmarkId() + "."
                + Benchmark.FILTERING, this, benchmarkingFeatures);
            }

            if(LogService.DEBUG > 1) {
              tm2 = new Date().getTime();
              tm3 = tm2 - tm1;
              tm3 /= 1000;
              System.out.println("time for filtering: " + tm3);
            }

            startTime = Benchmark.startPoint();

            // using the java code for training
            lightWeightApi.trainingJava(corpus.size(), learningSettings);

            Benchmark.checkPoint(startTime, getBenchmarkId() + "."
              + Benchmark.MODEL_TRAINING, this, benchmarkingFeatures);
            benchmarkingFeatures.remove("numDocs");

            if(LogService.DEBUG > 1) {
              tm1 = new Date().getTime();
              tm3 = tm1 - tm2;
              tm3 /= 1000;
              System.out.println("time for NLP training: " + tm3);
            }
            break;
          case APPLICATION:

            // first checking if the model file is available or not
            String modelFileName =
              wdResults.toString() + File.separator
                + ConstantParameters.FILENAMEOFModels;
            if(!new File(modelFileName).exists()) {
              System.out
                .println("Warning: the model is not available at the moment!!");
              return;
            }
            if(endDocIdApp > startDocIdApp) {
              if(LogService.minVerbosityLevel > 0)
                System.out.println("** "
                  + "Application mode for document from " + startDocIdApp
                  + " to " + endDocIdApp + "(not included):");
              LogService.logMessage("** Application mode for document from "
                + startDocIdApp + " to " + endDocIdApp + "(not included):", 1);

              isTraining = false;
              String classTypeOriginal =
                learningSettings.datasetDefinition.getClassAttribute()
                  .getType();
              outNLPFeatures =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                  new File(wdResults,
                    ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
              numDoc = endDocIdApp - startDocIdApp;

              benchmarkingFeatures.put("numDocs", "" + numDoc);
              startTime = Benchmark.startPoint();

              for(int i = startDocIdApp; i < endDocIdApp; ++i) {
                Document toProcess = (Document)corpus.get(i);
                lightWeightApi
                  .annotations2NLPFeatures(toProcess, i - startDocIdApp,
                    outNLPFeatures, isTraining, learningSettings);
                if(toProcess.getDataStore() != null
                  && corpus.getDataStore() != null) {// (isDatastore)
                  Factory.deleteResource(toProcess);
                  corpus.getDataStore().sync(corpus);
                }
              }
              outNLPFeatures.flush();
              outNLPFeatures.close();

              lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
                learningSettings);

              Benchmark.checkPoint(startTime, getBenchmarkId() + "."
                + Benchmark.ANNOTS_TO_NLP_FEATURES, this, benchmarkingFeatures);

              /** Open the normal NLP feature file. */
              inNLPFeatures =
                new BufferedReader(new InputStreamReader(new FileInputStream(
                  new File(wdResults,
                    ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
              outFeatureVectors =
                new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                  new File(wdResults,
                    ConstantParameters.FILENAMEOFFeatureVectorDataApp)),
                  "UTF-8"));

              startTime = Benchmark.startPoint();

              lightWeightApi.nlpfeatures2FVs(wdResults, inNLPFeatures,
                outFeatureVectors, numDoc, isTraining, learningSettings);

              inNLPFeatures.close();
              outFeatureVectors.flush();
              outFeatureVectors.close();

              Benchmark.checkPoint(startTime, getBenchmarkId() + "."
                + Benchmark.NLP_FEATURES_TO_FVS, this, benchmarkingFeatures);

              // Applying th model
              String fvFileName =
                wdResults.toString() + File.separator
                  + ConstantParameters.FILENAMEOFFeatureVectorDataApp;

              startTime = Benchmark.startPoint();

              lightWeightApi.applyModelInJava(corpus, startDocIdApp,
                endDocIdApp, classTypeOriginal, learningSettings, fvFileName);

              Benchmark.checkPoint(startTime, getBenchmarkId() + "."
                + Benchmark.MODEL_APPLICATION, this, benchmarkingFeatures);
              benchmarkingFeatures.remove("numDocs");
              // Update the datastore for the added annotations
            }
            break;
          case EVALUATION:
            if(LogService.minVerbosityLevel > 0)
              System.out.println("** Evaluation mode:");
            LogService.logMessage("** Evaluation mode:", 1);
            evaluation =
              new EvaluationBasedOnDocs(corpus, wdResults, inputASName);

            benchmarkingFeatures.put("numDocs", corpus.size());
            startTime = Benchmark.startPoint();

            evaluation.evaluation(learningSettings, lightWeightApi);
            Benchmark.checkPoint(startTime, getBenchmarkId() + "."
              + Benchmark.EVALUATION, this, benchmarkingFeatures);
            benchmarkingFeatures.remove("numDocs");

            break;
          case MITRAINING:
            if(LogService.minVerbosityLevel > 0)
              System.out.println("** MITRAINING mode:");
            LogService.logMessage("** MITRAINING mode:", 1);
            isTraining = true;

            benchmarkingFeatures.put("numDocs", "" + numDoc);
            startTime = Benchmark.startPoint();

            /**
             * Need to write the NLP features into a temporary file, then copy
             * it into the NLP file.
             */
            BufferedWriter outNLPFeaturesTemp =
              new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(wdResults,
                  ConstantParameters.FILENAMEOFNLPFeaturesDataTemp)), "UTF-8"));
            for(int i = 0; i < numDoc; ++i) {
              lightWeightApi.annotations2NLPFeatures((Document)corpus.get(i),
                i, outNLPFeaturesTemp, isTraining, learningSettings);
            }

            outNLPFeaturesTemp.flush();
            outNLPFeaturesTemp.close();
            lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
              learningSettings);
            Benchmark.checkPoint(startTime, getBenchmarkId() + "."
              + Benchmark.ANNOTS_TO_NLP_FEATURES, this, benchmarkingFeatures);

            lightWeightApi.copyNLPFeat2NormalFile(wdResults,
              miLearningInfor.miNumDocsTraining);
            /**
             * Use the temp NLP feature file instead of the normal one for
             * MI-training.
             */
            inNLPFeatures =
              new BufferedReader(new InputStreamReader(new FileInputStream(
                new File(wdResults,
                  ConstantParameters.FILENAMEOFNLPFeaturesDataTemp)), "UTF-8"));
            outFeatureVectors =
              new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(wdResults,
                  ConstantParameters.FILENAMEOFFeatureVectorData), true),
                "UTF-8"));

            startTime = Benchmark.startPoint();

            lightWeightApi.nlpfeatures2FVs(wdResults, inNLPFeatures,
              outFeatureVectors, numDoc, isTraining, learningSettings);
            inNLPFeatures.close();
            outFeatureVectors.flush();
            outFeatureVectors.close();

            Benchmark.checkPoint(startTime, getBenchmarkId() + "."
              + Benchmark.NLP_FEATURES_TO_FVS, this, benchmarkingFeatures);

            System.gc(); // to make effort to delete the files.
            miLearningInfor.miNumDocsTraining += numDoc;
            miLearningInfor.miNumDocsFromLast += numDoc;
            if(miLearningInfor.miNumDocsFromLast >= learningSettings.miDocInterval) {
              // Start learning
              // if fitering the training data
              if(learningSettings.fiteringTrainingData
                && learningSettings.filteringRatio > 0.0) {

                benchmarkingFeatures.put("numDocs",
                  miLearningInfor.miNumDocsTraining + "");
                startTime = Benchmark.startPoint();

                lightWeightApi.FilteringNegativeInstsInJava(
                  miLearningInfor.miNumDocsTraining, learningSettings);

                Benchmark.checkPoint(startTime, getBenchmarkId() + "."
                  + Benchmark.FILTERING, this, benchmarkingFeatures);
              }

              startTime = Benchmark.startPoint();

              // using the java code for training
              lightWeightApi.trainingJava(miLearningInfor.miNumDocsTraining,
                learningSettings);

              Benchmark.checkPoint(startTime, getBenchmarkId() + "." + "."
                + Benchmark.MODEL_TRAINING, this, benchmarkingFeatures);
              benchmarkingFeatures.remove("numDocs");

              // Reset the num from last training as 0
              miLearningInfor.miNumDocsFromLast = 0;
            }
            File miLeFile =
              new File(wdResults, ConstantParameters.FILENAMEOFMILearningInfor);
            miLearningInfor.writeDataIntoFile(miLeFile);
            break;
          default:
            throw new GateException("The learning mode is not defined!");
        }
        LogService.logMessage("This learning session finished!.", 1);
        LogService.close();
      }
      catch(IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch(GateException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // reset the parentBenchmarkID
      if(oldLightWeightApiId != null) {
        lightWeightApi.setBenchmarkId(oldLightWeightApiId);
      }

      if(LogService.minVerbosityLevel > 0)
        System.out.println("This learning session finished!.");
    } // end of learning (position=corpus.size()-1)
  }

  /** Print out the information for featureData only option. */
  private void displayDataFilesInformation() {
    StringBuffer logMessage = new StringBuffer();
    logMessage.append("NLP features for all the documents are in the file"
      + wdResults.getAbsolutePath() + File.separator
      + ConstantParameters.FILENAMEOFNLPFeaturesData + "\n");
    logMessage.append("Feature vectors in sparse format are in the file"
      + wdResults.getAbsolutePath() + File.separator
      + ConstantParameters.FILENAMEOFFeatureVectorData + "\n");
    logMessage.append("Label list is in the file" + wdResults.getAbsolutePath()
      + File.separator + ConstantParameters.FILENAMEOFLabelList + "\n");
    logMessage.append("NLP features list is in the file"
      + wdResults.getAbsolutePath() + File.separator
      + ConstantParameters.FILENAMEOFNLPFeatureList + "\n");
    logMessage
      .append("The statistics of entity length for each class is in the file"
        + wdResults.getAbsolutePath() + File.separator
        + ConstantParameters.FILENAMEOFChunkLenStats + "\n");
    System.out.println(logMessage.toString());
    LogService.logMessage(logMessage.toString(), 1);
  }

  public void setConfigFileURL(URL workingDirectory) {
    this.configFileURL = workingDirectory;
  }

  public URL getConfigFileURL() {
    return this.configFileURL;
  }

  public void setInputASName(String iasn) {
    this.inputASName = iasn;
  }

  public String getInputASName() {
    return this.inputASName;
  }

  public void setOutputASName(String iasn) {
    this.outputASName = iasn;
  }

  public String getOutputASName() {
    return this.outputASName;
  }

  public RunMode getLearningMode() {
    return this.learningMode;
  }

  public void setLearningMode(RunMode learningM) {
    this.learningMode = learningM;
  }

  public EvaluationBasedOnDocs getEvaluation() {
    return evaluation;
  }

  public EvaluationBasedOnDocs setEvaluation(EvaluationBasedOnDocs eval) {
    return this.evaluation = eval;
  }

  // /////// Benchmarkable ////////////////

  private String benchmarkID;

  /**
   * Returns the benchmark ID of this resource.
   * 
   * @return
   */
  public String getBenchmarkId() {
    if(this.benchmarkID == null) {
      benchmarkID = getName().replaceAll("[ ]+", "_");
      ;
    }
    return this.benchmarkID;
  }
  
  public void setBenchmarkId(String benchmarkID) {
    this.benchmarkID = benchmarkID;
  }

}
