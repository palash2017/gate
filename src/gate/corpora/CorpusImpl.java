/*
 *  CorpusImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 11/Feb/2000
 *
 *  $Id$
 */

package gate.corpora;

import java.util.*;

import gate.*;
import gate.util.*;
import gate.annotation.*;
import gate.persist.*;
import java.io.*;
import java.net.*;

/** Corpora are sets of Document. They are ordered by lexicographic collation
  * on Url.
  */
public class CorpusImpl extends TreeSet implements Corpus {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public CorpusImpl() {
  } // Construction

  /** The data store this LR lives in. */
  protected transient DataStore dataStore;

  /** Initialise this resource, and return it. */
  public Resource init() {
    return this;
  } // init()

  /** Get the name of the corpus. */
  public String getName() { return name; }

  /** Set the name of the corpus. */
  public void setName(String name) { this.name = name; }

  /** Get the data store the document lives in. */
  public DataStore getDataStore() {
    return dataStore;
  }

  /** Set the data store that this LR lives in. */
  public void setDataStore(DataStore dataStore) throws PersistenceException {
    this.dataStore = dataStore;
  } // setDataStore(DS)

  /** Save: synchonise the in-memory image of the corpus with the persistent
    * image.
    */
  public void sync() throws PersistenceException {
    if(dataStore == null)
      throw new PersistenceException("LR has no DataStore");

    dataStore.sync(this);
  } // sync()

  /** Get the features associated with this corpus. */
  public FeatureMap getFeatures() { return features; }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) { this.features = features; }

  /** The name of the corpus */
  protected String name;

  /** The features associated with this corpus. */
  protected FeatureMap features;

} // class CorpusImpl
