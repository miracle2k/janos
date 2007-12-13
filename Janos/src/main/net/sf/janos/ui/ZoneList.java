/*
 * Created on 25/07/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sbbi.upnp.devices.DeviceIcon;
import net.sf.janos.Debug;
import net.sf.janos.control.SonosController;
import net.sf.janos.control.SonosControllerListener;
import net.sf.janos.control.ZonePlayer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ZoneList extends Composite implements SonosControllerListener {
  
  private final org.eclipse.swt.widgets.List zoneTable;
  private final SonosController controller;
  private final List<ZonePlayer> devices = new ArrayList<ZonePlayer>();

  public ZoneList(Composite parent, int style, SonosController controller) {
    super(parent, style);
    this.controller = controller;

    setLayout(new FillLayout(SWT.VERTICAL));
    zoneTable = new org.eclipse.swt.widgets.List(this, SWT.SINGLE);
//    zoneTable.set
    zoneTable.setToolTipText("Drag a zone onto another to link them");
    
    controller.addControllerListener(this);
  }
  
  public void deviceAdded(final ZonePlayer dev) {
    getDisplay().asyncExec(new Runnable() {
      public void run() {
        devices.add(dev);
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
        redraw();
      }
    });
  }
  
  public void deviceRemoved(final ZonePlayer dev) {
    getDisplay().asyncExec(new Runnable() {
      public void run() {
       devices.remove(dev);
       zoneTable.remove(dev.getDevicePropertiesService().getZoneAttributes().getName());
       redraw();
      }
    });
  }

}
