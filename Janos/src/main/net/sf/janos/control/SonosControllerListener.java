/*
 * Created on 04/08/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.control;

/**
 * Signifies that this object should be notified of changes to the zones.
 * 
 * @author David Wheeler
 *
 */
public interface SonosControllerListener {

  /**
   * Notification that the given device has been added.
   * @param dev
   */
  public void deviceAdded(ZonePlayer dev);
  
  /**
   * Notification that the given device has been removed.
   * @param dev
   */
  public void deviceRemoved(ZonePlayer dev);
}
