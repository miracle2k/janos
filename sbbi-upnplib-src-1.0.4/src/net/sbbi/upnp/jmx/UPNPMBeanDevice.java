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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.modelmbean.ModelMBean;

/**
 * This class can be used to expose a JMX MBean as an UPNP device.
 * The MBeans methods names and params obtained via the MBeanInfo Object
 * will be used to create the UPNP device operations set, and the UPNP devie state variables
 * will be obtained from the MBeans attributes.
 * STILL A WORK IN PROGRESS MUST BE CONSIDERED AS BETA QUALITY SOFTWARE
 * doc net.sbbi.upnp.UPNPMBeanDevice.boundAddr sys prop
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPMBeanDevice {

  private static String libVersion = null;
  static {
    Properties props = new Properties();
    try {
      props.load( UPNPMBeanDevice.class.getClassLoader().getResourceAsStream( "net/sbbi/upnp/version.properties" ) );
      libVersion = props.getProperty( "release.version" );
    } catch ( IOException ex ) {
      ex.printStackTrace();
    }
  }

  public static final String IMPL_NAME = System.getProperty( "os.name" ) + " UPnP/1.0 SuperBonBon Industries JMX UPNP/" + libVersion;
  public static int DEFAULT_MAX_AGE = 1800;
  public static int DEFAULT_TTL = 4;
  
  private static InetSocketAddress defaultBindAddr = getUPNPMBeansBoundAddr();
  
  private String uuid;
  private String internalId;
  private String deviceInfo;

  private InetSocketAddress bindAddress;
  private String location;
 
  private boolean rootDevice = true;
  private boolean started = false;
  
  private List childrens = new ArrayList();
  private List services = new ArrayList();
  
  // required fields
  private String vendorDomain;
  private String deviceType;
  private String manufacturer;
  private int deviceVersion;
  private String friendlyName;
  private String modelName;
  
  // optional fields
  private URL manufacturerURL;
  private String modelDescription;
  private String modelNumber;
  private URL modelURL;
  private String serialNumber;
  private String UPC;
  
  private int SSDPAliveDelay = DEFAULT_MAX_AGE;
  private int SSDPTTL = DEFAULT_TTL;
  
  public UPNPMBeanDevice( String deviceType, int deviceVersion, String manufacturer, 
                          String friendlyName, String modelName, String internalId ) throws RuntimeException {
    this( "urn:schemas-upnp-org", deviceType, deviceVersion, manufacturer, friendlyName, modelName, internalId );
  }
  
  public UPNPMBeanDevice( String vendorDomain, String deviceType, int deviceVersion, 
                          String manufacturer, String friendlyName, String modelName, String internalId ) throws RuntimeException {
    
    this.vendorDomain = vendorDomain;
    this.deviceVersion = deviceVersion;
    this.deviceType = "urn:" + this.vendorDomain + ":device:" + deviceType + ":" + this.deviceVersion;
    this.manufacturer = manufacturer;
    this.friendlyName = friendlyName;
    this.modelName = modelName;
    this.internalId = internalId;
    if ( this.internalId == null ) this.internalId = this.deviceType;
    bindAddress = defaultBindAddr;
    generateDeviceUUID();
  }
  
  public void setBindAddress( InetSocketAddress bindAddress ) {
    this.bindAddress = bindAddress;
    generateDeviceUUID();
  }
  
  public InetSocketAddress getBindAddress() {
    return bindAddress;
  }
  
  public int getSSDPAliveDelay() {
    return SSDPAliveDelay;
  }
  
  /**
   * The SSDP alive broadcast message sending delay in seconds,
   * should be greater than 1800 secs
   * @param aliveDelay
   */
  public void setSSDPAliveDelay( int aliveDelay ) {
    if ( aliveDelay < DEFAULT_MAX_AGE ) throw new IllegalArgumentException( "SSDPAliveDelay must be greater than " + DEFAULT_MAX_AGE + " secs" );
    SSDPAliveDelay = aliveDelay;
  }
  
  public int getSSDPTTL() {
    return SSDPTTL;
  }

  public void setSSDPTTL(int ssdpttl) {
    SSDPTTL = ssdpttl;
  }

  protected UPNPMBeanService getUPNPMBeanService( String serviceUuid ) {
    for ( Iterator i = services.iterator(); i.hasNext(); ) {
      
      UPNPMBeanService srv = (UPNPMBeanService)i.next();
      if ( srv.getServiceUUID().equals( serviceUuid ) ) {
        return srv;
      }
    }
    return null;
  }
  
  protected List getUPNPMBeanServices() {
    return services;
  }
  
  protected List getUPNPMBeanChildrens() {
    return childrens;
  }
  
  protected String getUuid() {
    return uuid;
  }
  
  protected String getDeviceInfo() {
    return deviceInfo;
  }
  
  public boolean isStarted() {
    return started;
  }
  
  protected String getLocation() {
    return location;
  }

  protected String getDeviceType() {
    return deviceType;
  }
  
  protected boolean isRootDevice() {
    return rootDevice;
  }

  public void addChildMBean( UPNPMBeanDevice device ) {
    device.rootDevice = false;
    childrens.add( device );
  }
  
  public void addService( ModelMBean mbean, ObjectName beanName, MBeanServer targetServer, 
                          String serviceId, String serviceType, int serviceVersion ) throws IOException {
    addService( mbean.getMBeanInfo(), beanName, targetServer, serviceId, serviceType, serviceVersion );
  }

  public void addService( DynamicMBean mbean, ObjectName beanName, MBeanServer targetServer, 
                          String serviceId, String serviceType, int serviceVersion ) throws IOException {
    addService( mbean.getMBeanInfo(), beanName, targetServer, serviceId, serviceType, serviceVersion );
  }
  
  public void addService( Object mbean, ObjectName beanName, MBeanServer targetServer,
                          String serviceId, String serviceType, int serviceVersion ) throws IOException, IntrospectionException, InstanceNotFoundException, ReflectionException { 
    addService( targetServer.getMBeanInfo( beanName ), beanName, targetServer, serviceId, serviceType, serviceVersion );
  }
  
  public void addService( MBeanInfo info, ObjectName beanName, MBeanServer targetServer, 
                          String serviceId, String serviceType, int serviceVersion ) throws IOException {
    UPNPMBeanService deviceService = new UPNPMBeanService( uuid, this.vendorDomain, serviceId, serviceType, serviceVersion, info, beanName, targetServer );
    // check that there is no duplicate serviceType
    String newServiceType = deviceService.getServiceType();
    for ( Iterator i = services.iterator(); i.hasNext(); ) {
      UPNPMBeanService srv = (UPNPMBeanService)i.next();
      if ( srv.getServiceType().equals( newServiceType ) ) {
        throw new IOException( "Service type " + serviceType + " for MBeans " + beanName + " is already used by MBeans " + srv.getObjectName() + ", you must use an unique service type" );
      }
    }
    services.add( deviceService );
  }
  
  private void generateDeviceUUID() {
    try {
      MessageDigest md5 = MessageDigest.getInstance( "MD5" );
      // the uuid is based on the device type, the internal id
      // and the host name
      md5.update( deviceType.getBytes() );
      md5.update( internalId.getBytes() );
      md5.update( bindAddress.getHostName().getBytes() );
      StringBuffer hexString = new StringBuffer();
      byte[] digest = md5.digest();
      for (int i=0;i< digest.length; i++ ) {
        hexString.append( Integer.toHexString( 0xFF & digest[i] ) );
      }
      uuid = hexString.toString().toUpperCase();
    } catch ( Exception ex ) {
      RuntimeException runTimeEx = new RuntimeException( "Unexpected error during MD5 hash creation, check your JRE" );
      runTimeEx.initCause( ex );
      throw runTimeEx;
    }
  }
  
  public void start() throws Exception {
    if ( !started ) {
      if ( services.size() == 0 ) throw new Exception( "No UPNP service defined" );
      deviceInfo = getRootDeviceInfo( bindAddress, uuid );
      location = "http://" + bindAddress.getAddress().getHostAddress() + ":" + bindAddress.getPort() + "/" + uuid + "/desc.xml";
  
      UPNPMBeanDevicesRequestsHandler.getInstance( bindAddress ).addUPNPMBeanDevice( this );
      UPNPMBeanDevicesDiscoveryHandler.getInstance( bindAddress ).addUPNPMBeanDevice( this );
      started = true;
    }
  }
  
  private static InetSocketAddress getUPNPMBeansBoundAddr() {
    String boundAdr = System.getProperty( "net.sbbi.upnp.UPNPMBeanDevice.boundAddr" );
    InetSocketAddress defaultBoundAddr = null;
    try {
      InetAddress adr = null;
      if ( boundAdr != null ) {
        adr = InetAddress.getByName( boundAdr );
      } else {
        adr = InetAddress.getLocalHost();
      }
      defaultBoundAddr = new InetSocketAddress( adr, 8895 );
    } catch ( IOException ex ) {
      defaultBoundAddr = new InetSocketAddress( "localhost", 8895 );
    }
    return defaultBoundAddr;
  }
  
  public void stop() throws IOException {
    if ( started ) {
      UPNPMBeanDevicesRequestsHandler.getInstance( bindAddress ).removeUPNPMBeanDevice( this );
      UPNPMBeanDevicesDiscoveryHandler.getInstance( bindAddress ).removeUPNPMBeanDevice( this );
    }
  }

  private String getRootDeviceInfo( InetSocketAddress adr, String uuid ) {

    StringBuffer rtrVal = new StringBuffer();
    rtrVal.append( "<?xml version=\"1.0\" ?>\r\n" );
    rtrVal.append( "<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\r\n" );
    rtrVal.append( "<specVersion><major>1</major><minor>0</minor></specVersion>\r\n" );
    rtrVal.append( "<URLBase>http://" ).append( adr.getAddress().getHostAddress() ).append( ":" ).append( adr.getPort() ).append( "</URLBase>\r\n" );
    getDeviceInfo( this, rtrVal );
    rtrVal.append( "</root>" );
    return rtrVal.toString();

  }
  
  private void getDeviceInfo( UPNPMBeanDevice device, StringBuffer buffer ) {
    
    buffer.append( "<device>\r\n" );
    buffer.append( "<deviceType>" ).append( device.deviceType ).append( "</deviceType>\r\n" );
    buffer.append( "<friendlyName>" ).append( device.friendlyName ).append( "</friendlyName>\r\n" );
    buffer.append( "<manufacturer>" ).append( device.manufacturer ).append( "</manufacturer>\r\n" );
    if ( device.manufacturerURL != null ) {
      buffer.append( "<manufacturerURL>" ).append( device.manufacturerURL ).append( "</manufacturerURL>\r\n" );
    }
    if ( device.modelDescription != null ) {
      buffer.append( "<modelDescription>" ).append( device.modelDescription ).append( "</modelDescription>\r\n" );
    }
    buffer.append( "<modelName>" ).append( device.modelName ).append( "</modelName>\r\n" );
    if ( device.modelNumber != null ) {
      buffer.append( "<modelNumber>" ).append( device.modelNumber ).append( "</modelNumber>\r\n" );
    }
    if ( device.modelURL != null ) {
      buffer.append( "<modelURL>" ).append( device.modelURL ).append( "</modelURL>\r\n" );
    }
    if ( device.serialNumber != null ) {
      buffer.append( "<serialNumber>" ).append( device.serialNumber ).append( "</serialNumber>\r\n" );
    }
    buffer.append( "<UDN>uuid:" ).append( device.uuid ).append( "</UDN>\r\n" );
    if ( device.UPC != null ) {
      buffer.append( "<UPC>" ).append( device.serialNumber ).append( "</UPC>\r\n" );
    }
    buffer.append( "<serviceList>\r\n" );
    
    for ( Iterator i = device.services.iterator(); i.hasNext(); ) {
      UPNPMBeanService srv = (UPNPMBeanService)i.next();
      buffer.append( srv.getServiceInfo() );
    }
    buffer.append( "</serviceList>\r\n" );
    if ( device.childrens.size() > 0 ) {
      buffer.append( "<deviceList>\r\n" );
      for ( Iterator i = device.childrens.iterator(); i.hasNext(); ) {
        UPNPMBeanDevice dv = (UPNPMBeanDevice)i.next();
        getDeviceInfo( dv, buffer );
      }
      buffer.append( "</deviceList>\r\n" );
    }
    buffer.append( "</device>\r\n" );
  }

}
