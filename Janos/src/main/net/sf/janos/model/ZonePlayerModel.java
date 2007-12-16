/*
 * Created on 13/12/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.janos.control.ZonePlayer;

public class ZonePlayerModel {
  
  private final List<ZonePlayer> zonePlayers = new ArrayList<ZonePlayer>();
  private final List<ZonePlayerModelListener> listeners = new ArrayList<ZonePlayerModelListener>();
  
  public ZonePlayerModel() {
    
  }
  
  /**
   * Adds the given zone player to the end of the list.
   * @param zp
   */
  public void addZonePlayer(ZonePlayer zp) {
    this.zonePlayers.add(zp);
    for (ZonePlayerModelListener l : listeners ) {
      l.zonePlayerAdded(zp);
    }
  }
  
  public void remove(ZonePlayer zp) {
    this.zonePlayers.remove(zp);
    for (ZonePlayerModelListener l : listeners) {
      l.zonePlayerRemoved(zp);
    }
  }
  
  /**
   * @return a List of all the zone players
   */
  public List<ZonePlayer> getAllZones() {
    return zonePlayers;
  }

  /**
   * @param id
   *          the ID of a zone player (including "UUID:").
   * @return a zone player matching that id, or null if one could not be found.
   */
  public ZonePlayer getById(String id) {
    for (ZonePlayer zp : zonePlayers) {
      if (zp.getRootDevice().getUDN().substring(5).equals(id)) {
        return zp;
      }
    }
    return null;
  }
  
  /**
   * @param index
   * @return the zone player at the given index.
   */
  public ZonePlayer get(int index) {
    return zonePlayers.get(index);
  }
  
  /**
   * @param zp
   * @return the index of the given zone player, or -1 if it is not in the list.
   */
  public int getIndexOf(ZonePlayer zp) {
    return zonePlayers.indexOf(zp);
  }
  
  public void addZonePlayerModelListener(ZonePlayerModelListener l) {
    listeners.add(l);
  }
  
  public void removeZonePlayerModelListener(ZonePlayerModelListener l) {
    listeners.remove(l);
  }
}
