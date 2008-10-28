package net.sf.janos.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import net.sf.janos.control.ZonePlayer;

public abstract class SubZoneVolumeControl extends VolumeControl {
	
	ZonePlayer zone;
	
	public SubZoneVolumeControl(Composite parent, ZonePlayer zone) {
		super(parent, SWT.NONE, zone.getDevicePropertiesService().getZoneAttributes().getName());
		this.zone = zone;
		
		updateValuesFromHardware();
	}
	
	@Override
	protected void setMute(boolean mute) {
		super.setMute(mute);
		
		try {
			getZone().getMediaRendererDevice().getRenderingControlService().setMute(mute?1:0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		postSetCallback();
	}

	@Override
	protected void setVolume(int volume) {
		super.setVolume(volume);
	
		try {
			getZone().getMediaRendererDevice().getRenderingControlService().setVolume(volume);
		} catch (Exception e) {
			e.printStackTrace();
		}
		postSetCallback();
	}
	
	public int getVolumeFromHardware() {
		try {
			return getZone().getMediaRendererDevice().getRenderingControlService().getVolume();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public boolean getMuteFromHardware() {
		try {
			return getZone().getMediaRendererDevice().getRenderingControlService().getMute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void updateValuesFromHardware() {
		forceVolume(getVolumeFromHardware());
		forceMute(getMuteFromHardware());
	}
	
	public abstract void postSetCallback();
	
	public ZonePlayer getZone() {
		return zone;
	}

}
