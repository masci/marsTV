package com.example.massi.marstv;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PostMetrics extends IntentService {

    public PostMetrics() {
        super("PostMetrics");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WakefulBroadcastReceiver.completeWakefulIntent(intent);

        String broker = "tcp://test.mosquitto.org:1883";
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            Log.e("MARSTV", "Sending packet...");
            MqttClient sampleClient = new MqttClient(broker, "marsTV", persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            sampleClient.connect(connOpts);

            while (true) {
                MqttMessage message = new MqttMessage("MESSAGE".getBytes());
                message.setQos(2);
                sampleClient.publish("FOOTOPIC", message);
                SystemClock.sleep(1000);
            }

        } catch (MqttException me) {
            Log.e("MARSTV", me.toString());
        }
    }
}
