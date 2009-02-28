/*
   Copyright 2008 davidwheeler

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
package net.sf.janos.ui.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.sf.janos.model.Entry;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

/**
 * A Transfer for an array of Entrys
 * @author David Wheeler
 *
 */
public class EntryTransfer extends ByteArrayTransfer {
  private static final String MIME_TYPE = "janos_library_entry";

  private static final int TYPE_ID = registerType(MIME_TYPE);
  
  private static EntryTransfer _instance = new EntryTransfer();

  public static EntryTransfer getInstance() {
    return _instance;
  }

  @Override
  protected int[] getTypeIds() {
    return new int[] {TYPE_ID};
  }

  @Override
  protected String[] getTypeNames() {
    return new String[] {MIME_TYPE};
  }
  
  @Override
  protected void javaToNative(Object object, TransferData transferData) {
    if (!checkMyType(object) || !isSupportedType(transferData)) {
      DND.error(DND.ERROR_INVALID_DATA);
    }

    Entry[] entries = (Entry[]) object;
    try {
      // write data to a byte array and then ask super to convert to pMedium
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ObjectOutputStream writeOut = new ObjectOutputStream(out);
      writeOut.writeObject(entries);
      byte[] buffer = out.toByteArray();
      writeOut.close();
      super.javaToNative(buffer, transferData);
    } catch (IOException e) {
    }
  }
  
  @Override
  protected Object nativeToJava(TransferData transferData) {
    if (isSupportedType(transferData)) {
      byte[] buffer = (byte[]) super.nativeToJava(transferData);
      if (buffer == null)
        return null;

      Entry[] myData = new Entry[0];
      try {
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        ObjectInputStream readIn = new ObjectInputStream(in);
        myData = (Entry[]) readIn.readObject();
        readIn.close();
      } catch (IOException ex) {
        return null;
      } catch (ClassNotFoundException e) {
        return null;
      }
      return myData;
    }

    return null;
  }
  
  private boolean checkMyType(Object object) {
    if (object == null || !(object instanceof Entry[]) || ((Entry[]) object).length == 0) {
      return false;
    }
    return true;
  }

}
