/*
 *  ObjectModificationEvent.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 21/Sep/2001
 *
 */


package gate.event;

import junit.framework.*;

public class ObjectModificationEvent extends GateEvent {

  public static final int OBJECT_CREATED  = 1000;
  public static final int OBJECT_MODIFIED = 1001;
  public static final int OBJECT_DELETED  = 1002;

  private Object oldValue;
  private Object newValue;

  public ObjectModificationEvent(Object oldValue,Object newValue, int type) {

    super(oldValue,type);

    Assert.assert(type == OBJECT_CREATED ||
                  type == OBJECT_DELETED ||
                  type == OBJECT_MODIFIED);

  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }

  }