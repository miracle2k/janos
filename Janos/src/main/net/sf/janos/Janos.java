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
package net.sf.janos;

import net.sbbi.upnp.ServicesEventing;
import net.sf.janos.control.SonosController;
import net.sf.janos.ui.SonosControllerShell;

import org.eclipse.swt.widgets.Display;

public class Janos {

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.setProperty("net.sbbi.upnp.Discovery.bindPort", "2000");
    ServicesEventing.getInstance().setDaemonPort(6832);
    
    SonosController controller = SonosController.getInstance();
    SonosControllerShell shell = new SonosControllerShell(new Display(), controller);
    shell.start();
  }

}
