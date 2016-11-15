package org.jfunktor.core.rx.resource.impl;

import org.jfunktor.core.events.api.Event;
import org.jfunktor.core.resource.api.ResourceException;
import org.jfunktor.core.rx.resource.api.Resource;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RxResource implements Resource<Event> {

	private String resource;
	private String version;
	
	private Subject<Event,Event> resourceSubject = new SerializedSubject<Event,Event>(PublishSubject.create());
	//private Subject<Event,Event> defaultSubject = null;
	private Map<String,ActionEntry<Event>> actionMap = new ConcurrentHashMap<String, ActionEntry<Event>>();
	//private Observable<Event> eventObservable;
	private Func1<Event,Event> switchFunction;
	private static final String DEFAULT_FUNCTION = "_default_";

	 
	private class ActionEntry<Event>{
		
		private String name;
		private boolean isActive;
		private Func1<Event,Event> function;
		private Observable<Event> observable;

		
		private ActionEntry(String name,Func1<Event,Event> function,Observable<Event> obs){
			this.name = name;
			isActive = true;
			this.function = function;
			observable = obs;
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

		public Func1<Event, Event> getFunction() {
			return function;
		}

		public Observable<Event> getObservable() {
			return observable;
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
		defineDefaultAction();
		switchFunction = createSwitchFunction();
	}

	private Func1<Event,Event> createSwitchFunction() {

		return safely(event->{

			Func1<Event,Event> functionToInvoke = null;
			String eventLower = event.getEventName().toLowerCase();
			ActionEntry<Event> actionEntry = actionMap.get(eventLower);
			Event resultEvent = actionEntry.getFunction().call(event);
			return resultEvent;
			/*if(actionEntry != null && actionEntry.isActive()){ //means defined and active
				//get the action and invoke it
				functionToInvoke = actionEntry.getFunction();
			}else{
				ActionEntry<Event> defaultActionEntry = actionMap.get(DEFAULT_FUNCTION);
				if(defaultActionEntry != null && defaultActionEntry.isActive()){
					functionToInvoke = actionEntry.getFunction();
				}
			}

			if(functionToInvoke != null){
				Event resultEvent = functionToInvoke.call(event);
				return resultEvent;
			}else{
				throw new IllegalStateException(String.format("Resource %s-%s is in an illegal state. No function to handle event %s",resource,version,event));
			}*/
		});
	}

	@Override
	public Observable<Event> defineAction(String action,Func1<Event,Event> actionFunction) throws ResourceException {

		assert actionFunction != null : "Action function cannot be null";
		assertActionNullOrEmpty(action);

		String actionLower = action.trim().toLowerCase();

		assertDefaultActionName(actionLower);

		return defineActionInternal(actionLower, actionFunction,event->{return hasSubscribers(actionLower,event);});
	}

	private boolean hasSubscribers(String action,Event event){
		boolean retVal = false;
		if (isActionActive(action) && event.getEventName().equalsIgnoreCase(action)) {
			retVal = true;
		}
		return retVal;
	}

	private Observable<Event> defineActionInternal(String action, Func1<Event, Event> actionFunction,Func1<Event,Boolean> filterFunction) throws ResourceException {
		if(isActionDefined(action))
			throw new ResourceException(String.format("Action %s is already defined and active", action));

		Observable<Event> actionObservable = resourceSubject.filter(filterFunction).map(switchFunction);

		ActionEntry actionEntry = new ActionEntry(action,actionFunction,actionObservable);

		actionMap.put(action, actionEntry);

		return actionObservable;
	}

	private void assertActionNullOrEmpty(String action) {
		assert (action != null || action.trim().length() > 0) : "Action name cannot be null or empty";
	}

	private void assertDefaultActionName(String actionLower) {
		assertDefaultActionName(actionLower,String.format("Action name %s is invalid. Please use some other name!",actionLower));
	}

	private void assertDefaultActionName(String actionLower,String msg) {
		assert (!actionLower.equals(DEFAULT_FUNCTION)):msg;
	}

	@Override
	public void onNext(Event event) {
		resourceSubject.onNext(event);
		
	}

	@Override
	public void deactivateAction(String action)throws ResourceException {
		
		assertActionNullOrEmpty(action);

		String actionLower = action.toLowerCase();

		assertDefaultActionName(actionLower,String.format("Action %s cannot be deactivated!",actionLower));

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

		assertActionNullOrEmpty(action);


		String actionLower = action.toLowerCase();

		assertDefaultActionName(actionLower);

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
		
		//remove the action from the list of action map
		ActionEntry<Event> removedEntry = actionMap.remove(actionLower);

		//now unsubscribe the observable from source
		removedEntry.getObservable().unsubscribeOn(Schedulers.immediate());
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

		return actionMap.get(DEFAULT_FUNCTION).getObservable();
	}

	@Override
	public Observable<Event> getAction(String action) throws ResourceException{
		String actionLower = action.toLowerCase();
		if(!isActionDefined(actionLower)){
			throw new ResourceException(String.format("Action %s is not defined", action));
		}

		return actionMap.get(actionLower).getObservable();
	}


	private void defineDefaultAction() {
		try {
			Func1<Event,Event> defaultAction = event -> {
				return event;
			};

			Observable<Event> defaultActionObs = defineActionInternal(DEFAULT_FUNCTION,defaultAction,event->{return !hasSubscribers(event.getEventName(),event);});

			ActionEntry<Event> actionEntry = new ActionEntry(DEFAULT_FUNCTION,defaultAction,defaultActionObs);
			actionMap.put(DEFAULT_FUNCTION,actionEntry);

		}catch (Exception e){
			throw new RuntimeException("Unable to define default action. Something seriously wrong!",e);
		}
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
