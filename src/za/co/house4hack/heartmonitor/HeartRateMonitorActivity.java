package za.co.house4hack.heartmonitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;



import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HeartRateMonitorActivity extends Activity {
	private static final String TAG = "BlueTemp";
	private GraphicalView chart;
	private TimeSeries mSeries[];
	private XYMultipleSeriesDataset mDataset;
	private XYMultipleSeriesRenderer mRenderer;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothService mService = null;
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	private static final int TIMESERIESCOUNT = 1;
	int[] colors = new int[] { Color.BLUE };
	PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE };

	
	private static final double DEFAULTWINDOW = 10 * 60 * 1000;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	private TextView mTitle;
	private List<String> mReceived;
	private boolean mRunning;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		setContentView(R.layout.main);
		LinearLayout ll = (LinearLayout) findViewById(R.id.linearlayout);
		mSeries = new TimeSeries[TIMESERIESCOUNT];
		mDataset = new XYMultipleSeriesDataset();

		for (int i = 0; i < TIMESERIESCOUNT; i++) {
			mSeries[i] = new TimeSeries("T" + i);
			mDataset.addSeries(mSeries[i]);
		}

		chart = getChart();
		ll.addView(chart);

		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		mReceived = Collections.synchronizedList(new ArrayList<String>());

	}

	@Override
	public void onStart() {
		super.onStart();

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth chat services
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the BluetoothChatService to perform bluetooth connections
		mService = new BluetoothService(this, mHandlerBT, mReceived);

	}

	@Override
	public synchronized void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mService != null)
			mService.stop();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case REQUEST_CONNECT_DEVICE:

			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// Get the BLuetoothDevice object
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// Attempt to connect to the device
				mService.connect(device);
			}
			break;

		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode != Activity.RESULT_OK) {

				finishDialogNoBluetooth();
			}
		}
	}

	public void finishDialogNoBluetooth() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.alert_dialog_no_bt)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.app_name)
				.setCancelable(false)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								finish();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}


	private void updateData() {
		if (mService != null) {
			if (mService.getState() == BluetoothService.STATE_CONNECTED) {
				mService.write("*".getBytes());
			}
		}

	}

	public void connectDevice(final BluetoothDevice device) {
		if (mService != null) {
			mService.connect(device);
		} else {
			Log.d(TAG, "mService null, can't connect to " + device.getName());
		}
	}

	// The Handler that gets information back from the BluetoothService
	private final Handler mHandlerBT = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					if (mMenuItemConnect != null) {
						mMenuItemConnect
								.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
						mMenuItemConnect.setTitle(R.string.disconnect);
					}

					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					setRunning(true);
					break;

				case BluetoothService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;

				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					if (mMenuItemConnect != null) {
						mMenuItemConnect
								.setIcon(android.R.drawable.ic_menu_search);
						mMenuItemConnect.setTitle(R.string.connect);
					}

					mTitle.setText(R.string.title_not_connected);
					setRunning(false);

					break;
				}
				break;
			case MESSAGE_WRITE:

				break;

			case MESSAGE_READ:
				addData();
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};
	private MenuItem mMenuItemConnect;
	private MenuItem mMenuItemRun;
	private Timer mTimer;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		mMenuItemConnect = menu.getItem(0);
		mMenuItemRun = menu.getItem(1);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.connect_scan:
			if (mService.getState() == BluetoothService.STATE_NONE) {
				// Launch the DeviceListActivity to see devices and do scan
				Intent serverIntent = new Intent(this, DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			} else if (mService.getState() == BluetoothService.STATE_CONNECTED) {
				mService.stop();
				mService.start();
			}
			return true;
		case R.id.run:
			
				setRunning(!mRunning);
			
		}
		return false;
	}

	public GraphicalView getChart() {

		mRenderer = buildRenderer(colors, styles);
		int length = mRenderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) mRenderer.getSeriesRendererAt(i))
					.setFillPoints(true);
		}
		mRenderer.setXLabels(12);
		mRenderer.setYLabels(10);
		mRenderer.setShowGrid(true);
		mRenderer.setXLabelsAlign(Align.RIGHT);
		mRenderer.setYLabelsAlign(Align.RIGHT);
		// mRenderer.setZoomButtonsVisible(true);
		mRenderer.setPanEnabled(true, true);
		mRenderer.setZoomEnabled(true, true);

		GraphicalView result = ChartFactory.getTimeChartView(this, mDataset,
				mRenderer, "h:mm:ss");
		return result;
	}

	protected XYMultipleSeriesRenderer buildRenderer(int[] colors,
			PointStyle[] styles) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		setRenderer(renderer, colors, styles);
		return renderer;
	}

	protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors,
			PointStyle[] styles) {
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setMargins(new int[] { 20, 30, 15, 20 });
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			renderer.addSeriesRenderer(r);
		}
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer,
			String title, String xTitle, String yTitle, double xMin,
			double xMax, double yMin, double yMax, int axesColor,
			int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}

	/*
	 * protected XYMultipleSeriesDataset buildDateDataset(String[] titles,
	 * List<Date[]> xValues, List<double[]> yValues) { XYMultipleSeriesDataset
	 * dataset = new XYMultipleSeriesDataset(); int length = titles.length; for
	 * (int i = 0; i < length; i++) { TimeSeries series = new
	 * TimeSeries(titles[i]); Date[] xV = xValues.get(i); double[] yV =
	 * yValues.get(i); int seriesLength = xV.length; for (int k = 0; k <
	 * seriesLength; k++) { series.add(xV[k], yV[k]); }
	 * dataset.addSeries(series); } return dataset; }
	 * 
	 * protected XYMultipleSeriesDataset buildDataset(String[] titles,
	 * List<double[]> xValues, List<double[]> yValues) { XYMultipleSeriesDataset
	 * dataset = new XYMultipleSeriesDataset(); addXYSeries(dataset, titles,
	 * xValues, yValues, 0); return dataset; }
	 * 
	 * public void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles,
	 * List<double[]> xValues, List<double[]> yValues, int scale) { int length =
	 * titles.length; for (int i = 0; i < length; i++) { XYSeries series = new
	 * XYSeries(titles[i], scale); double[] xV = xValues.get(i); double[] yV =
	 * yValues.get(i); int seriesLength = xV.length; for (int k = 0; k <
	 * seriesLength; k++) { series.add(xV[k], yV[k]); }
	 * dataset.addSeries(series); } }
	 */
	public void addData() {
		for (TimeSeries s : mSeries)
			s.clear();
		synchronized (mReceived) {
			double max = 30;
			double min = 0;

			for (String readData : mReceived) {
				try {
					String[] parts = readData.trim().split(",");
					if (parts.length == TIMESERIESCOUNT + 1) {
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						Date date = sdf.parse(parts[0]);
						for (int i = 0; i < TIMESERIESCOUNT; i++) {
							double val = Double.parseDouble(parts[i + 1]);
							mSeries[i].add(date.getTime(), val);
							if (val > max)
								max = val;
							if (val < min)
								min = val;
						}
					}

					mRenderer.setXAxisMax(mSeries[0].getMaxX());
					if(mSeries[0].getMaxX()- mSeries[0].getMinX() > DEFAULTWINDOW){
						mRenderer
						.setXAxisMin(mSeries[0].getMaxX() - DEFAULTWINDOW);	
					} else {
						mRenderer
						.setXAxisMin(mSeries[0].getMinX());
						
					}
					
					mRenderer.setYAxisMax(max);
					mRenderer.setYAxisMin(min);
					chart.repaint();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
				}
			}
		}

	}

	public void startTimer() {
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				updateData();
			}

		}, 0, 5000);

	}
	
	public void stopTimer(){
		if(mTimer !=null) mTimer.cancel();
	}

	public boolean ismRunning() {
		return mRunning;
	}

	public void setRunning(boolean mRunning) {
		this.mRunning = mRunning;
		if(mMenuItemRun!=null){
			if(mRunning){
				mMenuItemRun.setTitle(getString(R.string.stop));
				//updateData();
				updateData();
				startTimer();
			} else{
				mMenuItemRun.setTitle(getString(R.string.run));
				stopTimer();
			}
		}
	}

}