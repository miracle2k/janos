/*
 * Created on 04/08/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.control;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sbbi.upnp.devices.UPNPRootDevice;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.janos.model.Entry;

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
    // TODO this just plays it...
    try {
      AVTransportService serv = getMediaRendererDevice().getAvTransportService();
      serv.addToQueue(entry);
      serv.play();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (UPNPResponseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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
}
