package bgu.spl.mics.application.objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ERROR {
    private static String error;
    private static String faultySensor;
    private static ConcurrentHashMap<String,StampedDetectedObjects> lastCamerasFrame=new ConcurrentHashMap<>();;
    private static ConcurrentHashMap<String,TrackedObject>  lastLiDarWorkerTrackersFrame=new ConcurrentHashMap<>();
    private static List<Pose> poses;
    private static StatisticalFolder statistics;
    public ERROR (){
    }
    public static void addCameraFrame(String name,StampedDetectedObjects stampedDetectedObjects){
        lastCamerasFrame.put(name,stampedDetectedObjects);
    }
    public static void addLidarFrame(String name,TrackedObject trackedObject){
        lastLiDarWorkerTrackersFrame.put(name,trackedObject);
    }

    public static void setFaultySensor(String faultySensor) {
        ERROR.faultySensor = faultySensor;
    }

    public static void setError(String error) {
        ERROR.error = error;
    }

    public static void setPoses(List<Pose> poses) {
        ERROR.poses = poses;
    }
    public static void setStatistics(StatisticalFolder statistics){
        ERROR.statistics=statistics;
    }


    public static String getError() {
        return error;
    }

    public static ConcurrentHashMap<String, TrackedObject> getLastLiDarWorkerTrackersFrame() {
        return lastLiDarWorkerTrackersFrame;
    }

    public static String getFaultySensor() {
        return faultySensor;
    }

    public static ConcurrentHashMap<String, StampedDetectedObjects> getLastCamerasFrame() {
        return lastCamerasFrame;
    }

    public static List<Pose> getPoses() {
        return poses;
    }




}
