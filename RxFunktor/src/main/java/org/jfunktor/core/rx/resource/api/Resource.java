package org.jfunktor.core.rx.resource.api;

import org.jfunktor.core.resource.api.ResourceException;

import rx.functions.Func1;


public interface Resource<T> {

	public final String DEFAULT_ACTION = "_default_";
	
	public Action<T> defineAction(String action, Func1<T,T> function)throws ResourceException;

	public void onNext(T event);

	public void undefineAction(String action)throws ResourceException;

	public boolean isActionDefined(String action);

	public void onCompleted();


	public Action<T> getDefaultAction();

    public Action<T> getAction(String action)throws ResourceException;

	//public Subscription subscribe(String action, Observer<Event> observer)throws ResourceException;

	//public void unsubscribe(String action,Subscription subscription )throws ResourceException;
}
