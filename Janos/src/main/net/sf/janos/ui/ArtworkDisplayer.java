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
package net.sf.janos.ui;

import java.net.URL;

import net.sf.janos.ApplicationContext;
import net.sf.janos.util.ui.ImageUtilities;
import net.sf.janos.util.ui.ImageUtilities.Callback;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ArtworkDisplayer extends MouseAdapter implements MouseTrackListener, Callback {

  private Shell shell;
  private Label target;
  private Label label;

  public ArtworkDisplayer(Label target) {
    shell = new Shell(ApplicationContext.getInstance().getShell().getShell(), SWT.TITLE);
    this.target = target;
    this.label = new Label(shell, SWT.NONE);
    MouseListener clickListener = new MouseAdapter() {
      @Override
      public void mouseUp(MouseEvent e) {
        shell.setVisible(false);
      }
    };
    shell.addMouseListener(clickListener);
    label.addMouseListener(clickListener);
    shell.setLayout(new FillLayout());
    addTargetLabel(target);
  }

  private void addTargetLabel(Label target) {
    target.addMouseListener(this);
    target.addMouseTrackListener(this);
  }

  @Override
  public void mouseUp(MouseEvent e) {
    if (shell.isVisible()) {
      shell.setVisible(false);
    } else {
      mouseHover(e);
    }
  }

  public void mouseEnter(MouseEvent e) {
  }

  public void mouseExit(MouseEvent e) {
  }

  public void mouseHover(MouseEvent e) {
    Object data = target.getData();
    if (data != null && data != label.getData() && data instanceof URL) {
      URL url = (URL) data;
      Image oldImage = label.getImage();
      label.setImage(null);
      if (oldImage != null && !oldImage.isDisposed()) {
        oldImage.dispose();
      }
      ImageUtilities.loadImageAsync(url, this);
      shell.setVisible(true);
    }
  }

  public void imageLoaded(final ImageData data) {
    shell.getDisplay().asyncExec(new Runnable() {
      public void run() {
        if (shell.isVisible()) {
          Image oldImage = label.getImage();
          label.setImage(new Image(label.getDisplay(), data));
          if (oldImage != null && !oldImage.isDisposed()) {
            oldImage.dispose();
          }
          shell.pack();
          shell.redraw();
        }
      }
    });
  }

}
