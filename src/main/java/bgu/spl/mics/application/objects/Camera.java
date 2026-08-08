package bgu.spl.mics.application.objects;
import com.google.gson.annotations.SerializedName;
import java.util.List;

import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    @SerializedName("camera_key")
    private String camerakey ;
    private int id;
    private int frequency;
    private STATUS status;
    private List<StampedDetectedObjects> detectedObjectsList;
    public Camera(){}
    public Camera(String camerakey,int id,int frequency){
        this.camerakey=camerakey;
        this.id=id;
        this.frequency=frequency;
    }

    public int getFrequency() {
        return frequency;
    }
    public int getId(){
        return id;
    }
    public STATUS getStatus(){
        return status;
    }

    public List<StampedDetectedObjects> getDetectedObjectsList() {
        return detectedObjectsList;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setDetectedObjectsList(List<StampedDetectedObjects> detectedObjectsList) {
        this.detectedObjectsList = detectedObjectsList;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public String getCamerakey() {
        return camerakey;
    }

    public void setCamerakey(String camera_key) {
        this.camerakey = camera_key;
    }
}
