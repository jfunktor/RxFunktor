package org.jfunktor.core.events.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestThreadedConsumer<T> extends TestConsumer<T> {
	
	private String creationThread;
	
	private Map<String,List<T>> eventMap = new ConcurrentHashMap<String,List<T>>();
	
	public TestThreadedConsumer(){
		creationThread = Thread.currentThread().getName();
	}
	
	@Override
	public void accept(T evt){
		String threadName = Thread.currentThread().getName();
		
		List<T> events = null;
		if(eventMap.containsKey(threadName)){
			events = eventMap.get(threadName);
			events.add(evt);
		}else{
			events = new ArrayList<T>();
			events.add(evt);
			eventMap.put(threadName, events);
		}
		
	}

	@Override
	public List<T> getAccepted() {
		
		List<T> finalList = new ArrayList();
		
		eventMap.values().forEach(list->{list.forEach(event->{finalList.add(event);});});
		
		return finalList;
	}
	
	public String getCreationThread(){
		return creationThread;
	}
	
	public Map<String,List<T>> getEventMap(){
		return eventMap;
	}

	
	
	

}
