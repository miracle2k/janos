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
import java.util.Enumeration;
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
  
  private final Executor EXECUTOR = Executors.newFixedThreadPool(3, new ThreadFactory() {
    // yes, this i isn't threadsafe, but who cares?
    private int i=0;
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "SonosControllerThread" + i++);
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
    public void eventSSDPAlive(String usn, String udn, String nt, String maxAge, URL location) {
      try {
        addZonePlayer(new UPNPRootDevice(location, maxAge));
        LOG.info("Discovered device " + usn);
      } catch (MalformedURLException e) {
        LOG.warn("Discovered device " + usn + " with invalid URL: " + location);
      } catch (IllegalStateException e) {
        LOG.warn("Discovered device of a too-recent version.");
      }
    }
    public void eventSSDPByeBye(String usn, String udn, String nt) {
      removeZonePlayer(udn);
      LOG.info("Bye bye from " + usn);
    }
    public void discoveredDevice(String usn, String udn, String nt, String maxAge, URL location, String firmware) {
      try {
        addZonePlayer(new UPNPRootDevice(location, maxAge, firmware));
        LOG.info("Discovered device " + usn);
      } catch (MalformedURLException e) {
        LOG.warn("Discovered device " + usn + " with invalid URL: " + location);
      } catch (IllegalStateException e) {
        LOG.warn("Discovered device of a too-recent version.");
      }
    }
  };
  
  private final DiscoveryHandler discoveryHandler = new DiscoveryHandler();
  private final ZoneGroupStateModel groups = new ZoneGroupStateModel();
  private final ZonePlayerModel zonePlayers = new ZonePlayerModel();

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
    ServicesEventing.getInstance().setDaemonPort(Integer.parseInt(System.getProperty("net.sf.EventingPort", "2001")));
    try {
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
  private void sendSearchPacket(String searchTarget) throws IOException {
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
  
  /**
   * Creates a new ZonePlayer from the given device and adds it to our list.
   * @param dev
   * @throws IllegalArgumentException if <code>dev</code> is not a sonos device
   */
  private void addZonePlayer(final UPNPRootDevice dev) {
    // Check if we've already got this zone player
    for (ZonePlayer zone : zonePlayers.getAllZones()) {
      if (zone.getRootDevice().getUDN().equals(dev.getUDN())) {
    	return;
      }
    }
    ZonePlayer sd = new ZonePlayer(dev);
    zonePlayers.addZonePlayer(sd);
    sd.getZoneGroupTopologyService().addZoneGroupTopologyListener(this);
  }
  
  /**
   * Removes a zone player if it has the specified UDN.
   * @param udn
   */
  private void removeZonePlayer(final String udn) {
    ZonePlayer zp = zonePlayers.getById(udn);
    zonePlayers.remove(zp);
    zp.getZoneGroupTopologyService().removeZoneGroupTopologyListener(this);
    zp.dispose();
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

  public void dispose() {
    DiscoveryAdvertisement.getInstance().unRegisterEvent(DiscoveryAdvertisement.EVENT_SSDP_ALIVE, ZonePlayerConstants.SONOS_DEVICE_TYPE, discoveryHandler);
    DiscoveryAdvertisement.getInstance().unRegisterEvent(DiscoveryAdvertisement.EVENT_SSDP_BYE_BYE, ZonePlayerConstants.SONOS_DEVICE_TYPE, discoveryHandler);
    DiscoveryListener.getInstance().unRegisterResultsHandler(discoveryHandler, ZonePlayerConstants.SONOS_DEVICE_TYPE);
    for (ZonePlayer zp : zonePlayers.getAllZones()) {
      zp.dispose();
    }
  }

	/*
	 * (non-Javadoc)
	 * @see net.sf.janos.control.ZoneGroupTopologyListener#valuesChanged()
	 */
	@Override
	public synchronized void zoneGroupTopologyChanged(ZoneGroupState groupState) {
		groups.handleGroupUpdate(groupState);
	}
}
