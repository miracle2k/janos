/*
   Copyright 2009 david

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

import net.sf.janos.control.SonosController;
import net.sf.janos.ui.SonosControllerShell;

/**
 * A merge of the Singleton and Encapsulating Context patterns.
 * @author David Wheeler
 *
 */
public class ApplicationContext {
  
  /**
   * The singleton instance
   */
  private static ApplicationContext INSTANCE;
  
  /**
   * Creates a new ApplicationContext with the given parameters
   * @param controller
   * @param shell
   * @param toolTipHandler
   */
  protected static void create(SonosController controller, SonosControllerShell shell) {
    INSTANCE = new ApplicationContext(controller, shell);
  }
  
  protected static void destroy() {
    INSTANCE = null;
  }

  private SonosController controller;
  private SonosControllerShell shell;
  
  /**
   * Get the ApplicationContext
   * @return the singleton instance
   */
  public static ApplicationContext getInstance() {
    if (INSTANCE == null) {
      throw new RuntimeException("Application has been destroyed, no ApplicationContext is available");
    }
    return INSTANCE;
  }
  
  /**
   * The constructor
   * @param controller
   * @param shell
   * @param toolTipHandler
   */
  private ApplicationContext(SonosController controller, SonosControllerShell shell) {
    this.controller = controller;
    this.shell = shell;
  }

  /**
   * @return the SonosController instance
   */
  public SonosController getController() {
    return controller;
  }

  /**
   * @return the SonosControllerShell instance
   */
  public SonosControllerShell getShell() {
    return shell;
  }

}
