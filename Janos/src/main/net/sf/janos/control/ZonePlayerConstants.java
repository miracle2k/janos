/*
 * Created on 17/10/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.control;

public class ZonePlayerConstants {

  public static final String SONOS_DEVICE_TYPE = "urn:schemas-upnp-org:device:ZonePlayer:1";
  public static final String MEDIA_SERVER_DEVICE_TYPE = "urn:schemas-upnp-org:device:MediaServer:1";
  public static final String MEDIA_RENDERER_DEVICE_TYPE = "urn:schemas-upnp-org:device:MediaRenderer:1";
  
  public static final String SONOS_SERVICE_ZONE_GROUP_TOPOLOGY = "urn:schemas-upnp-org:service:ZoneGroupTopology:1";
  public static final String SONOS_SERVICE_ZONE_GROUP_MANAGEMENT = "urn:schemas-upnp-org:service:GroupManagement:1";
  public static final String SONOS_SERVICE_CONTENT_DIRECTORY = "urn:schemas-upnp-org:service:ContentDirectory:1"; 
  public static final String SONOS_SERVICE_AV_TRANSPORT = "urn:schemas-upnp-org:service:AVTransport:1"; 
  public static final String SONOS_SERVICE_RENDERING_CONTROL = "urn:schemas-upnp-org:service:RenderingControl:1";
  public static final String SONOS_SERVICE_GROUP_MANAGEMENT = "urn:schemas-upnp-org:service:GroupManagement:1";
  public static final String SONOS_SERVICE_DEVICE_PROPERTIES = "urn:schemas-upnp-org:service:DeviceProperties:1";
  public static final String SONOS_SERVICE_SYSTEM_PROPERTIES = "urn:schemas-upnp-org:service:SystemProperties:1";
  public static final String SONOS_SERVICE_CONNECTION_MANAGER = "urn:schemas-upnp-org:service:ConnectionManager:1";
  public static final String SONOS_SERVICE_ALARM_CLOCK = "urn:schemas-upnp-org:service:AlarmClock:1";
  public static final String SONOS_SERVICE_AUDIO_IN = "urn:schemas-upnp-org:service:AudioIn:1";
  
  public static final String SONOS_VARIABLE_ZONE_GROUP_STATE = "ZoneGroupState";

  private ZonePlayerConstants() {
    // don't instantiate me
  }
}
