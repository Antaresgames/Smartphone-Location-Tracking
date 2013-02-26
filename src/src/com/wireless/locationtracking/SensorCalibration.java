package com.wireless.locationtracking;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class SensorCalibration implements SensorEventListener  {
	MainActivity parent = null;
	
	final int sampleSize = 10;
	int counter = 0;
	
	double samples[][] = new double[3][sampleSize];
	double x[] = new double[sampleSize], y[] = new double[sampleSize], z[] = new double[sampleSize];
	
	double means[] = new double[3];
	double stdDevs[] = new double[3];
	
	public SensorCalibration(MainActivity parent) {
		this.parent = parent;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		for (int i = 0; i<3; ++i)
		{
			means[i] += event.values[i];
			samples[i][counter] = event.values[i];
		}
		++counter;
		
		if (counter==sampleSize)
		{
			double sums[] = new double[3];
			
			for (int i = 0; i<3; ++i)
				means[i] /= sampleSize;
			
			for (int i = 0; i<3; ++i) {
				for (int j = 0; j<sampleSize; ++j)
					sums[i] += Math.pow(samples[i][j] - means[i],2);
				sums[i] /= sampleSize;
			}
			
			parent.setSensorsCalibrated(means, sums);
		}
	}

}
