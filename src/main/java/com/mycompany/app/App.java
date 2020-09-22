package com.mycompany.app;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class App {

    private static final String METRICS_JOB_NAME = "adds_file_client";
    private static final String METRICS_LABEL_PLUGIN_NAME = "plugin_name";
    private static final String METRICS_LABEL_FILE_CLIENT_ID = "file_client_id";
    private static final String METRICS_LABEL_PLUGIN_VALUE = "file_processor";
    private static Counter processedRecords;
    static PushGateway pg = new PushGateway("localhost:9091");
    static CollectorRegistry registryPreProcess = new CollectorRegistry();
    static CollectorRegistry regitryPostProcess = new CollectorRegistry();
    static Map<String, String> groupingKey  = new HashMap<String, String>() {{
        put(METRICS_LABEL_FILE_CLIENT_ID, String.valueOf(getFileClientId()));
    }};
    private static int school_id;


    public static void main(String[] args) throws IOException {
        try {
            executeBatchJob();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void executeBatchJob() throws IOException, InterruptedException {
        processedRecords = Counter.build()
                .name(METRICS_JOB_NAME + "_records_processed")
                .help("Total requests.")
                .labelNames("school_id", "short_name")
                .register(registryPreProcess);

        pushMetrics(METRICS_JOB_NAME, true);
        // record start time
        Gauge startTime = Gauge.build()
                .name(METRICS_JOB_NAME + "_last_start_time")
                .labelNames(METRICS_LABEL_PLUGIN_NAME)
                .help("Last time job started, in unixtime.")
                .register(registryPreProcess);
        startTime.labels(METRICS_LABEL_PLUGIN_VALUE).setToCurrentTime();
        pushMetrics(METRICS_JOB_NAME, true);
        Thread.sleep(5000);

        // record duration
        Gauge duration = Gauge.build()
                .name(METRICS_JOB_NAME + "_duration_seconds")
                .labelNames(METRICS_LABEL_PLUGIN_NAME)
                .help("Duration of my batch job in seconds.")
                .register(registryPreProcess);
        Gauge.Timer durationTimer = duration.labels(METRICS_LABEL_PLUGIN_VALUE).startTimer();

        try {
            for (int i = 0; i < 4; i++) {
                System.out.println("processing records");
                //simulate a null record because the app has them
                if (i == 0){
                    setProcessedRecords(123,"my_school");

                }else{
                    setProcessedRecords(getSchool_id(),null);

                }
                TimeUnit.SECONDS.sleep(2);
                pushMetrics(METRICS_JOB_NAME, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // This is only added to the registry after success,
            // so that a previous success in the Pushgateway isn't overwritten on failure.
            Gauge finishTime = Gauge.build()
                    .name(METRICS_JOB_NAME + "_last_finished_time")
                    .labelNames(METRICS_LABEL_PLUGIN_NAME, "instance")
                    .help("Last time my batch job succeeded, in unixtime.")
                    .register(regitryPostProcess);
            finishTime.labels(METRICS_LABEL_PLUGIN_VALUE, "foo").setToCurrentTime();
            durationTimer.setDuration();
            pushMetrics(METRICS_JOB_NAME, true);
            pushMetrics(METRICS_JOB_NAME, false);
        }
    }

    public static void setProcessedRecords(int schoolId, String schoolShortName) throws IOException {
        try {
            processedRecords.labels(String.valueOf(schoolId), schoolShortName);
        } catch (Exception e) {
            System.out.println("An exception occurred adding labels to the processedRecords prometheus metric. " + e);
        }
        pushMetrics(METRICS_JOB_NAME, true);
    }

    public static void pushMetrics(String job, boolean useKey) {
        Map<String, String> groupingKey;
        CollectorRegistry registryNew;
        try {
            if (useKey){
                groupingKey  = new HashMap<String, String>() {{
                    put(METRICS_LABEL_FILE_CLIENT_ID, String.valueOf(getFileClientId()));
                }};
                registryNew = registryPreProcess;
            } else{
                groupingKey = new HashMap<String, String>() {{
                    put(METRICS_LABEL_FILE_CLIENT_ID + "_static", String.valueOf(getFileClientId()));
                }};
                registryNew = regitryPostProcess;
            }
            pg.push(registryNew, job, groupingKey);
        } catch (Exception e) {
            System.out.println("An error occurred pushing metrics to prometheus " + e);
        }
    }

    public static int getSchool_id() {
        return school_id;
    }

    public static int getFileClientId(){
        return 51;
    }
}
