package net.sf.janos.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sbbi.upnp.devices.DeviceIcon;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZoneListSelectionListener;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.ZoneGroup;
import net.sf.janos.model.ZoneGroupStateModel;
import net.sf.janos.model.ZoneGroupStateModelListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

public class ZoneControlList implements ExpandListener, ZoneGroupStateModelListener {

	private final List<ZoneListSelectionListener> selectionListeners = new ArrayList<ZoneListSelectionListener>();
	private ExpandBar bar;
	private ZonePlayer currentZone;
	private SonosController controller;

	public ZoneControlList(ExpandBar bar, SonosController controller) {
		this.bar = bar;
		this.controller = controller;
		bar.addExpandListener(this);
		controller.getZoneGroupStateModel().addListener(this);

		// I'd really like to set the background color to white so that the 
		// Sonos ZP icons look better (they appear to be authored to look good 
		// on a white background) but calling setBackground() has
		// undesirable side-effects like removing hover behaviors
		// (at least on Vista).
		//		bar.setBackground(new Color(bar.getDisplay(), new RGB(0, 0, 0)));
	}

	@Override
	public void itemCollapsed(ExpandEvent arg0) {
	}

	@Override
	public void itemExpanded(ExpandEvent arg0) {

		for (ExpandItem i : bar.getItems()) {
			if (i.getExpanded()) {
				i.setExpanded(false);				
			}
		}

		ExpandItem item = (ExpandItem)arg0.item;
		ZoneControl zc = (ZoneControl)item.getControl();
		zc.getNowPlaying().showNowPlaying();
		zc.layout(true);
		setCurrentZone(zc.getZonePlayer());
	}

	private void setIcon(final ExpandItem newItem, final ZonePlayer dev) {
		Image oldImage = newItem.getImage();
		if (oldImage != null) {
			oldImage.dispose();
		}
		List<?> icons = dev.getMediaRendererDevice().getUPNPDevice().getDeviceIcons();
		if (icons == null || icons.isEmpty()) {
			newItem.setImage((Image)null);
			return;
		}

		final DeviceIcon deviceIcon = (DeviceIcon) icons.get(0);
		SonosController.getInstance().getExecutor().execute(new Runnable() {
			public void run() {
				InputStream is = null;
				URL url = deviceIcon.getUrl();
				try {
					is = url.openStream();
					final ImageData[] images = new ImageLoader().load(is);

					// TODO: use the zone-name-specific icons
					// TODO: superimpose zone member count here

					if (images != null && images.length > 0) {
						newItem.getDisplay().asyncExec(new Runnable() {
							public void run() {
								if (!newItem.isDisposed()) {
									newItem.setImage(new Image(newItem.getDisplay(), images[0]));
								}
							}
						});
					}
				} catch (IOException e) {
					// LOG.error("Couldn't load image " + url, e);
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException ex1) {}
					}
				}
			}
		});
	}

	public ZonePlayer getSelectedZone() {
		return currentZone;
	}

	private void setCurrentZone(ZonePlayer zp) {
		currentZone = zp;
		fireZoneSelectionChanged(zp);
	}

	public void addSelectionListener(ZoneListSelectionListener l) {
		this.selectionListeners.add(l);
		if (getSelectedZone() != null) {
			l.zoneSelectionChangedTo(getSelectedZone());
		}
	}

	public void removeSelectionListener(ZoneListSelectionListener l) {
		this.selectionListeners.remove(l);
	}

	protected void fireZoneSelectionChanged(ZonePlayer newSelection) {
		for (ZoneListSelectionListener l : this.selectionListeners) {
			l.zoneSelectionChangedTo(newSelection);
		}
	}

	@Override
	public void zoneGroupAdded(final ZoneGroup group, final ZoneGroupStateModel source) {
		bar.getDisplay().asyncExec(new Runnable() {
			public void run() {
				addZone(group, source);
			}
		});
	}

	@Override
	public void zoneGroupMembersChanged(final ZoneGroup group, final ZoneGroupStateModel source) {
		bar.getDisplay().asyncExec(new Runnable() {
			public void run() {
				changeZone(group, source);
			}
		});
	}

	@Override
	public void zoneGroupRemoved(final ZoneGroup group, final ZoneGroupStateModel source) {
		bar.getDisplay().asyncExec(new Runnable() {
			public void run() {
				removeZone(group, source);
			}
		});
	}
	
	protected void addZone(ZoneGroup group, ZoneGroupStateModel source) {
		// extract the coordinator since he's the one we'll key off of
		ZonePlayer coordinator = group.getCoordinator();
		String coordinatorName = coordinator.getDevicePropertiesService().getZoneAttributes().getName();

		// Create a new Now Playing display object
		ZoneControl zoneControl = new ZoneControl(bar, group);

		// Find out where in the ExpandBar to place it.  Current policy
		// is to alphabetize, based on coordinator name.
		int index = 0;
		for (ExpandItem i : bar.getItems()) {
			String thisName = (String)i.getData("SORT_KEY");
			if (coordinatorName.compareToIgnoreCase(thisName) > 0) {
				index++;
			} else {
				break;
			}
		}

		// as a special case, expand the first item we insert into the list
		boolean expand = false;
		if (bar.getItemCount() == 0 ) {
			setCurrentZone(coordinator);
			expand = true;
		}

		ExpandItem item = new ExpandItem(bar, 0, index);

		// generate the title by concatenating zone names
		String title = new String("");
		for (ZonePlayer zp : group.getMembers()) {
			if (title.compareTo("") != 0) {
				title += ", ";
			}
			title += zp.getDevicePropertiesService().getZoneAttributes().getName();
		}

		item.setText(title);
		item.setData("SORT_KEY", coordinatorName);
		item.setData("GROUP_ID", group.getId());
		item.setHeight(zoneControl.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y);
		setIcon(item, coordinator);
		item.setControl(zoneControl);
		item.setExpanded(expand);
	}
	
	protected void removeZone(ZoneGroup group, ZoneGroupStateModel source) {
		String ID = group.getId();
		for (ExpandItem i : bar.getItems()) {
			String thisID = (String)i.getData("GROUP_ID");
			if (thisID.compareToIgnoreCase(ID) == 0) {
				Control c = i.getControl();
				i.setControl(null);
				c.dispose();
				i.dispose();
			} 
		}
	}
	
	protected void changeZone(ZoneGroup group, ZoneGroupStateModel source) {
		removeZone(group, source);
		addZone(group, source);
	}
}
