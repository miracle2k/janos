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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

		GridLayout layout = new GridLayout();
		layout.numColumns = 6;
		setLayout(layout);

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
		GridData barGridData = new GridData(GridData.FILL_HORIZONTAL);
		barGridData.horizontalSpan = 6;
		barGridData.grabExcessHorizontalSpace = true;
		progressBar.setLayoutData(barGridData);

		skipBackward = new Button(this, SWT.PUSH);
		skipBackward.setImage(Images.SKIP_BACKWARD.image());
		skipBackward.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				previous();
			}
		});

		rewind = new Button(this, SWT.PUSH);
		rewind.setImage(Images.REWIND.image());
		rewind.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				// previous();
			}
		});

		play = new Button(this, SWT.PUSH);
		play.setImage(Images.PLAY.image());
		play.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				play();
			}
		});
		setIsPlaying(isPlaying());

		fastForward = new Button(this, SWT.PUSH);
		fastForward.setImage(Images.FAST_FORWARD.image());
		fastForward.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				// previous();
			}
		});
		skipForward = new Button(this, SWT.PUSH);
		skipForward.setImage(Images.SKIP_FORWARD.image());
		skipForward.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				next();
			}
		});

		progressText = new Label(this, SWT.NONE);
		progressText.setText("0:00/0:00");

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
