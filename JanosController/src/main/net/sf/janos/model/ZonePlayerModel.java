/*
   Copyright 2007 David Wheeler

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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
   * Adds the given zone player to the list in a sorted order.
   * @param zp
   */
  public void addZonePlayer(ZonePlayer zp) {
	  zonePlayers.add(zp);
	  for (ZonePlayerModelListener l : listeners ) {
		  l.zonePlayerAdded(zp, this);
	  }
  }
  
  /**
   * Removes the given zone player from the list. 
   * @param zp
   */
  public void remove(ZonePlayer zp) {
    this.zonePlayers.remove(zp);
    for (ZonePlayerModelListener l : listeners) {
      l.zonePlayerRemoved(zp, this);
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
	  try {
		  return zonePlayers.get(index);
	  } catch (Exception e) {
		  return null;
	  }
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

  public int getSize() {
    return zonePlayers.size();
  }
}
