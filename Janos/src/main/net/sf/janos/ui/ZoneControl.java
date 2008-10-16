package net.sf.janos.ui;

import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.ZoneGroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ZoneControl extends Composite implements ControlListener {

	private final ZoneGroup group;
	private final NowPlaying nowPlaying;
	private final VolumeControl volumeControl;
	private final Button subGroups;
	
	
	
	public ZoneControl(Composite parent, ZoneGroup group) {
		super(parent, 0);
		this.group = group;
		ZonePlayer zone = group.getCoordinator();
		boolean isMultiGroup = group.getMembers().size()>1;
		
		// Row 1
		this.nowPlaying = new NowPlaying(this, 0, zone);
		GridData npgd = new GridData();
		npgd.horizontalSpan = 2;
		getNowPlaying().setLayoutData(npgd);
		
		// Row 2
		this.volumeControl = new VolumeControl(this, 0, zone); 
		GridData volumeControlGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		volumeControl.setLayoutData(volumeControlGD);
		
		if (isMultiGroup) {
			this.subGroups = new Button(this, SWT.NONE);
			subGroups.setText("Group Members/Volume");
			GridData subGroupGD = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			subGroups.setLayoutData(subGroupGD);
		
		} else {
			this.subGroups = null;
		}
		
		GridLayout gridLayout = new GridLayout(2, false);
 		setLayout(gridLayout);

 		addControlListener(this);
	}


	@Override
	public void controlMoved(ControlEvent arg0) {
	}


	@Override
	public void controlResized(ControlEvent arg0) {
		GridData gd = (GridData) getNowPlaying().getLayoutData();
		// TODO: Account for this magic number (-10) which appears to be required
		// to get a nice border around the NP group
		gd.widthHint = this.getBounds().width - 10;	
		layout();
	}


	public NowPlaying getNowPlaying() {
		return nowPlaying;
	}

	public ZonePlayer getZonePlayer() {
		return group.getCoordinator();
	}
}
