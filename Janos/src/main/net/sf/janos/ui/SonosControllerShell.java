/*
 * Created on 25/07/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.ui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
	
  private final ExecutorService background = Executors.newFixedThreadPool(5, new ThreadFactory() {
    private int i = 0;
    public Thread newThread(Runnable r) {
      return new Thread(r, "SonosController BG thread " + i++);
    }
  });
  
  private final SonosController controller;
  
  private ZoneList zoneList;
  private QueueDisplay queue;

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
		
    MusicControlPanel controls = new MusicControlPanel(shell, SWT.BORDER, null);
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
		
    queue = new QueueDisplay(shell, SWT.NONE, controller);
    GridData nowPlayingData = new GridData(); 
    nowPlayingData.widthHint=200;
    nowPlayingData.verticalAlignment=SWT.TOP;
    queue.setLayoutData(nowPlayingData);
    zoneList.addSelectionListener(queue);
	}
	
  private void dispose() {
    zoneList.dispose();
    queue.dispose();
  }
  
  public SonosController getController() {
    return controller;
  }
  
  public ZoneList getZoneList() {
    return zoneList;
  }
}
