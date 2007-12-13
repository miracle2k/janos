/*
 * Created on 11/11/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * contains each of the known zone groups.
 * @author David Wheeler
 *
 */
public class ZoneGroupState {

  private final List<ZoneGroup> zoneGroups;
  public ZoneGroupState(Collection<ZoneGroup> groups) {
    this.zoneGroups = new ArrayList<ZoneGroup>(groups);
  }
  
  public List<ZoneGroup> getGroups() {
    return zoneGroups;
  }
  
}
