package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;

public class TrackedObjectsEvent implements Event<Boolean> {
    private TrackedObject trackedObject;
    public TrackedObjectsEvent(TrackedObject trackedObject){
        this.trackedObject=trackedObject;
    }
    public List<CloudPoint>getCloudPoint(){
        return trackedObject.getCoordinates();
    }
    public String getId(){
        return trackedObject.getId();
    }
    public int getTime(){
        return trackedObject.getTime();
    }
    public String getDescription(){
        return trackedObject.getDescription();
    }
    public TrackedObject getTrackedObject(){
        return trackedObject;
    }
}
