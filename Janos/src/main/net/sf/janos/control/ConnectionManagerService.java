/*
 * Created on 21/10/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.control;

import net.sbbi.upnp.services.UPNPService;

public class ConnectionManagerService extends AbstractService {

  protected ConnectionManagerService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_CONNECTION_MANAGER);
  }

}
