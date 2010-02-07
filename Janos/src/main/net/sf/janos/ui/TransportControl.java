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
import java.util.Collection;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.janos.control.AVTransportListener;
import net.sf.janos.control.AVTransportService;
import net.sf.janos.control.ZonePlayer;
import net.sf.janos.model.PositionInfo;
import net.sf.janos.model.SeekTarget;
import net.sf.janos.model.SeekTargetFactory;
import net.sf.janos.model.TransportAction;
import net.sf.janos.model.TransportInfo.TransportState;
import net.sf.janos.model.xml.AVTransportEventHandler.AVTransportEventType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

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
	private final Timer transportTimer;
	private UpdateHandler updateHandler;
	private FFREWHandler ffrewHandler; 

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

		progressBar = new ProgressBar(this, SWT.PUSH | SWT.FLAT);
		progressBar.addMouseListener( new MouseListener() {

			public void mouseDoubleClick(MouseEvent arg0) {
			}

			public void mouseDown(MouseEvent arg0) {
				progressBar.setCapture(true);
			}

			public void mouseUp(MouseEvent arg0) {
				seekToPercent(getPercentageFromX(arg0.x));
				progressBar.setCapture(false);
			}
		});
		
		progressBar.addMouseMoveListener( new MouseMoveListener() {

			public void mouseMove(MouseEvent arg0) {
				String target = getSeekTargetFromPercentage(getPercentageFromX(arg0.x)).getTarget();
				int end = target.lastIndexOf(".");
				if (end == -1) { end = target.length(); }
				progressBar.setToolTipText(target.substring(0, end));
			}
			
		});
		FormData pbData = new FormData();
		pbData.left = new FormAttachment(0, 0);
		pbData.right = new FormAttachment(100,0);
		progressBar.setLayoutData(pbData);

		skipBackward = new Button(this, SWT.PUSH | SWT.FLAT);
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

		rewind = new Button(this, SWT.PUSH | SWT.FLAT);
		rewind.setImage(Images.REWIND.image());
		rewind.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				stopFFREW();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				startFFREW(-1000);
			}
		});
		FormData rewData = new FormData();
		rewData.left = new FormAttachment(skipBackward);
		rewData.top = new FormAttachment(progressBar);
		rewind.setLayoutData(rewData);

		play = new Button(this, SWT.PUSH | SWT.FLAT);
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

		fastForward = new Button(this, SWT.PUSH | SWT.FLAT);
		fastForward.setImage(Images.FAST_FORWARD.image());
		fastForward.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				stopFFREW();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				startFFREW(1000);
			}
		});
		FormData ffData = new FormData();
		ffData.left = new FormAttachment(play);
		ffData.top = new FormAttachment(progressBar);
		fastForward.setLayoutData(ffData);

		skipForward = new Button(this, SWT.PUSH | SWT.FLAT);
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
		setLayout(layout);

		updateEnabledness();

		transportTimer = new Timer("Timer Task:" + zone.getDevicePropertiesService().getZoneAttributes().getName(), true);
		zone.getMediaRendererDevice().getAvTransportService().addAvTransportListener(this);
		updateTimers();
	}

	public void updateEnabledness() {
		try {
			Collection<TransportAction> actions = zone.getMediaRendererDevice().getAvTransportService().getCurrentTransportActions();
			skipBackward.setEnabled(actions.contains(TransportAction.Previous));
			rewind.setEnabled(actions.contains(TransportAction.Seek));
			play.setEnabled(actions.contains(TransportAction.Play) || actions.contains(TransportAction.Pause));
			fastForward.setEnabled(actions.contains(TransportAction.Seek));
			skipForward.setEnabled(actions.contains(TransportAction.Next));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateTimers() {
		if (updateHandler != null) {
			updateHandler.cancel();
		}
		if (isPlaying()) {
			updateHandler = new UpdateHandler();
			transportTimer.scheduleAtFixedRate(updateHandler, 1000, 1000);
		}
	}

	@Override
	public void dispose() {
		zone.getMediaRendererDevice().getAvTransportService().removeAvTransportListener(this);
		if (updateHandler != null) {
			updateHandler.cancel();
		}
		transportTimer.cancel();		
		super.dispose();
	}

	public void valuesChanged(Set<AVTransportEventType> events, AVTransportService source) {
		if (source == zone.getMediaRendererDevice().getAvTransportService() 
				&& events.contains(AVTransportEventType.TransportState)) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					setIsPlaying(isPlaying());
					updateEnabledness();
				}
			});
		}

		updateTimers();
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

	private boolean seekIsEnabled() {
		boolean rv = false;
		Collection<TransportAction> actions;
		try {
			actions = zone.getMediaRendererDevice().getAvTransportService().getCurrentTransportActions();
			rv = actions.contains(TransportAction.Seek);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return rv;
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

	private int getPercentageFromX(int x) {
		int val = Math.round((float)x * (float)100.0 / (float)(progressBar.getBounds().width));
		if (val < progressBar.getMinimum()) { 
			val = progressBar.getMinimum();	
		}
		if (val > progressBar.getMaximum()) {
			val = progressBar.getMaximum();
		}
		return val;
	}
	
	private SeekTarget getSeekTargetFromPercentage(int percent) {
		try {
			PositionInfo posInfo = zone.getMediaRendererDevice().getAvTransportService().getPositionInfo();
			final long duration = posInfo.getTrackDuration();
			return SeekTargetFactory.createRelTimeSeekTarget((long)(((double)duration * (double)percent) / (double)100));
		} catch (Exception e) {
		}
		return SeekTargetFactory.createRelTimeSeekTarget(0);
	}
	
	public void seekToPercent(int percent) {
		if (!seekIsEnabled()) {
			return;
		}
		
		try {
			zone.getMediaRendererDevice().getAvTransportService().seek(getSeekTargetFromPercentage(percent));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void seekToIncrement(int increment) {
		if (!seekIsEnabled()) {
			return;
		}
		
		try {
			PositionInfo posInfo = zone.getMediaRendererDevice().getAvTransportService().getPositionInfo();
			SeekTarget target = SeekTargetFactory.createRelTimeSeekTarget(posInfo.getRelTime() + increment);
			zone.getMediaRendererDevice().getAvTransportService().seek(target);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static final PeriodFormatter periodFormatter;
	static {
		PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
		
		periodFormatter = builder.printZeroNever()
		.appendHours()
		.maximumParsedDigits(2)
		.appendSeparator(":")
		.printZeroAlways()
		.minimumPrintedDigits(1)
		.appendMinutes()
		.appendSeparator(":")
		.minimumPrintedDigits(2)
		.appendSecondsWithOptionalMillis().toFormatter();
	}

	public static String convertLongToDuration(long duration) {
		Period period = new Period(duration);
		return periodFormatter.print(period);
	}
	
	class UpdateHandler extends TimerTask {
		@Override
		public void run() {
			try {
				PositionInfo posInfo = zone.getMediaRendererDevice().getAvTransportService().getPositionInfo();

				final long currentTime = posInfo.getRelTime();
				final long duration = posInfo.getTrackDuration();
				final String label = convertLongToDuration(posInfo.getRelTime()) + "/" + convertLongToDuration(posInfo.getTrackDuration());

				if (!isDisposed()) {
					getDisplay().asyncExec(new Runnable() {
						public void run() {
						  if (!isDisposed()) {
						    progressText.setText(label);

						    progressBar.setMaximum((int)(duration/1000));
						    progressBar.setSelection((int)(currentTime/1000));
						  }
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void startFFREW(int increment) {
		if (ffrewHandler != null) {
			ffrewHandler.cancel();
		}
		if (updateHandler != null) {
			updateHandler.cancel();
		}
		ffrewHandler = new FFREWHandler();
		
		ffrewHandler.setIncrement(increment);
		transportTimer.scheduleAtFixedRate(ffrewHandler, 0, 250);
	}
	
	public void stopFFREW() {
		ffrewHandler.cancel();
		updateTimers();
	}	
	
	class FFREWHandler extends TimerTask {
		private int increment;
		
		FFREWHandler() {
			increment = 0;
		}
		
		@Override
		public void run() {
			try {
				System.out.println("Updating Position");
				seekToIncrement(getIncrement());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public int getIncrement() {
			return increment;
		}

		public void setIncrement(int increment) {
			this.increment = increment;
		}
	}
}
