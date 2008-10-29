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

import java.util.Set;

import javax.management.MBeanRegistration;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * MBean to discover UPNP devices on the network and register the devices service as
 * UPNPServiceMBean objects during the MBean registration. The registered
 * UPNPServiceMBean will also be automatically unregistered when the device is leaving the network
 * or when the UPNPDiscoveryMBean is unregistered from teh MBeans server.
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public interface UPNPDiscoveryMBean extends MBeanRegistration {

  /**
   * Notification type for devices joining the network
   */
  public final static String SSDP_ALIVE_NOTIFICATION = UPNPDiscoveryMBean.class.getName() + ".ssdp.alive";
  /**
   * Notification type for devices leaving the network
   */
  public final static String SSDP_BYEBYE_NOTIFICATION = UPNPDiscoveryMBean.class.getName() + ".ssdp.byebye";
  
  /**
   * The registered devices search targets
   * @return a set of search targets
   */
  public Set getSearchTargets();
  
  /**
   * Computes an array of object names of registered UPNPServiceMBeans for a given UPNP device UDN
   * @param deviceUDN the UPNP device UDN ( unique id on the network )
   * @return an array of object names or null if not matchs found for the given UDN
   * @throws MalformedObjectNameException if an object name cannot be computed for an UPNPServiceMBean
   */
  public ObjectName[] getRegisteredUPNPServiceMBeans( String deviceUDN ) throws MalformedObjectNameException;
  
  /**
   * The list of registered devices UDN, the returned UDN can be used with the getRegisteredUPNPServiceMBeans(String deviceUDN)
   * method to retreive UDN bound UPNPServiceMBean object names
   * @return a string array of UDN or null if no UPNP device services registered as UPNPServiceMBean
   */
  public String[] getRegisteredUPNPServiceMBeansUDNs();

}
