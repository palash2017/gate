/*
 *  SimpleFeatureMapImpl.java
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 7/Feb/2000
 *  borislav popov, 1/May/2002
 *
 *  $Id$
 */

package gate.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.Vector;

import com.ontotext.gate.ontology.OntologyImpl;

import gate.FeatureMap;
import gate.creole.ontology.Ontology;
import gate.event.FeatureMapListener;

/** Simple case of features. */
//>>> DAM: was (derived from HashMap)
/*
public class SimpleFeatureMapImpl  extends HashMap implements FeatureMap
*/
//=== DAM: FeatArray optimization, now derived from SimpleMapImpl
public class SimpleFeatureMapImpl
    extends SimpleMapImpl
//    extends HashMap
    implements FeatureMap, java.io.Serializable, java.lang.Cloneable,
    gate.creole.ANNIEConstants
//>>> DAM: end
{
  /** Debug flag */
  private static final boolean DEBUG = false;


 /** Freeze the serialization UID. */
  static final long serialVersionUID = -2747241616127229116L;

  /** Test if <b>this</b> featureMap includes all features from aFeatureMap
    * @param aFeatureMap object which will be included or not in
    * <b>this</b> FeatureMap obj.If this param is null then it will return true.
    * @return <code>true</code> if aFeatureMap is incuded in <b>this</b> obj.
    * and <code>false</code> if not.
    */
  public boolean subsumes(FeatureMap aFeatureMap){
    // null is included in everything
    if (aFeatureMap == null) return true;

    if (this.size() < aFeatureMap.size()) return false;

    SimpleFeatureMapImpl sfm = (SimpleFeatureMapImpl)aFeatureMap;

    Object key;
    Object keyValueFromAFeatureMap;
    Object keyValueFromThis;

    for (int i = 0; i < sfm.count; i++) {
      key = sfm.theKeys[i];
      keyValueFromAFeatureMap = sfm.theValues[i];
      int v = super.getSubsumeKey(key);
      if (v < 0) return false;
      keyValueFromThis = theValues[v];//was: get(key);

      if  ( (keyValueFromThis == null && keyValueFromAFeatureMap != null) ||
            (keyValueFromThis != null && keyValueFromAFeatureMap == null)
          ) return false;

      /*ontology aware subsume implementation
      ontotext.bp*/
      if ((keyValueFromThis != null) && (keyValueFromAFeatureMap != null)) {

        if ( key.equals(LOOKUP_CLASS_FEATURE_NAME) ) {
          /* ontology aware processing */
          Object sfmOntoObj = sfm.get(LOOKUP_ONTOLOGY_FEATURE_NAME);
          Object thisOntoObj = this.get(LOOKUP_ONTOLOGY_FEATURE_NAME);
          if (null!=sfmOntoObj && null!= thisOntoObj) {
            if (sfmOntoObj.equals(thisOntoObj)) {
              boolean doSubsume = ontologySubsume(
                          sfmOntoObj.toString(),
                          keyValueFromAFeatureMap.toString(),
                          keyValueFromThis.toString());
              if (!doSubsume ) {
                return false;
              }
            } // if ontologies are with the same url
          } //if not null objects
          else {
            // incomplete feature set: missing ontology feature
            return false;
          }
        } else {
          /* processing without ontology awareness */
          if (!keyValueFromThis.equals(keyValueFromAFeatureMap)) return false;
        }  // else

      } // if
    } // for

    return true;
  }//subsumes()

   /** Tests if <b>this</b> featureMap object includes aFeatureMap features. <br>
   * If the feature map contains <code>class</code> and (optionally) <code>ontology</code> features:<br>
   * then the ontologyLR is used to provide ontology based subsume with respect to the subClassOf relations.
   * @param ontologyLR an ontology to be used for the subsume
   * @param aFeatureMap object which will be included  or not in  <b>this</b>
   * FeatureMap obj.
   * @return <code>true</code> if <b>this</b> includes aFeatureMap
   * and <code>false</code> if not.
   */
  public boolean subsumes(Ontology ontologyLR, FeatureMap aFeatureMap) {

    if (ontologyLR == null) {
      return this.subsumes(aFeatureMap);
    }

    if (aFeatureMap == null)
      return true;

    if (this.size() < aFeatureMap.size())
      return false;

    SimpleFeatureMapImpl sfm = (SimpleFeatureMapImpl) aFeatureMap;

    Object key;
    Object keyValueFromAFeatureMap;
    Object keyValueFromThis;

    for (int i = 0; i < sfm.count; i++) {
      key = sfm.theKeys[i];
      keyValueFromAFeatureMap = sfm.theValues[i];
      int v = super.getSubsumeKey(key);
      if (v < 0)
        return false;
      keyValueFromThis = theValues[v]; //was: get(key);

      if ( (keyValueFromThis == null && keyValueFromAFeatureMap != null) ||
          (keyValueFromThis != null && keyValueFromAFeatureMap == null)
          )
        return false;

      /*ontology aware subsume implementation based on the ontology LR
          ontotext.bp*/
      if ( (keyValueFromThis != null) && (keyValueFromAFeatureMap != null)) {

        if (key.equals(LOOKUP_CLASS_FEATURE_NAME)) {
          // ontology aware processing

          try {

            if (DEBUG) {
              Out.prln("\nClass in rule: " + keyValueFromAFeatureMap.toString());
              Out.prln("\nClass in annotation: " + keyValueFromThis.toString());
              Out.prln("\nisSubClassOf: " +
                       ontologyLR.isSubClassOf(keyValueFromAFeatureMap.toString(),
                                               keyValueFromThis.toString()));
            }

            return ontologyLR.isSubClassOf(keyValueFromAFeatureMap.toString(),
                                           keyValueFromThis.toString());
          } catch (Exception ex) {
            throw new gate.util.GateRuntimeException(ex);
          }
        }
        else {
          /* processing without ontology awareness */
          if (!keyValueFromThis.equals(keyValueFromAFeatureMap))
            return false;
        } // else

      } // if
    } // for

    return true;
  } //subsumes(ontology)


  /** Tests if <b>this</b> featureMap object includes aFeatureMap but only
    * for the those features present in the aFeatureNamesSet.
    * @param aFeatureMap which will be included or not in <b>this</b>
    * FeatureMap obj.If this param is null then it will return true.
    * @param aFeatureNamesSet is a set of strings representing the names of the
    * features that would be considered for subsumes. If aFeatureNamesSet is
    * <b>null</b> then subsumes(FeatureMap) will be called.
    * @return <code>true</code> if all features present in the aFeaturesNameSet
    * from aFeatureMap are included in <b>this</b> obj, or <code>false</code>
    * otherwise.
    */
  public boolean subsumes(FeatureMap aFeatureMap, Set aFeatureNamesSet){
    // This means that all features are taken into consideration.
    if (aFeatureNamesSet == null) return this.subsumes(aFeatureMap);
    // null is included in everything
    if (aFeatureMap == null) return true;
    // This means that subsumes is supressed.
    if (aFeatureNamesSet.isEmpty()) return true;

    SimpleFeatureMapImpl sfm = (SimpleFeatureMapImpl)aFeatureMap;

    Object key;
    Object keyValueFromAFeatureMap;
    Object keyValueFromThis;

    for (int i = 0; i < sfm.count; i++) {
      key = sfm.theKeys[i];

      if (!aFeatureNamesSet.contains(key))
        continue;

      keyValueFromAFeatureMap = sfm.theValues[i];
        keyValueFromThis = get(key);

      if  ( (keyValueFromThis == null && keyValueFromAFeatureMap != null) ||
            (keyValueFromThis != null && keyValueFromAFeatureMap == null)
          ) return false;

      if ((keyValueFromThis != null) && (keyValueFromAFeatureMap != null)) {
        if ( key.equals(LOOKUP_CLASS_FEATURE_NAME) ) {
          /* ontology aware processing */
          if (!aFeatureNamesSet.contains(LOOKUP_ONTOLOGY_FEATURE_NAME))
            continue;

          Object sfmOntoObj = sfm.get(LOOKUP_ONTOLOGY_FEATURE_NAME);
          Object thisOntoObj = this.get(LOOKUP_ONTOLOGY_FEATURE_NAME);
          if (null!=sfmOntoObj && null!= thisOntoObj) {
            if (sfmOntoObj.equals(thisOntoObj)) {
              if (! ontologySubsume(
                          sfmOntoObj.toString(),
                          keyValueFromAFeatureMap.toString(),
                          keyValueFromThis.toString()))
                return false;
            } // if ontologies are with the same url
          } //if not null objects
          else {
            // incomplete feature set: missing ontology feature
            return false;
          }
        } else {
          /*processing without ontology awareness*/
          if (!keyValueFromThis.equals(keyValueFromAFeatureMap)) return false;
        } //else
      } // if values not null
    } // for

    return true;
  }// subsumes()


  /**
   * Overriden to fire events, so that the persistent objects
   *  can keep track of what's updated
   */
  public Object put(Object key, Object value) {
    Object result = super.put(key, value);
    this.fireMapUpdatedEvent();
    return result;
  } // put

  /**
   * Overriden to fire events, so that the persistent objects
   *  can keep track of what's updated
   */
  public Object remove(Object key) {
    Object result = super.remove(key);
    this.fireMapUpdatedEvent();
    return result;
  } // remove

  public void clear() {
    super.clear();
    //tell the world if they're listening
    this.fireMapUpdatedEvent();
  } // clear

  // Views
  public Object clone() {
    return super.clone();
  } //clone

  public boolean equals(Object o) {
    return super.equals(o);
  } // equals

//////////////////THE EVENT HANDLING CODE//////////////
//Needed so an annotation can listen to its features//
//and update correctly the database//////////////////
  private transient Vector mapListeners;
  /**
   * Removes a gate listener
   */
  public synchronized void removeFeatureMapListener(FeatureMapListener l) {
    if (mapListeners != null && mapListeners.contains(l)) {
      Vector v = (Vector) mapListeners.clone();
      v.removeElement(l);
      mapListeners = v;
    }
  } //removeFeatureMapListener
  /**
   * Adds a gate listener
   */
  public synchronized void addFeatureMapListener(FeatureMapListener l) {
    Vector v = mapListeners == null ? new Vector(2) : (Vector)mapListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      mapListeners = v;
    }
  } //addFeatureMapListener
  
  /**
   *
   */
  protected void fireMapUpdatedEvent () {
    if (mapListeners != null) {
      Vector listeners = mapListeners;
      int count = listeners.size();
      if (count == 0) return;
      for (int i = 0; i < count; i++)
        ((FeatureMapListener) listeners.elementAt(i)).featureMapUpdated();
    }
  }//fireMapUpdatedEvent


  /**ontology enhanced subsume
   * @param ontoUrl the url of the ontology to be used
   * @return true if value1 subsumes value2 in the specified ontology */
  protected boolean ontologySubsume(String ontoUrl,String value1,String value2) {
    boolean result = false;
    try {
      URL url;
      try {
        url = new URL(ontoUrl);
      } catch (MalformedURLException e){
        throw new RuntimeException(
        "\nin SimpleFeatureMapImpl on ontologySubsume()\n"
        +e.getMessage()+"\n");
      }

      /* GET ONTOLOGY BY URL : a bit tricky reference
      since the behaviour behind the getOntology method is
      certainly static.
      : should be temporary */
      Ontology o = new OntologyImpl().getOntology(url);

      result = o.isSubClassOf(value1, value2);

    } catch  (gate.creole.ResourceInstantiationException x) {
      x.printStackTrace(Err.getPrintWriter());
    } catch (gate.creole.ontology.NoSuchClosureTypeException clex) {
      clex.printStackTrace(Err.getPrintWriter());
    }
    return result;
  } // ontologySubsume

} // class SimpleFeatureMapImpl

