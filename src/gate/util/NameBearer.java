/*
 *  NameBearer.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 17/Sept/2001
 *
 *  $Id$
 */

package gate.util;

/** Classes that have features.
  */
public interface NameBearer
{
  /** Sets the name of this resource*/
  public void setName(String name);

  /** Returns the name of this resource*/
  public String getName();

} // interface NameBearer