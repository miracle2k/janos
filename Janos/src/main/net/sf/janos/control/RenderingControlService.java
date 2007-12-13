/*
 * Created on 21/10/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.control;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sbbi.upnp.ServiceEventHandler;
import net.sbbi.upnp.ServiceEventSubscription;
import net.sbbi.upnp.ServicesEventing;
import net.sbbi.upnp.messages.ActionMessage;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sbbi.upnp.services.UPNPService;
import net.sf.janos.Debug;

/**
 * Provides control over the rendering (playing) of media (audio).
 * 
 * NOTE: this class is incomplete
 * 
 * @author David Wheeler
 * 
 */
public class RenderingControlService extends AbstractService {
  
  private static final String GET_MUTE_ACTION = "GetMute";
  private static final String SET_MUTE_ACTION = "SetMute";
  private static final String GET_VOLUME_ACTION = "GetVolume";
  private static final String SET_VOLUME_ACTION = "SetVolume";
  private static final String SET_RELATIVE_VOLUME_ACTION = "SetRelativeVolume";
  private static final String GET_VOLUME_DB_ACTION = "GetVolumeDB";
  private static final String SET_VOLUME_DB_ACTION = "SetVolumeDB";
  private static final String GET_VOLUME_DB_RANGE_ACTION = "GetVolumeDBRange";
  private static final String GET_BASS_ACTION = "GetBass";
  private static final String SET_BASS_ACTION = "SetBass";
  private static final String GET_TREBLE_ACTION = "GetTreble";
  private static final String SET_TREBLE_ACTION = "SetTreble";
  private static final String GET_LOUDNESS_ACTION = "GetLoudness";
  private static final String SET_LOUDNESS_ACTION = "SetLoudness";
  private static final String GET_SUPPORTS_OUTPUT_FIXED_ACTION = "GetSupportsOutputFixed";
  private static final String GET_OUTPUT_FIXED_ACTION = "GetOutputFixed";
  private static final String SET_OUTPUT_FIXED_ACTION = "SetOutputFixed";
  private static final String RAMP_TO_VOLUME_ACTION = "RampToVolume";
  private static final String RESTORE_VOLUME_ACTION = "RestoreVolumePriorToRamp";
    
  // TODO synchronize
  private Map<String, String> stateVars = new HashMap<String, String>();

  protected RenderingControlService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_RENDERING_CONTROL);
    
    ServicesEventing instance = ServicesEventing.getInstance();
    ServiceEventSubscription sub;
    try {
      sub = instance.registerEvent(service, new RenderingControlServiceEventHandler(), 60000);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } 

    // TODO create timer task to refresh registration
  }
  
//  public boolean getMute() {
//    ActionMessage msg = messageFactory.getMessage(GET_MUTE_ACTION);
//    msg.setInputParameter(parameterName, parameterValue)
//  }
  
  /**
   * Sets the volume to the given %. Must be between 0-100 inclusive.
   * 
   * @param vol
   *          the new volume as a percentage of maximum volume.
   * @throws IOException
   * @throws UPNPResponseException
   */
  public void setVolume(int vol) throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage(SET_VOLUME_ACTION);
    message.setInputParameter("InstanceID", 0);
    message.setInputParameter("Channel", "Master"); // can also be LF or RF
    message.setInputParameter("DesiredVolume", vol);
    message.service();
  }
  
  /**
   * @return the current volume, as a percentage of maximum volume.
   * @throws IOException
   * @throws UPNPResponseException
   */
  public int getVolume() throws IOException, UPNPResponseException {
    ActionMessage message = messageFactory.getMessage(GET_VOLUME_ACTION);
    message.setInputParameter("InstanceID", 0);
    message.setInputParameter("Channel", "Master"); // can also be LF or RF
    ActionResponse resp = message.service();
    return Integer.parseInt(resp.getOutActionArgumentValue("CurrentVolume"));
  }
  
  protected class RenderingControlServiceEventHandler implements ServiceEventHandler {

    public void handleStateVariableEvent(String varName, String newValue) {
      Debug.debug("received event " + varName + ": " + newValue);
      stateVars.put(varName, newValue);
    }
  }
}
