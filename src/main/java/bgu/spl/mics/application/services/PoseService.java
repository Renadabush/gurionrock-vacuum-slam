package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.ERROR;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

import java.util.ArrayList;
import java.util.List;

//import java.util.concurrent.Flow;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private int currentTime;
    private GPSIMU gpsimu;
    private List<Pose> poses;
    private List<Pose> posesToreturn;
    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("gpsimu");
        this.gpsimu=gpsimu;
        currentTime=0;
        poses=gpsimu.getPoseList();
        posesToreturn=new ArrayList<>();
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        sendEvent(new SubsicribeMicroserviceEvent());
        subscribeBroadcast(TickBroadcast.class,message->{
            currentTime++;
            if(currentTime<poses.size()){
            gpsimu.setCurrentTick(currentTime);
            posesToreturn.add(poses.get(currentTime));
            sendEvent(new PoseEvent(poses.get(currentTime)));
            }
            if(currentTime==poses.size()){
                sendBroadcast(new TerminatedBroadcast());
                gpsimu.setStatus(STATUS.UP);
                sendBroadcast(new TerminatedBroadcast());}
        });
        subscribeBroadcast(CrashedBroadcast.class,message->{
            gpsimu.setStatus(STATUS.ERROR);
            ERROR.setPoses(posesToreturn);
            sendBroadcast(new TerminatedBroadcast());
            terminate();
        });
        subscribeBroadcast(TerminatedBroadcast.class, message->{});
        subscribeBroadcast(NotifiyterminatedBroadcast.class,message->{
            sendBroadcast(new TerminatedBroadcast());
            gpsimu.setStatus(STATUS.DOWN);
            terminate();
        });
    }
}
