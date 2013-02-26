package com.wireless.locationtracking;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.TextView;


public class MainActivity extends Activity implements SensorEventListener {

	TextView 	tvAccelX , tvAccelY, tvAccelZ,
				tvVelX, tvVelY, tvVelZ,
				tvPosX, tvPosY, tvPosZ;
	
	NumberFormat df = DecimalFormat.getInstance();
	
	
	SensorManager manager = null;
	Sensor accelerometer = null;
	long lastTimestamp = -1;
	double 	accX = 0.0, accY = 0.0, accZ = 0.0,
			velX = 0.0, velY = 0.0, velZ = 0.0,
			posX = 0.0, posY = 0.0, posZ = 0.0;
	double timeConstant = 0.297; //the constant used in the low pass filter
	double alpha = 0.0; //another value used in the low pass filter
	
	double offsets[], stdDevs[]; //stores the sensor calibration data
	
	SensorCalibration sC = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		df.setMinimumFractionDigits(1);
		df.setMaximumFractionDigits(2);

		tvAccelX = (TextView)findViewById(R.id.accelX);
		tvAccelY =  (TextView)findViewById(R.id.accelY);
		tvAccelZ =  (TextView)findViewById(R.id.accelZ);

		tvVelX =  (TextView)findViewById(R.id.velX);
		tvVelY =  (TextView)findViewById(R.id.velY);
		tvVelZ =  (TextView)findViewById(R.id.velZ);

		tvPosX =  (TextView)findViewById(R.id.posX);
		tvPosY =  (TextView)findViewById(R.id.posY);
		tvPosZ =  (TextView)findViewById(R.id.posZ);
		

		manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

		sC = new SensorCalibration(this);
		manager.registerListener(sC, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		

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
		
		//adds the offset from the calibration and checks if it's within one stdDev 
		for (int i = 0; i<3; ++i)
		{
			event.values[i] -= offsets[i];
			if (Math.abs(event.values[i]) < Math.abs(stdDevs[i]) )
				event.values[i] = 0.0f;
		}
		
		double dt = (double)(event.timestamp - lastTimestamp)/1000000000.0; //get change in time in seconds
		lastTimestamp = event.timestamp; 
		
		alpha = dt/(timeConstant + dt);

		//low pass filter for accel
		accX = truncateValues( accX + alpha * (event.values[0] - accX) );
		accY = truncateValues( accY + alpha * (event.values[1] - accY) );
		accZ = truncateValues( accZ + alpha * (event.values[2] - accZ) );

		//if (Math.abs(event.values[0])<.4) event.values[0] = 0.0f;
		//if (Math.abs(event.values[1])<.4) event.values[1] = 0.0f;
		//if (Math.abs(event.values[2])<.4) event.values[2] = 0.0f;
		
		posX = posX + dt*velX + dt*dt/2.0*accX;
		velX = truncateValues(velX + dt*accX);

		posY = posY + dt*velY + dt*dt/2.0*accY;
		velY = truncateValues(velY + dt*accY);
		
		posZ = posZ + dt*velZ + dt*dt/2.0*accZ;
		velZ= truncateValues(velZ + dt*accZ);
		
		
		tvAccelX.setText(df.format(accX));
		tvAccelY.setText(df.format(accY));
		tvAccelZ.setText(df.format(accZ));

		tvVelX.setText(df.format(velX));
		tvVelY.setText(df.format(velY));
		tvVelZ.setText(df.format(velZ));

		tvPosX.setText(df.format(posX));
		tvPosY.setText(df.format(posY));
		tvPosZ.setText(df.format(posZ));
	
		
	}
	
	public double truncateValues(double val) {
		return (double)((int)(val*10))/10.0;
	}
	
	public void setSensorsCalibrated(double offsets[], double stddev[]) {
		manager.unregisterListener(sC);
		this.offsets = offsets;
		this.stdDevs = stddev;
		lastTimestamp = System.nanoTime();
		manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

}
