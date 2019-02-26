package com.example.neetx.controllertest;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class KeyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key);
        UsbDevice device = null;
        UsbDeviceConnection usbConnection = null;


        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        final UsbSerialDriver driver = availableDrivers.get(0);
        final UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }
        final UsbSerialPort port = driver.getPorts().get(0);

        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final TextView textv = findViewById(R.id.textView);

        Button sendButton = findViewById(R.id.button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Read some data! Most have just one port (port 0).
                try {
                    Key key;
                    SecureRandom rand = new SecureRandom();
                    KeyGenerator generator = null;
                    generator = KeyGenerator.getInstance("AES");
                    generator.init(256, rand);
                    key = generator.generateKey();
                    byte[] bkey = key.getEncoded();
                    Log.i("KEYYY", Arrays.toString(bkey));
                    Log.i("KEYYY", String.valueOf(bkey.length));

                    byte[] iv = new byte[12];
                    SecureRandom iv_rand = new SecureRandom();
                    iv_rand.nextBytes(iv);

                    Log.i("KEYYY", Arrays.toString(iv));
                    Log.i("KEYYY", String.valueOf(iv.length));

                    byte[] out = new byte[44];
                    out = Encryptor.concatenateByteArrays(bkey,iv);

                    port.write(out, 1000);
                    Log.i("SD","Post-Write");

                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("key",bkey);
                    bundle.putByteArray("iv", iv);
                    i.putExtras(bundle);
                    startActivity(i);

                } catch (IOException e) {
                    textv.setText(e.getMessage());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } finally {
                    /*try {
                        port.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                }
            }
        });
    }
}
