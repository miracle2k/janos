/*
 * ============================================================================
 *                 The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following  acknowledgment: "This product includes software
 *    developed by SuperBonBon Industries (http://www.sbbi.net/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "UPNPLib" and "SuperBonBon Industries" must not be
 *    used to endorse or promote products derived from this software without
 *    prior written permission. For written permission, please contact
 *    info@sbbi.net.
 *
 * 5. Products  derived from this software may not be called 
 *    "SuperBonBon Industries", nor may "SBBI" appear in their name, 
 *    without prior written permission of SuperBonBon Industries.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT,INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software  consists of voluntary contributions made by many individuals
 * on behalf of SuperBonBon Industries. For more information on 
 * SuperBonBon Industries, please see <http://www.sbbi.net/>.
 */
package net.sbbi.upnp.jmx;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sbbi.upnp.Discovery;
import net.sbbi.upnp.DiscoveryAdvertisement;
import net.sbbi.upnp.DiscoveryEventHandler;
import net.sbbi.upnp.devices.UPNPDevice;
import net.sbbi.upnp.devices.UPNPRootDevice;
import net.sbbi.upnp.services.UPNPService;

/**
 * MBean to discover UPNP devices on the network and register the devices service as
 * UPNPServiceMBean objects during the MBean registration. The registered
 * UPNPServiceMBean will also be automatically unregistered when the device is leaving the network
 * ( if notifySSDPEvents constructor param is set to true ) or when the UPNPDiscoveryMBean is
 * unregistered from teh MBeans server.
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPDiscovery implements DiscoveryEventHandler, UPNPDiscoveryMBean, NotificationBroadcaster {

  private final static Log log = LogFactory.getLog( UPNPDiscovery.class );
  
  private MBeanServer server;
  private NotificationBroadcasterSupport notifier;
  private MBeanNotificationInfo[] notifInfo;
  private Map registeredBeansPerUDN;
  private Set searchTargets;
  private int discoveryTimeout;
  private boolean notifySSDPEvents;
  private boolean registerChildDevices;
  private long ssdpAliveSequenceNumber = 0;
  private long ssdpByeByeSequenceNumber = 0;

  /**
   * Main constructor, will discover all devices types
   * @param discoveryTimeout devices discoverytimeout in MS, 0 for default timeout, 
   *                         increase this value if devices are not responding
   * @param notifySSDPEvents boolean to indicate if the MBean should broadcast JMX UPNPDiscoveryNotifications when an matching
   *                         device is joining or leaving the network.
   * @param registerChildDevices when set to true, discovered device child devices services will also be exposed as
   *                             UPNPServiceMBean objects
   */
  public UPNPDiscovery( int discoveryTimeout, boolean notifySSDPEvents, boolean registerChildDevices ) {
    this.registerChildDevices = registerChildDevices;
    this.notifySSDPEvents = notifySSDPEvents;
    this.discoveryTimeout = discoveryTimeout;
    if ( this.discoveryTimeout == 0 ) {
      this.discoveryTimeout = Discovery.DEFAULT_TIMEOUT;
    }
    notifier = new NotificationBroadcasterSupport();
    registeredBeansPerUDN = new HashMap();
    String[] types = new String[] { SSDP_ALIVE_NOTIFICATION, SSDP_BYEBYE_NOTIFICATION };
    notifInfo = new MBeanNotificationInfo[]{ new MBeanNotificationInfo( types, Notification.class.getName(), "SSDP UPNP events notifications" ) };
    searchTargets = new HashSet();
    searchTargets.add( Discovery.ROOT_DEVICES );
  }
  
  /**
   * Discover devices of a given type
   * @param searchTargets a list of devices types URI (I.E : urn:schemas-upnp-org:device:WANDevice:1) that should be handled, list delimited by commas
   * @param discoveryTimeout devices discoverytimeout in MS, 0 for default timeout, 
   *                         increase this value if devices are not responding
   * @param notifySSDPEvents boolean to indicate if the MBean should broadcast JMX UPNPDiscoveryNotifications when an matching
   *                         device is joining or leaving the network
   * @param registerChildDevices when set to true, discovered device child devices services will also be exposed as
   *                             UPNPServiceMBean objects
   */
  public UPNPDiscovery( String searchTargets, int discoveryTimeout, boolean notifySSDPEvents, boolean registerChildDevices ) {
    this( discoveryTimeout, notifySSDPEvents, registerChildDevices );
    String[] targets = searchTargets.split( "," );
    this.searchTargets.clear();
    for ( int i = 0; i < targets.length; i++ ) {
      this.searchTargets.add( targets[i] );
    }
  }
  
  /**
   * Discover devices of a given type
   * @param searchTargets a list of devices types URI (I.E : urn:schemas-upnp-org:device:WANDevice:1) that should be handled.
   *                      All discovered device children services will also be automatically registered as UPNPServiceMBean.
   * @param discoveryTimeout devices discoverytimeout in MS, 0 for default timeout, 
   *                         increase this value if devices are not responding
   * @param notifySSDPEvents boolean to indicate if the MBean should broadcast JMX UPNPDiscoveryNotifications when an matching
   *                         device is joining or leaving the network
   * @param registerChildDevices when set to true, discovered device child devices services will also be exposed as
   *                             UPNPServiceMBean objects
   */
  public UPNPDiscovery( String[] searchTargets, int discoveryTimeout, boolean notifySSDPEvents, boolean registerChildDevices ) {
    this( discoveryTimeout, notifySSDPEvents, registerChildDevices );
    this.searchTargets.clear();
    for ( int i = 0; i < searchTargets.length; i++ ) {
      this.searchTargets.add( searchTargets[i] );
    }
  }
  
  /**
   * Computes an array of object names of registered UPNPServiceMBeans for a given UPNP device UDN
   * @param deviceUDN the UPNP device UDN ( unique id on the network )
   * @return an array of object names or null if not matchs found for the given UDN
   * @throws MalformedObjectNameException if an object name cannot be computed for an UPNPServiceMBean
   */
  public ObjectName[] getRegisteredUPNPServiceMBeans( String deviceUDN ) throws MalformedObjectNameException {
    Set registeredBeans = (Set)registeredBeansPerUDN.get( deviceUDN );
    ObjectName[] rtrVal = null;
    if ( registeredBeans != null && registeredBeans.size() > 0 ) {
      Set copy = new HashSet( registeredBeans );
      rtrVal = new ObjectName[copy.size()];
      int z = 0;
      for ( Iterator i = copy.iterator(); i.hasNext(); ) {
        UPNPServiceMBean srv = (UPNPServiceMBean)i.next();
        rtrVal[z++] = srv.getObjectName();
      }
    }
    return rtrVal;
  }

  /**
   * The list of registered devices UDN, the returned UDN can be used with the getRegisteredUPNPServiceMBeans(String deviceUDN)
   * method to retreive UDN bound UPNPServiceMBean object names
   * @return a string array of UDN or null if no UPNP device services registered as UPNPServiceMBean
   */
  public String[] getRegisteredUPNPServiceMBeansUDNs() {
    if ( registeredBeansPerUDN.size() == 0 ) return null;
    Map copy = new HashMap( registeredBeansPerUDN );
    String[] rtrVal = new String[copy.size()];
    int z = 0;
    for ( Iterator i = copy.keySet().iterator(); i.hasNext(); ) {
      rtrVal[z++] = (String)i.next();
    }
    return rtrVal;
  }
  
  /**
   * The registered devices search targets
   * @return a set of search targets
   */
  public Set getSearchTargets() {
    return Collections.unmodifiableSet( searchTargets );
  }

  public void addNotificationListener( NotificationListener listener , NotificationFilter filter, Object callback ) throws IllegalArgumentException {
    notifier.addNotificationListener( listener, filter, callback );
  }

  public MBeanNotificationInfo[] getNotificationInfo() {
    if ( notifySSDPEvents ) {
      return notifInfo;
    }
    return null;
  }

  public void removeNotificationListener( NotificationListener listener ) throws ListenerNotFoundException {
    notifier.removeNotificationListener( listener );
  }

  public void postDeregister() {
  }

  public void postRegister( Boolean arg0 ) {
  }

  public void preDeregister() throws Exception {
    if ( notifySSDPEvents ) {
      for ( Iterator i = searchTargets.iterator(); i.hasNext(); ) {
        String st = (String)i.next();
        DiscoveryAdvertisement.getInstance().unRegisterEvent( DiscoveryAdvertisement.EVENT_SSDP_ALIVE, st, this );
        DiscoveryAdvertisement.getInstance().unRegisterEvent( DiscoveryAdvertisement.EVENT_SSDP_BYE_BYE, st, this );
      }
    }
    synchronized( registeredBeansPerUDN ) {
      for ( Iterator i = registeredBeansPerUDN.values().iterator(); i.hasNext(); ) {
        Set registeredMBeans = (Set)i.next();
        for ( Iterator z = registeredMBeans.iterator(); z.hasNext(); ) {
          UPNPServiceMBean bean = (UPNPServiceMBean)z.next();
          server.unregisterMBean( bean.getObjectName() );
        }
      }
    }
    registeredBeansPerUDN.clear();
  }

  public ObjectName preRegister( MBeanServer server, ObjectName objectname ) throws Exception {
    this.server = server;
    discoverDevices( discoveryTimeout );
    if ( notifySSDPEvents ) {
      for ( Iterator i = searchTargets.iterator(); i.hasNext(); ) {
        String st = (String)i.next();
        DiscoveryAdvertisement.getInstance().registerEvent( DiscoveryAdvertisement.EVENT_SSDP_ALIVE, st, this );
        DiscoveryAdvertisement.getInstance().registerEvent( DiscoveryAdvertisement.EVENT_SSDP_BYE_BYE, st, this );
      }
    }
    return objectname;
  }

  public void eventSSDPAlive( String usn, String udn, String nt, String maxAge, URL location ) {
    if ( registeredBeansPerUDN.get( udn ) == null ) {
      // new device...
      if ( searchTargets.contains( Discovery.ROOT_DEVICES ) || searchTargets.contains( nt ) ) {
        try {
          UPNPRootDevice newDevice = new UPNPRootDevice( location, maxAge, null, usn, udn );
          log.info( "Registering new device " + newDevice.getModelName() + " at " + location );
          register( newDevice, nt, null, null );
          UPNPDiscoveryNotification notif = new UPNPDiscoveryNotification( UPNPDiscoveryMBean.SSDP_ALIVE_NOTIFICATION, this, ssdpAliveSequenceNumber++, System.currentTimeMillis() );
          notif.setLocation( location );
          notif.setNt( nt );
          notif.setUdn( udn );
          notif.setUsn( usn );
          notif.setUPNPServiceMBeans( this.getRegisteredUPNPServiceMBeans( udn ) );
          notifier.sendNotification( notif );
        } catch ( Exception ex ) {
          log.error( "Error during new device " + location + " registration", ex );
        }
      }
    }
  }

  public void eventSSDPByeBye( String usn, String udn, String nt ) {
    synchronized( registeredBeansPerUDN ) {
      Set registeredBeans = (Set)registeredBeansPerUDN.get( udn );
      if ( registeredBeans != null ) {
        UPNPDiscoveryNotification notif = new UPNPDiscoveryNotification( UPNPDiscoveryMBean.SSDP_BYEBYE_NOTIFICATION, this, ssdpByeByeSequenceNumber++, System.currentTimeMillis() );
        notif.setNt( nt );
        notif.setUdn( udn );
        notif.setUsn( usn );
        try {
          notif.setUPNPServiceMBeans( this.getRegisteredUPNPServiceMBeans( udn ) );
        } catch ( MalformedObjectNameException ex ) {
          log.error( "Error during UPNPServiceMBean unregistration notification", ex );
        }
        log.info( "Device " + usn + " shutdown" );
        for ( Iterator i = registeredBeans.iterator(); i.hasNext(); ) {
          UPNPServiceMBean bean = (UPNPServiceMBean)i.next();
          try {
            server.unregisterMBean( bean.getObjectName() );
          } catch ( Exception ex ) {
            log.error( "Error during UPNPServiceMBean unregistration", ex );
          }
        }
        registeredBeansPerUDN.remove( udn );
        notifier.sendNotification( notif );
      }
    }
  }

  public void discoverDevices( int timeout ) throws Exception {
    // lookup for all root devices
    UPNPRootDevice[] dev = Discovery.discover( timeout, Discovery.ROOT_DEVICES ); 
    if ( dev != null ) {
      for ( int i = 0; i < dev.length; i++ ) {
        for ( Iterator j = searchTargets.iterator(); j.hasNext(); ) {
          String st = (String)j.next();
          register( dev[i], st, null, null );
        }
      }
    } else {
      log.info( "No devices found on the network" );
    }
  }
  
  private void register( UPNPDevice device, String searchTarget, Set registeredMBeansContainer, String deviceUDN ) throws Exception {
    
    List childrens = device.getTopLevelChildDevices();

    if ( searchTarget.equals( Discovery.ROOT_DEVICES ) ||
         device.getDeviceType().equals( searchTarget ) ) {
      synchronized( registeredBeansPerUDN ) {
        if ( deviceUDN == null ) {
          deviceUDN = device.getUDN();
        }
        log.info( "Registering UPNP device " + device.getDeviceType() + " " + device.getUDN() + " services" );
        if ( registeredMBeansContainer == null ) {
          registeredMBeansContainer = new HashSet();
          registeredBeansPerUDN.put( deviceUDN, registeredMBeansContainer );
        }
        List services = device.getServices();
        if ( services != null ) {
          registerServices( device, server, services, registeredMBeansContainer );
        }
        if( childrens != null ) {
          if ( registerChildDevices ) {
            for ( Iterator itr = childrens.iterator(); itr.hasNext(); ) {
              UPNPDevice childDevice = (UPNPDevice)itr.next();
              // all childrens of the device are automatically registered
              register( childDevice, Discovery.ROOT_DEVICES, registeredMBeansContainer, deviceUDN );
            }
          }
          childrens = null;
        }
      }
    }
    if( childrens != null ) {
      for ( Iterator itr = childrens.iterator(); itr.hasNext(); ) {
        UPNPDevice childDevice = (UPNPDevice)itr.next();
        register( childDevice, searchTarget, null, deviceUDN );
      }
    }
  }
  
  private void registerServices( UPNPDevice device, MBeanServer server, List services, Set beansContainer ) throws Exception {
    for ( Iterator i = services.iterator(); i.hasNext(); ) {
      UPNPService srv = (UPNPService)i.next();
      UPNPServiceMBean mBean = new UPNPServiceMBean( device, srv, null, null );
      log.info( "Registering service " + srv.getServiceId() );
      server.registerMBean( mBean, mBean.getObjectName() );
      beansContainer.add( mBean );
    }
  }

}
