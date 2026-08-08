# GurionRock Pro Max Ultra Over 9000 🤖🧹
### Vacuum Robot Perception & Mapping System Simulation

A Java concurrency project implementing a lightweight, ROS-inspired **microservice framework** (threads + a shared message bus with events/broadcasts) and using it to build a simulated **sensor-fusion SLAM pipeline** for a robot vacuum — cameras, LiDAR workers, pose (GPS/IMU) tracking, and a Fusion-SLAM service that builds a global landmark map.

> Built for SPL 225 — Java Generics, Concurrency, and Synchronization (Ben-Gurion University).

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Core Components](#core-components)
  - [Future\<T\>](#futuret)
  - [MessageBus](#messagebus)
  - [MicroService](#microservice)
- [Perception & Mapping Pipeline](#perception--mapping-pipeline)
  - [Sensors](#sensors)
  - [Message Flow](#message-flow)
  - [MicroServices](#microservices)
- [Data Model](#data-model)
- [Input / Output Format](#input--output-format)
- [Error Handling & Termination](#error-handling--termination)
- [Project Structure](#project-structure)
- [Building & Running](#building--running)
- [Testing](#testing)
- [Design Notes](#design-notes)

---

## Overview

Modern robot vacuums combine multiple sensors — camera, LiDAR, GPS, IMU — to map their environment and navigate safely. Managing these sensors concurrently, and fusing their data into a coherent world model, is the core challenge this project solves.

The project is split into two layers:

1. **A generic microservice framework** (`bgu.spl.mics`) — a minimal actor-like system where independent components (`MicroService`s) run on their own thread and communicate exclusively through a shared, thread-safe `MessageBus`, using **events** (point-to-point, round-robin, resolvable via `Future`) and **broadcasts** (one-to-many).
2. **A concrete application** built on top of that framework — cameras and LiDAR workers detect and track objects over discrete time "ticks," and a Fusion-SLAM service combines everything into a global map of landmarks, relative to the robot's charging station.

---

## Architecture

```
                         ┌───────────────────┐
                         │    TimeService     │  broadcasts TickBroadcast
                         └─────────┬─────────┘
                                   │
                 ┌─────────────────┼─────────────────┐
                 ▼                 ▼                 ▼
        ┌────────────────┐ ┌───────────────┐ ┌────────────────┐
        │ CameraService(s)│ │ PoseService   │ │ (other sensors) │
        └────────┬─────────┘ └──────┬────────┘ └────────────────┘
                 │ DetectObjectsEvent      │ PoseEvent
                 ▼                          │
        ┌──────────────────────┐            │
        │ LiDarWorkerService(s) │            │
        │  (queries LiDARDataBase)          │
        └──────────┬───────────┘            │
                    │ TrackedObjectsEvent    │
                    ▼                        ▼
              ┌───────────────────────────────────┐
              │           FusionSlamService         │
              │  transforms + merges landmarks      │
              └───────────────────────────────────┘
                              │
                              ▼
                     output_file.json
```

All communication passes through a single shared **MessageBus** — no MicroService is aware of any other MicroService's existence.

---

## Core Components

### `Future<T>`

A minimal promise/future implementation used to return asynchronous results from event processing.

| Method | Description |
|---|---|
| `T get()` | Blocks until the result is available, then returns it. |
| `void resolve(T result)` | Called once by the MicroService handling the event to supply the result. |
| `boolean isDone()` | Non-blocking check for completion. |
| `T get(long timeout, TimeUnit unit)` | Waits up to a given duration; returns `null` on timeout. |

### `MessageBus`

A thread-safe **singleton** responsible for:

- **Registration** — `register(MicroService)` creates a private message queue for a MicroService; `unregister(MicroService)` tears it down.
- **Subscriptions** — `subscribeEvent(Class, MicroService)` / `subscribeBroadcast(Class, MicroService)`.
- **Event dispatch** — `Future<T> sendEvent(Event<T>)` delivers the event to exactly **one** subscribed MicroService, selected in **round-robin** order across all subscribers of that event type. Returns `null` if no MicroService is subscribed.
- **Broadcast dispatch** — `sendBroadcast(Broadcast)` delivers the message to **every** subscriber of that broadcast type.
- **Completion** — `complete(Event<T>, T result)` resolves the `Future` tied to a previously sent event.
- **Consumption** — `awaitMessage(MicroService)` blocks until a message is available in the calling MicroService's queue, then returns it.

Synchronization is deliberately **minimal and targeted** — the goal is a responsive system where MicroServices are never blocked longer than necessary (e.g. per-queue locking rather than a single global lock).

### `MicroService`

An abstract base class, `Runnable`, representing one concurrent system component:

1. **`run()`** — registers with the `MessageBus`, calls the abstract `initialize()` hook (where subclasses subscribe to events/broadcasts), then enters a **message loop**: repeatedly `awaitMessage()` → dispatch to the matching registered callback → repeat until `terminate()` is signaled → `unregister()`.
2. Subclasses never touch the `MessageBus` directly — they use protected wrapper methods (`subscribeEvent`, `subscribeBroadcast`, `sendEvent`, `sendBroadcast`, `complete`, `terminate`) inherited from `MicroService`.
3. Each MicroService has a (non-unique) `name` assigned at construction.

---

## Perception & Mapping Pipeline

### Sensors

| Sensor | Simulated behavior |
|---|---|
| **Camera** | Detects objects (id + description) per tick but has **no distance information**. |
| **LiDAR** | Provides 3D point-cloud coordinates for previously detected object IDs, looked up from a `LiDARDataBase`. Simulated as running across multiple worker threads. |
| **GPS/IMU** | Reports the robot's `Pose` (x, y, yaw) at each tick, relative to the charging station. |
| **Fusion-SLAM** | Combines camera + LiDAR + pose data into globally-referenced **landmarks**, averaging repeated detections of the same object for increasing accuracy. |

### Message Flow

1. `TimeService` emits a `TickBroadcast` every tick.
2. `CameraService` — once `detection_time + camera_frequency` has elapsed — sends a `DetectObjectsEvent` to one subscribed `LiDarWorkerService` (round-robin).
3. The `LiDarWorkerService` waits until `detection_time + lidar_frequency`, looks up the corresponding cloud points in the `LiDARDataBase`, stores them, replies `true` on the event's `Future`, and sends a `TrackedObjectsEvent` to `FusionSlamService`.
4. `PoseService` sends `PoseEvent`s each tick so Fusion-SLAM knows the robot's pose history.
5. `FusionSlamService` transforms each tracked object's coordinates into the **global frame** using the robot's pose **at the object's original detection timestamp** (not the time the event arrives), and either creates a new `Landmark` or averages it into an existing one.
6. When all sensors finish (or `duration` ticks elapse, or a sensor reports an `ERROR`), the system terminates and Fusion-SLAM writes `output_file.json`.

### MicroServices

| MicroService | Sends | Subscribes to |
|---|---|---|
| `TimeService` | `TickBroadcast` | — |
| `CameraService` | `DetectObjectsEvent` | `TickBroadcast`, `TerminatedBroadcast`, `CrashedBroadcast` |
| `LiDarWorkerService` | `TrackedObjectsEvent` | `TickBroadcast`, `TerminatedBroadcast`, `CrashedBroadcast`, `DetectObjectsEvent` |
| `PoseService` | `PoseEvent` | `TickBroadcast`, `CrashedBroadcast`, `TerminatedBroadcast` |
| `FusionSlamService` | *(none)* | `TickBroadcast`, `TrackedObjectsEvent`, `PoseEvent`, `TerminatedBroadcast`, `CrashedBroadcast` |

---

## Data Model

| Class | Purpose |
|---|---|
| `Camera` | id, frequency, status (`Up`/`Down`/`Error`), list of `StampedDetectedObjects`. |
| `StampedDetectedObjects` | A timestamped batch of `DetectedObject`s. |
| `DetectedObject` | `id`, `description`. |
| `LiDarTrackerWorker` | id, frequency, status, `lastTrackedObjects`. |
| `TrackedObject` | `id`, `time`, `description`, list of `CloudPoint`. |
| `StampedCloudPoints` | `id`, `time`, raw cloud points (as `List<List<Double>>`). |
| `CloudPoint` | `x`, `y`. |
| `LiDARDataBase` | Holds all `StampedCloudPoints`, keyed for lookup by object id / time. |
| `GPSIMU` | Current tick, status, list of `Pose`. |
| `Pose` | `x`, `y`, `yaw`, `time` — robot pose relative to the charging station. |
| `FusionSLAM` | List of `Landmark`, list of historical `Pose`. |
| `Landmark` | `id`, `description`, global `CloudPoint` coordinates. |
| `StatisticalFolder` | Running counters: `systemRuntime`, `numDetectedObjects`, `numTrackedObjects`, `numLandmarks`. |

**Messages:** `DetectObjectsEvent`, `TrackedObjectsEvent`, `PoseEvent`, `TickBroadcast`, `TerminatedBroadcast`, `CrashedBroadcast`.

---

## Input / Output Format

The simulation is driven entirely by JSON:

1. **Configuration file** (passed as the program's only argument) — camera and LiDAR worker configs (id, frequency, data file paths), pose file path, `TickTime`, and `Duration`.
2. **Pose data file** — `{ time, pose: [x, y, yaw] }` entries.
3. **LiDAR data file** — `{ time, cloudPoints: [[x, y, z], ...] }` entries (the `z` coordinate is ignored).
4. **Camera data file** — per-camera `{ time, detectedObjects: [{ id, description }] }` entries.

**Output** — a single `output_file.json`, written next to the configuration file:

- On success: final `systemRuntime`, `numDetectedObjects`, `numTrackedObjects`, `numLandmarks`, and the resulting `landMarks` map.
- On error: an `Error` description, the `faultySensor`, the `lastFrames` seen by each camera/LiDAR, the full `poses` history up to the failure, and the statistics gathered so far.

---

## Error Handling & Termination

- **Normal completion** — once every sensor has exhausted its data and sent a `TerminatedBroadcast`, or once `Duration` ticks have elapsed (whichever comes first), `FusionSlamService` writes the output file and the system shuts down.
- **Sensor error** — a `DetectedObject`/cloud-point entry with id `ERROR` signals a fault (e.g. a simulated disconnection). The affected sensor sends a `CrashedBroadcast`; every other MicroService stops immediately, and the output file records the fault and system state at the moment of failure.

---

## Project Structure

```
.
├── pom.xml
└── src
    ├── main/java/bgu/spl/mics/
    │   ├── Future.java
    │   ├── Message.java / Event.java / Broadcast.java
    │   ├── MessageBus.java / MessageBusImpl.java
    │   ├── MicroService.java
    │   ├── application/
    │   │   ├── messages/         # DetectObjectsEvent, TrackedObjectsEvent, PoseEvent, ...
    │   │   ├── objects/          # Camera, LiDAR, Pose, FusionSLAM, StatisticalFolder, ...
    │   │   └── services/         # TimeService, CameraService, LiDarWorkerService,
    │   │                         # PoseService, FusionSlamService
    │   └── example/              # Framework usage example
    └── test/java/bgu/spl/mics/
        └── ...                  # JUnit tests
```

---

## Building & Running

Built with **Maven**, targeting **Java 8**.

```bash
# Build
mvn clean package

# Run
java -jar target/<artifact-name>.jar path/to/configuration_file.json
```

`output_file.json` will be written in the same directory as the configuration file.

> ⚠️ This project is developed and tested against **CS Lab UNIX machines**. Compile and run there before submitting/relying on results.

---

## Testing

Unit tests (JUnit) are located under `src/test/java` and cover, at minimum:

- **Fusion-SLAM** — the transformation of tracked objects into global-frame landmarks.
- **Camera / LiDAR** — the logic that prepares data before sending events.
- **MessageBus** — registration, subscription, round-robin event dispatch, broadcast delivery, and future resolution.

```bash
mvn test
```

---

## Design Notes

- **Concurrency-first:** each `MicroService` owns its own thread and message queue; all cross-component communication is mediated by the `MessageBus`, avoiding shared mutable state between services.
- **Minimal synchronization:** locking is scoped as narrowly as possible (e.g. per-queue rather than a single global lock) to keep the system responsive under load.
- **Round-robin fairness:** when multiple MicroServices subscribe to the same event type, work is distributed evenly across them.
- **Time-correct fusion:** Fusion-SLAM always transforms cloud points using the robot's pose *at the detection timestamp*, not the time the event was actually processed — critical for correctness under concurrent, out-of-order delivery.

---

## Authors

- Renad Abu Shareb
- Adan Abu Salok

---

*SPL 225 — Assignment 2, Ben-Gurion University. Teaching Assistants: Lotem Sakira, Ahmed Massalha.*