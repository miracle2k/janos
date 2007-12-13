/*
 * Created on 01/11/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.model;

/**
 * Information regarding the state, status and speed of a transport.
 * @author David Wheeler
 *
 */
public class TransportInfo {
  
  public enum TransportState {
    STOPPED, 
    PLAYING, 
    PAUSED_PLAYBACK, 
    TRANSITIONING
  }
  
  private final TransportState state;
  private final String status;
  private final int speed;

  public TransportInfo(String state, String status, String speed) {
    this.state = TransportState.valueOf(state);
    this.status = status;
    this.speed = Integer.parseInt(speed);
  }

  public int getSpeed() {
    return speed;
  }

  public TransportState getState() {
    return state;
  }

  public String getStatus() {
    return status;
  }
  
  
}
