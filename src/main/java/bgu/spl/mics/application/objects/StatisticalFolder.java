package bgu.spl.mics.application.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private static final  AtomicInteger systemRuntime=new AtomicInteger(1);
    private static final  AtomicInteger numDetectedObjects =new AtomicInteger(0);
    private static final  AtomicInteger TrackedObjects = new AtomicInteger(0);
    private static final  AtomicInteger numLandmarks = new AtomicInteger(0);
    public static void incrementSystemRuntimeInc(){
        systemRuntime.incrementAndGet();
    }
    public StatisticalFolder(){}

    public static void incrementNumLandmarks() {
        numLandmarks.incrementAndGet();
    }

    public static void incrementNumDetectedObjects() {
        numDetectedObjects.incrementAndGet();
    }

    public static void incrementTrackedObjects() {
        TrackedObjects.incrementAndGet();
    }

    public static int getNumLandmarks() {
        return numLandmarks.get();
    }

    public static int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public static int getSystemRuntime() {
        return systemRuntime.get();
    }

    public static int getTrackedObjects() {
        return TrackedObjects.get();
    }
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("systemRuntime", getSystemRuntime());
        stats.put("numLandmarks", getNumLandmarks());
        stats.put("numDetectedObjects", getNumDetectedObjects());
        stats.put("trackedObjects", getTrackedObjects());
        return stats;
    }
}
