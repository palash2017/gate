/*
 *  Copyright (c) 2010, The University of Sheffield.
 *
 *  This file is part of the GATE/Groovy integration layer, and is free
 *  software, released under the terms of the GNU Lesser General Public
 *  Licence, version 2.1 (or any later version).  A copy of this licence
 *  is provided in the file LICENCE in the distribution.
 *
 *  Groovy is developed by The Codehaus, details are available from
 *  http://groovy.codehaus.org
 */

package gate.groovy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.Resource;
import gate.SimpleAnnotationSet;
import gate.util.InvalidOffsetException;
import groovy.lang.Closure;
import groovy.lang.IntRange;

/**
 * Class containing static methods that will be mixed in to
 * several core GATE classes when the Groovy plugin is loaded,
 * making the methods available as instance methods on their
 * respective types (in the same way as Groovy does by default
 * for DefaultGroovyMethods).
 */
public class GateGroovyMethods {

  /**
   * Call the closure once for each document in this corpus, loading
   * and unloading documents as appropriate in the case of a persistent
   * corpus.
   * 
   * @param self the corpus to traverse
   * @param closure the closure to call
   * @return the corpus.
   */
  public static Object each(Corpus self, Closure closure) {
    for(int i = 0; i < self.size(); i++) {
      boolean docWasLoaded = self.isDocumentLoaded(i);
      Document doc = (Document)self.get(i);
      closure.call(doc);
      if(!docWasLoaded) {
        self.unloadDocument(doc);
        Factory.deleteResource(doc);
      }
    }
    return self;
  }
  
  /**
   * Call the closure once for each document in this corpus, loading
   * and unloading documents as appropriate in the case of a persistent
   * corpus.  The closure will receive two parameters, the document
   * and its Integer index in the corpus.
   * 
   * @param self the corpus to traverse
   * @param closure the closure to call
   * @return the corpus.
   */
  public static Object eachWithIndex(Corpus self, Closure closure) {
    for(int i = 0; i < self.size(); i++) {
      boolean docWasLoaded = self.isDocumentLoaded(i);
      Document doc = (Document)self.get(i);
      closure.call(new Object[] {doc, i});
      if(!docWasLoaded) {
        self.unloadDocument(doc);
        Factory.deleteResource(doc);
      }
    }
    return self;    
  }
  
  /**
   * Sub-range access for annotation sets (mapping to getContained).
   * Allows <code>someAnnotationSet[15..20]</code>
   * @see AnnotationSet#getContained(Long, Long)
   */
  public static AnnotationSet getAt(AnnotationSet self, IntRange range) {
    return self.getContained(Long.valueOf(range.getFromInt()),
            Long.valueOf(range.getToInt()));
  }
  
  /**
   * Sub-range access for document content.  Allows
   * <code>documentContent[15..20]</code>
   * @param self
   * @param range
   * @return
   */
  public static DocumentContent getAt(DocumentContent self, IntRange range) {
    try {
      return self.getContent(Long.valueOf(range.getFromInt()),
              Long.valueOf(range.getToInt()));
    }
    catch(InvalidOffsetException ioe) {
      throw new IndexOutOfBoundsException(ioe.getMessage());
    }
  }
  
  /**
   * Array-style access for annotation sets.  Allows things like
   * <code>someAnnotationSet["Token"]</code>
   * @see SimpleAnnotationSet#get(String)
   */
  public static AnnotationSet getAt(SimpleAnnotationSet self, String type) {
    return self.get(type);
  }

  /**
   * Array-style access for annotation sets.  Allows
   * <code>someAnnotationSet["Token", "SpaceToken"]</code>
   * @see SimpleAnnotationSet#get(Set)
   */
  public static AnnotationSet getAt(SimpleAnnotationSet self, List<String> types) {
    return self.get(new HashSet<String>(types));
  }
  
  /**
   * Call the given closure passing this resource as a parameter,
   * and ensuring that the resource is deleted when the closure
   * returns.  This would typically be used in this kind of
   * construction:
   * <pre>
   * Factory.newDocument(someUrl).withResource {
   *   // do something with the document (it)
   * }
   * </pre>
   * @param self
   * @param closure
   * @return the value returned from the closure
   */
  public static Object withResource(Resource self, Closure closure) {
    try {
      return closure.call(self);
    }
    finally {
      Factory.deleteResource(self);
    }
  }
}
