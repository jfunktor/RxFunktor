package org.jfunktor.core.resource.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.jfunktor.core.events.api.Event;

import rx.Observable;

public class Resource {

	private String resourceName;
	private String resourceVersion;
	
	private HashMap<String,BiFunction<Map<String,Object>, Event, Observable>> actionMap = new HashMap();
	
	public Resource(String name, String version) {
		assert ((name != null) || (name.trim().length() > 0)) : "Resource Name cannot be null or empty";
		assert ((version != null) || (version.trim().length() > 0)) : "Resource Version cannot be null or empty";
		this.resourceName = name;
		this.resourceVersion = version;
	}

	public Resource withAction(String name, BiFunction<Map<String, Object>, Event, Observable> fn) throws ResourceException {
		assert ((name != null) || (name.trim().length() > 0)) : "Action Name cannot be null or empty";
		assert (fn != null) : "Action body cannot be null";
		if(!isSupported(name))
			actionMap.put(name.toLowerCase(), fn);
		else
			throw new ResourceException(String.format("Action %s is already defined for this resource %s", name,resourceName));
		return this;
	}

	public boolean isSupported(String name) {
		return actionMap.containsKey(name.toLowerCase());
	}

	public int getActionCount() {
		return actionMap.size();
	}

	public Observable process(HashMap<String, Object> context, Event event) throws ResourceException{
		assert ((context != null) || (context.size() > 0)) : "Context cannot be null or empty";
		assert (event != null) : "event cannot be null";
		
		//get the name of the event and look up the function to execute
		if(!isSupported(event.getEventName())){
			throw new ResourceException(String.format("Action %s is undefined for this resource %s", event.getEventName(),resourceName));
		}
		
		
		
		return actionMap.get(event.getEventName()).apply(context, event);
		
	}
	
	

}
