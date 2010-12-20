package net.sf.janos.web.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.janos.control.AVTransportListener;
import net.sf.janos.control.AVTransportService;
import net.sf.janos.control.RenderingControlListener;
import net.sf.janos.control.RenderingControlService;
import net.sf.janos.model.ZoneGroup;
import net.sf.janos.model.ZoneGroupStateModel;
import net.sf.janos.model.ZoneGroupStateModelListener;
import net.sf.janos.model.xml.AVTransportEventHandler.AVTransportEventType;
import net.sf.janos.model.xml.RenderingControlEventHandler.RenderingControlEventType;
import net.sf.janos.web.structure.Element;

public class UpdateListener implements ZoneGroupStateModelListener, RenderingControlListener, AVTransportListener {

	private boolean zonesupdated;
	private boolean volumeupdated;
	private boolean musicupdated;
	private Map<String, Long> zonesadded;
	private Map<String, Long> zonesremoved;
	private Map<String, Long> zoneschanged;
	
	public UpdateListener() {
		zonesupdated = false;
		zonesadded = new HashMap<String, Long>(1);
		zonesremoved = new HashMap<String, Long>(1);
		zoneschanged = new HashMap<String, Long>(1);
		
		volumeupdated = false;
		
		musicupdated = false;
	}
	
	@Override
	public synchronized  void zoneGroupRemoved(ZoneGroup group, ZoneGroupStateModel source) {
		String gid = group.getId();
		//if a group was added and then removed, there is no reason to keep it in the added list.
		if (null == zonesadded.remove(gid)) {
			//add the removed group to the list of removed groups
			zonesremoved.put(gid, new Long(System.currentTimeMillis()));
		}
		zonesupdated = true;
		notifyAll();
	}

	@Override
	public synchronized  void zoneGroupAdded(ZoneGroup group, ZoneGroupStateModel source) {
		String gid = group.getId();
		if (null == zonesremoved.remove(gid)) {
			zonesadded.put(gid, new Long(System.currentTimeMillis()));
		}
		zonesupdated = true;	
		notifyAll();
	}

	@Override
	public synchronized  void zoneGroupMembersChanged(ZoneGroup group, ZoneGroupStateModel source) {
		zoneschanged.put(group.getId(), new Long(System.currentTimeMillis()));
		zonesupdated = true;
		notifyAll();
	} 
	
	public boolean getZonesUpdated() {
		return zonesupdated;
	}
	
	//method that listens for updates for up to 2 minutes (blocking) before returning
	public Element getZoneChanges() {
		return getChanges(true, false, false);
	}
	
	
	
	@Override
	public synchronized void valuesChanged(Set<RenderingControlEventType> events,
			RenderingControlService source) {
		volumeupdated = true;
		notifyAll();
	}
	
	public Element getVolumeChanged() {
		return getChanges(false, true, false);
	}
	
	
	
	
	public synchronized void valuesChanged(Set<AVTransportEventType> events,
			AVTransportService source) {
		musicupdated = true;
		notifyAll();
	}
	
	public Element getMusicChanged() {
		return getChanges(false, false, true);
	}
	
	public Element getAllChanges() {
		return getChanges(true, true, true);
	}
	
	private synchronized Element getChanges(boolean group, boolean volume, boolean music) {
		int count = 0;
		while (!(group && zonesupdated) && !(volume && volumeupdated) && !(music && musicupdated) && count < 1) {
			try {
				wait(120000);
			} catch (InterruptedException e) { }
			count++;
		}
		
		Element updates = new Element("updates");
		
		if (group) {
		
			//look for additional updates for 1 second
			if (zonesupdated) {
				zonesupdated = false;
				while (group && !zonesupdated && count < 1) {
					try {
						wait(1000);
					} catch (InterruptedException e) { }
					count++;
				}
				zonesupdated = true;
			}
			Element gupdates = new Element("groupUpdates");
			if (!zonesupdated) {
				gupdates.addChild(new Element("groupsChanged", "false"));
			} else {
				gupdates.addChild(new Element("groupsChanged", "true"));
			}
		
			Element addedgrps = new Element("addedGroups", true);
			for (Entry<String, Long> entry : zonesadded.entrySet()) {
				addedgrps.addChild(new Element("groupID", entry.getKey()));
				addedgrps.addChild(new Element("time", entry.getValue()+""));
			}
			gupdates.addChild(addedgrps);
			Element removedgrps = new Element("removedGroups", true);
			for (Entry<String, Long> entry : zonesremoved.entrySet()) {
				removedgrps.addChild(new Element("groupID", entry.getKey()));
				removedgrps.addChild(new Element("time", entry.getValue()+""));
			}
			gupdates.addChild(removedgrps);
			Element changedgrps = new Element("changedGroups", true);
			for (Entry<String, Long> entry : zoneschanged.entrySet()) {
				changedgrps.addChild(new Element("groupID", entry.getKey()));
				changedgrps.addChild(new Element("time", entry.getValue()+""));
			}
			gupdates.addChild(changedgrps);
			updates.addChild(gupdates);
		}
		
		if (volume) {
			if (!volumeupdated) {
				updates.addChild(new Element("volumeChanged", "false"));
			} else {
				updates.addChild(new Element("volumeChanged", "true"));
			}
		}

		if (music) {
			if (!musicupdated) {
				updates.addChild(new Element("musicChanged", "false"));
			} else {
				updates.addChild(new Element("musicChanged", "true"));
			}
		}
		return updates;

		
	}

}
