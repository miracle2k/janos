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

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import net.sf.janos.control.RenderingControlListener;
import net.sf.janos.control.RenderingControlService;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.xml.RenderingControlEventHandler.RenderingControlEventType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Slider;

/**
 * a UI component with actions for controlling the music eg. stop
 * 
 * @author David Wheeler
 * 
 */
public class VolumeControl extends Composite implements RenderingControlListener {

	private final ZonePlayer zone;
	private final Slider volume;
	private final Image muted;
	private final Image notMuted;
	private final Button mute;
	
	public VolumeControl(Composite parent, int style, ZonePlayer zone) {
		super(parent, style);
		this.zone = zone;


		InputStream is;
		is = getClass().getResourceAsStream("/sound-16x16.png");
		notMuted = new Image(getDisplay(), is);
		try {
			is.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		is = getClass().getResourceAsStream("/sound-off-16x16.png");
		muted = new Image(getDisplay(), is);
		try {
			is.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		mute = new Button(this, SWT.TOGGLE);
		updateMuteButton();
		mute.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setMute(!getMute());
			}
		});

		volume = new Slider(this, SWT.HORIZONTAL);
		volume.setMinimum(0);
		volume.setMaximum(100);
		volume.setSelection(getVolume());

		volume.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setVolume(volume.getSelection());
			}
		});

		FormData data1 = new FormData();
		data1.left = new FormAttachment(0, 0);
		mute.setLayoutData(data1);
		
		FormData data2 = new FormData();
		data2.left = new FormAttachment(mute, -10);
		data2.right = new FormAttachment(100,0);
		data2.top = new FormAttachment(0,0);
		data2.bottom = new FormAttachment(100,0);
		volume.setLayoutData(data2);
		
		FormLayout layout = new FormLayout();
		setLayout(layout);

		zone.getMediaRendererDevice().getRenderingControlService().addListener(this);
	}


	protected void setVolume(int vol) {
		try {
			zone.getMediaRendererDevice().getRenderingControlService().setVolume(vol);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected int getVolume() {
		try {
			return zone.getMediaRendererDevice().getRenderingControlService().getVolume();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	protected boolean getMute() {
		boolean muted = false;
		try {
			muted = zone.getMediaRendererDevice().getRenderingControlService().getMute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return muted;
	}
	
	protected void setMute(boolean mute) {
		try {
			zone.getMediaRendererDevice().getRenderingControlService().setMute(mute?1:0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void updateMuteButton() {
		mute.setImage(getMute()?muted:notMuted);
	}
	
	public void valuesChanged(Set<RenderingControlEventType> events, RenderingControlService source) {
		if (events.contains(RenderingControlEventType.VOLUME_MASTER)) {
			final int newVol = getVolume();
			
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					volume.setSelection(newVol);
					updateMuteButton();
				}
			});
		}
		// TODO other volumes?
	}

	@Override
	public void dispose() {
		muted.dispose();
		notMuted.dispose();
		super.dispose();
	}
}
