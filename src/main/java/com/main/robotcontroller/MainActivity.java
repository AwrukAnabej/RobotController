package com.main.robotcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import java.lang.Math;
import com.main.robotcontroller.ComputedCoordinates;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MainActivity  extends Activity implements BluetoothSPPConnectionListener {

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothSPPConnection mBluetoothSPPConnection;
    private BluetoothAdapter mBluetoothAdapter = null;
    private PowerManager.WakeLock wl;
    private boolean connected=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing the gravity vector to zero.
        // Initializing the accelerometer stuff
        // Register this as SensorEventListener


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //If bluetooth is not activated, ask the user to activate
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        // Initializing the bluetooth SPP connection.
        // Register this as the BluetoothSPPConnectionListener.
        mBluetoothSPPConnection = new BluetoothSPPConnection(this); // Registers the
        final SeekBar angle_1=(SeekBar)findViewById(R.id.seek_angle1);
        final SeekBar angle_2=(SeekBar)findViewById(R.id.seek_angle2);
        final SeekBar angle_3=(SeekBar)findViewById(R.id.seek_angle3);
        angle_1.setProgress(90);
        angle_2.setProgress(90);
        angle_3.setProgress(90);

        final EditText px=(EditText)findViewById(R.id.px_et);
        final EditText py=(EditText)findViewById(R.id.py_et);
        final EditText pz=(EditText)findViewById(R.id.pz_et);

        final EditText theta_1=(EditText)findViewById(R.id.theta1_et);
        final EditText theta_2=(EditText)findViewById(R.id.theta2_et);
        final EditText d_3=(EditText)findViewById(R.id.d_3_et);

        final TextView px_coordinate=(TextView)findViewById(R.id.computed_px_tv);
        final TextView py_coordinate=(TextView)findViewById(R.id.computed_py_tv);
        final TextView pz_coordinate=(TextView)findViewById(R.id.computed_pz_tv);

        final Button send_coordinates=(Button)findViewById(R.id.coordinates_btn);
        final Button check_coordinates=(Button)findViewById(R.id.computecoordinates_btn);
        Button bt = (Button) findViewById(R.id.connect);
        // Initializing the "connect" button.
        // Register this an the OnClickListener.
        bt.setText("Connect");
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        if (!connected) {
                            Intent serverIntent = new Intent(v.getContext(), DeviceListActivity.class);
                            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                            // When connected, close the bluetooth connection.
                        } else {
                            mBluetoothSPPConnection.close();
                        }
            }
        });
        send_coordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        if(ComputedInverseCinematics.ComputeInverseCinematics(Double.parseDouble(px.getText().toString()),
                                Double.parseDouble(py.getText().toString()),
                                Double.parseDouble(pz.getText().toString()), 127,127)) {
                            int[] Frame_to_send = new int[6];
                            Frame_to_send[0] = 0;
                            Frame_to_send[1] = (int)Math.round(ComputedInverseCinematics.getTheta1());
                            Frame_to_send[2] = (int)Math.round(ComputedInverseCinematics.getTheta2());
                            Frame_to_send[3] = (int)Math.round(ComputedInverseCinematics.getD3());
                            Frame_to_send[4] = Operation.COORDINATES.getIndex();
                            Frame_to_send[5] = 0;
                            bluetoothWrite(Frame_to_send);
                            angle_1.setProgress(90+(int)Math.round( ComputedInverseCinematics.getTheta1()));
                            angle_2.setProgress(90+(int)Math.round( ComputedInverseCinematics.getTheta2()));
                            angle_3.setProgress(90+(int)Math.round(ComputedInverseCinematics.getD3()));
                        }
            }
        });
        check_coordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComputedCoordinates.ComputeCoordinates(Double.parseDouble(theta_1.getText().toString()), Double.parseDouble(theta_2.getText().toString()),
                        Double.parseDouble(d_3.getText().toString()), 127, 127);
                DecimalFormat df=new DecimalFormat("#.##");
                DecimalFormatSymbols symbols=DecimalFormatSymbols.getInstance();
                symbols.setDecimalSeparator('.');
                df.setDecimalFormatSymbols(symbols);
                px_coordinate.setText("px: "+ df.format(ComputedCoordinates.getPx()));
                py_coordinate.setText("py: " + df.format(ComputedCoordinates.getPy()));
                pz_coordinate.setText("pz: " + df.format(ComputedCoordinates.getPz()));
                px.setText(df.format(ComputedCoordinates.getPx()));
                py.setText(df.format(ComputedCoordinates.getPy()));
                pz.setText(df.format(ComputedCoordinates.getPz()));
            }
        });
        angle_1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int[] Frame_to_send = new int[6];
                Frame_to_send[0] = 0;
                Frame_to_send[1] = angle_1.getProgress()-90;
                Frame_to_send[2] = angle_2.getProgress()-90;
                Frame_to_send[3] = angle_3.getProgress()-90;
                Frame_to_send[4] = Operation.ANGLES.getIndex();
                Frame_to_send[5] = 0;
                bluetoothWrite(Frame_to_send);
                ComputedInverseCinematics.setTheta1(angle_1.getProgress()-90);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        angle_2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int[] Frame_to_send = new int[6];
                Frame_to_send[0] = 0;
                Frame_to_send[1] = angle_1.getProgress()-90;
                Frame_to_send[2] = angle_2.getProgress()-90;
                Frame_to_send[3] = angle_3.getProgress()-90;
                Frame_to_send[4] = Operation.ANGLES.getIndex();
                Frame_to_send[5] = 0;
                bluetoothWrite(Frame_to_send);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        angle_3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int[] Frame_to_send = new int[6];
                Frame_to_send[0] = 0;
                Frame_to_send[1] = angle_1.getProgress()-90;
                Frame_to_send[2] = angle_2.getProgress()-90;
                Frame_to_send[3] = angle_3.getProgress()-90;
                Frame_to_send[4] = Operation.ANGLES.getIndex();
                Frame_to_send[5] = 0;
                bluetoothWrite(Frame_to_send);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
                // Getting a WakeLock. This insures that the phone does not sleep
                // while driving the robot.

                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
        wl.acquire();
    }

    public enum Operation {
        ANGLES(0), COORDINATES(1);
        private final int value;
        Operation(int value) {
            this.value = value;
        }
        public int getIndex() {
            return value;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close the bluetooth connection
        mBluetoothSPPConnection.close();
        wl.release();
        // Release the WakeLock so that the phone can go to sleep to preserve battery.
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mBluetoothSPPConnection.open(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                }
                break;
        }
    }

    public void bluetoothWrite(int[] number)
    {

        final ByteBuffer data=ByteBuffer.allocate(number.length*Integer.SIZE);
        for(int i=0;i<number.length;i++)
        data.putInt(number[i]);

        byte[] temp=data.array();
        byte[] data_to_send=new byte[number.length];
        for(int i=0;i<number.length;i++)
        {
            data_to_send[i]=temp[3+i*4];
        }
        mBluetoothSPPConnection.write(data_to_send);
    }

    public void bluetoothWrite(int bytes, byte[] buffer) {

            mBluetoothSPPConnection.write(buffer);
    }

    public void onConnecting() {
        // This function is called on the moment the phone starts making a connecting with the bluetooth module.
        // The function is executed in the main thread.

        // Change the text in the connectionInfo TextView
        TextView connectionView = (TextView) findViewById(R.id.connectionInfo);
        connectionView.setText("Connecting...");
    }

    public void onConnected() {
        // This function is called on the moment a connection is realized between the phone and the bluetooth module.
        // The function is executed in the main thread.

        connected = true;

        // Change the text in the connectionInfo TextView
        TextView connectionView = (TextView) findViewById(R.id.connectionInfo);
        connectionView.setText("Connected to "+mBluetoothSPPConnection.getDeviceName());

        // Change the text in the connect button.
        Button bt = (Button) findViewById(R.id.connect);
        bt.setText("Disconnect");

        // Send the 's' character so that the communication can start.
        byte[] command = new byte[1];
        command[0]='s';
        mBluetoothSPPConnection.write(command);
    }

    public void onConnectionFailed() {
        // This function is called when the intended connection could not be realized.
        // The function is executed in the main thread.

        connected = false;

        // Change the text in the connectionInfo TextView
        TextView connectionView = (TextView) findViewById(R.id.connectionInfo);
        connectionView.setText("Connection failed!");

        // Change the text in the connect button.
        Button bt = (Button) findViewById(R.id.connect);
        bt.setText("Connect");
    }

    public void onConnectionLost() {
        // This function is called when the intended connection could not be realized.
        // The function is executed in the main thread.

        connected = false;

        // Change the text in the connectionInfo TextView
        TextView connectionView = (TextView) findViewById(R.id.connectionInfo);
        connectionView.setText("Not Connected!");

        // Change the text in the connect button.
        Button bt = (Button) findViewById(R.id.connect);
        bt.setText("Connect");
    }
}