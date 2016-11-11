package org.jfunktor.core.rx.resource.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jfunktor.core.resource.api.ResourceException;
import org.jfunktor.core.rx.resource.api.Resource;
import org.jfunktor.core.events.api.Event;


import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxResource implements Resource<Event> {

	private String resource;
	private String version;
	
	private Subject<Event,Event> resourceSubject = new SerializedSubject<Event,Event>(PublishSubject.create());
	private Subject<Event,Event> defaultSubject = null;
	private Map<String,ActionEntry<Event>> actionMap = new ConcurrentHashMap<String, ActionEntry<Event>>();

	 
	private class ActionEntry<Event>{
		
		private String name;
		private boolean isActive;
		private Subject<Event,Event> actionSubject;
		private Subscription subscription;
		
		
		private ActionEntry(String name,Subject<Event,Event> actSubject,Subscription subs){
			this.name = name;
			isActive = true;
			actionSubject = actSubject;
			subscription = subs;
		}

		boolean isActive() {
			return isActive;
		}

		void setActive(boolean isActive) {
			this.isActive = isActive;
		}

		String getName() {
			return name;
		}

		Subject<Event,Event> getSubject() {
			return actionSubject;
		}


		Subscription getSubscription() {
			return subscription;
		}
		
		

		@Override
		public String toString() {
			return "ActionEntry [name=" + name + ", isActive=" + isActive + ", actionSubject=" + actionSubject
					+ ", subscription=" + subscription + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (isActive ? 1231 : 1237);
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ActionEntry other = (ActionEntry) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (isActive != other.isActive)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		private RxResource getOuterType() {
			return RxResource.this;
		}
		
		
	}

	
	
	public RxResource(String resourcename, String version) {
		resource = resourcename;
		this.version = version;
		this.defaultSubject = defineDefaultAction();
	}

	@Override
	public Observable<Event> defineAction(String action) throws ResourceException {
		
		String actionLower = action.toLowerCase();
		
		if(isActionDefined(actionLower))
			throw new ResourceException(String.format("Action %s is already defined and active", actionLower));

		//create the subject
		Subject<Event,Event> actionSubject = new SerializedSubject<Event,Event>(PublishSubject.create());
		
		//now connect to the resource subject using a filter
		Subscription actionSubscription = resourceSubject.filter(event->{
			boolean retVal = false;
			if(isActionActive(actionLower) && event.getEventName().equalsIgnoreCase(action)){
				retVal = true;
			}
			return retVal;
		}).subscribe(actionSubject);
		
		

		ActionEntry actionEntry = new ActionEntry(action,actionSubject,actionSubscription);
		
		actionMap.put(actionLower, actionEntry);
		
		return actionSubject;
	}
	
	

	@Override
	public void onNext(Event event) {
		resourceSubject.onNext(event);
		
	}

	@Override
	public void deactivateAction(String action)throws ResourceException {
		
		
		String actionLower = action.toLowerCase();
		
		if(!isActionDefined(actionLower)){
			throw new ResourceException(String.format("Action %s is not defined", action));
		}
		
		if(isActionActive(actionLower)){
			ActionEntry<Event> entry = actionMap.get(actionLower);
			entry.setActive(false);
			
		}else{
			throw new ResourceException(String.format("Action %s is already inactive", action));
		}
	}


	@Override
	public void activateAction(String action)throws ResourceException {

		String actionLower = action.toLowerCase();

		if(!isActionDefined(actionLower)){
			throw new ResourceException(String.format("Action %s is not defined", action));
		}
		
		if(!isActionActive(action)){
			ActionEntry<Event> entry = actionMap.get(actionLower);
			entry.setActive(true);
			
		}else{
			throw new ResourceException(String.format("Action %s is already active", action));
		}
		
	}

	@Override
	public void undefineAction(String action)throws ResourceException{
		String actionLower = action.toLowerCase();

		if(!isActionDefined(actionLower)){
			throw new ResourceException(String.format("Action %s is not defined", action));
		}
		
		//get the defined action subject
		ActionEntry entry = actionMap.get(actionLower);
		

		//complete the subject
		entry.getSubject().onCompleted(); //this should stop the other child subscriptions

		//then unsubscribe from the resource subject
		entry.getSubscription().unsubscribe();
		
		
		
		//remove the action from the list of action map
		actionMap.remove(actionLower);
	}

	@Override
	public boolean isActionActive(String action) {
		assert action != null : "Action name cannot be null";
		String actionLower = action.toLowerCase();
		boolean retVal = false;
		if(isActionDefined(actionLower)){
			ActionEntry<Event> entry = actionMap.get(actionLower);
			retVal = entry.isActive();
		}
		return retVal;
	}

	@Override
	public boolean isActionDefined(String action) {
		assert action != null : "Action name cannot be null";
		return actionMap.containsKey(action.toLowerCase());
	}

	@Override
	public void onCompleted() {
		resourceSubject.onCompleted();
	}

	@Override
	public Observable<Event> getDefaultAction() {
		return defaultSubject;
	}


	private Subject<Event, Event> defineDefaultAction() {
		//create the subject
		Subject<Event,Event> subject = new SerializedSubject<Event,Event>(PublishSubject.create());

		//now connect to the resource subject using a filter
		Subscription actionSubscription = resourceSubject.filter(event->{
			boolean retVal = false;
			if(!isActionDefined(event.getEventName())){
				retVal = true;
			}
			return retVal;
		}).subscribe(subject);


		return subject;
	}


	/**
	 * Wraps the given function safely
	 * Translates any unhandled exceptions in the given function to a safe funtion which translates the error into a Error Event
	 * @param in
	 * @return
	 */
	public static Func1<Event,Event> safely(Func1<Event,Event> in){
		return new Func1<Event, Event>() {
			@Override
			public Event call(Event event) {
				try{
					return in.call(event);
				}catch(Throwable anyError){
					HashMap dataMap = new HashMap();
					dataMap.put(Event.ERROR, anyError);
					Event errorEvent = new Event(Event.ERROR_EVENT,dataMap);
					return errorEvent;
				}
			}
		};
	}
}
