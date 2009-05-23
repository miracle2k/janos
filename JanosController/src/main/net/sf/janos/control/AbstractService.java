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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

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
  
  /**
   * The default length of time in seconds to register events for.
   */
  protected static final int DEFAULT_EVENT_PERIOD = 3600; // 1 hour
  
  protected final UPNPService service;
  protected final UPNPMessageFactory messageFactory;
  private static final Timer timer = new Timer("ServiceEventRefresher", true);
  private final List<EventingRefreshTask> tasks = new ArrayList<EventingRefreshTask>();
  
  public AbstractService(UPNPService service, String type) {
    if (!service.getServiceType().equals(type)) {
      throw new IllegalArgumentException("service must be " + type + ", not " + service.getServiceType());
    }
    
    this.service = service;
    this.messageFactory = UPNPMessageFactory.getNewInstance(service);
  }
  
  public void dispose() {
    LOG.info("Unregistering event listeners for " + getClass());
    List<EventingRefreshTask> tasksCopy = new ArrayList<EventingRefreshTask>(tasks);
    for (EventingRefreshTask task : tasksCopy) {
      unregisterServiceEventing(task.handler);
    }
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
  private int refreshServiceEventing(int duration, ServiceEventHandler handler) throws IOException {
    ServicesEventing eventing = ServicesEventing.getInstance();
    int i = eventing.register(service, handler, duration);
    LOG.info("Registered " + getClass() + " for eventing for " + i + "s");
    return i;
  }
  
  /**
   * Registers this service as a ServiceEventListener on its UPNPService. This
   * should be cleaned up by calling unregisterServiceEventing()
   * 
   * @param handler
   * @return true if the registration process completed successfully. if false
   *         is returned, registration will still be attempted periodically
   *         until unregisterServiceEventing() is called.
   */
  protected boolean registerServiceEventing(final ServiceEventHandler handler) {
    ThreadChangingEventHandlerWrapper handlerWrapper = new ThreadChangingEventHandlerWrapper(handler);
    try {
      refreshServiceEventing(DEFAULT_EVENT_PERIOD, handlerWrapper);
      EventingRefreshTask task = new EventingRefreshTask(handlerWrapper);
      tasks.add(task);
      // schedule to refresh 1 minute less than the event period (in milis, not seconds)
      timer.schedule(task, (DEFAULT_EVENT_PERIOD-60) * 1000, (DEFAULT_EVENT_PERIOD-60) * 1000);
      return true;
    } catch (IOException e) {
      LOG.warn("Could not register service eventing: ", e);
      return false;
    }
  }
  
  /**
   * Removes the given handler from Service Event registration. It will recieve
   * no further events.
   * 
   * @param handler
   * @throws IOException
   */
  protected void unregisterServiceEventing(ServiceEventHandler handler) {
    ServicesEventing eventing = ServicesEventing.getInstance();
    for (ListIterator<EventingRefreshTask> i= tasks.listIterator(); i.hasNext(); ) {
      EventingRefreshTask task = i.next();
      if (task.handler.getWrappedHandler() == handler) {
        task.cancel();
        i.remove();
        try {
          eventing.unRegister(service, task.handler);
        } catch (IOException e) {
          LOG.error("Could not unregister eventing from " + service, e);
        }
      }
    }
  }
  
  
  
  private class EventingRefreshTask extends TimerTask {
    private final ThreadChangingEventHandlerWrapper handler;
    
    protected EventingRefreshTask(ThreadChangingEventHandlerWrapper handler) {
      this.handler=handler;
    }
    
    @Override
    public void run() {
      try {
        refreshServiceEventing(DEFAULT_EVENT_PERIOD, handler);
      } catch (IOException e) {
        LOG.warn("Could not refresh eventing: ", e);
      }
    }
  }
  
  /**
   * A ServiceEventHandler that adapts the event to occur in the SonosController thread.
   * @author David Wheeler
   *
   */
  private static final class ThreadChangingEventHandlerWrapper implements ServiceEventHandler {
    
    private ServiceEventHandler handler;
    public ThreadChangingEventHandlerWrapper(ServiceEventHandler handler) {
      this.handler = handler;
    }
    public void handleStateVariableEvent(final String varName, final String newValue) {
      SonosController.getInstance().getControllerExecutor().execute(new Runnable() {
        public void run() {
          handler.handleStateVariableEvent(varName, newValue);
        }
      });
    }
    public ServiceEventHandler getWrappedHandler() {
      return handler;
    }
  }
}
