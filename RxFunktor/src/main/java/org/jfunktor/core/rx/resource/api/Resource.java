package org.jfunktor.core.rx.resource.api;

import org.jfunktor.core.events.api.Event;
import org.jfunktor.core.resource.api.ResourceException;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.observers.TestSubscriber;


public interface Resource<T> {
	
	public Resource<T> defineAction(String action, Func1<T,T> function)throws ResourceException;

	public void onNext(T event);

	public void deactivateAction(String action)throws ResourceException;

	public boolean isActionActive(String action);

	public void activateAction(String action)throws ResourceException;

	public void undefineAction(String action)throws ResourceException;

	public boolean isActionDefined(String action);

	public void onCompleted();


	Observable<T> getDefaultAction();

    Observable<T> getAction(String action)throws ResourceException;

	Subscription subscribe(String action, Observer<Event> observer)throws ResourceException;
}
