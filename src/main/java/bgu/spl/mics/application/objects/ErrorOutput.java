package bgu.spl.mics.application.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ErrorOutput {
        private String error;
        private String faultySensor;
        private ConcurrentHashMap<String, StampedDetectedObjects> lastCamerasFrame;
        private ConcurrentHashMap<String, TrackedObject> lastLiDarWorkerTrackersFrame;
        private List<Pose> poses;
        private statics statistics;
        private List<LandMark> landMarks;

        public ErrorOutput() {
            // Populate fields from static ERROR class
            this.error = ERROR.getError();
            this.faultySensor = ERROR.getFaultySensor();
            this.lastCamerasFrame = ERROR.getLastCamerasFrame();
            this.lastLiDarWorkerTrackersFrame = ERROR.getLastLiDarWorkerTrackersFrame();
            this.poses = ERROR.getPoses();
            statistics =new statics();
            landMarks=FusionSlam.getInstance().getLandMarks();
        }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("error", this.error);
        map.put("faultySensor", this.faultySensor);
        map.put("lastCamerasFrame", this.lastCamerasFrame);
        map.put("lastLiDarWorkerTrackersFrame", this.lastLiDarWorkerTrackersFrame);
        map.put("poses", this.poses);
        map.put("statistics", statistics); // Serialize the statistics map
        return map;
    }

}
