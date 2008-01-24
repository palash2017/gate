/*
 *  FeatureMapFactoryBean.java
 *
 *  Copyright (c) 1998-2008, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Ian Roberts, 22/Jan/2008
 *
 *  $Id$
 */

package gate.util.spring;

import gate.Factory;
import gate.FeatureMap;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

/**
 * Spring FactoryBean to create a FeatureMap from a source Map
 * (typically one created with a &lt;map&gt; element in a spring config
 * file). Values in the source map whose type is Spring "Resource" are
 * converted to URLs in the feature map (file: URLs if possible). If you
 * are defining the source map inline (as opposed to ref-ing another
 * bean) the following shorthand is available.
 * 
 * <pre>
 * &lt;gate:feature-map&gt;
 *   &lt;entry key=&quot;kind&quot; value=&quot;word&quot; /&gt;
 * &lt;/gate:feature-map&gt;
 * </pre>
 * 
 * The <code>entry</code> elements follow the same pattern as those in
 * a <code>&lt;map&gt;</code> element in normal Spring configuration.
 * See {@link Init} for an example of how to include the
 * <code>gate</code> namespace.
 */
public class FeatureMapFactoryBean extends GateAwareObject implements
                                                          FactoryBean {

  private Map<Object, Object> sourceMap;

  public void setSourceMap(Map<Object, Object> sourceMap) {
    this.sourceMap = sourceMap;
  }

  public Object getObject() throws IOException {
    ensureGateInit();
    FeatureMap fm = Factory.newFeatureMap();
    if(sourceMap != null) {
      for(Map.Entry<Object, Object> entry : sourceMap.entrySet()) {
        Object key = entry.getKey();
        Object value = entry.getValue();

        // convert Spring resources to URLs
        if(value instanceof Resource) {
          File resourceFile = null;
          try {
            resourceFile = ((Resource)value).getFile();
          }
          catch(IOException e) {
            // ignore, leaving resourceFile == null
          }

          if(resourceFile == null) {
            // couldn't get a file, so resolve the resource as a URL
            value = ((Resource)value).getURL();
          }
          else {
            // get the URL to the file
            value = resourceFile.toURI().toURL();
          }
        }

        fm.put(key, value);
      }
    }

    return fm;
  }

  public Class getObjectType() {
    return gate.FeatureMap.class;
  }

  public boolean isSingleton() {
    return false;
  }

}
