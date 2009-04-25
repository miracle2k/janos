package net.sf.janos.ui;

import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.ZoneGroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
		
//		// Row 1
//		nowPlaying = new NowPlaying(this, 0, zone);
//		FormData data1 = new FormData();
//		data1.left = new FormAttachment(0, 0);
//		data1.right = new FormAttachment(100, 0);
//		nowPlaying.setLayoutData(data1);
//		
//		// Row 2
//		volumeControl = new ZoneGroupVolumeControl(this, 0, group);
//		
//		FormData data2 = new FormData();
//		data2.left = new FormAttachment(0, 0);
//		data2.top = new FormAttachment(nowPlaying);
//		data2.right = new FormAttachment(100, 0);
//		volumeControl.setLayoutData(data2);
//
//		volumeControl.addControlListener(this);
//		
//		// row 3
//		queue = new QueueDisplay(this, SWT.NONE, zone);
//		FormData data4 = new FormData();
//		data4.left = new FormAttachment(0, 0);
//		data4.right = new FormAttachment(100, 0);
//		data4.top = new FormAttachment(volumeControl);
//		data4.bottom = new FormAttachment(100,0);
//		data4.height = 100;
//		queue.setLayoutData(data4);
//		
//		FormLayout layout = new FormLayout();
//		layout.marginWidth = 3;
//		layout.marginHeight = 3;
//		setLayout(layout);
		
		
		setLayout(new GridLayout(1, false));
		nowPlaying = new NowPlaying(this, 0, zone);
		GridData nowPlayingData = new GridData();
		nowPlayingData.grabExcessHorizontalSpace =true;
		nowPlayingData.horizontalAlignment=SWT.FILL;
		nowPlayingData.verticalAlignment = SWT.TOP;
		nowPlaying.setLayoutData(nowPlayingData);
		
		volumeControl = new ZoneGroupVolumeControl(this, 0, group);
		GridData volumeControlData = new GridData();
		volumeControlData.grabExcessHorizontalSpace=true;
		volumeControlData.horizontalAlignment=SWT.FILL;
		volumeControlData.verticalAlignment=SWT.TOP;
		volumeControl.setLayoutData(volumeControlData);
		
		queue = new QueueDisplay(this, SWT.NONE, zone);
    GridData queueData = new GridData();
    queueData.grabExcessHorizontalSpace=true;
    queueData.grabExcessVerticalSpace=true;
    queueData.horizontalAlignment=SWT.FILL;
    queueData.verticalAlignment=SWT.FILL;
    queueData.heightHint = 200;
    queue.setLayoutData(queueData);
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

	public void controlMoved(ControlEvent arg0) {
	}

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
