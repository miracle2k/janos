/*
 * Created on 13/11/2007
 * By David Wheeler
 * Student ID: 3691615
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
