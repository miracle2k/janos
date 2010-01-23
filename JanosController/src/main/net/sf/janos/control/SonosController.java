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
package net.sf.janos.control;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.sbbi.upnp.Discovery;
import net.sbbi.upnp.DiscoveryAdvertisement;
import net.sbbi.upnp.DiscoveryEventHandler;
import net.sbbi.upnp.DiscoveryListener;
import net.sbbi.upnp.DiscoveryResultsHandler;
import net.sbbi.upnp.ServicesEventing;
import net.sbbi.upnp.devices.UPNPRootDevice;
import net.sf.janos.model.ZoneGroup;
import net.sf.janos.model.ZoneGroupState;
import net.sf.janos.model.ZoneGroupStateModel;
import net.sf.janos.model.ZonePlayerModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The main controller class that discovers the sonos devices, and provides
 * control over all the known zone player devices.
 * 
 * provides convenience methods for zone and zone player related activities.
 * 
 * @author David Wheeler
 * 
 */
public class SonosController implements ZoneGroupTopologyListener {
  
  private static final Log LOG = LogFactory.getLog(SonosController.class);

  private static SonosController INSTANCE;
  
  private final Executor workerExecutor = Executors.newFixedThreadPool(3, new ThreadFactory() {
    // yes, this i isn't threadsafe, but who cares?
    private int i=0;
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "SonosControllerWorkerThread" + i++);
      t.setDaemon(true);
      return t;
    }
  });
  
  private final Executor controllerExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "SonosControllerThread");
      t.setDaemon(true);
      return t;
    }
  });

  
  /**
   * A class to  handle both types of discovery events.
   * @author David Wheeler
   *
   */
  private class DiscoveryHandler implements DiscoveryEventHandler, DiscoveryResultsHandler {
    public void eventSSDPAlive(final String usn, String udn, String nt, final String maxAge, final URL location) {
      controllerExecutor.execute(new Runnable() {
        public void run() {
          try {
            addZonePlayer(new UPNPRootDevice(location, maxAge));
          } catch (MalformedURLException e) {
            LOG.warn("Discovered device " + usn + " with invalid URL: " + location);
          } catch (IllegalStateException e) {
            LOG.warn("Discovered device of a too-recent version.");
          }
        }
      });
    }
    public void eventSSDPByeBye(final String usn, final String udn, String nt) {
      controllerExecutor.execute(new Runnable() {
        public void run() {
          removeZonePlayer(udn);
          LOG.info("Bye bye from " + usn);
        }
      });
    }
    public void discoveredDevice(final String usn, String udn, String nt, final String maxAge, final URL location, final String firmware) {
      controllerExecutor.execute(new Runnable() {
        public void run() {
          try {
            addZonePlayer(new UPNPRootDevice(location, maxAge, firmware));
          } catch (MalformedURLException e) {
            LOG.warn("Discovered device " + usn + " with invalid URL: " + location);
          } catch (IllegalStateException e) {
            LOG.warn("Discovered device of a too-recent version.");
          }
        }
      });
    }
  };
  
  private final DiscoveryHandler discoveryHandler = new DiscoveryHandler();
  private final ZoneGroupStateModel groups = new ZoneGroupStateModel();
  private final ZonePlayerModel zonePlayers = new ZonePlayerModel();
  // a map from zone UDN to when the zone was last seen
  private final Map<String, Long> zonePlayerDiscoveries = new HashMap<String, Long>();
  private boolean registeredForSearching = false;

  /**
   * @return the singleton instance of SonosController
   */
  // TODO I hate singletons
  public synchronized static SonosController getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new SonosController();
    }
    return INSTANCE;
  }
  
  private SonosController() {
    // private constructor
  }
  
  public void searchForDevices() {
    try {
      if (!registeredForSearching) {
        ServicesEventing.getInstance().setDaemonPort(Integer.parseInt(System.getProperty("net.sf.EventingPort", "2001")));
        // These first two listen for broadcast advertisements, while the 3rd
        // listens for responses to a search request.
        DiscoveryAdvertisement.getInstance().registerEvent(
            DiscoveryAdvertisement.EVENT_SSDP_ALIVE, 
            ZonePlayerConstants.SONOS_DEVICE_TYPE, 
            discoveryHandler);
        DiscoveryAdvertisement.getInstance().registerEvent(
            DiscoveryAdvertisement.EVENT_SSDP_BYE_BYE, 
            ZonePlayerConstants.SONOS_DEVICE_TYPE, 
            discoveryHandler);
        DiscoveryListener.getInstance().registerResultsHandler(
            discoveryHandler, 
            ZonePlayerConstants.SONOS_DEVICE_TYPE);
      }
      sendSearchPacket(ZonePlayerConstants.SONOS_DEVICE_TYPE);
    } catch (IOException e) {
      LOG.error("Could not search for devices.", e);
    }
  }
  
  /**
   * Broadcasts a search packet on all network interfaces. This method should
   * really be a part of the Discovery class, but that's not my code...
   * 
   * @param searchTarget
   * @throws IOException 
   */
  public void sendSearchPacket(String searchTarget) throws IOException {
    for ( Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements(); ) {
      NetworkInterface intf = (NetworkInterface)e.nextElement();
      for ( Enumeration<InetAddress> adrs = intf.getInetAddresses(); adrs.hasMoreElements(); ) {
        InetAddress adr = (InetAddress)adrs.nextElement();
        if ( adr instanceof Inet4Address && !adr.isLoopbackAddress()  ) {
          Discovery.sendSearchMessage( adr, Discovery.DEFAULT_TTL, Discovery.DEFAULT_MX, searchTarget );
        }
      }
    }

  }
  
  public void purgeStaleDevices(long staleThreshold) {
    long now = System.currentTimeMillis();
    for (Entry<String, Long> zone : zonePlayerDiscoveries.entrySet()) {
      if (now - zone.getValue() > staleThreshold) {
        removeZonePlayer(zone.getKey());
      }
    }
  }
  
  /**
   * Creates a new ZonePlayer from the given device and adds it to our list.
   * @param dev
   * @throws IllegalArgumentException if <code>dev</code> is not a sonos device
   */
  private void addZonePlayer(final UPNPRootDevice dev) {
    zonePlayerDiscoveries.put(dev.getUDN().substring(5), System.currentTimeMillis());
    
    // Check if we've already got this zone player
    for (ZonePlayer zone : zonePlayers.getAllZones()) {
      if (zone.getRootDevice().getUDN().equals(dev.getUDN())) {
    	return;
      }
    }
    LOG.info("Discovered device " + dev.getDiscoveryUDN());

    // Ignore zone bridges 
    // TODO may need to implement cut down zone player for the zone bridge
    // I believe the bridge only supports the following interfaces:
    // DeviceProperties
    // GroupManagement
    // SystemProperties
    // ZoneGroup
    if (dev.getModelNumber().contains("ZB100")) {
      LOG.warn("Ignoring Zone " + dev.getDeviceType() + " " + dev.getModelDescription() + " " + dev.getModelName() + " " + dev.getModelNumber());
      return;
    }
    if (LOG.isInfoEnabled()) {
      LOG.info("Adding zone: " + dev.getDeviceType() + " " + dev.getModelDescription() + " " + dev.getModelName() + " " + dev.getModelNumber());
    }
    try {
      ZonePlayer zone = new ZonePlayer(dev);
      zonePlayers.addZonePlayer(zone);
      zone.getZoneGroupTopologyService().addZoneGroupTopologyListener(this);
      zoneGroupTopologyChanged(zone.getZoneGroupTopologyService().getGroupState());
    } catch (Exception e) {
      LOG.error("Couldn't add zone" + dev.getDeviceType() + " " + dev.getModelDescription() + " " + dev.getModelName() + " " + dev.getModelNumber(), e);
    }
  }
  
  /**
   * Removes a zone player if it has the specified UDN.
   * @param udn
   */
  private void removeZonePlayer(final String udn) {
    ZonePlayer zp = zonePlayers.getById(udn);
    if (zp != null) {
      LOG.info("Removing ZonePlayer " + udn + " " + zp.getRootDevice().getModelDescription());
      zonePlayers.remove(zp);
      zp.getZoneGroupTopologyService().removeZoneGroupTopologyListener(this);
      if (zonePlayers.getSize() == 0)
      {
        zoneGroupTopologyChanged(new ZoneGroupState(Collections.EMPTY_LIST));
      }
      zp.dispose();
    }
  }
  
  /**
   * @return the ZonePlayerModel
   */
  public ZonePlayerModel getZonePlayerModel() {
    return zonePlayers;
  }
  
  /**
   * @return the ZoneGroupStateModel
   */
  public ZoneGroupStateModel getZoneGroupStateModel() {
    return groups;
  }
  
  /**
   * @param zp
   *          a zone player
   * @return the coordinator of the zone player's group, or zp if it could not
   *         be discovered.
   */
  public ZonePlayer getCoordinatorForZonePlayer(ZonePlayer zp) {
    if (zp == null || zp.getZoneGroupTopologyService().getGroupState() == null) {
      return zp;
    }
    for (ZoneGroup zg : zp.getZoneGroupTopologyService().getGroupState().getGroups()) {
      if (zg.getMembers().contains(zp.getId())) {
        return getZonePlayerModel().getById(zg.getCoordinator());
      }
    }
    return zp;
  }
  
  /**
   * @return an Executor for performing asynchronous activities.
   */
  public Executor getWorkerExecutor() {
    return workerExecutor;
  }
  
  public Executor getControllerExecutor() {
    return controllerExecutor;
  }

  public void dispose() {
    DiscoveryAdvertisement.getInstance().unRegisterEvent(DiscoveryAdvertisement.EVENT_SSDP_ALIVE, ZonePlayerConstants.SONOS_DEVICE_TYPE, discoveryHandler);
    DiscoveryAdvertisement.getInstance().unRegisterEvent(DiscoveryAdvertisement.EVENT_SSDP_BYE_BYE, ZonePlayerConstants.SONOS_DEVICE_TYPE, discoveryHandler);
    DiscoveryListener.getInstance().unRegisterResultsHandler(discoveryHandler, ZonePlayerConstants.SONOS_DEVICE_TYPE);
    for (ZonePlayer zp : zonePlayers.getAllZones()) {
      zp.dispose();
    }
  }

	/**
	 * @see net.sf.janos.control.ZoneGroupTopologyListener#valuesChanged()
	 */
	public synchronized void zoneGroupTopologyChanged(ZoneGroupState groupState) {
		if (groupState == null) {
			return;
		}
		groups.handleGroupUpdate(groupState);
	}
}
