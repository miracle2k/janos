/*
 * Created on 22/07/2007
 * By David Wheeler
 * Student ID: 3691615
 */
package net.sf.janos;

import net.sf.janos.control.SonosController;
import net.sf.janos.ui.SonosControllerShell;

import org.eclipse.swt.widgets.Display;

public class Janos {

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.setProperty("net.sbbi.upnp.Discovery.bindPort", "2000");
    
    SonosController controller = SonosController.getInstance();
    SonosControllerShell shell = new SonosControllerShell(new Display(), controller);
    shell.start();
    
    // shell has been disposed, clean up
//    controller.dispose();
    
  }

}
