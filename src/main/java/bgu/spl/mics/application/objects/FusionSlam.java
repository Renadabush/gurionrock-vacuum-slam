package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
    private List<LandMark> landMarks;
    private List<Pose> poses;
    private String outputPath;
    private String ErrorPath;

    public String getErrorPath() {
        return ErrorPath;
    }

    public String getOutputPath() {
        return outputPath;
    }


    private static class FusionSlamHolder {
        private static final FusionSlam fusionSlam = new FusionSlam();
    }

    private FusionSlam() {
        landMarks = new ArrayList<>();
        poses = new ArrayList<>();
    }

    public void addLandMark(String name, LandMark land) {
        landMarks.add(land);
    }

    public LandMark getLandMark(String id) {
        for (int i = 0; i < landMarks.size(); i = i + 1) {
            if ((landMarks.get(i)).getId().equals(id)) {
                return landMarks.get(i);
            }
        }
        return null;
    }

    public List<LandMark> getLandMarks() {
        return landMarks;
    }

    public List<Pose> getPoses() {
        return poses;
    }

    public static FusionSlam getInstance() {
        return FusionSlamHolder.fusionSlam;
    }

    public Pose getPose(int x) {
        for (int i = 0; i < poses.size(); i = i + 1) {
            if (poses.get(i).getTime() == x) {
                return poses.get(i);
            }
        }
        return null;
    }

    public void addPose(Pose pose) {
        poses.add(pose);
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setErrorOutputPath(String errorPath) {
        ErrorPath = errorPath;
    }

    public LandMark convertToLandMark(TrackedObject trackedObject, Pose pose) {
        double theta = Math.toRadians(pose.getYaw());
        double sin = Math.sin(theta);
        double cos = Math.cos(theta);
        List<CloudPoint> output = new ArrayList<>();
        List<CloudPoint> list = trackedObject.getCoordinates();
        for (CloudPoint point : list) {
            double newX = (cos * point.getX()) - (sin * point.getY()) + pose.getX();
            double newY = (sin * point.getX()) + (cos * point.getY()) + pose.getY();
            CloudPoint p = new CloudPoint(newX, newY);
            output.add(p);

        }
        return new LandMark(trackedObject.getId(), trackedObject.getDescription(), output);
    }
}
