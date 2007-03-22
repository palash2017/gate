/*
 *  ObjectPropertyImpl.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: ObjectPropertyImpl.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology.owlim;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import service.client.OWLIM;
import service.client.Property;
import service.client.ResourceInfo;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.OResource;
import gate.creole.ontology.ObjectProperty;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.URI;
import gate.util.GateRuntimeException;

/**
 * Implementation of the ObjectProperty
 * @author niraj
 * 
 */
public class ObjectPropertyImpl extends RDFPropertyImpl implements
                                                       ObjectProperty {
  /**
   * Constructor
   * @param aURI
   * @param ontology
   * @param repositoryID
   * @param owlimPort
   */
  public ObjectPropertyImpl(URI aURI, Ontology ontology, String repositoryID,
          OWLIM owlimPort) {
    super(aURI, ontology, repositoryID, owlimPort);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.ObjectProperty#getInverseProperties()
   */
  public Set<ObjectProperty> getInverseProperties() {
    try {
      Property[] properties = owlim.getInverseProperties(this.repositoryID,
              this.uri.toString());
      Set<ObjectProperty> set = new HashSet<ObjectProperty>();
      for(int i = 0; i < properties.length; i++) {
        byte type = properties[i].getType();
        if(type != OConstants.OBJECT_PROPERTY
                && type != OConstants.SYMMETRIC_PROPERTY
                && type != OConstants.TRANSITIVE_PROPERTY)
          throw new GateRuntimeException(
                  "Invalid Property type returned as an inverse property");
        set.add((ObjectProperty)Utils.createOProperty(this.repositoryID,
                this.ontology, this.owlim, properties[i].getUri(),
                properties[i].getType()));
      }
      return set;
    } catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.ObjectProperty#setInverseOf(gate.creole.ontology.ObjectProperty)
   */
  public void setInverseOf(ObjectProperty theInverse) {
    try {
      owlim.setInverseOf(this.repositoryID, uri.toString(), theInverse.getURI()
              .toString());
    } catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.ObjectProperty#isValidRange(gate.creole.ontology.OInstance)
   */
  public boolean isValidRange(OInstance anInstance) {
    try {
      ResourceInfo[] oClasses = owlim.getRange(this.repositoryID, this.uri
              .toString());
      if(oClasses.length == 0) return true;
      // obtain sub classes of
      Set<String> listOfOClasses = new HashSet<String>();
      for(int i = 0; i < oClasses.length; i++) {
        listOfOClasses.add(oClasses[i].getUri());
        OResource resource = ontology.getOResourceFromMap(oClasses[i].getUri());
        if(resource != null && resource instanceof OClass) {
          Set<OClass> classes = ((OClass)resource)
                  .getSubClasses(OConstants.TRANSITIVE_CLOSURE);
          Iterator<OClass> iter = classes.iterator();
          while(iter.hasNext()) {
            listOfOClasses.add(iter.next().getURI().toString());
          }
        }
      }
      // we need to obtain all the classes of anInstance
      ResourceInfo[] instanceOClasses = owlim.getClassesOfIndividual(
              this.repositoryID, anInstance.getURI().toString(),
              OConstants.DIRECT_CLOSURE);
      Set<String> listOfICs = new HashSet<String>();
      for(int i = 0; i < instanceOClasses.length; i++) {
        listOfICs.add(instanceOClasses[i].getUri());
      }
      return listOfOClasses.containsAll(listOfICs);
    } catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.DatatypeProperty#isValidDomain(gate.creole.ontology.OInstance)
   */
  public boolean isValidDomain(OInstance anInstance) {
    try {
      ResourceInfo[] oClasses = owlim.getDomain(this.repositoryID, this.uri
              .toString());
      if(oClasses.length == 0) return true;
      // obtain sub classes of
      Set<String> listOfOClasses = new HashSet<String>();
      for(int i = 0; i < oClasses.length; i++) {
        listOfOClasses.add(oClasses[i].getUri());
        OResource resource = ontology.getOResourceFromMap(oClasses[i].getUri());
        if(resource != null && resource instanceof OClass) {
          Set<OClass> classes = ((OClass)resource)
                  .getSubClasses(OConstants.TRANSITIVE_CLOSURE);
          Iterator<OClass> iter = classes.iterator();
          while(iter.hasNext()) {
            listOfOClasses.add(iter.next().getURI().toString());
          }
        }
      }
      // we need to obtain all the classes of anInstance
      ResourceInfo[] instanceOClasses = owlim.getClassesOfIndividual(
              this.repositoryID, anInstance.getURI().toString(),
              OConstants.DIRECT_CLOSURE);
      Set<String> listOfICs = new HashSet<String>();
      for(int i = 0; i < instanceOClasses.length; i++) {
        listOfICs.add(instanceOClasses[i].getUri());
      }
      return listOfOClasses.containsAll(listOfICs);
    } catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isValidRange(gate.creole.ontology.OResource)
   */
  public boolean isValidRange(OResource aResource) {
    if(aResource instanceof OInstance)
      return isValidRange((OInstance)aResource);
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#isValidDomain(gate.creole.ontology.OResource)
   */
  public boolean isValidDomain(OResource aResource) {
    if(aResource instanceof OInstance)
      return isValidDomain((OInstance)aResource);
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#getDomain()
   */
  public Set<OResource> getDomain() {
    try {
      ResourceInfo[] list = owlim.getDomain(this.repositoryID, uri.toString());
      // this is a list of classes
      Set<OResource> domain = new HashSet<OResource>();
      // these resources can be anything - an instance, a property, or a class
      for(int i = 0; i < list.length; i++) {
        domain.add(Utils.createOClass(this.repositoryID, this.ontology,
                this.owlim, list[i].getUri(), list[i].isAnonymous()));
      }
      return domain;
    } catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.RDFProperty#getRange()
   */
  public Set<OResource> getRange() {
    try {
      ResourceInfo[] list = owlim.getRange(this.repositoryID, uri.toString());
      // this is a list of classes
      Set<OResource> domain = new HashSet<OResource>();
      // these resources can be anything - an instance, a property, or a class
      for(int i = 0; i < list.length; i++) {
        domain.add(Utils.createOClass(this.repositoryID, this.ontology,
                this.owlim, list[i].getUri(), list[i].isAnonymous()));
      }
      return domain;
    } catch(RemoteException re) {
      throw new GateRuntimeException(re);
    }
  }
}
