package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;

public class DetectObjectsEvent implements Event<Boolean> {
    private DetectedObject detectedObject;
    private int time;
    public DetectObjectsEvent(DetectedObject detectedObject,int time){

        this.detectedObject=detectedObject;
        this.time=time;
    }
    public DetectedObject getDetectedObject(){
        return detectedObject;
    }
    public String getId(){
        return detectedObject.getId();
    }
    public String getDescription(){
        return detectedObject.getDescription();
    }
    public int getTime(){
        return time;
    }
}
