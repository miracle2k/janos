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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.xml.sax.SAXException;

import net.sbbi.upnp.devices.UPNPRootDevice;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.janos.model.Entry;
import net.sf.janos.model.TransportInfo.TransportState;

/**
 * Corresponds to a physical Zone Player, and gives access all the devices and
 * services that a Zone Player has.
 * 
 * @author David Wheeler
 * 
 */
public class ZonePlayer {
  
  private final UPNPRootDevice dev;
  
  private final MediaServerDevice mediaServer;
  private final MediaRendererDevice mediaRenderer;
  
  private final AlarmClockService alarm;
  private final AudioInService audioIn;
  private final DevicePropertiesService deviceProperties;
  private final SystemPropertiesService systemProperties;
  private final ZoneGroupTopologyService zoneGroupTopology;
  private final ZoneGroupManagementService zoneGroupManagement;

  private InetAddress ip;
  private final int port;
  
  /**
   * Creates a new sonos device around the given UPNPRootDevice. This device
   * must be a sonos device
   * 
   * @param dev
   * @throws IllegalArgumentException if dev is not a sonos device.
   */
  protected ZonePlayer(UPNPRootDevice dev) {
    if (!dev.getDeviceType().equals(ZonePlayerConstants.SONOS_DEVICE_TYPE)) {
      throw new IllegalArgumentException("dev must be a sonos device, not "
          + dev.getDeviceType());
    }
    this.dev = dev;
    try {
      this.ip = InetAddress.getByName(dev.getURLBase().getHost());
    } catch (UnknownHostException e) {
      // will not happen - should be IP not host
      e.printStackTrace();
    }
    this.port = dev.getURLBase().getPort();
    this.mediaServer = new MediaServerDevice(dev
        .getChildDevice(ZonePlayerConstants.MEDIA_SERVER_DEVICE_TYPE));
    this.mediaRenderer = new MediaRendererDevice(dev
        .getChildDevice(ZonePlayerConstants.MEDIA_RENDERER_DEVICE_TYPE));
    this.alarm = new AlarmClockService(dev
        .getService(ZonePlayerConstants.SONOS_SERVICE_ALARM_CLOCK));
    this.audioIn = new AudioInService(dev
        .getService(ZonePlayerConstants.SONOS_SERVICE_AUDIO_IN));
    this.deviceProperties = new DevicePropertiesService(dev
        .getService(ZonePlayerConstants.SONOS_SERVICE_DEVICE_PROPERTIES));
    this.systemProperties = new SystemPropertiesService(dev
        .getService(ZonePlayerConstants.SONOS_SERVICE_SYSTEM_PROPERTIES));
    this.zoneGroupTopology = new ZoneGroupTopologyService(dev
        .getService(ZonePlayerConstants.SONOS_SERVICE_ZONE_GROUP_TOPOLOGY));
    this.zoneGroupManagement = new ZoneGroupManagementService(dev
        .getService(ZonePlayerConstants.SONOS_SERVICE_ZONE_GROUP_MANAGEMENT));
  }
  
  public void dispose() {
    this.mediaServer.dispose();
    this.mediaRenderer.dispose();
    this.alarm.dispose();
    this.audioIn.dispose();
    this.deviceProperties.dispose();
    this.systemProperties.dispose();
    this.zoneGroupTopology.dispose();
    this.zoneGroupManagement.dispose();
  }
  
  /**
   * @return the UPNPRootDevice around which this object has been created.
   */
  public UPNPRootDevice getRootDevice() {
    return dev;
  }
  
  /**
   * @return the DeviceProperties service for this zone player
   */
  public DevicePropertiesService getDevicePropertiesService() {
    return deviceProperties;
  }

  /**
   * @return a SonosMediaServerDevice for our zone player.
   */
  public MediaServerDevice getMediaServerDevice() {
    return mediaServer;
  }

  /**
   * @return a UPNPDevice of type MediaRenderer, from our sonos object.
   */
  public MediaRendererDevice getMediaRendererDevice() {
    return mediaRenderer;
  }

  /**
   * @return the AlarmClockService for this zone player.
   */
  public AlarmClockService getAlarmService() {
    return alarm;
  }

  /**
   * @return the audio in service for this zone player.
   */
  public AudioInService getAudioInService() {
    return audioIn;
  }

  /**
   * @return system properties service for this zone player.
   */
  public SystemPropertiesService getSystemPropertiesService() {
    return systemProperties;
  }

  /**
   * @return the zone group management service for this player.
   */
  public ZoneGroupManagementService getZoneGroupManagementService() {
    return zoneGroupManagement;
  }

  /**
   * @return the zone group topology service for this zone player.
   */
  public ZoneGroupTopologyService getZoneGroupTopologyService() {
    return zoneGroupTopology;
  }
  
  // --- a few convenience methods
  
  /**
   * Adds the given entry to the play queue for this zone player.
   * 
   * NOTE: this should only be called if this zone player is the zone group
   * coordinator.
   * 
   * @param entry
   *          the entry to enqueue.
   */
  public void enqueueEntry(Entry entry) {
    try {
      AVTransportService serv = getMediaRendererDevice().getAvTransportService();
      int index = serv.addToQueue(entry);
      if (serv.getMediaInfo().getCurrentURI().startsWith("x-rincon-queue:")) {
        playQueueEntry(index - 1);
      }
      if (!serv.getTransportInfo().getState().equals(TransportState.PLAYING)) {
        serv.play();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void playQueueEntry(int index) throws IOException, UPNPResponseException {
    AVTransportService serv = getMediaRendererDevice().getAvTransportService();
    List<Entry> entries = getMediaServerDevice().getContentDirectoryService().getQueue(index-1, 1);
    if (!entries.isEmpty()) {
      Entry queue = entries.get(0);
      serv.setAvTransportUri(queue);
    }
  }

  /**
   * @return the IP address for this zone player.
   */
  public InetAddress getIP() {
    return ip;
  }
  
  /**
   * @return the port for HTTP requests to this zone player.
   */
  public int getPort() {
    return port;
  }
  
  // TODO is UDN a sensible choice for identity?
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj instanceof ZonePlayer) {
      ZonePlayer zp = (ZonePlayer) obj;
      return zp.getRootDevice().getUDN().equals(getRootDevice().getUDN());
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return getRootDevice().getUDN().hashCode();
  }
}
