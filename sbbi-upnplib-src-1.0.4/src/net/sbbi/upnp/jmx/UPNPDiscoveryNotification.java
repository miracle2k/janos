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

import javax.management.Notification;
import javax.management.ObjectName;

/**
 * Discovery notification sent when a new set of UPNPServiceMBean for a given discovered
 * UPNP device is registered within the server. The same notification is also sent when a device is leaving the network.
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPDiscoveryNotification extends Notification {

  private String usn, udn, nt;
  private URL location;
  private ObjectName[] UPNPServiceMBeans;
  
  public UPNPDiscoveryNotification( String type, Object source, long sequenceNumber, long timeStamp ) {
    super( type, source, sequenceNumber, timeStamp );
  }

  /**
   * The Device descriptor location only provided when an ssdp alive notification is recieved
   * @return the device descriptor location
   */
  public URL getLocation() {
    return location;
  }

  protected void setLocation( URL location ) {
    this.location = location;
  }

  /**
   * The device type
   * @return the device type
   */
  public String getNt() {
    return nt;
  }

  protected void setNt( String nt ) {
    this.nt = nt;
  }

  /**
   * The device Identifier
   * @return the device ID
   */
  public String getUdn() {
    return udn;
  }

  protected void setUdn( String udn ) {
    this.udn = udn;
  }

  /**
   * The device id + ":" + type
   * @return the device USN
   */
  public String getUsn() {
    return usn;
  }

  protected void setUsn( String usn ) {
    this.usn = usn;
  }

  /**
   * The registered UPNPServiceMBeans object names bound to this device joining or leaving the network
   * @return UPNPServiceMBeans object names array
   */
  public ObjectName[] getUPNPServiceMBeans() {
    return UPNPServiceMBeans;
  }

  protected void setUPNPServiceMBeans( ObjectName[] serviceMBeans ) {
    UPNPServiceMBeans = serviceMBeans;
  }
  
  
  

}
