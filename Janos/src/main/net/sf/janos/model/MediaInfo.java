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
package net.sf.janos.model;


/**
 * A class representing information about media.
 * 
 * @author David Wheeler
 * 
 */
public class MediaInfo {

  private final int numTracks;
  private final String mediaDuration;
  private final String currentURI;
  private final String currentURIMetaData;
  private final String nextURI;
  private final String nextURIMetaData;
  private final String playMedium;
  private final String recordMedium;
  private final String writeStatus;

  /**
   * This only seems to be useful for num tracks and current URI - even
   * currentURIMetadata seems to return garbage.
   */
  public MediaInfo(String numTracks, String mediaDuration, 
      String currentURI, String currentURIMetaData, 
      String nextURI, String nextURIMetaData, 
      String playMedium, String recordMedium, 
      String writeStatus) {
    this.numTracks = Integer.parseInt(numTracks);
    this.mediaDuration = mediaDuration;
    /* TODO Duration of the current track, specified as a string of the following form:  
     * H+:MM:SS[.F+] or H+:MM:SS[.F0/F1]  
     *                         where : 
     *                         ¥ H+ means one or more digits to indicate elapsed hours  
     *                         ¥ MM means exactly 2 digits to indicate minutes (00 to 59) 
     *                         ¥ SS means exactly 2 digits to indicate seconds (00 to 59) 
     *                         ¥ [.F+] means optionally a dot followed by one or more digits to indicate fractions of seconds 
     *                         ¥ [.F0/F1] means optionally a dot followed by a fraction, with F0 and F1 at least one digit long, and F0 < 
     *                         F1 
     *                         The string may be preceded by an optional + or Ð  sign, and  the decimal point itself may be omitted if 
     *                         there are no fractional second digits. This variable does not apply to Tuners. If the service implementation 
     *                         doesnÕt support track duration information then this state variable must be set to value 
     *                         ÒNOT_IMPLEMENTEDÓ. 
     */
    this.currentURI = currentURI;
    this.nextURI = nextURI;
    this.currentURIMetaData = currentURIMetaData;
    this.nextURIMetaData = nextURIMetaData;
    this.playMedium = playMedium;
    this.recordMedium = recordMedium;
    this.writeStatus = writeStatus;
  }

  public String getCurrentURI() {
    return currentURI;
  }

  public String getCurrentURIMetaData() {
    return currentURIMetaData;
  }

  public String getMediaDuration() {
    return mediaDuration;
  }

  public String getNextURI() {
    return nextURI;
  }

  public String getNextURIMetaData() {
    return nextURIMetaData;
  }

  public int getNumTracks() {
    return numTracks;
  }

  public String getPlayMedium() {
    return playMedium;
  }

  public String getRecordMedium() {
    return recordMedium;
  }

  public String getWriteStatus() {
    return writeStatus;
  }

}
