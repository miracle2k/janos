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
 * Information regarding the position of a zone player.
 * @author David Wheeler
 *
 */
public class PositionInfo {

  private final int trackNum;
  private final String trackMetaData;
  private final String trackURI;
  private final int relCount;
  private final int absCount;

  public PositionInfo(String trackNum, String trackDuration, String trackMetaData, 
      String trackURI, String relTime, String absTime, String relCount, String absCount) {
    this.trackNum = Integer.parseInt(trackNum);
    // TODO need a time converter
//    this.trackDuration = convertToDuration(trackDuration);
    this.trackMetaData = trackMetaData;
    this.trackURI = trackURI;
//    this.relTime = convertToDuration(relTime);
//    this.absTime = convertToDuration(absTime);
    this.relCount = Integer.parseInt(relCount);
    this.absCount = Integer.parseInt(absCount);
  }

  public int getAbsCount() {
    return absCount;
  }

  public int getRelCount() {
    return relCount;
  }

  public String getTrackMetaData() {
    return trackMetaData;
  }

  public int getTrackNum() {
    return trackNum;
  }

  public String getTrackURI() {
    return trackURI;
  }

}
