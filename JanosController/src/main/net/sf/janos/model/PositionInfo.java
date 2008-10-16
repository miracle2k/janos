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
 * An immutable data transfer object containing information regarding the
 * position of a zone player.
 * 
 * @author David Wheeler
 * 
 */
public class PositionInfo {

  private final int trackNum;
  private final long trackDuration;
  private final TrackMetaData trackMetaData;
  private final String trackURI;
  private final long relTime;
  private final long absTime;
  private final int relCount;
  private final int absCount;

  public PositionInfo(int trackNum, long trackDuration, TrackMetaData trackMetaData, 
      String trackURI, long relTime, long absTime, int relCount, int absCount) {
    this.trackNum = trackNum;
    this.trackDuration = trackDuration;
    this.trackMetaData = trackMetaData;
    this.trackURI = trackURI;
    this.relTime = relTime;
    this.absTime = absTime;
    this.relCount = relCount;
    this.absCount = absCount;
  }

  public int getAbsCount() {
    return absCount;
  }

  public int getRelCount() {
    return relCount;
  }

  public TrackMetaData getTrackMetaData() {
    return trackMetaData;
  }

  public int getTrackNum() {
    return trackNum;
  }

  public String getTrackURI() {
    return trackURI;
  }

  public long getAbsTime() {
    return absTime;
  }

  public long getRelTime() {
    return relTime;
  }

  public long getTrackDuration() {
    return trackDuration;
  }

}
