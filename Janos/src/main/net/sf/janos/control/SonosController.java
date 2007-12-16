/*
 * Created on 29/07/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.sbbi.upnp.Discovery;
import net.sbbi.upnp.devices.UPNPRootDevice;
import net.sf.janos.Debug;
import net.sf.janos.model.ZoneGroup;
import net.sf.janos.model.ZonePlayerModel;
import net.sf.janos.ui.zonelist.ZoneListSelectionListener;

/**
 * The main controller class that discovers the sonos devices, and provids
 * control over all the known zone player devices.
 * 
 * provides convenience methods for zone and zone player related activities.
 * 
 * @author David Wheeler
 * 
 */
public class SonosController implements ZoneListSelectionListener{
  
  private static SonosController INSTANCE;

  private final Executor EXECUTOR = Executors.newFixedThreadPool(3, new ThreadFactory() {
    // yes, this i isn't threadsafe, but who cares?
    private int i=0;
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "SonosControllerThread" + i++);
      t.setDaemon(true);
      return t;
    }
  });

  private ZonePlayerModel zonePlayers = new ZonePlayerModel();

//  private ZonePlayer currentZonePlayer;
  
  /**
   * @return the singleton instance of SonosController
   */
  // TODO I hate singletons
  public static SonosController getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new SonosController();
    }
    return INSTANCE;
  }
  
  private SonosController() {
    searchForZones();
  }
  
  /**
   * performs a search for zone player devices, adding the results using
   * {@link #addZonePlayer(UPNPRootDevice)}.
   * 
   */
  private void searchForZones() {
    UPNPRootDevice[] devices;
    try {
      devices = Discovery.discover(ZonePlayerConstants.SONOS_DEVICE_TYPE);
      if (devices != null) {
        for (UPNPRootDevice device : devices) {
          Debug.info("Device found: " + device.getFriendlyName());
          addZonePlayer(device);
        }
      } else {
        Debug.warn("No devices found");
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /**
   * Creates a new ZonePlayer and adds it to our list.
   * @param dev
   */
  private void addZonePlayer(final UPNPRootDevice dev) {
    ZonePlayer sd = new ZonePlayer(dev);
    zonePlayers.addZonePlayer(sd);
//    for (SonosControllerListener l : listeners) {
//      l.deviceAdded(sd);
//    }
  }
  
//  /**
//   * Adds a listener to be notified of new zone players. Will be notified of
//   * currently known zone players immediately.
//   * 
//   * @param listener
//   */
//  public void addControllerListener(SonosControllerListener listener) {
//    this.listeners.add(listener);
//    for (ZonePlayer dev : zonePlayers.getAllZones()) {
//      listener.deviceAdded(dev);
//    }
//  }

//  /**
//   * removes the given listener from the list to be notified of new zone
//   * players.
//   * 
//   * @param listener
//   */
//  public void removeControllerListener(SonosControllerListener listener) {
//    this.listeners.remove(listener);
//  }

  /**
   * TODO this should be elsewhere?
   * 
   */
//  public ZonePlayer getCurrentZonePlayerController() {
//    return getCoordinatorForZonePlayer(getCurrentZonePlayer());
//  }
  
//  public ZonePlayer getCurrentZonePlayer() {
//    return currentZonePlayer;
//  }
  
  public ZonePlayerModel getZonePlayerModel() {
    return zonePlayers;
  }

  /**
   * @param zp
   *          a zone player
   * @return the coordinator of the zone player's group, or zp if it could not
   *         be discovered.
   */
  public static ZonePlayer getCoordinatorForZonePlayer(ZonePlayer zp) {
    if (zp == null || zp.getZoneGroupTopologyService().getGroupState() == null) {
      return zp;
    }
    for (ZoneGroup zg : zp.getZoneGroupTopologyService().getGroupState().getGroups()) {
      if (zg.getMembers().contains(zp)) {
        return zg.getCoordinator();
      }
    }
    return zp;
  }
  
  /**
   * @return an Executor for performing asynchronous activities.
   */
  public Executor getExecutor() {
    return EXECUTOR;
  }

  public void zoneSelectionChangedTo(ZonePlayer newSelection) {
//    setCurrentZonePlayer(newSelection);
  }
  
  public void dispose() {
    
  }
  
//  public void setCurrentZonePlayer(ZonePlayer zone) {
//    this.currentZonePlayer = zone;
//  }
}
