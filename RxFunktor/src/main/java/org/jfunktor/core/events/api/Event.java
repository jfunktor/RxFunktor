package org.jfunktor.core.events.api;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class Event {

	private String eventName;
	private Map eventDetails;
	public static final String ERROR_EVENT = "Error";
	public static final String ERROR = "Error";
	public static final String EVENT_TYPE = "event-type";
	public static final String SOURCE_EVENT = "source-event";

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