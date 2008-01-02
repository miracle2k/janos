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

import net.sbbi.upnp.ServiceEventHandler;
import net.sbbi.upnp.ServicesEventing;
import net.sbbi.upnp.messages.UPNPMessageFactory;
import net.sbbi.upnp.services.UPNPService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An abstract class that wraps a UPNPService. Intended to be subclassed to have
 * functionality added.
 * 
 * @author David Wheeler
 * 
 */
public abstract class AbstractService {
  
  private static final Log LOG = LogFactory.getLog(AbstractService.class);
  
  protected static final int DEFAULT_EVENT_PERIOD = 600; // 10 mins
  
  protected final UPNPService service;
  protected final UPNPMessageFactory messageFactory;
  
  public AbstractService(UPNPService service, String type) {
    if (!service.getServiceType().equals(type)) {
      throw new IllegalArgumentException("service must be " + type + ", not " + service.getServiceType());
    }
    
    this.service = service;
    this.messageFactory = UPNPMessageFactory.getNewInstance(service);
  }
  
  /**
   * @return the UPNPService wrapped by this class.
   */
  public UPNPService getUPNPService() {
    return service;
  }
  
  /**
   * Adds or refreshes the registration of service event notifications to the
   * given handler
   * 
   * @param duration
   *          the requested length of time for event notifications
   * @param handler
   *          the object that handles the notifications
   * @return the length of time that the handler has been registered for.
   * @throws IOException
   *           if the device could not be contacted for some reason.
   */
  protected int refreshServiceEventing(int duration, ServiceEventHandler handler) throws IOException {
    ServicesEventing eventing = ServicesEventing.getInstance();
    int i = eventing.register(service, handler, duration);
    LOG.info("Registered " + getClass() + " for eventing for " + i + "s");
    return i;
  }
  
  /**
   * Removes the given handler from Service Event registration. It will recieve
   * no further events.
   * 
   * @param handler
   * @throws IOException
   */
  protected void unregisterServiceEventing(ServiceEventHandler handler) throws IOException {
    ServicesEventing eventing = ServicesEventing.getInstance();
    eventing.unRegister(service, handler);
  }
}
