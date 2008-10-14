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

import java.io.InputStream;
import java.util.Set;

import net.sf.janos.control.AVTransportListener;
import net.sf.janos.control.AVTransportService;
import net.sf.janos.control.RenderingControlListener;
import net.sf.janos.control.RenderingControlService;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZoneListSelectionListener;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.TransportInfo.TransportState;
import net.sf.janos.model.xml.AVTransportEventHandler.AVTransportEventType;
import net.sf.janos.model.xml.RenderingControlEventHandler.RenderingControlEventType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;

/**
 * a UI component with actions for controlling the music eg. stop
 * 
 * @author David Wheeler
 * 
 */
public class MusicControlPanel extends Composite implements ZoneListSelectionListener, RenderingControlListener, AVTransportListener {

  private ZonePlayer currentZone;
  private Button play;
  private Button next;
  private Button previous;
  private Scale volume;
  
  private enum Images { 
	  PLAY 	("/Button-Play-32x32.png"), 
	  PAUSE ("/Button-Pause-32x32.png"), 
	  PREV	("/Button-First-32x32.png"), 
	  NEXT	("/Button-Last-32x32.png");
	  
	  private String filename;
	  private Image image;
	  
	  public String filename() {return filename;};
	  public Image image() {return image;};
	  public void setImage(Image i) {image=i;};
	  
	  Images (String filename) {
		  this.filename = filename;
	  }
	  
  };
   

  public MusicControlPanel(Composite parent, int style, ZonePlayer zone) {
    super(parent, style);
    buildComponents();
    zoneSelectionChangedTo(zone);
  }

  private void buildComponents() {
    RowLayout layout = new RowLayout(SWT.HORIZONTAL);
    layout.wrap = false;
    layout.spacing=4;
    setLayout(layout);
    
    for (Images i : Images.values()) {
    	try {
    		InputStream is;
    		is = getClass().getResourceAsStream(i.filename());
    		i.setImage(new Image(getDisplay(), is));
    		is.close();
    	} catch (Exception e) {
    	}
    }
    
    volume = new Scale(this, SWT.HORIZONTAL);
    volume.setMinimum(0);
    volume.setMaximum(100);
    
    previous = new Button(this, SWT.PUSH);
    previous.setImage(Images.PREV.image());
    previous.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        previous();
      }
    });

    play = new Button(this, SWT.PUSH);
    play.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        play();
      }
    });
    
    next = new Button(this, SWT.PUSH);
    next.setImage(Images.NEXT.image());
    next.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        next();
      }
    });
    
    volume.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        setVolume(volume.getSelection());
      }
    });
 }

  private boolean isPlaying() {
    try {
      if (currentZone != null) {
        return currentZone.getMediaRendererDevice().getAvTransportService().getTransportInfo().getState().equals(TransportState.PLAYING);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }
  
  private void setIsPlaying(boolean isPlaying) {
    play.setImage(isPlaying ? Images.PAUSE.image() : Images.PLAY.image());
    play.setData(isPlaying ? "Pause" : "Play" );
  }

  protected void previous() {
    try {
      if (currentZone != null) {
        currentZone.getMediaRendererDevice().getAvTransportService().previous();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  protected void play() {
    final String action = (String) play.getData();
    SonosController.getInstance().getExecutor().execute(new Runnable() {
      public void run() {
        try {
          if (action.equals("Pause")) {
            currentZone.getMediaRendererDevice().getAvTransportService().pause();
          } else if (action.equals("Play")) {
            currentZone.getMediaRendererDevice().getAvTransportService().play();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  protected void next() {
    try {
      if (currentZone != null) {
        currentZone.getMediaRendererDevice().getAvTransportService().next();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  protected void setVolume(int vol) {
    try {
      if (currentZone != null) {
        currentZone.getMediaRendererDevice().getRenderingControlService().setVolume(vol);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  protected int getVolume() {
    try {
      if (currentZone != null) {
        return currentZone.getMediaRendererDevice().getRenderingControlService().getVolume();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  public void zoneSelectionChangedTo(ZonePlayer newSelection) {
    if (currentZone != null) {
      currentZone.getMediaRendererDevice().getRenderingControlService().removeListener(this);
      currentZone.getMediaRendererDevice().getAvTransportService().removeAvTransportListener(this);
    }
    currentZone = newSelection;
    if (currentZone != null) {
      currentZone.getMediaRendererDevice().getRenderingControlService().addListener(this);
      currentZone.getMediaRendererDevice().getAvTransportService().addAvTransportListener(this);
    }
    volume.setSelection(getVolume());
    setIsPlaying(isPlaying());
    updateEnabledness();
  }

  /**
   * Sets the buttons to enabled/disabled depending on whether a zoneplayer is
   * selected.
   * 
   */
  private void updateEnabledness() {
    boolean enabled = (currentZone != null);
    play.setEnabled(enabled);
    volume.setEnabled(enabled);
    next.setEnabled(enabled);
    previous.setEnabled(enabled);
  }

  public void valuesChanged(Set<RenderingControlEventType> events, RenderingControlService source) {
    if (events.contains(RenderingControlEventType.VOLUME_MASTER)) {
      final int newVol = getVolume();
      getDisplay().asyncExec(new Runnable() {
        public void run() {
          volume.setSelection(newVol);
        }
      });
    }
    // TODO other volumes?
  }
  
  @Override
  public void dispose() {
    try {
      zoneSelectionChangedTo(null);

      previous.setImage(null);
      next.setImage(null);
      play.setImage(null);
    } catch (Exception e) {
    }
    for (Images i : Images.values()) {
    	try {
    		Image tmp = i.image();
    		i.setImage(null);
    		tmp.dispose();
    	} catch (Exception e) {
    	}
    }
    super.dispose();
  }

  public void valuesChanged(Set<AVTransportEventType> events, AVTransportService source) {
    if (source == currentZone.getMediaRendererDevice().getAvTransportService() 
        && events.contains(AVTransportEventType.TransportState)) {
      getDisplay().asyncExec(new Runnable() {
        public void run() {
          setIsPlaying(isPlaying());
        }
      });
    }
  }
}
