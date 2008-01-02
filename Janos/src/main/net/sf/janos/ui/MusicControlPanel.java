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
import java.util.Set;

import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.janos.control.RenderingControlService;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.control.ZonePlayerServiceListener;
import net.sf.janos.model.TransportInfo.TransportState;
import net.sf.janos.model.xml.RenderingControlEventHandler.EventType;
import net.sf.janos.ui.zonelist.ZoneListSelectionListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
public class MusicControlPanel extends Composite implements ZoneListSelectionListener, ZonePlayerServiceListener<RenderingControlService> {

  private ZonePlayer currentZone;
  private Button play;
  private Scale volume;

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
    
    volume = new Scale(this, SWT.HORIZONTAL);
    volume.setMinimum(0);
    volume.setMaximum(100);
    Button previous = new Button(this, SWT.PUSH);
    previous.setText("<");
    previous.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        previous();
      }
    });
    // TODO initialize play and volume to correct values
    play = new Button(this, SWT.PUSH);
    play.addMouseListener(new MouseAdapter() {
      public void mouseUp(MouseEvent e) {
        play();
      }
    });
    Button next = new Button(this, SWT.PUSH);
    next.setText(">");
    next.addMouseListener(new MouseAdapter() {
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
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return false;
  }
  
  private void setIsPlaying(boolean isPlaying) {
    play.setText(isPlaying ? "Pause" : "Play");
  }

  protected void previous() {
    try {
      if (currentZone != null) {
        currentZone.getMediaRendererDevice().getAvTransportService().previous();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  protected void play() {
    try {
      if (play.getText().equals("Pause")) {
        currentZone.getMediaRendererDevice().getAvTransportService().pause();
        setIsPlaying(isPlaying());
      } else if (play.getText().equals("Play")) {
        currentZone.getMediaRendererDevice().getAvTransportService().play();
        setIsPlaying(isPlaying());
      }
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected void next() {
    try {
      if (currentZone != null) {
        currentZone.getMediaRendererDevice().getAvTransportService().next();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  protected void setVolume(int vol) {
    try {
      if (currentZone != null) {
        currentZone.getMediaRendererDevice().getRenderingControlService().setVolume(vol);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  protected int getVolume() {
    try {
      if (currentZone != null) {
        return currentZone.getMediaRendererDevice().getRenderingControlService().getVolume();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return 0;
  }

  public void zoneSelectionChangedTo(ZonePlayer newSelection) {
    if (currentZone != null) {
      currentZone.getMediaRendererDevice().getRenderingControlService().removeListener(this);
    }
    currentZone = newSelection;
    if (currentZone != null) {
      currentZone.getMediaRendererDevice().getRenderingControlService().addListener(this);
    }
    volume.setSelection(getVolume());
    setIsPlaying(isPlaying());
  }

  public void valuesChanged(Set<EventType> events, RenderingControlService source) {
    if (events.contains(EventType.VOLUME_MASTER)) {
      final int newVol = getVolume();
      getDisplay().asyncExec(new Runnable() {
        public void run() {
          volume.setSelection(newVol);
        }
      });
    }
    // TODO other volumes?
  }
  
  public void dispose() {
    zoneSelectionChangedTo(null);
    super.dispose();
  }
}
