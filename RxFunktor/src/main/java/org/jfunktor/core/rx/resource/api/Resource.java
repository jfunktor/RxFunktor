package org.jfunktor.core.rx.resource.api;

import org.jfunktor.core.events.api.Event;
import org.jfunktor.core.resource.api.ResourceException;

import rx.Observable;

public interface Resource<T> {
	
	public Observable<T> defineAction(String action)throws ResourceException;

	public void onNext(T event);

	public void deactivateAction(String action)throws ResourceException;

	public boolean isActionActive(String action);

	public void activateAction(String action)throws ResourceException;

	public void undefineAction(String action)throws ResourceException;

	public boolean isActionDefined(String action);

	public void onCompleted();
	

}
