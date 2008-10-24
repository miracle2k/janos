/*
   Copyright 2007 David Wheeler

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.sf.janos.ui;

import java.util.Set;

import net.sf.janos.control.RenderingControlListener;
import net.sf.janos.control.RenderingControlService;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.xml.RenderingControlEventHandler.RenderingControlEventType;

import org.eclipse.swt.widgets.Composite;

/**
 * a UI component for controlling volume
 * 
 * @author David Wheeler
 * 
 */
public class ZonePlayerVolumeControl extends VolumeControl implements RenderingControlListener {

	ZonePlayer zone;
	
	public ZonePlayerVolumeControl(Composite parent, int style, ZonePlayer zone) {
		super(parent, style, zone.getDevicePropertiesService().getZoneAttributes().getName());
		this.zone = zone;

		zone.getMediaRendererDevice().getRenderingControlService().addListener(this);
	}

	protected void setVolume(int vol) {
		super.setVolume(vol);
		try {
			zone.getMediaRendererDevice().getRenderingControlService().setVolume(vol);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getVolume() {
		int rv = 0;
		try {
			rv = zone.getMediaRendererDevice().getRenderingControlService().getVolume();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rv;
	}
	

	protected void setMute(boolean mute) {
		super.setMute(mute);
		try {
			zone.getMediaRendererDevice().getRenderingControlService().setMute(mute?1:0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean getMute() {
		boolean rv = false;
		try {
			rv = zone.getMediaRendererDevice().getRenderingControlService().getMute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rv;
	}
	
	public void valuesChanged(final Set<RenderingControlEventType> events, RenderingControlService source) {
		// on its own, a ZonePlayerVolumeControl does not respond to external events
	}

	@Override
	public void dispose() {
		zone.getMediaRendererDevice().getRenderingControlService().removeListener(this);
		super.dispose();
	}
}
