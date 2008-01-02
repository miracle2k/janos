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
package net.sf.janos.util.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

public class ImageUtilities {
  
  /**
   * Scales the given image to the given size.
   * 
   * NOTE that the original image is <strong>NOT</strong> disposed by this
   * method.
   * 
   * @param image
   * @param width
   * @param height
   * @return a new Image of the given dimensions.
   */
  public static Image scaleImageTo(Image image, int width, int height) {
    Image outImage = new Image(image.getDevice(), width, height);
    GC gc = new GC(outImage);
    gc.setAntialias(SWT.ON);
    gc.drawImage(image, image.getBounds().x, image.getBounds().y, 
        image.getBounds().width, image.getBounds().height, 0, 0, width, height);
    gc.dispose();
    return outImage;
  }

}
