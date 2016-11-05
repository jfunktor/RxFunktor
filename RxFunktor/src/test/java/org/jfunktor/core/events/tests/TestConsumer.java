package org.jfunktor.core.events.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TestConsumer<T> implements Consumer<T> {

	private List<T> acceptedEvents = new ArrayList<T>();
	
	@Override
	public void accept(T event) {
		acceptedEvents.add(event);
		
	}
	
	public List<T> getAccepted(){
		return acceptedEvents;
	}
	
	
}
