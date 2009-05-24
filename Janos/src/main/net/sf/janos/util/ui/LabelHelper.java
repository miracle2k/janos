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
package net.sf.janos.util.ui;

public final class LabelHelper {

  private LabelHelper() {
    // don't instantiate me
  }
  
  /**
   * Escapes text for display in a label
   * @param text
   * @return the escaped text
   */
  public static String escapeText(String text) {
    return text.replaceAll("&", "&&");
  }
}
