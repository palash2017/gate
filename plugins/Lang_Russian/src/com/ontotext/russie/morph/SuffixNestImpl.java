package com.ontotext.russie.morph;

import gate.Factory;
import gate.FeatureMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**SuffixNest.java
 *
 * The suffit nest keeps the inflection suffixes of a lemma with their respective
 * morpho-syntactic type.
 * <p>Title: RussIE</p>
 * <p>Description: Russian Information Extraction based on GATE</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Ontotext Lab.</p>
 * @author borislav popov
 * @version 1.0
 */
public class SuffixNestImpl implements SuffixNest {

  /** map of word-form suffixes vs types */
  private Map<String, Object> suffixVsType;
  private String mainFormSuffix = "";
  private FeatureMap fm = Factory.newFeatureMap();

  public SuffixNestImpl() {
    suffixVsType = new HashMap<String, Object>();
  }

  /**
   * Sets the main form suffix in order to allow easy construction of the main
   * form in the time of generation of MSD annotations
   * @param suf the main-form suffix according to the common root.
   */
  public void setMainFormSuffix(String suf) {
    mainFormSuffix = suf;
  }

  public String getMainFormSuffix(){
    return mainFormSuffix;
  }

  /**
   * Adds a suffix with its morpho-syntactic type
   * @param suffix the suffix
   * @param type the type
   */
  public void add(String suffix, String type) {
    if (mainFormSuffix.length() == 0) {
      char ch0 = type.charAt(0);
      int len = type.length();
      char chN = type.charAt(len-1);
      if (
          ((ch0 == 'N') && (chN == 'n'))
          ||
          ((ch0 == 'V') && (chN == 'i'))){
        mainFormSuffix = suffix;
      }
    }
    Set<String> typeSet = new HashSet<String>();
    typeSet.add(type);
    add ( suffix, typeSet);
  }

  /**
   * Adds a suffix with its morpho-syntactic type set
   * @param suffix the suffix
   * @param typeSet the set of types
   */
  public void add(String suffix, Set<String> typeSet)  {
    Set<String> set = (Set<String>)suffixVsType.get(suffix);
    if (set != null) {
      typeSet.addAll(set);
    }
    suffixVsType.put(suffix,typeSet);
  }

  /**
   * Get all suffixes in the nest.
   * @return the suffixes in the nest
   */
  public Set<String> getSuffixes() {
    return suffixVsType.keySet();
  }

  /**
   * Get all morpho-syntactic types.
   * @return all morpho-syntactic types
   */
  public Set<Object> getTypes() {
    Iterator<Object> it = suffixVsType.values().iterator();
    Set<Object> allTypes = new HashSet<Object>();
    Object o;
    while (it.hasNext()) {
      o = it.next();
      if ( o == null ) continue;
      if (o instanceof Set) {
        allTypes.addAll((Set)o);
      }
      if (o instanceof String) {
        allTypes.add(o);
      }
    } // while there are type sets
    return allTypes;
  } // getTypes()

  /**
   * Gets the set of types associated with a suffix
   * @param suffix
   * @return the set of types associated with the suffix
   */
  public Set<Object> getType(String suffix) {
    return (Set<Object>)suffixVsType.get(suffix);
  }

  public int hashCode() {
    return this.toString().hashCode();
  }

  /**
   * Compares roots, suffixes and set of types
   * - if all equal - considers the objects are equal  */
  public boolean equals(Object obj) {
    if (!(obj instanceof SuffixNest) ) return false;
    SuffixNest s2 = (SuffixNest) obj;
    if (!s2.getSuffixes().equals(this.getSuffixes())) return false;
    if (!s2.getTypes().equals(this.getTypes())) return false;
    return true;
  } // equals(obj)


  /**Adds a prefix to all suffixes in the nest
   * @param prefix
   */
  public void addPrefix2Suffixes(String prefix) {
    Iterator<String> ki = suffixVsType.keySet().iterator();
    Object types;
    String suffix;
    Map<String, Object> suffixVsTypeNew = new HashMap<String, Object>();
    while(ki.hasNext()) {
      suffix = ki.next();
      types = suffixVsType.get(suffix);
      suffixVsTypeNew.put(prefix+suffix,types);
    } // while keys
    suffixVsType = suffixVsTypeNew;
  } // addPrefix2Suffixes(prefix)

  public String toString(){
    StringBuffer result = new StringBuffer();
    result.append("\nSUFFIX NEST {");
    ArrayList<String> sufList = new ArrayList<String>(getSuffixes());

    Collections.sort(sufList);

    String suffix;
    for ( int i = 0 ; i < sufList.size(); i++){
      if (i>0) result.append(",");
      suffix = sufList.get(i);
      result.append(suffix).append(":");
      result.append(this.getType(suffix));
    }
    result.append("}.\n");
    return result.toString();
  } // toString()


  public FeatureMap getFeatureMap () { return fm;  }
  public void setFeatureMap (FeatureMap fm) { this.fm = fm; }


}  // class SuffixNestImpl