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
import net.sf.janos.ui.tooltip.EntryToolTipHandler;
import net.sf.janos.ui.tooltip.ToolTipHandler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SonosControllerShell {

  private final Display display;
	private final Shell shell;
	private final SonosController controller;
	private MusicLibraryTable music;
	private SearchBar searchBar;
	private UrlAdder urlAdder;
	private ZoneControlList zoneControls;
  private ToolTipHandler toolTipHandler;
	
	public SonosControllerShell(Display display, SonosController controller) {
		this.display = display;
		this.shell = new Shell(display);
		this.controller = controller;
		this.toolTipHandler = new EntryToolTipHandler(shell);
		buildComponents();
	}

	public void start() {
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		controller.dispose();
		display.dispose();
		dispose();
	}

	private void buildComponents() {

		shell.setText("SonosJ");
		shell.setLayout(new GridLayout(2, false));
		
		ExpandBar bar = new ExpandBar(shell, SWT.V_SCROLL);
		GridData barData = new GridData();
		barData.grabExcessVerticalSpace = true;
		barData.horizontalAlignment = SWT.FILL;
		barData.verticalAlignment = SWT.FILL;
		bar.setLayoutData(barData);
		
		zoneControls = new ZoneControlList(bar, controller);
		
		Composite rightSide = new Composite(shell, SWT.NONE);
		GridLayout layout = new GridLayout(4, false);
		rightSide.setLayout(layout);
		GridData rightSideData = new GridData();
		rightSideData.grabExcessHorizontalSpace=true;
		rightSideData.grabExcessVerticalSpace=true;
		rightSideData.horizontalAlignment=SWT.FILL;
		rightSideData.verticalAlignment=SWT.FILL;
		rightSide.setLayoutData(rightSideData);
		
		Label urlAdderLabel = new Label(rightSide, SWT.NONE);
		urlAdderLabel.setText("URL Adder: ");
		
		urlAdder = new UrlAdder(rightSide, SWT.NONE);
		GridData adderData = new GridData();
		adderData.grabExcessHorizontalSpace = true;
		adderData.horizontalAlignment = SWT.FILL;
		urlAdder.setLayoutData(adderData);

    searchBar = new SearchBar(rightSide, SWT.NONE, this);
    GridData data3 = new GridData();
    data3.horizontalAlignment = SWT.END;
    data3.widthHint = 120;
    searchBar.setLayoutData(data3);


		music = new MusicLibraryTable(rightSide, SWT.NONE, this);
		GridData data2 = new GridData();
		data2.grabExcessHorizontalSpace = true;
		data2.grabExcessVerticalSpace = true;
		data2.verticalAlignment = SWT.FILL;
		data2.horizontalAlignment = SWT.FILL;
		data2.horizontalSpan = 4;
		music.setLayoutData(data2);
		
		SonosMenuBar menuBar = new SonosMenuBar(shell);
    menuBar.activate();
//		shell.getDisplay().addListener(SWT.KeyDown, new KeyControlAdapter());
	}

  private void dispose() {
		searchBar.dispose();
		urlAdder.dispose();
		music.dispose();
		shell.dispose();
	}

	public SonosController getController() {
		return controller;
	}

	public MusicLibraryTable getMusicLibrary() {
		return music;
	}
	
	public ZoneControlList getZoneList() {
		return zoneControls;
	}
	
	public ToolTipHandler getToolTipHandler() {
	  return this.toolTipHandler;
	}
	
	public Shell getShell() {
	  return shell;
	}
}
