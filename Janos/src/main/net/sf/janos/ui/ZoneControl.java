package net.sf.janos.ui;

import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.ZoneGroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

public class ZoneControl extends Composite implements ControlListener {

	private final ZoneGroup group;
	private final NowPlaying nowPlaying;
	private final ZoneGroupVolumeControl volumeControl;
	private final QueueDisplay queue;
	
	public ZoneControl(Composite parent, ZoneGroup group) {
		super(parent, 0);
		this.group = group;
		ZonePlayer zone = group.getCoordinator();
		
		// Row 1
		nowPlaying = new NowPlaying(this, 0, zone);
		FormData data1 = new FormData();
		data1.left = new FormAttachment(0, 0);
		data1.right = new FormAttachment(100, 0);
		nowPlaying.setLayoutData(data1);
		
		// Row 2
		volumeControl = new ZoneGroupVolumeControl(this, 0, group);
		
		FormData data2 = new FormData();
		data2.left = new FormAttachment(0, 0);
		data2.top = new FormAttachment(nowPlaying);
		data2.right = new FormAttachment(100, 0);
		volumeControl.setLayoutData(data2);

		volumeControl.addControlListener(this);
		
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
	
	public QueueDisplay getQueue() {
		return queue;
	}

	@Override
	public void controlMoved(ControlEvent arg0) {
	}

	@Override
	public void controlResized(ControlEvent arg0) {
		pack();
		layout();
	}
	
	@Override
	public void dispose() {
		nowPlaying.dispose();
		volumeControl.dispose();
		queue.dispose();
		super.dispose();
	}

}
