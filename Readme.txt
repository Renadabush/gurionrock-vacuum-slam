# GurionRock Pro Max Ultra Over 9000 🤖🧹
### Vacuum Robot Perception & Mapping System Simulation

A Java concurrency project featuring a custom, ROS-inspired **microservice framework** (threads communicating via a shared, thread-safe message bus) and a simulated **sensor-fusion SLAM pipeline** built on top of it — cameras, LiDAR workers, and pose tracking feed into a Fusion-SLAM service that builds a global landmark map of the robot's environment.

Built for **SPL 225 — Java Generics, Concurrency, and Synchronization** (Ben-Gurion University).

---

## How it works

- **`Future<T>`** — a minimal promise/future used to return results from async event processing.
- **`MessageBus`** — a thread-safe singleton that routes messages between MicroServices:
  - **Events** go to exactly one subscriber, in round-robin order.
  - **Broadcasts** go to every subscriber.
- **`MicroService`** — an abstract, `Runnable` base class. Each instance runs on its own thread, registers with the bus, subscribes to message types, and processes its queue in a message loop until terminated.

On top of this framework, several MicroServices simulate the robot's sensors:

| Service | Role |
|---|---|
| `TimeService` | Emits `TickBroadcast`s to drive the global clock. |
| `CameraService` | Detects objects (no distance data) and sends `DetectObjectsEvent`. |
| `LiDarWorkerService` | Resolves detected objects into 3D coordinates and sends `TrackedObjectsEvent`. |
| `PoseService` | Reports the robot's pose (x, y, yaw) each tick via `PoseEvent`. |
| `FusionSlamService` | Fuses tracked objects + pose into global-frame landmarks, averaging repeat detections. |

The system reads JSON config/sensor files, runs the simulation tick-by-tick, and writes the final map and statistics to `output_file.json` — or an error report if a sensor fails mid-run.

---

## Build & Run

```bash
mvn clean package
java -jar target/<artifact-name>.jar path/to/configuration_file.json
```

Tested on **CS Lab UNIX machines** with **Java 8** and **Maven**.

## Testing

```bash
mvn test
```
Unit tests cover the `MessageBus`, Fusion-SLAM's coordinate transformation logic, and camera/LiDAR data preparation.

---

## Authors

- Renad Abu Shareb
- Adan Abu Salok

---

*SPL 225 — Assignment 2, Ben-Gurion University. Teaching Assistants: Lotem Sakira, Ahmed Massalha.*
