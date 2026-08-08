package bgu.spl.mics.application.objects;

import java.util.HashMap;
import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private List<Pose> PoseList;
    private HashMap<Integer,Pose> temp;
    public GPSIMU(List<Pose> PoseList){
        temp=new HashMap<>();
        this.PoseList=PoseList;
        for(Pose p:PoseList){
            temp.put(p.getTime(),p);
        }
        currentTick=0;
    }
    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setPoseList(List<Pose> poseList) {
        PoseList = poseList;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public STATUS getStatus() {
        return status;
    }

    public List<Pose> getPoseList() {
        return PoseList;
    }
    public Pose getPose(int time){
        return temp.get(time);
    }
}
