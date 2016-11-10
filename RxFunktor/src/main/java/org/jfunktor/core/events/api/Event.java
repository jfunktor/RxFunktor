package org.jfunktor.core.events.api;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class Event {

	private String eventName;
	private Map eventDetails;
	
	public Event(String name, Map<String, Object> params) {
		eventName = name;
		eventDetails = ImmutableMap.copyOf(params);
		
	}

	public String getEventName() {
		return eventName;
	}

	public Map getEventDetails() {
		return eventDetails;
	}

	@Override
	public String toString() {
		return "Event [eventName=" + eventName + ", eventDetails=" + eventDetails + "]";
	}
	
	
	

}