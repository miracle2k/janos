/*
 * Created on 21/10/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.control;

import net.sbbi.upnp.messages.UPNPMessageFactory;
import net.sbbi.upnp.services.UPNPService;

/**
 * An abstract class that wraps a UPNPService. Intended to be subclassed to have
 * functionality added.
 * 
 * @author David Wheeler
 * 
 */
public abstract class AbstractService {
  
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

}
