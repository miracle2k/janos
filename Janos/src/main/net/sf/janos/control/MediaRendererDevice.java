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

import net.sbbi.upnp.devices.UPNPDevice;

/**
 * A device that "renders media" ie plays music and/or video. Contains an
 * AVTransportService, a ConnectionManager and a RenderingControlService.
 * 
 * @author David Wheeler
 * 
 */
public class MediaRendererDevice {

  private final UPNPDevice dev;
  
  private final RenderingControlService renderingControl;
  private final ConnectionManagerService connectionManager;
  private final AVTransportService avTransport;
  
  protected MediaRendererDevice(UPNPDevice dev) {
    if (!dev.getDeviceType().equals(ZonePlayerConstants.MEDIA_RENDERER_DEVICE_TYPE)) {
      throw new IllegalArgumentException("Device must be media renderer, not " + dev.getDeviceType());
    }
    this.dev=dev;
    
    this.renderingControl = new RenderingControlService(dev.getService(ZonePlayerConstants.SONOS_SERVICE_RENDERING_CONTROL));
    this.connectionManager = new ConnectionManagerService(dev.getService(ZonePlayerConstants.SONOS_SERVICE_CONNECTION_MANAGER));
    this.avTransport = new AVTransportService(dev.getService(ZonePlayerConstants.SONOS_SERVICE_AV_TRANSPORT));
  }
  
  public void dispose() {
    this.renderingControl.dispose();
    this.connectionManager.dispose();
    this.avTransport.dispose();
  }
  
  /**
   * @return the wrapped UPNPDevice.
   */
  public UPNPDevice getUPNPDevice() {
    return dev;
  }

  /**
   * @return the AVTransportService for this MediaRenderer
   */
  public AVTransportService getAvTransportService() {
    return avTransport;
  }

  /**
   * @return the ConnectionManager for this MediaRenderer.
   */
  public ConnectionManagerService getConnectionManagerService() {
    return connectionManager;
  }

  /**
   * @return The RenderingControlService for this MediaRenderer.
   */
  public RenderingControlService getRenderingControlService() {
    return renderingControl;
  }
  
}
