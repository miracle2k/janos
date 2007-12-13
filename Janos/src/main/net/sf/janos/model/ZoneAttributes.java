/*
 * Created on 21/10/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos.model;


import net.sf.janos.Debug;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

/**
 * Contains the name and icon (URI) for a zone player.
 * 
 * TODO: this class could use a real getIcon() that loads from a file depending
 * on the icon URI?
 * 
 * @author David Wheeler
 * 
 */
public class ZoneAttributes {
  private final String name;

  private final String icon;

  public ZoneAttributes(String name, String icon) {
    this.name = name;
    this.icon = icon;
    Debug.debug("Zone name: " + name + ", zone icon: " + icon);
  }

  public String getName() {
    return name;
  }

  public String getIcon() {
    return icon;
  }

  public Image loadIcon(Device dev) {
    return new Image(dev, icon);
  }
}
