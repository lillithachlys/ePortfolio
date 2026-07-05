package com.example.lillybryan6_3;

// imports
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

// main
public class MainActivity extends AppCompatActivity {
    // list of variables
    TextView data;
    SensorManager sensor;
    List<Sensor> sensorList;

    // creates a listener via SensorEventListner
    SensorEventListener listener = new SensorEventListener() {
        // changes the values when the Sensor changes
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] sensorData = event.values; // gets the values of the SensorEvent
            // sets the TextView 'data' to display the X-, Y-, and Z-Axes
            data.setText(String.format(
                    "X-Axis: %s\nY-Axis: %s\nZ-Axis: %s",
                    // X-, Y-, and Z-Axes
                    sensorData[0], sensorData[1], sensorData[2]));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    // initializes variables upon create
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = findViewById(R.id.sensorData); // link 'data' to the layout ID 'sensorData'
        sensor = (SensorManager)getSystemService(SENSOR_SERVICE); // gets the System Sensor Service
        sensorList = sensor.getSensorList(Sensor.TYPE_ACCELEROMETER); // gets the accelerometer sensor

        if(sensorList.isEmpty()){ // tells the user if tha accelerometer is not available
            Toast.makeText(getBaseContext(), "Accelerometer Unavailable!", Toast.LENGTH_LONG).show();
        }else{ // register the listener if there is an accelerometer available
            sensor.registerListener(listener, sensorList.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // unregister the listener upon stop
    @Override
    protected void onStop() {
        super.onStop();
        if(sensorList.isEmpty())
            sensor.unregisterListener(listener);
    }
}
