package com.wireless.locationtracking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import android.util.Log;
import java.text.NumberFormat;
import java.util.List;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.*;


public class MainActivity extends Activity implements SensorEventListener {

	TextView 	tvAccel[] = new TextView[3],
				tvVel[] = new TextView[3],
				tvPos[] = new TextView[3],
				tvOri[] = new TextView[3];
	TextView 	tvAccelX , tvAccelY, tvAccelZ,
				tvVelX, tvVelY, tvVelZ,
				tvPosX, tvPosY, tvPosZ;
	
	NumberFormat df = DecimalFormat.getInstance();
	
	WifiManager wifiManager = null;
	
	SensorManager manager = null;
	Sensor accelerometer = null;
	Sensor gravitySensor = null;
	Sensor magneticSensor = null;
	long lastTimestamp = -1;
	double 	acc[] = new double[3],
			vel[] = new double[3],
			pos[] = new double[3],
			ori[] = new double[3];
	float	lastGravity[] = null,
			lastMagnetic[] = null,
			rotation;
	double timeConstant = 0.297; //the constant used in the low pass filter
	double alpha = 0.0; //another value used in the low pass filter
	
	double offsets[], stdDevs[]; //stores the sensor calibration data
	
	SensorCalibration sC = null;
	
	Button dbFlushButton;
	Button dbExportButton;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//how to format decimal numbers
		df.setMinimumFractionDigits(1);
		df.setMaximumFractionDigits(2);
		
		/*
		 * Get the interfaces to the GUI
		 */
		tvAccel[0] = (TextView)findViewById(R.id.accelX);
		tvAccel[1] =  (TextView)findViewById(R.id.accelY);
		tvAccel[2] =  (TextView)findViewById(R.id.accelZ);

		tvVel[0] =  (TextView)findViewById(R.id.velX);
		tvVel[1] =  (TextView)findViewById(R.id.velY);
		tvVel[2] =  (TextView)findViewById(R.id.velZ);

		tvPos[0] =  (TextView)findViewById(R.id.posX);
		tvPos[1] =  (TextView)findViewById(R.id.posY);
		tvPos[2] =  (TextView)findViewById(R.id.posZ);
		
		tvOri[0] = (TextView)findViewById(R.id.azimuth);
		tvOri[1] = (TextView)findViewById(R.id.pitch);
		tvOri[2] = (TextView)findViewById(R.id.roll);
		
		/*
		 * Register the Sensor listeners
		 */
		manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		gravitySensor = manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		magneticSensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		//not actually caling the Sensor Calibratin anymore because it wasn't doing much
		sC = new SensorCalibration(this); 
		lastTimestamp = System.nanoTime();
		manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		manager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		manager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
		
		/* DB stuff takes to long to run, removing it for now -Nick
		dbFlushButton = (Button) findViewById(R.id.button1);
        dbFlushButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("LocalService", "Flush Button Clicked");
				DBManager dbMan =  DBManager.getInstance(v.getContext());
				SQLiteDatabase db = dbMan.getWritableDatabase(); 
				dbMan.flushTables(db);
				
			}
		});
		
        dbExportButton = (Button) findViewById(R.id.button2);
        dbExportButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("LocalService", "Export Button Clicked");
				try {
			        File sd = Environment.getExternalStorageDirectory();
			        File data = Environment.getDataDirectory();

			        if (sd.canWrite()) {
			            
			        	String currentDBPath = "//data//com.wireless.locationtracking//databases//pos_tracking_log.db";
			            String backupDBPath = "pos_tracking_log.db";
			            File currentDB = new File(data, currentDBPath);
			            File backupDB = new File(sd, backupDBPath);

			            if (currentDB.exists()) {
			                FileChannel src = new FileInputStream(currentDB).getChannel();
			                FileChannel dst = new FileOutputStream(backupDB).getChannel();
			                dst.transferFrom(src, 0, src.size());
			                src.close();
			                dst.close();
			                Toast.makeText(getBaseContext(), backupDB.toString(), Toast.LENGTH_LONG).show();

			                 Log.i("LocalService", "DB backed up to "+backupDBPath);
			            }else{
			            	Log.i("LocalService", "DB doesn't exist??");
			            }
			        }else {
			        	Log.i("LocalService", "Can't write to SD!");
			        }
			    } catch (Exception e) {
			    }
			}
		});*/
        

        /*
         * Create the listener for Wifi Scan results
         */
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		IntentFilter iF = new IntentFilter();
		iF.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(new BroadcastReceiver(){

			@Override
			public void onReceive(Context con, Intent in) {
				// TODO Auto-generated method stub
				List<ScanResult> scanResults = wifiManager.getScanResults();
			}
			
		},iF);
		wifiManager.startScan();
	}
	
	
	public void onDestroy()	{
		manager.unregisterListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			float values[] = event.values.clone();

			//adds the offset from the calibration and checks if it's within three stdDev 
			/*for (int i = 0; i<3; ++i)
			{
				values[i] -= offsets[i];
				if (Math.abs(values[i]) < 3*Math.abs(stdDevs[i]) )
					values[i] = 0.0f;
			}*/
			
			
			double dt = (double)(event.timestamp - lastTimestamp)/1000000000.0; //get change in time in seconds
			lastTimestamp = event.timestamp; 
			
			//calculate a dynamic alpha
			alpha = dt/(timeConstant + dt);

			
			for (int i = 0; i<3; ++i) {
				/*low pass filter for acceleration*/
				acc[i] = truncateValues(acc[i] + alpha * (values[i] - acc[i]), 0.01); //low pass filter for acceleration
				
				/* double integration for position and acceleration calculation */
				pos[i] = truncateValues(pos[i] + dt*truncateValues(vel[i],0.01) + dt*dt/2.0*acc[i], 0.0001);
				vel[i] = truncateValues(vel[i] + dt*acc[i], 0.0001);
			}
			
			/* output the data to the screen */
			for (int i = 0; i<3; ++i) {
				tvAccel[i].setText(df.format(acc[i]));
				tvVel[i].setText(df.format(vel[i]));
				tvPos[i].setText(df.format(pos[i]));
			}
			
			//log the data to the db
			//DBManager.getInstance(this).insertEntries(event.timestamp, acc, vel, pos);
		}
		else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			lastGravity = event.values;
		}
		else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			lastMagnetic = event.values;
		}
	

	}
	
	/**
	 * 
	 * @param val - The value that you want to truncate
	 * @param accuracy - How many decimal places you want to keep (ie. .1 for one, .01 for two, etc)
	 * @return Returns the truncated decimal value
	 */
	public double truncateValues(double val, double accuracy) {
		return (double)((int)(val/accuracy))*accuracy;
	}
	
	/**
	 * 
	 * @param offsets - The offset values to use for accelerometer readings
	 * @param stddev - The standard deviation in the accelerometer readings
	 */
	public void setSensorsCalibrated(double offsets[], double stddev[]) {
		
		manager.unregisterListener(sC); //remove the calibration listener
		
		/* store the values of the offsets and stddev */
		Log.i("Offsets","Offsets: [" + offsets[0] + ", " + offsets[1] + ", " + offsets[2] + "]");
		Log.i("StdDev","StdDev: [" + stddev[0] + ", " + stddev[1] + ", " + stddev[2] + "]");
		this.offsets = offsets;
		this.stdDevs = stddev;
		
		/* setup the new listener */
		lastTimestamp = System.nanoTime();
		manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

}
