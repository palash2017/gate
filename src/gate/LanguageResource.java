/*
 *  LanguageResource.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 11/Feb/2000
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.io.*;

import gate.util.*;
import gate.persist.*;

/** Models all sorts of language resources.
  */
public interface LanguageResource extends Resource
{
  /** Get the data store that this LR lives in. Null for transient LRs. */
  public DataStore getDataStore();

  /** Set the data store that this LR lives in. */
  public void setDataStore(DataStore dataStore) throws PersistenceException;

  /** Save: synchonise the in-memory image of the LR with the persistent
    * image.
    */
  public void sync() throws PersistenceException;

} // interface LanguageResource
