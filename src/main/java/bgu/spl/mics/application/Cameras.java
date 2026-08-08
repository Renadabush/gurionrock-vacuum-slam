package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.Camera;

import java.util.List;

public class Cameras {
    private List<Camera> CamerasConfigurations;
    private String camera_datas_path;

    // Getters and Setters
    public List<Camera> getCamerasConfigurations() {
        return CamerasConfigurations;
    }

    public void setCamerasConfigurations(List<Camera> camerasConfigurations) {
        CamerasConfigurations = camerasConfigurations;
    }

    public String getCameraDatasPath() {
        return camera_datas_path;
    }

    public void setCameraDatasPath(String cameraDatasPath) {
        this.camera_datas_path = cameraDatasPath;
    }
}
