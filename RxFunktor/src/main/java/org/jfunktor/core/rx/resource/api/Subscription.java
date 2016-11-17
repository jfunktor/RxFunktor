package org.jfunktor.core.rx.resource.api;

/**
 * Created by s00085 on 16/11/2016.
 */
public interface Subscription {

    public boolean isUnsubscribed();

    public void unsubscribe();
}
