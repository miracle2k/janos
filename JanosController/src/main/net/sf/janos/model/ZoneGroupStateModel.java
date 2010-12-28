package net.sf.janos.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZoneGroupStateModel {
	private static final Log LOG = LogFactory.getLog(ZoneGroupStateModel.class);
	private ZoneGroupState oldGroupState = new ZoneGroupState(new ArrayList<ZoneGroup>());
	private final List<ZoneGroupStateModelListener> listeners = new ArrayList<ZoneGroupStateModelListener>();

	public ZoneGroupStateModel() {
	}

	public void handleGroupUpdate(ZoneGroupState newGroupState) {

		synchronized(listeners) {
			//1 Save the old group state
			ZoneGroupState ozgstate = oldGroupState;
			
			//2 update the group state;
			oldGroupState = newGroupState;

			//3 fire changes
			
			// First, look for groups that have been removed.  These
			// are defined as groups which are in oldGroupState but not
			// in newGroupState
			for (ZoneGroup oldGroup: ozgstate.getGroups()) {
				if (!newGroupState.getGroups().contains(oldGroup)) {
					fireGroupRemoved(oldGroup);
				}
			}

			// Now, Look for groups that have been added.  These are defined
			// as groups who are present in newGroupState but not in 
			// oldGroupState
			for (ZoneGroup newGroup: newGroupState.getGroups()) {
				if (!ozgstate.getGroups().contains(newGroup)) {
					fireGroupAdded(newGroup);
				}
			}

			// Now, look for groups who have had group membership changes.  These are
			// defined as groups which are present on both lists but whose elements
			// are not equal
			for (ZoneGroup newGroup: newGroupState.getGroups()) {
				for (ZoneGroup oldGroup: ozgstate.getGroups()) {
					if (oldGroup.equals(newGroup)) {
						if ( (!oldGroup.getMembers().containsAll(newGroup.getMembers())) ||
								(!newGroup.getMembers().containsAll(oldGroup.getMembers()))) {
							fireGroupMembershipChanged(newGroup);
						}
					}
				}
			}
		}
	}

	protected void fireGroupRemoved(ZoneGroup group) {
		LOG.info("REMOVING GROUP: " + group.getId());
		for (ZoneGroupStateModelListener l : listeners) {
			l.zoneGroupRemoved(group, this);
		}
	}

	protected void fireGroupAdded(ZoneGroup group) {
		LOG.info("ADDING GROUP: " + group.getId());
		for (ZoneGroupStateModelListener l : listeners) {
			l.zoneGroupAdded(group, this);
		}	
	}

	protected void fireGroupMembershipChanged(ZoneGroup group) {
		LOG.info("CHANGING GROUP: " + group.getId());
		for (ZoneGroupStateModelListener l : listeners) {
			l.zoneGroupMembersChanged(group, this);
		}
	}

	public void addListener(ZoneGroupStateModelListener l) {
		synchronized(listeners) {
			listeners.add(l);
		}
	}

	public void removeListener(ZoneGroupStateModelListener l) {
		synchronized(listeners) {
			listeners.remove(l);
		}
	}
}
