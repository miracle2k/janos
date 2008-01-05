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
 * A device for serving media to other devices. Contains a
 * ContentDirectoryService and a ConnectionManagerService.
 * 
 * @author David Wheeler
 * 
 */
public class MediaServerDevice {
  
  private final UPNPDevice dev;
  private final ContentDirectoryService contentDirectory;
  private final ConnectionManagerService connectionManager;
  
  protected MediaServerDevice(UPNPDevice dev) {
    if (!dev.getDeviceType().equals(ZonePlayerConstants.MEDIA_SERVER_DEVICE_TYPE)) {
      throw new IllegalArgumentException("Device must be media server, not " + dev.getDeviceType());
    }
    this.dev=dev;
    this.contentDirectory = new ContentDirectoryService(dev.getService(ZonePlayerConstants.SONOS_SERVICE_CONTENT_DIRECTORY));
    this.connectionManager = new ConnectionManagerService(dev.getService(ZonePlayerConstants.SONOS_SERVICE_CONNECTION_MANAGER));
  }
  
  /**
   * @return a ContentDirectoryService that allows the searching and listing of
   *         music.
   */
  public ContentDirectoryService getContentDirectoryService() {
    return contentDirectory;
  }
  
  /**
   * @return the ConnectionManagerService for this MediaServer.
   */
  public ConnectionManagerService getConnectionManagerService() {
    return connectionManager;
  }
  
  /**
   * @return the wrapped UPNPDevice.
   */
  public UPNPDevice getUPNPDevice() {
    return dev;
  }

  public void dispose() {
    this.contentDirectory.dispose();
    this.connectionManager.dispose();
  }
}
