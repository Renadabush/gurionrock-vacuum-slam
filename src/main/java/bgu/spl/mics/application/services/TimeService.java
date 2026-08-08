package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.concurrent.TimeUnit;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {
    private int countTick;
    private int TickTime;
    private int Duration;
    private boolean flag;

    /**
     * Constructor for TimeService.
     *
     * @param TickTime The duration of each tick in milliseconds.
     * @param Duration The total number of ticks before the service terminates.
     */
    public TimeService(int TickTime, int Duration) {
        super("Time_service");
        countTick = 0;
        this.TickTime = TickTime;
        this.Duration = Duration;
        flag=false;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class,message->{
            try {
                Thread.sleep(TickTime * 1000L);
            } catch (InterruptedException e) {

            }
            countTick++;
            StatisticalFolder.incrementSystemRuntimeInc();
            if(countTick<Duration){
                sendBroadcast(new TickBroadcast());
            }
            else{
                sendBroadcast(new NotifiyterminatedBroadcast());
                terminate();
            }
        });
        subscribeBroadcast(FusionSlamTerminatedBroadcast.class, message->{
            terminate();
        });
        subscribeBroadcast(CrashedBroadcast.class,message->{
            terminate();
        });

    }
}
