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
 * An immutable data transfer object representing information about media.
 * 
 * @author David Wheeler
 * 
 */
public class MediaInfo {

  private final int numTracks;
  private final long mediaDuration;
  private final String currentURI;
  private final TrackMetaData currentURIMetaData;
  private final String nextURI;
  private final String nextURIMetaData;
  private final String playMedium;
  private final String recordMedium;
  private final String writeStatus;

  /**
   * This only seems to be useful for num tracks and current URI - even
   * currentURIMetadata seems to return garbage.
   */
  public MediaInfo(String numTracks, long mediaDuration, 
      String currentURI, TrackMetaData currentURIMetaData, 
      String nextURI, String nextURIMetaData, 
      String playMedium, String recordMedium, 
      String writeStatus) {
    this.numTracks = Integer.parseInt(numTracks);
    this.mediaDuration = mediaDuration;
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

  public TrackMetaData getCurrentURIMetaData() {
    return currentURIMetaData;
  }

  /**
   * @return the duration of the media, or -1 if this is not implemented.
   */
  public long getMediaDuration() {
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
