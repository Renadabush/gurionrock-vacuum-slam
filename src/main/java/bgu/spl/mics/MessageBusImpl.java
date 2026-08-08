package bgu.spl.mics;

import bgu.spl.mics.application.messages.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> microServiceHashMap;
	//fix
	private ConcurrentHashMap<Class<?extends Event>,LinkedBlockingQueue<MicroService>> events;
	private ConcurrentHashMap<Class<? extends Broadcast>,LinkedBlockingQueue<MicroService>> broadcast;
	private ConcurrentHashMap<Event,Future> futures;
	private static MessageBusImpl instance;
	private MessageBusImpl(){
		microServiceHashMap=new ConcurrentHashMap<>();
		broadcast=new ConcurrentHashMap<>();
		broadcast.put(TickBroadcast.class,new LinkedBlockingQueue<>());
		broadcast.put(TerminatedBroadcast.class,new LinkedBlockingQueue<>());
		broadcast.put(CrashedBroadcast.class,new LinkedBlockingQueue<>());
		broadcast.put(FusionSlamTerminatedBroadcast.class,new LinkedBlockingQueue<>());
		broadcast.put(lidarBroadcast.class,new LinkedBlockingQueue<>());
		broadcast.put(NotifiyterminatedBroadcast.class,new LinkedBlockingQueue<>());
		events=new ConcurrentHashMap<>();
		events.put(DetectObjectsEvent.class,new LinkedBlockingQueue<>());
		events.put(PoseEvent.class,new LinkedBlockingQueue<>());
		events.put(TrackedObjectsEvent.class,new LinkedBlockingQueue<>());
		events.put(SubsicribeMicroserviceEvent.class,new LinkedBlockingQueue<>());
		futures=new ConcurrentHashMap<>();

	}
	public static MessageBusImpl getInstance(){
		if (instance == null) {
			instance=new MessageBusImpl();
		}
		return instance;
	}
	
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		(events.get(type)).offer(m);
	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		boolean offer= (broadcast.get(type)).offer(m);

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		if(futures.get(e)!=null)
			(futures.get(e)).resolve(result);

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		LinkedBlockingQueue<MicroService>output=broadcast.get(b.getClass());
		for(MicroService a:output){
			(microServiceHashMap.get(a)).offer(b);
		}


	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T>result=new Future<>();
		LinkedBlockingQueue<MicroService>output=events.get(e.getClass());
		MicroService now=output.poll();
		if(now==null)
			return null;
		else{
			(microServiceHashMap.get(now)).offer(e);
			output.offer(now);
			futures.put(e,result);
			return result;}
	}

	@Override
	public void register(MicroService m) {
		microServiceHashMap.put(m,new LinkedBlockingQueue<>());

	}

	@Override
	public void unregister(MicroService m) {
		LinkedBlockingQueue<Message> temp=microServiceHashMap.get(m);
		microServiceHashMap.remove(m);
		events.forEach((key, value) -> value.remove(m));
		broadcast.forEach((key,value)-> value.remove(m));

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
        return (microServiceHashMap.get(m)).take();
	}


}

	


