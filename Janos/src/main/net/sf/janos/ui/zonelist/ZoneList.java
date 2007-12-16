/*
 * Created on 25/07/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.ui.zonelist;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sbbi.upnp.devices.DeviceIcon;
import net.sf.janos.Debug;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.ZonePlayerModel;
import net.sf.janos.model.ZonePlayerModelListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class ZoneList extends Composite implements ZonePlayerModelListener {
  
  private final List<ZoneListSelectionListener> selectionListeners = new ArrayList<ZoneListSelectionListener>();
  private final org.eclipse.swt.widgets.List zoneTable;
  private final SonosController controller;
  private final ZonePlayerModel model;
  private int currentSelection = -1;

  public ZoneList(Composite parent, int style, SonosController controller) {
    super(parent, style);
    this.controller = controller;
    this.model = controller.getZonePlayerModel();

    setLayout(new FillLayout(SWT.VERTICAL));
    zoneTable = new org.eclipse.swt.widgets.List(this, SWT.SINGLE);
//    zoneTable.set
    zoneTable.setToolTipText("Drag a zone onto another to link them");
    
    zoneTable.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent arg0) {
        // Don't care
      }
      public void widgetSelected(SelectionEvent arg0) {
        int newSel = zoneTable.getSelectionIndex();
        if (newSel != currentSelection) {
          fireZoneSelectionChanged(model.get(newSel));
        }
      }
    });
    
    controller.getZonePlayerModel().addZonePlayerModelListener(this);
    for (ZonePlayer zp : controller.getZonePlayerModel().getAllZones()) {
      zonePlayerAdded(zp);
    }
  }
  
  public void zonePlayerAdded(final ZonePlayer dev) {
    getDisplay().asyncExec(new Runnable() {
      public void run() {
//        Label deviceLabel = new Label(ZoneList.this, SWT.NONE);
//        deviceLabel.setText(dev.getRootDevice().getFriendlyName());
        List<DeviceIcon> icons = dev.getRootDevice().getDeviceIcons(); //getChildDevice(SonosController.MEDIA_SERVER_DEVICE_TYPE).getDeviceIcons();
        Debug.info ("device icons: " + icons);
        if (icons != null && icons.size() > 0) {
          URL location = icons.get(0).getUrl();
          Debug.debug("Setting icon to "+ location);
          InputStream stream = null;
          try {
            stream = location.openStream();
//            deviceLabel.setImage(new Image(getDisplay(), stream));
          } catch (IOException e) {
            // who cares
          } finally {
            if (stream != null) {
              try {
                stream.close();
              } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
          }
        }
        zoneTable.add(dev.getDevicePropertiesService().getZoneAttributes().getName());
        if (zoneTable.getItemCount() == 1) {
          zoneTable.setSelection(0);
        }
        redraw();
      }
    });
  }
  
  public void zonePlayerRemoved(final ZonePlayer dev) {
    getDisplay().asyncExec(new Runnable() {
      public void run() {
       zoneTable.remove(dev.getDevicePropertiesService().getZoneAttributes().getName());
       redraw();
      }
    });
  }
  
  public ZonePlayer getSelectionZone() {
    return model.get(currentSelection);
  }

  public void addSelectionListener(ZoneListSelectionListener l) {
    this.selectionListeners.add(l);
  }
  
  public void removeSelectionListener(ZoneListSelectionListener l) {
    this.selectionListeners.remove(l);
  }
  
  protected void fireZoneSelectionChanged(ZonePlayer newSelection) {
    for (ZoneListSelectionListener l : this.selectionListeners) {
      l.zoneSelectionChangedTo(newSelection);
    }
  }
  
}
