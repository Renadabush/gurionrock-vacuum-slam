package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.LiDarWorkerTracker;

import java.util.List;

public class LidarWorkers {
    private List<LiDarWorkerTracker> LidarConfigurations;
    private String lidars_data_path;

    // Getters and Setters
    public List<LiDarWorkerTracker> getLidarConfigurations() {
        return LidarConfigurations;
    }

    public void setLidarConfigurations(List<LiDarWorkerTracker> lidarConfigurations) {
        LidarConfigurations = lidarConfigurations;
    }

    public String getLidarsDataPath() {
        return lidars_data_path;
    }

    public void setLidarsDataPath(String lidarsDataPath) {
        this.lidars_data_path = lidarsDataPath;
    }
}
