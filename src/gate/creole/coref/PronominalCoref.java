/*
 *  PronominalCoref.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 30/Dec/2001
 *
 *  $Id$
 */

package gate.creole.coref;

import java.util.*;

import junit.framework.*;

import gate.*;
import gate.creole.*;
import gate.util.*;

public class PronominalCoref extends AbstractProcessingResource
                              implements ProcessingResource{


  //annotation types
  private static final String PERSON_TYPE = "Person";
  private static final String ORG_TYPE = "Organization";
  private static final String LOC_TYPE = "Location";
  private static final String TOKEN_TYPE = "Token";
  private static final String SENTENCE_TYPE = "Sentence";

  //annotation features
  private static final String PRP_CATEGORY = "PRP";
  private static final String PRP$_CATEGORY = "PRP$";
  private static final String TOKEN_CATEGORY = "category";
  private static final String TOKEN_STRING = "string";
  private static final String PERSON_GENDER = "gender";
  private static final String PERSON_ORTHO_COREF = "matches";

  //scope
  private static final int SENTENCES_IN_SCOPE = 3;

//  private static final SentenceComparator SENTENCE_COMPARATOR;
  private static final AnnotationComparator ANNOTATION_COMPARATOR;

  private Document  doc;
  private AnnotationSet defaultAnnotations;
  private Sentence[] textSentences;
  private HashMap personGender;

  static {
//    SENTENCE_COMPARATOR = new SentenceComparator();
    ANNOTATION_COMPARATOR = new AnnotationComparator();
  }

  public PronominalCoref() {
    personGender = new HashMap();
  }

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    return super.init();
  } // init()

  /**
   * Reinitialises the processing resource. After calling this method the
   * resource should be in the state it is after calling init.
   * If the resource depends on external resources (such as rules files) then
   * the resource will re-read those resources. If the data used to create
   * the resource has changed since the resource has been created then the
   * resource will change too after calling reInit().
  */
  public void reInit() throws ResourceInstantiationException {
    init();
  } // reInit()

  /** Get the document we're running on. */
  public Document getDocument() {
    return this.doc;
  }

  /** Set the document to run on. */
  public void setDocument(Document newDocument) {
    Assert.assertNotNull(newDocument);
    this.doc = newDocument;
  }

  /**
   * This method runs the coreferencer. It assumes that all the needed parameters
   * are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException{

    if(null == this.doc) {
      throw new ExecutionException("[coreference] Document is not set!");
    }

    preprocess();

    //get personal pronouns
    FeatureMap constraintPRP = new SimpleFeatureMapImpl();
    constraintPRP.put("category",PRP_CATEGORY);
    AnnotationSet personalPronouns = this.defaultAnnotations.get(TOKEN_TYPE,constraintPRP);

    //get possesive pronouns
    FeatureMap constraintPRP$ = new SimpleFeatureMapImpl();
    constraintPRP$.put("category",PRP$_CATEGORY);
    AnnotationSet possesivePronouns = this.defaultAnnotations.get(TOKEN_TYPE,constraintPRP$);

    //combine them
    AnnotationSet pronouns = personalPronouns;
    pronouns.addAll(possesivePronouns);

    //sort them according to offset
    Object[] arrPronouns = pronouns.toArray();
    java.util.Arrays.sort(arrPronouns,ANNOTATION_COMPARATOR);

    //cleanup - ease the GC
    pronouns = personalPronouns = possesivePronouns = null;

    int prnSentIndex = 0;

    for (int i=0; i< arrPronouns.length; i++) {
      Annotation currPronoun = (Annotation)arrPronouns[i];
      while (this.textSentences[prnSentIndex].getEndOffset().longValue() <
                                      currPronoun.getStartNode().getOffset().longValue()) {
        prnSentIndex++;
      }

      Sentence currSentence = this.textSentences[prnSentIndex];
      Assert.assertTrue(currSentence.getStartOffset().longValue() <= currPronoun.getStartNode().getOffset().longValue());
      Assert.assertTrue(currSentence.getEndOffset().longValue() >= currPronoun.getEndNode().getOffset().longValue());

      Annotation antc = findAntecedent(currPronoun,prnSentIndex);
    }

  }

  private Annotation findAntecedent(Annotation currPronoun,int prnSentIndex) {

    //0. preconditions
    Assert.assertNotNull(currPronoun);
    Assert.assertTrue(prnSentIndex >= 0);
    Assert.assertTrue(currPronoun.getType().equals(TOKEN_TYPE));
    Assert.assertTrue(currPronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY) ||
                      currPronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP$_CATEGORY));

    String strPronoun = (String)currPronoun.getFeatures().get(TOKEN_STRING);

    if (strPronoun.equalsIgnoreCase("HE") ||
        strPronoun.equalsIgnoreCase("HIS")) {
      return _resolve$HE$HIM$HIS$(currPronoun,prnSentIndex);
    }
    else if (strPronoun.equalsIgnoreCase("SHE") ||
              strPronoun.equalsIgnoreCase("HER")) {
      return this._resolve$SHE$HER$(currPronoun,prnSentIndex);
    }
    else {
//      throw new MethodNotImplementedException();
      gate.util.Err.println("["+strPronoun+"] is not handled yet...");
      return null;
    }
  }


  private Annotation _resolve$HE$HIM$HIS$(Annotation pronoun, int sentenceIndex) {

    //0. preconditions
    Assert.assertTrue(pronoun.getType().equals(TOKEN_TYPE));
    Assert.assertTrue(pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY) ||
                      pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP$_CATEGORY));
    String pronounString = (String)pronoun.getFeatures().get(TOKEN_STRING);
    Assert.assertTrue(pronounString.equalsIgnoreCase("HE") ||
                      pronounString.equalsIgnoreCase("HIM") ||
                      pronounString.equalsIgnoreCase("HIS"));

    //1.
    boolean antecedentFound = false;
    int scopeFirstIndex = sentenceIndex - SENTENCES_IN_SCOPE;
    if (scopeFirstIndex < 0 ) scopeFirstIndex = 0;

    int currSentenceIndex = sentenceIndex;
    Annotation bestAntecedent = null;

    while (currSentenceIndex >= scopeFirstIndex || antecedentFound == false) {
      Sentence currSentence = this.textSentences[currSentenceIndex];
      AnnotationSet persons = currSentence.getPersons();

      Iterator it = persons.iterator();
      while (it.hasNext()) {
        Annotation currPerson = (Annotation)it.next();
        String gender = (String)this.personGender.get(currPerson);

        if (null == gender ||
            gender.equalsIgnoreCase("MALE") ||
            gender.equalsIgnoreCase("UNKNOWN")) {
          //hit
          antecedentFound = true;

          if (null == bestAntecedent) {
            bestAntecedent = currPerson;
          }
          else {
            bestAntecedent = chooseAntecedent(bestAntecedent,currPerson,pronoun);
          }
        }
      }

      if (0 == currSentenceIndex--)
        break;

    }
gate.util.Err.println("found antecedent for ["+pronounString+"] : " + bestAntecedent);
    return bestAntecedent;
  }


  private Annotation _resolve$SHE$HER$(Annotation pronoun, int sentenceIndex) {

    //0. preconditions
    Assert.assertTrue(pronoun.getType().equals(TOKEN_TYPE));
    Assert.assertTrue(pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY) ||
                      pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP$_CATEGORY));
    String pronounString = (String)pronoun.getFeatures().get(TOKEN_STRING);
    Assert.assertTrue(pronounString.equalsIgnoreCase("SHE") ||
                      pronounString.equalsIgnoreCase("HER"));

    //1.
    boolean antecedentFound = false;
    int scopeFirstIndex = sentenceIndex - SENTENCES_IN_SCOPE;
    if (scopeFirstIndex < 0 ) scopeFirstIndex = 0;
    int currSentenceIndex = sentenceIndex;
    Annotation bestAntecedent = null;

    while (currSentenceIndex >= scopeFirstIndex || antecedentFound == false) {
      Sentence currSentence = this.textSentences[currSentenceIndex];
      AnnotationSet persons = currSentence.getPersons();

      Iterator it = persons.iterator();
      while (it.hasNext()) {
        Annotation currPerson = (Annotation)it.next();
        String gender = (String)this.personGender.get(currPerson);

        if (null == gender ||
            gender.equalsIgnoreCase("FEMALE") ||
            gender.equalsIgnoreCase("UNKNOWN")) {
          //hit
          antecedentFound = true;

          if (null == bestAntecedent) {
            bestAntecedent = currPerson;
          }
          else {
            bestAntecedent = chooseAntecedent(bestAntecedent,currPerson,pronoun);
          }
        }
      }

      if (0 == currSentenceIndex--)
        break;
    }

gate.util.Err.println("found antecedent for ["+pronounString+"] : " + bestAntecedent);
    return bestAntecedent;
  }

  private Annotation _resolve$IT$ITS$(String pronoun) {
    throw new MethodNotImplementedException();
  }

  private void preprocess() {

    //1.get all annotation in the default set
    this.defaultAnnotations = this.doc.getAnnotations();

    //2.get all SENTENCE annotations
    AnnotationSet sentenceAnnotations = this.defaultAnnotations.get(SENTENCE_TYPE);

    this.textSentences = new Sentence[sentenceAnnotations.size()];
    Object[]  sentenceArray = sentenceAnnotations.toArray();

    java.util.Arrays.sort(sentenceArray,ANNOTATION_COMPARATOR);

    for (int i=0; i< sentenceArray.length; i++) {

      Annotation currSentence = (Annotation)sentenceArray[i];
      Long sentStartOffset = currSentence.getStartNode().getOffset();
      Long sentEndOffset = currSentence.getEndNode().getOffset();

      //2.1. get PERSOSNS in this sentence
      AnnotationSet sentPersons = this.defaultAnnotations.get(this.PERSON_TYPE,
                                                              sentStartOffset,
                                                              sentEndOffset);

      //2.2. get ORGANIZATIONS in this sentence
      AnnotationSet sentOrgs = this.defaultAnnotations.get(this.ORG_TYPE,
                                                              sentStartOffset,
                                                              sentEndOffset);

      //2.3. get LOCATION in this sentence
      AnnotationSet sentLocs = this.defaultAnnotations.get(this.LOC_TYPE,
                                                              sentStartOffset,
                                                              sentEndOffset);

      //2.4. create a Sentence for thei SENTENCE annotation
      this.textSentences[i] = new Sentence(i,
                                            0,
                                            sentStartOffset,
                                            sentEndOffset,
                                            sentPersons,
                                            sentOrgs,
                                            sentLocs
                                  );

      //2.5. for all PERSONs in the sentence - find their gender using the
      //orthographic coreferences if the gender of some entity is unknown
      Iterator itPersons = sentPersons.iterator();
      while (itPersons.hasNext()) {
        Annotation currPerson = (Annotation)itPersons.next();
        String gender = this.findPersonGender(currPerson);
        this.personGender.put(currPerson,gender);
      }
    }

  }


  private String findPersonGender(Annotation person) {

    String result = (String)person.getFeatures().get(PERSON_GENDER);

    if (null==result) {
      //gender is unknown - try to find it from the ortho coreferences
      List orthoMatches = (List)person.getFeatures().get(PERSON_ORTHO_COREF);

      if (null != orthoMatches) {
        Iterator itMatches = orthoMatches.iterator();

        while (itMatches.hasNext()) {
          Integer correferringID = (Integer)itMatches.next();
          Annotation coreferringEntity = this.defaultAnnotations.get(correferringID);
          Assert.assertTrue(coreferringEntity.getType().equalsIgnoreCase(PERSON_TYPE));
          String correferringGender = (String)coreferringEntity.getFeatures().get(PERSON_GENDER);

          if (null != correferringGender) {
            result = correferringGender;
            break;
          }
        }
      }
    }

    return result;
  }

//  private int findSentenceNumber(Annotation ann) {
//    int index = Arrays.binarySearch(this.textSentences,ann.getStartNode().getOffset(),SENTENCE_COMPARATOR);
//  }

  private static class AnnotationComparator implements Comparator {

    public int compare(Object o1,Object o2) {

      Assert.assertNotNull(o1);
      Assert.assertNotNull(o2);
      Assert.assertTrue(o1 instanceof Annotation);
      Assert.assertTrue(o2 instanceof Annotation);

      Annotation a1 = (Annotation)o1;
      Annotation a2 = (Annotation)o2;

      return (a1.getEndNode().getOffset().intValue() - a2.getEndNode().getOffset().intValue());
    }
  }


  private Annotation chooseAntecedent(Annotation ant1, Annotation ant2, Annotation pronoun) {

    //0. preconditions
    Assert.assertNotNull(ant1);
    Assert.assertNotNull(ant2);
    Assert.assertNotNull(pronoun);
    Assert.assertTrue(pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY) ||
                      pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP$_CATEGORY));
    String pronounString = (String)pronoun.getFeatures().get(TOKEN_STRING);
    Assert.assertTrue(pronounString.equalsIgnoreCase("SHE") ||
                      pronounString.equalsIgnoreCase("HER") ||
                      pronounString.equalsIgnoreCase("HE") ||
                      pronounString.equalsIgnoreCase("HIS"));

    if (pronounString.equalsIgnoreCase("HE") ||
        pronounString.equalsIgnoreCase("HIS")||
        pronounString.equalsIgnoreCase("SHE") ||
        pronounString.equalsIgnoreCase("HER")) {

      Long offset1 = ant1.getStartNode().getOffset();
      Long offset2 = ant2.getStartNode().getOffset();
      Long offsetPrn = pronoun.getStartNode().getOffset();

      long diff1 = offsetPrn.longValue() - offset1.longValue();
      long diff2 = offsetPrn.longValue() - offset2.longValue();
      Assert.assertTrue(diff1 != 0 && diff2 != 0);

      //get the one CLOSEST AND PRECEDING the pronoun
      if (diff1 > 0 && diff2 > 0) {
        //we have [...antecedentA...AntecedentB....pronoun...] ==> choose B
        if (diff1 < diff2)
          return ant1;
        else
          return ant2;
      }
      else if (diff1 < 0 && diff2 < 0){
        //we have [...pronoun ...antecedentA...AntecedentB.......] ==> choose A
        if (Math.abs(diff1) < Math.abs(diff2))
          return ant1;
        else
          return ant2;
      }
      else {
        Assert.assertTrue(Math.abs(diff1 + diff2) < Math.abs(diff1) + Math.abs(diff2));
        //we have [antecedentA...pronoun...AntecedentB] ==> choose A
        if (diff1 > 0)
          return ant1;
        else
          return ant2;
      }

    }
    else {
      throw new MethodNotImplementedException();
    }
  }

/*  private static class SentenceComparator implements Comparator {

    public int compare(Object o1,Object o2) {

      Assert.assertNotNull(o1);
      Assert.assertNotNull(o2);
      Assert.assertTrue(o1 instanceof Sentence);
      Assert.assertTrue(o2 instanceof Long);

      Sentence s = (Sentence)o1;
      Long offset = (Long)o2;
      return s.getStartOffset().longValue() - offset.intValue();
    }
  }
*/

  private class Sentence {

    private int sentNumber;
    private int paraNumber;
    private Long startOffset;
    private Long endOffset;

    private AnnotationSet persons;
    private AnnotationSet organizations;
    private AnnotationSet locations;


    public Sentence(int sentNumber,
                    int paraNumber,
                    Long startOffset,
                    Long endOffset,
                    AnnotationSet persons,
                    AnnotationSet organizations,
                    AnnotationSet locations) {

      this.sentNumber = sentNumber;
      this.paraNumber = paraNumber;
      this.startOffset = startOffset;
      this.endOffset = endOffset;
      this.persons = persons;
      this.organizations = organizations;
      this.locations = locations;
    }

    public void addEntity(Annotation entAnnotation,String entType) {
    }

    public Long getStartOffset() {
      return this.startOffset;
    }

    public Long getEndOffset() {
      return this.endOffset;
    }

    public AnnotationSet getPersons() {
      return this.persons;
    }

    public AnnotationSet getOrganizations() {
      return this.organizations;
    }

    public AnnotationSet getLocations() {
      return this.locations;
    }
  }


}