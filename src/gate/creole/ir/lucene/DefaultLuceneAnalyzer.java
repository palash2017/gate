/*
 *  DefaultLuceneAnalyzer.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Rosen Marinov, 19/Apr/2002
 *
 */

package gate.creole.ir.lucene;

import gate.creole.ir.*;
import java.util.*;
import org.apache.lucene.analysis.*;

public class DefaultLuceneAnalyzer extends Analyzer
                                  implements DocumentAnalyzer {

  public Iterator analyze(String content){
    //NOT IMPLEMENTED YET
    return null;
  }

  public Iterator analyze(Iterator tokenStream){
    //NOT IMPLEMENTED YET
    return null;
  }

}