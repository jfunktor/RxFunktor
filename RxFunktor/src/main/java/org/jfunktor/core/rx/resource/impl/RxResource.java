package org.jfunktor.core.rx.resource.impl;

import org.jfunktor.core.rx.resource.api.Resource;

import rx.Observable;

public class RxResource<T> implements Resource<T> {

	public RxResource(String resourcename, String version) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Observable<T> defineAction(String action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onNext(T event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deactivateAction(String action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isActionActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void activateAction(String action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void undefineAction(String action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isActionDefined() {
		// TODO Auto-generated method stub
		return false;
	}


}
