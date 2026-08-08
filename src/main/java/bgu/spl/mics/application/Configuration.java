package bgu.spl.mics.application;

public class Configuration {
    private Cameras Cameras;
    private bgu.spl.mics.application.LidarWorkers LiDarWorkers;
    private String poseJsonFile;
    private int TickTime;
    private int Duration;



public Cameras getCameras() {
    return Cameras;
}

public void setCameras(Cameras cameras) {
    Cameras = cameras;
}

public bgu.spl.mics.application.LidarWorkers getLiDarWorkers() {
    return LiDarWorkers;
}

public void setLiDarWorkers(bgu.spl.mics.application.LidarWorkers liDarWorkers) {
    this.LiDarWorkers = liDarWorkers;
}

public String getPoseJsonFile() {
    return poseJsonFile;
}

public void setPoseJsonFile(String poseJsonFile) {
    this.poseJsonFile = poseJsonFile;
}

public int getTickTime() {
    return TickTime;
}

public void setTickTime(int tickTime) {
    TickTime = tickTime;
}

public int getDuration() {
    return Duration;
}

public void setDuration(int duration) {
    Duration = duration;
}
}
