/*
 * Copyright 2007 David Wheeler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.janos.model;

/**
 * An immutable data transfer object containing attributes to the audio input
 * service.
 * 
 * @author David Wheeler
 * 
 */
public class AudioInputAttributes {
  
  private String name;
  private String iconUri;

  public AudioInputAttributes(String name, String iconUri) {
    this.name=name;
    this.iconUri = iconUri;
  }

  /**
   * 
   * @return a string representation of the URI of the icon.
   */
  public String getIconUri() {
    return iconUri;
  }

  /**
   * 
   * @return the name of the audio input.
   */
  public String getName() {
    return name;
  }

}
