/*
 *  SecurityInfo.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 10/Oct/2001
 *
 *  $Id$
 */

package gate.security;

import junit.framework.Assert;

public class SecurityInfo {

  /** world read/ group write */
  public static final int ACCESS_WR_GW = 1;
  /** group read/ group write */
  public static final int ACCESS_GR_GW = 2;
  /** group read/ owner write */
  public static final int ACCESS_GR_OW = 3;
  /** owner read/ owner write */
  public static final int ACCESS_OR_OW = 4;


  protected Group grp;
  protected User  usr;
  protected int   accessMode;

  public SecurityInfo(int accessMode,User usr,Group grp) {

    //0. preconditions
    Assert.assertTrue(accessMode == this.ACCESS_GR_GW ||
                  accessMode == this.ACCESS_GR_OW ||
                  accessMode == this.ACCESS_OR_OW ||
                  accessMode == this.ACCESS_WR_GW);

    this.accessMode = accessMode;
    this.usr = usr;
    this.grp = grp;

    //don't register as change listener for froups/users
    //because if an attempt to delete group/user is performed
    //and they own documents then the attempt will fail
  }


  public Group getGroup() {
    return this.grp;
  }


  public User getUser() {
    return this.usr;
  }

  public int getAccessMode() {
    return this.accessMode;
  }
}