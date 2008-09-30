package net.sf.janos.ui;

import java.util.HashMap;

import net.sf.janos.control.ZoneListSelectionListener;
import net.sf.janos.control.ZonePlayer;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ZoneInfo extends CTabFolder implements ZoneListSelectionListener {
	
	final private HashMap<String, Control> controls = new HashMap<String, Control>();

	public ZoneInfo(Composite parent, int style) {
		super(parent, style);
		setSimple(false);
		new CTabItem(this, 0);
		setSelection(0);
	}

	@Override
	public void zoneSelectionChangedTo(ZonePlayer newSelection) {
		String targetName = newSelection.getDevicePropertiesService().getZoneAttributes().getName();

		// create the Zone Metadata Display if we don't already have one
		if (! controls.containsKey(targetName)) {
			ZoneInfoDisplay q = new ZoneInfoDisplay(this, 0, newSelection);
			q.showNowPlaying();
			controls.put(targetName, q);
		}
		
		// fetch the underlying control and stick it in the display
		Control control = controls.get(targetName);
		{
			ZoneInfoDisplay q = (ZoneInfoDisplay)control;
			q.showNowPlaying();
		}
		CTabItem item = getItem(0);
		item.setControl(control);
		item.setText(targetName);
	}
}
