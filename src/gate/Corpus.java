/*
 *  Corpus.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 19/Jan/2000
 *
 *  $Id$
 */

package gate;
import java.util.*;

import gate.util.*;
import gate.event.*;

/** Corpora are lists of Document. TIPSTER equivalent: Collection.
  */
public interface Corpus extends LanguageResource, List, NameBearer {

  /**
   * Gets the names of the documents in this corpus.
   * @return a {@link List} of Strings representing the names of the documents
   * in this corpus.
   */
  public List getDocumentNames();

  /**
   * Gets the name of a document in this corpus.
   * @param index the index of the document
   * @return a String value representing the name of the document at
   * <tt>index</tt> in this corpus.
   */
  public String getDocumentName(int index);

  /**
   * Unloads the document from memory. Only needed if memory
   * preservation is an issue. Only supported for Corpus which is
   * stored in a Datastore. To get this document back in memory,
   * use get() on Corpus or if you have its persistent ID, request it
   * from the Factory.
   * <P>
   * Transient Corpus objects do nothing,
   * because there would be no way to get the document back
   * again afterwards.
   * @param Document to be unloaded from memory.
   * @return void.
   */
  public void unloadDocument(Document doc);


  /**
   * Removes one of the listeners registered with this corpus.
   * @param l the listener to be removed.
   */
  public void removeCorpusListener(CorpusListener l);

  /**
   * Registers a new {@link CorpusListener} with this corpus.
   * @param l the listener to be added.
   */
  public void addCorpusListener(CorpusListener l);

} // interface Corpus
