/*
 *  AbstractLanguageAnalyser.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 13/Nov/2000
 *
 *  $Id$
 */

package gate.creole;

import gate.*;

/**
 * A parent implementation of language analysers with some default code.
 */
abstract public class AbstractLanguageAnalyser
                      extends AbstractProcessingResource
                      implements LanguageAnalyser
{
  /** Set the document property for this analyser. */
  public void setDocument(Document document) {
    this.document = document;
  } // setDocument()

  /** Get the document property for this analyser. */
  public Document getDocument() {
    return document;
  } // getDocument()

  /** The document property for this analyser. */
  protected Document document;

  /** Set the corpus property for this analyser. */
  public void setCorpus(Corpus corpus) {
    this.corpus = corpus;
  } // setCorpus()

  /** Get the corpus property for this analyser. */
  public Corpus getCorpus() {
    return corpus;
  } // getCorpus()

  /** The corpus property for this analyser. */
  protected Corpus corpus;

} // class AbstractLanguageAnalyser
