package org.jfunktor.core.rx.resource.impl;

import org.jfunktor.core.events.api.Event;
import org.jfunktor.core.resource.api.ResourceException;
import org.jfunktor.core.rx.resource.api.Action;
import org.jfunktor.core.rx.resource.api.Resource;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.jfunktor.core.rx.resource.api.Resource.DEFAULT_ACTION;

public class RxResource implements Resource<Event> {

	private String resource;
	private String version;
	
	private Subject<Event,Event> resourceSubject = new SerializedSubject<Event,Event>(PublishSubject.create());
	//private Subject<Event,Event> defaultSubject = null;
	private Map<String,ActionEntry<Event>> actionMap = new ConcurrentHashMap<String, ActionEntry<Event>>();
	//private Observable<Event> eventObservable;
	private Func1<Event,Event> switchFunction;

	 
	private class ActionEntry<Event> implements Action<Event> {
		
		private String name;
		private boolean isActive;
		private Func1<Event,Event> function;
		private Observable<Event> observable;
		private Map<Object, org.jfunktor.core.rx.resource.api.Subscription> subscription = new ConcurrentHashMap<>();

		
		private ActionEntry(String name,Func1<Event,Event> function,Observable<Event> obs){
			this.name = name;
			isActive = true;
			this.function = function;
			observable = obs;
		}

		@Override
		public org.jfunktor.core.rx.resource.api.Subscription subscribe(Observer<Event> subscriber) {
			//check if the subscriber is already subscribed
			if(!subscription.containsKey(subscriber)){
				//create the new subscription and return it
				Subscription rxSubscription = observable.subscribe(subscriber);

				org.jfunktor.core.rx.resource.api.Subscription subs = new SubscriptionImpl(rxSubscription);

				//update the subscription map with the observer as the key
				subscription.put(subscriber,subs);

				return subs;

			}else{
				return subscription.get(subscriber);
			}
		}

		@Override
		public boolean isActive() {
			return isActive;
		}

		@Override
		public void activate(boolean activate) {
			isActive = activate;
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
	public Resource<Event> defineAction(String action,Func1<Event,Event> actionFunction) throws ResourceException {

		assert actionFunction != null : "Action function cannot be null";
		assertActionNullOrEmpty(action);

		String actionLower = action.trim().toLowerCase();

		assertDefaultActionName(actionLower);

		defineActionInternal(actionLower, actionFunction,event->{return hasSubscribers(actionLower,event);});

		return this;
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

		Observable<Event> actionObservable = resourceSubject.filter(filterFunction).map(log(action,safely(actionFunction)));


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
		assert (!actionLower.equals(DEFAULT_ACTION)):msg;
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
		//removedEntry.getObservable().
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

	/**@Override
	public Observable<Event> getDefaultAction() {

		return actionMap.get(DEFAULT_ACTION).getObservable();
	}*/

	/**@Override
	public Observable<Event> getAction(String action) throws ResourceException{
		String actionLower = action.toLowerCase();
		if(!isActionDefined(actionLower)){
			throw new ResourceException(String.format("Action %s is not defined", action));
		}

		return actionMap.get(actionLower).getObservable();
	}*/

	@Override
	public Subscription subscribe(String action, Observer<Event> observer) throws ResourceException {
		String actionLower = action.toLowerCase();

		if(!isActionDefined(actionLower)){
			throw new ResourceException(String.format("Action %s is not defined", action));
		}

		ActionEntry<Event> actionEntry = actionMap.get(actionLower);
		if(actionEntry.hasSubscription(observer)){
			//nothing to do just respond
			throw new ResourceException(String.format("The observer %s is already subscribed",observer));
		}

		Subscription subscription = actionMap.get(actionLower).getObservable().subscribe(observer);

		actionMap.get(actionLower).addSubscriber(subscription,observer);

		return subscription;
	}

	@Override
	public void unsubscribe(String action, Subscription subscription) throws ResourceException {
		String actionLower = action.toLowerCase();

		if(!isActionDefined(actionLower)){
			throw new ResourceException(String.format("Action %s is not defined", action));
		}

		subscription.unsubscribe();
	}


	private void defineDefaultAction() {
		try {
			Func1<Event,Event> defaultAction = event -> {
				System.out.println("Default Action  : event "+event);
				return event;
			};

			Observable<Event> defaultActionObs = defineActionInternal(DEFAULT_ACTION,defaultAction,event->{
				System.out.println("Default Action Filter : "+event);
				return !hasSubscribers(event.getEventName(),event);
			});

			ActionEntry<Event> actionEntry = new ActionEntry(DEFAULT_ACTION,defaultAction,defaultActionObs);
			actionMap.put(DEFAULT_ACTION,actionEntry);

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


	/**
	 * Wraps the given function with a log call
	 * @param in
	 * @return
	 */
	public static Func1<Event,Event> log(String prefix,Func1<Event,Event> in){
		return new Func1<Event, Event>() {
			@Override
			public Event call(Event event) {
				log(String.format("%s: In Event : %s",prefix,event));
				Event outEvent = in.call(event);
				log(String.format("%s: Out Event : %s",prefix,outEvent));
				return outEvent;
			}
		};
	}

	private static void log(String msg) {
		System.out.println(msg);
	}

}
