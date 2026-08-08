package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private int currentTime;
    private FusionSlam fusionSlam;
    private HashMap<Integer,List<TrackedObjectsEvent>> wait;
    private int numOfMicro;
    private int numOfTer;
    private boolean crached;
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        this.fusionSlam=fusionSlam;
        wait=new HashMap<>();
        this.numOfMicro=0;
        this.numOfTer=0;
        crached=false;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeEvent(PoseEvent.class,pose->{
            fusionSlam.addPose(pose.getPose());
            if(wait.containsKey(pose.getTime())){
                List<TrackedObjectsEvent> toProcess=wait.get(pose.getTime());
                for(TrackedObjectsEvent a:toProcess){
                    update(a, pose.getPose());
                }
            }
        });
        subscribeEvent(SubsicribeMicroserviceEvent.class,message-> numOfMicro++);
        subscribeEvent(TrackedObjectsEvent.class,tracked->{
            Pose myPose=fusionSlam.getPose(tracked.getTime());
                if(myPose==null){
                    if(wait.get(tracked.getTime())==null){
                        List<TrackedObjectsEvent> a=new ArrayList<>();
                        a.add(tracked);
                        wait.put(tracked.getTime(), a);
                    }
                    else
                        (wait.get(tracked.getTime())).add(tracked);
                }
                else
                    update(tracked,myPose);


        });
        subscribeBroadcast(TerminatedBroadcast.class,message->{
            numOfTer++;
            if(numOfTer==numOfMicro){
                sendBroadcast(new FusionSlamTerminatedBroadcast());
            }
            if(!crached)
                writeOutputFile();
            else
                writeErrorFile();
            terminate();
        });
        subscribeBroadcast(CrashedBroadcast.class,message->{
            crached=true;
        });
}

    public List<CloudPoint> convert(List<CloudPoint> list,Pose pose){
        double theta=Math.toRadians(pose.getYaw());
        double sin=Math.sin(theta);
        double cos=Math.cos(theta);
        List<CloudPoint> output=new ArrayList<>();
        for(CloudPoint point:list){
            double newX=(cos* point.getX())-(sin* point.getY())+pose.getX();
            double newY=(sin* point.getX())+(cos* point.getY())+ pose.getY();
            CloudPoint p=new CloudPoint(newX,newY);
            output.add(p);

        }
        return output;
    }
    public List<CloudPoint> average(List<CloudPoint> list1,List<CloudPoint> list2){
        List<CloudPoint> output=new ArrayList<>();
        int i=0;
        while((i<list1.size())&&(i<list2.size())){
            double x=0.5*((list1.get(i).getX())+(list2.get(i).getX()));
            double y=0.5*((list1.get(i)).getY()+(list2.get(i).getY()));
            output.add(new CloudPoint(x,y));
            i++;
        }
        if(list1.size()!=i){
            while(i< list1.size()){
                output.add(list1.get(i));
                i++;
            }

        }
        else if((list2.size()!=i)){
            while(i< list2.size()){
                output.add(list2.get(i));
                i++;
            }
        }
        return output;
    }
    public void update(TrackedObjectsEvent track,Pose pose){
        if(fusionSlam.getLandMark(track.getId())!=null){
            LandMark land=fusionSlam.getLandMark(track.getId());
            List<CloudPoint>newP=convert(track.getCloudPoint(),pose);
            newP=average(newP,land.getCoordinates());
            land.setCoordinates(newP);
        }
        else{
            LandMark land =fusionSlam.convertToLandMark(track.getTrackedObject(),pose);
            fusionSlam.addLandMark(track.getId(),land);
            StatisticalFolder.incrementNumLandmarks();
        }
    }
    private void writeOutputFile() {
        String outputPath =fusionSlam.getOutputPath();
        Map<String, Object> data = new HashMap<>();
        data.put("landMarks", fusionSlam.getLandMarks()); // List of LandMark
        data.put("statistics", (new StatisticalFolder().getStatistics())); // Statistics
        // Use Gson to write to JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(outputPath)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeErrorFile() {
            // Create a Gson object
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Create an instance of ERRORData to serialize
            ErrorOutput data = new ErrorOutput();

            // Serialize the object to JSON and write to file
            try (FileWriter writer = new FileWriter(fusionSlam.getErrorPath())) {
                gson.toJson(data, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



