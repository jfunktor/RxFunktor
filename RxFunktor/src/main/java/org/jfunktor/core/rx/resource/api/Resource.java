package org.jfunktor.core.rx.resource.api;

import org.jfunktor.core.events.api.Event;

import rx.Observable;

public interface Resource<T> {
	
	public Observable<T> defineAction(String action);

	public void onNext(T event);

	public void deactivateAction(String action);

	public boolean isActionActive();

	public void activateAction(String action);

	public void undefineAction(String action);

	public boolean isActionDefined();
	

}
