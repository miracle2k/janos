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

import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.ZoneGroup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * a UI component with actions for controlling the music eg. stop
 * 
 * @author David Wheeler
 * 
 */
public class ZoneGroupVolumeControl extends Composite {

	ZoneGroup group;
	final Image up;
	final Image down;

	VolumeControl master;
	Button expand;

	public ZoneGroupVolumeControl(Composite parent, int style, ZoneGroup group) {
		super(parent, style);
		this.group = group;

		InputStream is = getClass().getResourceAsStream("/arrow_up.gif");
		up = new Image(getDisplay(), is);
		try {
			is.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		is = getClass().getResourceAsStream("/arrow_down.gif");
		down = new Image(getDisplay(), is);
		try {
			is.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		FormLayout layout = new FormLayout();
		layout.spacing = 10 ;
		setLayout(layout);

		master = new VolumeControl(this, SWT.NONE, getGroupMode() ? 
				"Group Volume" : 
					group.getCoordinator().getDevicePropertiesService().getZoneAttributes().getName() ) {

			protected void setVolume(final int vol) {
				// System.out.println("MASTER SET: " + vol);
				super.setVolume(vol);
			
				// update the hardware
				new subZoneOperator() {
					@Override
					public void operate(ZonePlayer zone) {
						try {
							zone.getMediaRendererDevice().getRenderingControlService().setVolume(vol);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.iterate();
				
				// update the UI
				new subControlOperator() {
					public void operate(Control c) {
						if (c instanceof VolumeControl) {
							VolumeControl vc = (VolumeControl) c;
							vc.setVolume(vol);
						}
					}
				}.iterate();
			}

			public int getVolume() {
				GetVolumeFromHWOperator op = new GetVolumeFromHWOperator();
				op.iterate();
				return op.volume;
			}

			protected void setMute(final boolean mute) {
				super.setMute(mute);

				// update the UI
				new subControlOperator() {
					public void operate(Control c) {
						if (c instanceof VolumeControl) {
							VolumeControl vc = (VolumeControl) c;
							vc.setMute(mute);
						}
					}
				}.iterate();
				
				// update the hardware
				new subZoneOperator() {
					@Override
					public void operate(ZonePlayer zone) {
						try {
							zone.getMediaRendererDevice().getRenderingControlService().setMute(mute?1:0);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
				}.iterate();
			}

			public boolean getMute() {
				GetMuteFromHWOperator op = new GetMuteFromHWOperator();
				op.iterate();
				return op.mute;
			}
		};

		expand = new Button(this, SWT.TOGGLE);
		expand.setImage(down);
		expand.setVisible(getGroupMode());
		expand.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (expand.getSelection()) {
					expand.setImage(up);
					showSubControls();
				} else {
					expand.setImage(down);
					hideSubControls();
				}
			}
		});

		FormData data1 = new FormData();
		data1.left = new FormAttachment(0,0);
		data1.right = new FormAttachment(expand);
		master.setLayoutData(data1);

		FormData data2 = new FormData();
		data2.right = new FormAttachment(100, 0);
		data2.top = new FormAttachment(0, 0);
		expand.setLayoutData(data2);
	}

	public void updateMasterFromHW() {
		master.forceVolume(master.getVolume());
		master.forceMute(master.getMute());
	}
	
	public void updateMasterFromUI() {
		GetVolumeFromUIOperator volOp = new GetVolumeFromUIOperator();
		volOp.iterate();
		master.forceVolume(volOp.volume);
		
		GetMuteFromUIOperator muteOp = new GetMuteFromUIOperator();
		muteOp.iterate();
		master.forceMute(muteOp.mute);
	}
	
	protected void showSubControls() {

		Control previousControl = master;

		for (ZonePlayer zone: group.getMembers()) {

			Label sep = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);

			FormData data1 = new FormData();
			data1.left = new FormAttachment(0, 10);
			data1.right = new FormAttachment(expand);
			data1.top = new FormAttachment(previousControl);
			sep.setLayoutData(data1);

			VolumeControl vc = new VolumeControl(this, SWT.NONE, zone.getDevicePropertiesService().getZoneAttributes().getName()) {

				@Override
				protected void setMute(boolean mute) {
					super.setMute(mute);
					
					try {
						getZone().getMediaRendererDevice().getRenderingControlService().setMute(mute?1:0);
					} catch (Exception e) {
						e.printStackTrace();
					}
					updateMasterFromUI();
				}

				@Override
				protected void setVolume(int volume) {
					super.setVolume(volume);
				
					try {
						getZone().getMediaRendererDevice().getRenderingControlService().setVolume(volume);
					} catch (Exception e) {
						e.printStackTrace();
					}
					updateMasterFromUI();
				}
				
				private ZonePlayer getZone() {
					return (ZonePlayer)getData("ZONE");
				}
			};
			vc.setData("ZONE", zone);
			try {
				vc.forceVolume(zone.getMediaRendererDevice().getRenderingControlService().getVolume());
				vc.forceMute(zone.getMediaRendererDevice().getRenderingControlService().getMute());
			} catch (Exception e) {
				e.printStackTrace();
			}
			FormData data2 = new FormData();
			data2.left = new FormAttachment(0,0);
			data2.right = new FormAttachment(expand);
			data2.top = new FormAttachment(sep);
			vc.setLayoutData(data2);

			previousControl = vc;
		}
		pack();
		layout();
	}

	protected void hideSubControls() {
		
		new subControlOperator() {
			public void operate (Control c) {
				c.dispose();
			}
		}.iterate();
		pack();
		layout();
	}

	protected boolean getGroupMode() {
		return (group.getMembers().size() > 1);
	}

	@Override
	public void dispose() {
		up.dispose();
		down.dispose();
		super.dispose();
	}

	
	
	/*
	 * A class to operate on all the controls that are optionally present
	 */
	abstract class subControlOperator {
		public void iterate() {
			for (Control c : getChildren()) {
				if (!(c.equals(master) || c.equals(expand))) {
					operate(c);
				}
			}
		}

		abstract public void operate(Control c);
	}
	
	/*
	 * A class to operate on all the zones which are members of the group
	 */
	abstract class subZoneOperator {
		public void iterate() {
			for (ZonePlayer zone : group.getMembers()) {
				operate(zone);
			}
		}

		abstract public void operate(ZonePlayer zone);
	}



	class GetVolumeFromHWOperator extends subZoneOperator {
		public int volume = 0;
		
		public void iterate() {
			super.iterate();
			volume /= group.getMembers().size();
		}
		
		public void operate(ZonePlayer zone) {
			try {
				volume += zone.getMediaRendererDevice().getRenderingControlService().getVolume();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class GetVolumeFromUIOperator extends subControlOperator {
		public int volume = 0;
		
		public void iterate() {
			super.iterate();
			volume /= group.getMembers().size();
		}
		
		public void operate(Control c) {
			try {
				VolumeControl vc = (VolumeControl)c;
				volume += vc.getVolume();
			} catch (Exception e) {
				// ignore bad casts, they are expected
			}
		}
	}
	
	class GetMuteFromUIOperator extends subControlOperator {
		public boolean mute = true;
		
		public void operate(Control c) {
			try {
				VolumeControl vc = (VolumeControl)c;
				System.out.print("M: was " + mute);
				mute &= vc.getMute();
				System.out.println(" is " + mute);
			} catch (Exception e) {
				// ignore bad casts, they are expected
			}
		}
	}
	
	class GetMuteFromHWOperator extends subZoneOperator {
		public boolean mute = true;
		public void operate(ZonePlayer zone) {
			try {
				mute &= zone.getMediaRendererDevice().getRenderingControlService().getMute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
