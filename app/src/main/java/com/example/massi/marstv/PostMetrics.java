package com.example.massi.marstv;

import android.app.IntentService;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PostMetrics extends IntentService {
    String broker = "tcp://82.196.2.189:1883";
    //String broker = "tcp://test.mosquitto.org:1883";
    String topic = "iot-dogstatsd";

    public PostMetrics() {
        super("PostMetrics");
    }

    private String gatherMetrics() {
        List<String> metrics = new ArrayList<>();

        Runtime r = Runtime.getRuntime();

        metrics.add("_sc|tv_is_on|0|#host:marsTV");
        metrics.add(String.format("mqtt.free_memory:%d|g|#host:marsTV", r.freeMemory()));
        metrics.add(String.format("mqtt.cpu_usage:%.2f|g|#host:marsTV", cpuUsage()));
        metrics.add(String.format("mqtt.bytes_rcvd:%d|g|#host:marsTV", TrafficStats.getTotalRxBytes()));
        metrics.add(String.format("mqtt.bytes_sent:%d|g|#host:marsTV", TrafficStats.getTotalTxBytes()));

        return android.text.TextUtils.join("\n", metrics);
    }

    private float cpuUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" +");  // Split on one or more spaces

            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {}

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            MqttClient sampleClient = new MqttClient(broker, "marsTV", persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            sampleClient.connect(connOpts);
            while (true) {
                Log.d("MARSTV", "Sending packet...");
                MqttMessage message = new MqttMessage(gatherMetrics().getBytes());
                message.setQos(2);
                sampleClient.publish(topic, message);
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {}
            }
        } catch (MqttException me) {
            Log.e("MARSTV", me.toString());
        }
    }
}
