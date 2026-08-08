package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {
    Camera camera;

    @BeforeEach
    void setUp() {
        camera = new Camera("camera1",1,5);
        List<StampedDetectedObjects> detectedObjects = new ArrayList<>();
        List<DetectedObject> det1=new ArrayList<>();
        List<DetectedObject> det2 =new ArrayList<>();
        det1.add(new DetectedObject("1","description1"));
        det2.add(new DetectedObject("2","description2"));
        StampedDetectedObjects obj1=new StampedDetectedObjects(1,det1);
        StampedDetectedObjects obj2=new StampedDetectedObjects(2,det2);
        detectedObjects.add(obj1);
        detectedObjects.add(obj2);
        camera.setDetectedObjectsList(detectedObjects);
    }

    @Test
    void testGetFrequency() {
        assertEquals(5, camera.getFrequency());
    }

    @Test
    void testGetId() {
        assertEquals(1, camera.getId());
    }

    @Test
    void testGetDetectedObjectsList() {
        List<StampedDetectedObjects> retrievedList = camera.getDetectedObjectsList();
        assertNotNull(retrievedList);
        assertEquals(2, retrievedList.size());
        StampedDetectedObjects firstObject = retrievedList.get(0);
        assertEquals(1, firstObject.getTime());
        assertEquals("1", firstObject.getDetectedObjects().get(0).getId());
    }

    @Test
    void testGetDetectedObjectsList2() {
        List<StampedDetectedObjects> obj1=new ArrayList<>();
        camera.setDetectedObjectsList(obj1);
        List<StampedDetectedObjects> retrievedList = camera.getDetectedObjectsList();
        assertNotNull(retrievedList);
        assertTrue(retrievedList.isEmpty());
    }
}