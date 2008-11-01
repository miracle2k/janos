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
package net.sf.janos.ui;

import java.io.InputStream;
import java.util.Set;

import net.sf.janos.control.AVTransportListener;
import net.sf.janos.control.AVTransportService;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.TransportInfo.TransportState;
import net.sf.janos.model.xml.AVTransportEventHandler.AVTransportEventType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * a UI component with actions for controlling the music eg. stop
 * 
 * @author David Wheeler
 * 
 */
public class TransportControl extends Composite implements AVTransportListener {

	private final ZonePlayer zone;
	private final ProgressBar progressBar;
	private final Button play;
	private final Button fastForward;
	private final Button rewind;
	private final Button skipForward;
	private final Button skipBackward;
	private final Label progressText;

	private enum Images { 
		PLAY			("/play-16x16.png"),
		PAUSE			("/pause-16x16.png"),
		REWIND			("/rewind-16x16.png"),
		FAST_FORWARD	("/fast-forward-16x16.png"),
		SKIP_BACKWARD	("/skip-backward-16x16.png"),
		SKIP_FORWARD	("/skip-forward-16x16.png");

		private String filename;
		private Image image;

		public String filename() {return filename;};
		public Image image() {return image;};
		public void setImage(Image i) {image=i;};

		Images (String filename) {
			this.filename = filename;
		}

	};

	public TransportControl(Composite parent, int style, ZonePlayer zone) {
		super(parent, style);
		this.zone = zone;

		for (Images i : Images.values()) {
			try {
				InputStream is;
				is = getClass().getResourceAsStream(i.filename());
				i.setImage(new Image(getDisplay(), is));
				is.close();
			} catch (Exception e) {
			}
		}

		progressBar = new ProgressBar(this, SWT.NONE);
		FormData pbData = new FormData();
		pbData.left = new FormAttachment(0, 0);
		pbData.right = new FormAttachment(100,0);
		progressBar.setLayoutData(pbData);

		skipBackward = new Button(this, SWT.PUSH);
		skipBackward.setImage(Images.SKIP_BACKWARD.image());
		skipBackward.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				previous();
			}
		});
		FormData sbData = new FormData();
		sbData.left = new FormAttachment(0, 0);
		sbData.top = new FormAttachment(progressBar);
		skipBackward.setLayoutData(sbData);

		rewind = new Button(this, SWT.PUSH);
		rewind.setImage(Images.REWIND.image());
		rewind.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				// previous();
			}
		});
		FormData rewData = new FormData();
		rewData.left = new FormAttachment(skipBackward);
		rewData.top = new FormAttachment(progressBar);
		rewind.setLayoutData(rewData);
		

		play = new Button(this, SWT.PUSH);
		play.setImage(Images.PLAY.image());
		play.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				play();
			}
		});
		setIsPlaying(isPlaying());
		FormData playData = new FormData();
		playData.left = new FormAttachment(rewind);
		playData.top = new FormAttachment(progressBar);
		play.setLayoutData(playData);
		

		fastForward = new Button(this, SWT.PUSH);
		fastForward.setImage(Images.FAST_FORWARD.image());
		fastForward.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				// previous();
			}
		});
		FormData ffData = new FormData();
		ffData.left = new FormAttachment(play);
		ffData.top = new FormAttachment(progressBar);
		fastForward.setLayoutData(ffData);
		
		skipForward = new Button(this, SWT.PUSH);
		skipForward.setImage(Images.SKIP_FORWARD.image());
		skipForward.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				next();
			}
		});
		FormData sfData = new FormData();
		sfData.left = new FormAttachment(fastForward);
		sfData.top = new FormAttachment(progressBar);
		skipForward.setLayoutData(sfData);
		
		progressText = new Label(this, SWT.RIGHT);
		progressText.setText("0:00/0:00");
		FormData progressTextData = new FormData();
		progressTextData.left = new FormAttachment(skipForward);
		progressTextData.right = new FormAttachment(100,0);
		progressTextData.top = new FormAttachment(progressBar);
		progressText.setLayoutData(progressTextData);
		
		FormLayout layout = new FormLayout();
		//layout.spacing = 0;
		setLayout(layout);
		
		updateEnabledness();
		
		zone.getMediaRendererDevice().getAvTransportService().addAvTransportListener(this);
	}

	public void updateEnabledness() {
		// TODO: update enabledness based on the service type.  For example,
		// Rhapsody and Pandora stations do not allow rewind or skip back while
		// playback from the queue should allow these functions.
		skipBackward.setEnabled(true);
		rewind.setEnabled(false);
		play.setEnabled(true);
		fastForward.setEnabled(false);
		skipForward.setEnabled(true);
	}

	@Override
	public void dispose() {
		zone.getMediaRendererDevice().getAvTransportService().removeAvTransportListener(this);
		super.dispose();
	}

	public void valuesChanged(Set<AVTransportEventType> events, AVTransportService source) {
		if (source == zone.getMediaRendererDevice().getAvTransportService() 
				&& events.contains(AVTransportEventType.TransportState)
				&& !isDisposed() ) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					setIsPlaying(isPlaying());
					updateEnabledness();
				}
			});
		}
	}


	private boolean isPlaying() {
		try {
			return zone.getMediaRendererDevice().getAvTransportService().getTransportInfo().getState().equals(TransportState.PLAYING);
		} catch (Exception e) {
		}
		return false;
	}

	private void setIsPlaying(boolean isPlaying) {
		play.setImage(isPlaying ? Images.PAUSE.image() : Images.PLAY.image());
		play.setData(isPlaying ? "Pause" : "Play" );
	}

	protected void previous() {
		try {
			// TODO: previous() should behave such that a request to skip backwards
			// late in a song should skip back to the beginning of the current song.
			zone.getMediaRendererDevice().getAvTransportService().previous();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void play() {
		final String action = (String) play.getData();

		try {
			if (action.equals("Pause")) {
				zone.getMediaRendererDevice().getAvTransportService().pause();
			} else if (action.equals("Play")) {
				zone.getMediaRendererDevice().getAvTransportService().play();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		play.setEnabled(false);
	}

	protected void next() {
		try {
			zone.getMediaRendererDevice().getAvTransportService().next();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
