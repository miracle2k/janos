/*
 * Copyright 2007 David Wheeler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.janos.model;

/**
 * An immutable data transfer object containing detailed information about the zone.
 * @author David Wheeler
 *
 */
public class ZoneInfo {

  private String serialNumber;
  private String softwareVersion;
  private String displaySoftwareVersion;
  private String hardwareVersion;
  private String ipAddress;
  private String macAddres;
  private String copyrightInfo;
  private String extraInfo;

  /**
   * Creates a new ZoneInfo object.
   * @param serialNumber
   * @param softwareVersion
   * @param displaySoftwareVersion
   * @param hardwareVersion
   * @param ipAddress
   * @param macAddress
   * @param copyrightInfo
   * @param extraInfo
   */
  public ZoneInfo(String serialNumber, 
      String softwareVersion, 
      String displaySoftwareVersion, 
      String hardwareVersion, 
      String ipAddress, 
      String macAddress, 
      String copyrightInfo, 
      String extraInfo) {
    this.serialNumber = serialNumber;
    this.softwareVersion = softwareVersion;
    this.displaySoftwareVersion = displaySoftwareVersion;
    this.hardwareVersion = hardwareVersion;
    this.ipAddress = ipAddress;
    this.macAddres = macAddress;
    this.copyrightInfo = copyrightInfo;
    this.extraInfo = extraInfo;
  }

  public String getCopyrightInfo() {
    return copyrightInfo;
  }

  public String getDisplaySoftwareVersion() {
    return displaySoftwareVersion;
  }

  public String getExtraInfo() {
    return extraInfo;
  }

  public String getHardwareVersion() {
    return hardwareVersion;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getMacAddres() {
    return macAddres;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public String getSoftwareVersion() {
    return softwareVersion;
  }
}
