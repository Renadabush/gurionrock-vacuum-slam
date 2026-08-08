package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * <p>
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private int currentTime;
    private LiDarDataBase base;
    private LiDarWorkerTracker liDarWorkerTracker;
    private HashMap<Integer, List<DetectObjectsEvent>> toWait;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker, LiDarDataBase liDarDataBase) {
        super("lidarWorkerTracker");
        this.liDarWorkerTracker = LiDarWorkerTracker;
        base = liDarDataBase;
        this.currentTime = 0;
        toWait = new HashMap<>();
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        sendEvent(new SubsicribeMicroserviceEvent());
        subscribeBroadcast(TickBroadcast.class, message -> {
            currentTime++;
            if(toWait.containsKey(currentTime)){
                List<DetectObjectsEvent> list=toWait.get(currentTime);
                for(DetectObjectsEvent detectObjectsEvent:list){
                    List<CloudPoint> output = base.search(detectObjectsEvent.getTime(), detectObjectsEvent.getId());
                    update(detectObjectsEvent,output);
                }
        }});
        subscribeEvent(DetectObjectsEvent.class, DetectObjectsEvent -> {
            if (liDarWorkerTracker.getFrequency() == 0) {
                List<CloudPoint> output = base.search(DetectObjectsEvent.getTime(), DetectObjectsEvent.getId());
                update(DetectObjectsEvent, output);
            }
            else {
                if(toWait.containsKey(currentTime+ liDarWorkerTracker.getFrequency())){
                    (toWait).get(currentTime+ liDarWorkerTracker.getFrequency()).add(DetectObjectsEvent);
                }
                else{
                    List<DetectObjectsEvent>detectObjectsEventList=new ArrayList<>();
                    detectObjectsEventList.add(DetectObjectsEvent);
                    toWait.put(currentTime+ liDarWorkerTracker.getFrequency(),detectObjectsEventList);
                }
            }

        });
        subscribeBroadcast(CrashedBroadcast.class, message -> {
            liDarWorkerTracker.setStatus(STATUS.ERROR);
            int last = (liDarWorkerTracker.getLastTrackedObjects()).size() - 1;
            TrackedObject lastTrack = (liDarWorkerTracker.getLastTrackedObjects()).get(last);
            ERROR.addLidarFrame("liDarWorkerTracker " + liDarWorkerTracker.getId(), lastTrack);
            sendBroadcast(new TerminatedBroadcast());
            terminate();
        });
        subscribeBroadcast(TerminatedBroadcast.class, message -> {
        });
        subscribeBroadcast(NotifiyterminatedBroadcast.class, message -> {
            sendBroadcast(new TerminatedBroadcast());
            liDarWorkerTracker.setStatus(STATUS.DOWN);
            terminate();
        });
        subscribeBroadcast(lidarBroadcast.class, message -> {
            sendBroadcast(new TerminatedBroadcast());
            liDarWorkerTracker.setStatus(STATUS.DOWN);
            terminate();
        });
    }
    public void update(DetectObjectsEvent detectObjectsEvent, List<CloudPoint> cloudPoints) {
        if (cloudPoints == null) {
            liDarWorkerTracker.setStatus(STATUS.ERROR);
            sendBroadcast(new CrashedBroadcast());
            ERROR.setFaultySensor("liDarWorkerTracker " + liDarWorkerTracker.getId());
            ERROR.setError("Sensor " + "liDarWorkerTracker " + liDarWorkerTracker.getId() + " disconnected");
            int last = (liDarWorkerTracker.getLastTrackedObjects()).size() - 1;
            ERROR.addLidarFrame("liDarWorkerTracker " + liDarWorkerTracker.getId(), (liDarWorkerTracker.getLastTrackedObjects()).get(last));
            terminate();

        }
        else{
            TrackedObject trackedObject=new TrackedObject(detectObjectsEvent.getId(),detectObjectsEvent.getTime(), detectObjectsEvent.getDescription(),cloudPoints);
            liDarWorkerTracker.add(trackedObject);
            StatisticalFolder.incrementTrackedObjects();
            complete(detectObjectsEvent, true);
            Future<Boolean> future = sendEvent(new TrackedObjectsEvent(trackedObject));
            if (StatisticalFolder.getTrackedObjects() == base.getSize()) {
                sendBroadcast(new lidarBroadcast());
            }
        }
    }
}