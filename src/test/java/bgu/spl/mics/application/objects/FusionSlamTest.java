package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class FusionSlamTest {
    private FusionSlam fusionSlam;
    private Pose pose;
    private TrackedObject trackedObject;

    @BeforeEach
    void setUp() {
        fusionSlam = FusionSlam.getInstance();
        pose = new Pose(10, 20, 45, 1);
        List<CloudPoint> listOfCloudPoints = new ArrayList<>();
        listOfCloudPoints.add(new CloudPoint(5, 6));
        listOfCloudPoints.add(new CloudPoint(7, 7));
        trackedObject = new TrackedObject("wall", 1, "wall3", listOfCloudPoints);
    }

    @Test
    void testConvertToLandMark() {
        LandMark landmark = fusionSlam.convertToLandMark(trackedObject, pose);
        double expectedX1 = 9.292893218813454;
        double expectedY1 = 27.77817459305202;
        double expectedX2 = 10.0;
        double expectedY2 = 29.899494936611664;
        assertEquals(expectedX1, ((landmark.getCoordinates()).get(0)).getX(), 0.01);
        assertEquals(expectedY1, ((landmark.getCoordinates()).get(0)).getY(), 0.01);
        assertEquals(expectedX2, ((landmark.getCoordinates()).get(1)).getX(), 0.01);
        assertEquals(expectedY2, ((landmark.getCoordinates()).get(1)).getY(), 0.01);

    }

    @Test
    void testConvertToLandMarkScenario2() {
        Pose pose = new Pose(15, 25, 30, 1);
        List<CloudPoint> listOfCloudPoints = new ArrayList<>();
        listOfCloudPoints.add(new CloudPoint(3, 4));
        listOfCloudPoints.add(new CloudPoint(6, 8));
        TrackedObject trackedObject = new TrackedObject("wall2", 2, "wall4", listOfCloudPoints);
        LandMark landmark = fusionSlam.convertToLandMark(trackedObject, pose);
        double expectedX1 = 15.598076211353316;
        double expectedY1 = 29.964101615137753;
        double expectedX2 = 16.196152422706632;
        double expectedY2 = 34.92820323027551;
        assertEquals(expectedX1, ((landmark.getCoordinates()).get(0)).getX(), 0.01);
        assertEquals(expectedY1, ((landmark.getCoordinates()).get(0)).getY(), 0.01);
        assertEquals(expectedX2, ((landmark.getCoordinates()).get(1)).getX(), 0.01);
        assertEquals(expectedY2, ((landmark.getCoordinates()).get(1)).getY(), 0.01);

    }
}