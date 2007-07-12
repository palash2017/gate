/*
 *  Copyright (c) 2001-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  HepTag was originally written by Mark Hepple, this version contains
 *  modifications by Valentin Tablan and Niraj Aswani.
 *
 *  $Id$
 */

package hepple.postag.rules;

import hepple.postag.*;


/**
 * Title:        HepTag
 * Description:  Mark Hepple's POS tagger
 * Copyright:    Copyright (c) 2001
 * Company:      University of Sheffield
 * @author Mark Hepple
 * @version 1.0
 */

public class Rule_WDAND2TAGBFR extends Rule {

  public Rule_WDAND2TAGBFR() {
  }
  public boolean checkContext(POSTagger tagger) {
    return (tagger.lexBuff[1][0].equals(context[0]) &&
            tagger.wordBuff[3].equals(context[1]));
  }
}