/*
 *  Copyright (c) 2010--2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */
package gate.termraider.util;

import java.io.Serializable;


public class Term  implements Comparable<Term>, Serializable {
  
  private static final long serialVersionUID = -4849144989013687570L;
  
  private String termString, languageCode, type;
  private boolean typed;
  private int hashCode;
  private String toString;
  

  public Term(String termString, String languageCode, String type) {
    this.termString = termString;
    this.languageCode = languageCode;
    this.type = type;
    this.setup();
  }

  public Term(String termString, String languageCode) {
    this.termString = termString;
    this.languageCode = languageCode;
    this.type = "";
    this.setup();
  }

  private void setup() {
    if (type == null) {
      type = "";
    }
    typed = ! type.isEmpty();
    hashCode = termString.hashCode() + languageCode.hashCode();
    if (typed) {
      hashCode = termString.hashCode() + languageCode.hashCode() + type.hashCode();
      toString = termString + " (" + languageCode + "," + type + ")";
    }
    else {
      hashCode = termString.hashCode() + languageCode.hashCode();
      toString = termString + " (" + languageCode + ")";
    }
  }
  
  public boolean typed() {
    return typed;
  }
  
  public String toString() {
    return toString;
  }
  
  public String getTermString() {
    return this.termString;
  }
  
  public String getLanguageCode() {
    return this.languageCode;
  }
  
  public String getType() {
    return this.type;
  }
  
  
  public boolean equals(Object other) {
    return (other instanceof Term) && 
      this.termString.equals(((Term) other).termString) &&
      this.languageCode.equals(((Term) other).languageCode) &&
      this.type.equals(((Term) other).type);
  }
  
  public int hashCode() {
    return hashCode;
  }
  
  public int compareTo(Term other)  {
    int comp = this.getTermString().compareTo(other.getTermString());
    if (comp != 0) {
      return comp;
    }

    comp = this.getLanguageCode().compareTo(other.getLanguageCode());
    if (comp != 0) {
      return comp;
    }
    
    comp = this.getType().compareTo(other.getType());
    return comp;
  }

  
}