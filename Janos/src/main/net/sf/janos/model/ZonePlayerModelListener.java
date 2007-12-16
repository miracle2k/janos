/*
 * Created on 13/12/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.model;

import net.sf.janos.control.ZonePlayer;

public interface ZonePlayerModelListener {

  public void zonePlayerRemoved(ZonePlayer zp);

  public void zonePlayerAdded(ZonePlayer zp);

}
