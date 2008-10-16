package net.sf.janos.ui;

import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.ZoneGroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ZoneControl extends Composite {

	private final ZoneGroup group;
	private final NowPlaying nowPlaying;
	private final VolumeControl volumeControl;
	private final Button subGroups;
	private final QueueDisplay queue;
	
	
	public ZoneControl(Composite parent, ZoneGroup group) {
		super(parent, 0);
		this.group = group;
		ZonePlayer zone = group.getCoordinator();
		boolean isMultiGroup = group.getMembers().size()>1;
		
		// Row 1
		nowPlaying = new NowPlaying(this, 0, zone);
		FormData data1 = new FormData();
		data1.left = new FormAttachment(0, 0);
		data1.right = new FormAttachment(100, 0);
		nowPlaying.setLayoutData(data1);
		
		// Row 2
		volumeControl = new VolumeControl(this, 0, zone); 
		FormData data2 = new FormData();
		data2.left = new FormAttachment(0, 0);
		data2.top = new FormAttachment(nowPlaying);
		volumeControl.setLayoutData(data2);
		
		subGroups = new Button(this, SWT.NONE);
		subGroups.setText("Group Members/Volume");
		FormData data3 = new FormData();
		data3.left = new FormAttachment(volumeControl);
		data3.right = new FormAttachment(100, 0);
		data3.top = new FormAttachment(nowPlaying);
		subGroups.setLayoutData(data3);
			
		if (isMultiGroup) {
			subGroups.setVisible(true);
		} else {
			subGroups.setVisible(false);
		}
		
		// row 3
		queue = new QueueDisplay(this, SWT.NONE, zone);
		FormData data4 = new FormData();
		data4.left = new FormAttachment(0, 0);
		data4.right = new FormAttachment(100, 0);
		data4.top = new FormAttachment(volumeControl);
		data4.bottom = new FormAttachment(100,0);
		data4.height = 100;
		queue.setLayoutData(data4);
		
		FormLayout layout = new FormLayout();
		layout.marginWidth = 3;
		layout.marginHeight = 3;
		setLayout(layout);
 	}

	public NowPlaying getNowPlaying() {
		return nowPlaying;
	}

	public ZonePlayer getZonePlayer() {
		return group.getCoordinator();
	}
}
