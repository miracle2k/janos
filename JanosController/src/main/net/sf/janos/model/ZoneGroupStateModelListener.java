package net.sf.janos.model;

public interface ZoneGroupStateModelListener {
	public void zoneGroupRemoved(ZoneGroup group, ZoneGroupStateModel source);
	public void zoneGroupAdded(ZoneGroup group, ZoneGroupStateModel source);
	public void zoneGroupMembersChanged(ZoneGroup group, ZoneGroupStateModel source);
}
