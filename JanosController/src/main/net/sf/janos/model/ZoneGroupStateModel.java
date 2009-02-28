package net.sf.janos.model;

import java.util.ArrayList;
import java.util.List;

public class ZoneGroupStateModel {
	private ZoneGroupState oldGroupState = new ZoneGroupState(new ArrayList<ZoneGroup>());
	private final List<ZoneGroupStateModelListener> listeners = new ArrayList<ZoneGroupStateModelListener>();

	public ZoneGroupStateModel() {
	}

	public void handleGroupUpdate(ZoneGroupState newGroupState) {

		// First, look for groups that have been removed.  These
		// are defined as groups which are in oldGroupState but not
		// in newGroupState
		for (ZoneGroup oldGroup: oldGroupState.getGroups()) {
			if (!newGroupState.getGroups().contains(oldGroup)) {
				fireGroupRemoved(oldGroup);
			}
		}

		// Now, Look for groups that have been added.  These are defined
		// as groups who are present in newGroupState but not in 
		// oldGroupState
		for (ZoneGroup newGroup: newGroupState.getGroups()) {
			if (!oldGroupState.getGroups().contains(newGroup)) {
				fireGroupAdded(newGroup);
			}
		}

		// Now, look for groups who have had group membership changes.  These are
		// defined as groups which are present on both lists but whose elements
		// are not equal
		for (ZoneGroup newGroup: newGroupState.getGroups()) {
			for (ZoneGroup oldGroup: oldGroupState.getGroups()) {
				if (oldGroup.equals(newGroup)) {
					if ( (!oldGroup.getMembers().containsAll(newGroup.getMembers())) ||
						 (!newGroup.getMembers().containsAll(oldGroup.getMembers()))) {
						fireGroupMembershipChanged(newGroup);
					}
				}
			}
		}

		oldGroupState = newGroupState;
	}

	protected void fireGroupRemoved(ZoneGroup group) {
		System.out.println("REMOVING GROUP: " + group.getId());
		for (ZoneGroupStateModelListener l : listeners) {
			l.zoneGroupRemoved(group, this);
		}
	}

	protected void fireGroupAdded(ZoneGroup group) {
		System.out.println("ADDING GROUP: " + group.getId());
		for (ZoneGroupStateModelListener l : listeners) {
			l.zoneGroupAdded(group, this);
		}
	}

	protected void fireGroupMembershipChanged(ZoneGroup group) {
		System.out.println("CHANGING GROUP: " + group.getId());
		for (ZoneGroupStateModelListener l : listeners) {
			l.zoneGroupMembersChanged(group, this);
		}
	}

	public void addListener(ZoneGroupStateModelListener l) {
		listeners.add(l);
	}

	public void removeListener(ZoneGroupStateModelListener l) {
		listeners.remove(l);
	}
}
