package net.sf.janos.ui;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/*
 * An abstract base class which provides layout for a volume control.
 * It is intended to be used as a parent class for a zone specific 
 * volume control which represents and controls a single Zone.  In 
 * addition, it is intended to be a parent class for a group volume control
 * which looks similar to a zone-specific volume control, but a) implements
 * it's methods over all the zones in a group (rather than a specific zone)
 * and b) adds a control to show the sub zones of a group.
 */
public class VolumeControl extends Composite {

	private final Image muted;
	private final Image notMuted;

	private final Label title;
	private final Button mute;
	private final ProgressBar volume;

	public VolumeControl(Composite parent, int style, String titleText) {
		super(parent, style);

		InputStream is;
		is = getClass().getResourceAsStream("/sound-16x16.png");
		notMuted = new Image(getDisplay(), is);
		try {
			is.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		is = getClass().getResourceAsStream("/sound-off-16x16.png");
		muted = new Image(getDisplay(), is);
		try {
			is.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		title = new Label(this, SWT.WRAP | SWT.RIGHT);
		title.setText(titleText);

		mute = new Button(this, SWT.TOGGLE);
		mute.setImage(notMuted);
		mute.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setMute(!getMute());
			}
		});

		volume = new ProgressBar(this, SWT.HORIZONTAL | SWT.SMOOTH );
		volume.setMinimum(0);
		volume.setMaximum(100);
		volume.setSelection(getVolume());

		volume.addMouseListener( new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				volume.setCapture(true);
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				int val = Math.round((float)arg0.x * (float)100.0 / (float)(volume.getBounds().width));
				if (val < volume.getMinimum()) { 
					val = volume.getMinimum();	
				}
				if (val > volume.getMaximum()) {
					val = volume.getMaximum();
				}
				setVolume(val);
				volume.setCapture(false);
			}
		});

		FormData data1 = new FormData();
		data1.left = new FormAttachment(0,0);
		data1.top = new FormAttachment(0, 0);
		data1.bottom = new FormAttachment(100, 0);
		data1.width = 50;
		title.setLayoutData(data1);

		FormData data2 = new FormData();
		data2.left = new FormAttachment(title);
		data2.top = new FormAttachment(0, 0);
		data2.bottom = new FormAttachment(100, 0);
		mute.setLayoutData(data2);

		FormData data3 = new FormData();
		data3.left = new FormAttachment(mute);
		data3.right = new FormAttachment(100,0);
		data3.top = new FormAttachment(0, 0);
		data3.bottom = new FormAttachment(100, 0);
		volume.setLayoutData(data3);

		FormLayout layout = new FormLayout();
		layout.spacing = 10;
		setLayout(layout);
	}

	

	public boolean getMute() {
		return Boolean.parseBoolean((String)mute.getData());
	}

	protected void setMute(boolean mute) {
		forceMute(mute);
	}

	public void forceMute(boolean mute) {
		System.out.println("FORCING MUTE TO " + mute);
		this.mute.setImage(mute?muted:notMuted);
		this.mute.setData(Boolean.toString(mute));
	}

	public int getVolume() {
		return volume.getSelection();
	}

	protected void setVolume(int volume) {
		forceVolume(volume);
	}
	
	public void forceVolume(int volume) {
		this.volume.setSelection(volume);
	}

	public static void main (String [] args) {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.open ();

		new VolumeControl(shell, SWT.NONE, "Group Volume");

		shell.setLayout (new FillLayout ());
		shell.pack ();

		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}

	@Override
	public void dispose() {
		muted.dispose();
		notMuted.dispose();
		super.dispose();
	}
}


