/*
 * Copyright 2008 David Wheeler
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

public interface MusicLibraryModel {

  /**
   * Disposes the MusicLibrary model. 
   */
  public void dispose();

  /**
   * @return the total number of entries in the model, loaded or otherwise
   */
  public int getSize();

  /**
   * @param index
   * @return the entry at the given index
   */
  public Entry getEntryAt(int index);

  /**
   * Adds the given listener to be notified of new entries, or size changes
   * @param listener
   */
  public void addListener(MusicLibraryListener listener);

  /**
   * Removes the listener
   * @param listener
   */
  public void removeListener(MusicLibraryListener listener);

  /**
   * Removes all listeners
   *
   */
  public void removeListeners();

  /**
   * @param index
   * @return <code>true</code> if the entry corresponding to index has been loaded
   */
  public boolean hasEntryFor(int index);

  /**
   * @param entry
   * @return the index of the given entry, if it exists (-1 otherwise)
   */
  public int indexOf(Entry entry);

}