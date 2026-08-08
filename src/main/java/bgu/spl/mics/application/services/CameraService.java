package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.HashMap;
import java.util.List;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * <p>
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private int currentTime;
    private Camera camera;
    private int pointer;
    private List<StampedDetectedObjects> mylist;
    private StampedDetectedObjects last;
    private ERROR error;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("CameraService");
        this.camera=camera;
        this.currentTime = 0;
        this.pointer = 0;
        mylist = camera.getDetectedObjectsList();
        error = new ERROR();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        sendEvent(new SubsicribeMicroserviceEvent());
        subscribeBroadcast(TickBroadcast.class, message -> {
            currentTime++;
            boolean found = false;
            boolean flag=false;
            while ((pointer < mylist.size()) && (!found) && (((mylist.get(pointer))).getTime() + camera.getFrequency()) <= currentTime) {
                if (((((mylist.get(pointer))).getTime() + camera.getFrequency()) == currentTime)) {
                    found = true;
                    List<DetectedObject> myDetected = (mylist.get(pointer)).getDetectedObjects();
                    for (int i = 0; i < myDetected.size(); i = i + 1) {
                        if (((myDetected.get(i)).getId()).equals("ERROR")) {
                            sendBroadcast(new CrashedBroadcast());
                            camera.setStatus(STATUS.ERROR);
                            ERROR.setFaultySensor(camera.getCamerakey());
                            ERROR.setError((myDetected.get(i)).getDescription());
                            ERROR.addCameraFrame(camera.getCamerakey(), last);
                            flag=true;
                            terminate();
                            break;
                        } else {
                            Future<Boolean> future = sendEvent(new DetectObjectsEvent(myDetected.get(i),mylist.get(pointer).getTime()));
                            StatisticalFolder.incrementNumDetectedObjects();
                        }

                    }
                    if(!flag){
                        last=mylist.get(pointer);
                    }
                    pointer++;
                } else {
                    pointer++;
                }

            }
            if (pointer >= mylist.size() && (!flag)){
                camera.setStatus(STATUS.DOWN);
                sendBroadcast(new TerminatedBroadcast());
                terminate();
            }
        });
        subscribeBroadcast(CrashedBroadcast.class, message -> {
            camera.setStatus(STATUS.ERROR);
            ERROR.addCameraFrame(camera.getCamerakey(),last);
            sendBroadcast(new TerminatedBroadcast());
            terminate();

        });
        subscribeBroadcast(TerminatedBroadcast.class, message -> {
        });
        subscribeBroadcast(NotifiyterminatedBroadcast.class,message->{
            sendBroadcast(new TerminatedBroadcast());
            camera.setStatus(STATUS.DOWN);
            terminate();
        });

    }
}
