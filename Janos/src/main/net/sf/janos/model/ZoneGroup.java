/*
 * Created on 11/11/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.janos.control.ZonePlayer;

/**
 * Contains the members and controller of a zone group.
 * @author David Wheeler
 *
 */
public class ZoneGroup {

  private final List<ZonePlayer> members;
  private final ZonePlayer coordinator;
  private final String id;
  
  public ZoneGroup(String id, ZonePlayer coordinator, Collection<ZonePlayer> members) {
    this.members= new ArrayList<ZonePlayer>(members);
    if (!this.members.contains(coordinator)) {
      this.members.add(coordinator);
    }
    this.coordinator = coordinator;
    this.id = id;
  }
  
  public List<ZonePlayer> getMembers() {
    return members;
  }
  
  public ZonePlayer getCoordinator() {
    return coordinator;
  }
  
  public String getId() {
    return id;
  }
}
