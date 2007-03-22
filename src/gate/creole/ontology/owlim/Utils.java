/**
 * 
 */
package gate.creole.ontology.owlim;

import service.client.OWLIM;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OConstants;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.RDFProperty;
import gate.creole.ontology.URI;

/**
 * @author niraj
 * 
 */
public class Utils {

  /**
   * Given required parameters, this method, based on the provided type, returns
   * an appropriate object of a property.
   * 
   * @param repositoryID
   * @param ontology
   * @param owlim
   * @param uri
   * @param type
   * @return
   */
  public static RDFProperty createOProperty(String repositoryID,
          Ontology ontology, OWLIM owlim, String uri, byte type) {
    RDFProperty prop = (RDFProperty)ontology.getOResourceFromMap(uri);
    if(prop != null) return prop;
    switch(type){
      case OConstants.ANNOTATION_PROPERTY:
        prop = new AnnotationPropertyImpl(new URI(uri, false), ontology,
                repositoryID, owlim);
        break;
      case OConstants.RDF_PROPERTY:
        prop = new RDFPropertyImpl(new URI(uri, false), ontology,
                repositoryID, owlim);
        break;
      case OConstants.OBJECT_PROPERTY:
        prop = new ObjectPropertyImpl(new URI(uri, false), ontology,
                repositoryID, owlim);
        break;
      case OConstants.SYMMETRIC_PROPERTY:
        prop = new SymmetricPropertyImpl(new URI(uri, false), ontology,
                repositoryID, owlim);
        break;
      case OConstants.TRANSITIVE_PROPERTY:
        prop = new TransitivePropertyImpl(new URI(uri, false), ontology,
                repositoryID, owlim);
        break;
      case OConstants.DATATYPE_PROPERTY:
        prop = new DatatypePropertyImpl(new URI(uri, false), ontology,
                repositoryID, owlim);
        break;
    }
    ontology.addOResourceToMap(uri, prop);
    return prop;
  }

  /**
   * Creates a new instance of Ontology Class
   * 
   * @param repositoryID
   * @param ontology
   * @param owlim
   * @param uri
   * @param isAnonymousClass
   * @return
   */
  public static OClass createOClass(String repositoryID, Ontology ontology,
          OWLIM owlim, String uri, boolean isAnonymousClass) {
    OClass aClass = (OClass)ontology.getOResourceFromMap(uri);
    if(aClass != null) return aClass;
    if(isAnonymousClass) {
      aClass = new AnonymousClassImpl(new URI(uri, true), ontology,
            repositoryID, owlim);
    } else {
      aClass = new OClassImpl(new URI(uri, false), ontology,
            repositoryID, owlim);
    }
    ontology.addOResourceToMap(uri, aClass);
    return aClass;
  }

  /**
   * Creates a new instance of Ontology Instance
   * 
   * @param repositoryID
   * @param ontology
   * @param owlim
   * @param uri
   * @return
   */
  public static OInstance createOInstance(String repositoryID,
          Ontology ontology, OWLIM owlim, String uri) {
    OInstance anInstance = (OInstance)ontology.getOResourceFromMap(uri);
    if(anInstance != null) return anInstance;
    anInstance = new OInstanceImpl(new URI(uri, false), ontology,
            repositoryID, owlim);
    ontology.addOResourceToMap(uri, anInstance);
    return anInstance;
  }
  
  
  
  
}
