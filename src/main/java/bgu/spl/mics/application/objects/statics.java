package bgu.spl.mics.application.objects;

public class statics {
    private int systemRuntime;
    private int numDetectedObjects;
    private int TrackedObjects;
    private int numLandmarks;
    public statics (){
        systemRuntime=StatisticalFolder.getSystemRuntime();
        numDetectedObjects=StatisticalFolder.getNumDetectedObjects();
        TrackedObjects=StatisticalFolder.getTrackedObjects();
        numLandmarks=StatisticalFolder.getNumLandmarks();

    }
}
