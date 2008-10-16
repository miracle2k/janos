package net.sf.janos.control;

import net.sf.janos.model.ZoneGroupState;

public interface ZoneGroupTopologyListener {
	  /**
	 * @param zoneGroupState 
	   * 
	   */
	  public void zoneGroupTopologyChanged(ZoneGroupState zoneGroupState);
}
