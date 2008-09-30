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

import net.sf.janos.control.SonosController;
import net.sf.janos.ui.zonelist.ZoneList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SonosControllerShell {

	private final Display display;

	private final Shell shell;

	private final SonosController controller;

	private ZoneList zoneList;
	private QueueDisplay queue;
	private MusicControlPanel controls;
	private ZoneInfo zoneInfo;

	public SonosControllerShell(Display display, SonosController controller) {
		this.display = display;
		this.shell = new Shell(display);
		this.controller = controller;
		buildComponents();
	}

	public void start() {
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
		dispose();
		controller.dispose();
	}

	private void buildComponents() {
		shell.setText("SonosJ");
		shell.setLayout(new GridLayout(3, false));

		controls = new MusicControlPanel(shell, SWT.BORDER, null);
		GridData controlData = new GridData();
		controlData.horizontalSpan=3;
		controls.setLayoutData(controlData);

		zoneList = new ZoneList(shell, SWT.BORDER, controller);
		GridData zoneData = new GridData(GridData.FILL_VERTICAL);
		zoneData.widthHint = 200;
		zoneData.heightHint= 400;
		zoneList.setLayoutData(zoneData);
		zoneList.addSelectionListener(this.controller);
		zoneList.addSelectionListener(controls);

		Composite music = new MusicLibraryTable(shell, SWT.NONE, this);
		GridData musicData = new GridData(SWT.FILL, SWT.FILL, true, true);
		musicData.widthHint=400;
		musicData.heightHint=400;
		music.setLayoutData(musicData);

		if (false) {
			queue = new QueueDisplay(shell, SWT.NONE, controller);
			GridData nowPlayingData = new GridData(); 
			nowPlayingData.widthHint=250;
			nowPlayingData.verticalAlignment=GridData.FILL;
			nowPlayingData.horizontalAlignment=GridData.FILL;
			nowPlayingData.grabExcessVerticalSpace=true;
			queue.setLayoutData(nowPlayingData);
			zoneList.addSelectionListener(queue);
		} else {
			zoneInfo = new ZoneInfo(shell, SWT.SINGLE | SWT.BORDER);
			GridData ziData = new GridData();
			ziData.widthHint=250;
			ziData.verticalAlignment=GridData.FILL;
			ziData.horizontalAlignment=GridData.FILL;
			ziData.grabExcessVerticalSpace=true;
			zoneInfo.setLayoutData(ziData);
			zoneList.addSelectionListener(zoneInfo);
		}
	}

	private void dispose() {
		zoneList.removeSelectionListener(this.controller);
		zoneList.removeSelectionListener(controls);
		zoneList.dispose();
		
		if (queue != null) {
			queue.dispose();
		}
		
		if (zoneInfo != null) {
			zoneInfo.dispose();
		}
	}

	public SonosController getController() {
		return controller;
	}

	public ZoneList getZoneList() {
		return zoneList;
	}
}
