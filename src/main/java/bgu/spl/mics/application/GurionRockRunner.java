package bgu.spl.mics.application;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
        public static void main(String[] args) {
            // Ensure input file is provided
            if (args.length == 0) {
                System.err.println("No input file specified.");
                return;
            }

            File inputFile = new File(args[0]);

            // Get the parent directory of the input file (base directory for relative paths)
            String inputDirectory = inputFile.getParent();
            Path inputDirPath = Paths.get(inputDirectory).toAbsolutePath(); // Ensure it's absolute

            // Set the output paths for the output files
            FusionSlam.getInstance().setOutputPath(inputDirPath.resolve("output_file.json").toString());
            FusionSlam.getInstance().setErrorOutputPath(inputDirPath.resolve("OutputError.json").toString());

            // Step 1: Read the configuration file
            try (FileReader reader = new FileReader(inputFile)) {
                Gson gson = new Gson();
                Configuration config = gson.fromJson(reader, Configuration.class);
                (new Thread(new FusionSlamService(FusionSlam.getInstance()))).start();
                (new Thread(new TimeService(config.getTickTime(), config.getDuration()))).start();

                // Step 2: Read the camera data file (ensure the correct paths are resolved)
                FileReader cameraDataReader = new FileReader(inputDirPath.resolve(config.getCameras().getCameraDatasPath()).toFile());
                Type cameraDataType = new TypeToken<Map<String, ArrayList<StampedDetectedObjects>>>() {}.getType();
                Map<String, ArrayList<StampedDetectedObjects>> cameraData = gson.fromJson(cameraDataReader, cameraDataType);
                cameraDataReader.close();

                // Step 3: Map the camera data to the Camera objects
                List<Camera> cameras = config.getCameras().getCamerasConfigurations();
                for (Camera camera : cameras) {
                    List<StampedDetectedObjects> detectedObjects = cameraData.get(camera.getCamerakey());
                    if (detectedObjects != null) {
                        camera.setDetectedObjectsList(detectedObjects);
                    }
                }
                for (Camera camera : cameras) {
                    (new Thread(new CameraService(camera))).start();
                }
                // Step 5: Read LiDar data file (resolve the correct path)
                FileReader lidarDataReader = new FileReader(inputDirPath.resolve(config.getLiDarWorkers().getLidarsDataPath()).toFile());
                Type lidarDataType = new TypeToken<List<StampedCloudPoints>>(){}.getType();
                List<StampedCloudPoints> lidarData = gson.fromJson(lidarDataReader, lidarDataType);
                lidarDataReader.close();

                for (LiDarWorkerTracker l : config.getLiDarWorkers().getLidarConfigurations()) {
                    l.setLastTrackedObjects(new ArrayList<>());
                }
                List<LiDarWorkerTracker> liDarWorkerTrackers = config.getLiDarWorkers().getLidarConfigurations();
                LiDarDataBase liDarDataBase = LiDarDataBase.getInstance(lidarData);
                for (LiDarWorkerTracker lidar : liDarWorkerTrackers) {
                    (new Thread(new LiDarService(lidar, liDarDataBase))).start();
                }
                FileReader posesList = new FileReader(inputDirPath.resolve(config.getPoseJsonFile()).toFile());
                Type posesDataType = new TypeToken<List<Pose>>(){}.getType();
                List<Pose> poseData = gson.fromJson(posesList, posesDataType);
                posesList.close();
                GPSIMU gpsimu = new GPSIMU(poseData);
                (new Thread(new PoseService(gpsimu))).start();


            } catch (IOException e) {
                e.printStackTrace();
            }
            (new Thread(new Open())).start();


        }
    }


