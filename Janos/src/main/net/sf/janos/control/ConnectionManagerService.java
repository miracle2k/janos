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

import net.sbbi.upnp.services.UPNPService;

public class ConnectionManagerService extends AbstractService {

  protected ConnectionManagerService(UPNPService service) {
    super(service, ZonePlayerConstants.SONOS_SERVICE_CONNECTION_MANAGER);
  }
  
  public void handleStateVariableEvent(String varName, String newValue) {
    // TODO Auto-generated method stub
    
  }

}