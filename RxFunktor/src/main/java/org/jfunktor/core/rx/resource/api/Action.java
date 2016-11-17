package org.jfunktor.core.rx.resource.api;

import rx.Observer;

/**
 * Created by s00085 on 16/11/2016.
 */
public interface Action<T> {

    public Subscription subscribe(Observer<T> subscriber);

    public boolean isActive();

    public void activate(boolean activate);


}
