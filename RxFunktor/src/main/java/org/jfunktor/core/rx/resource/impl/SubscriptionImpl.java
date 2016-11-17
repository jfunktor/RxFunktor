package org.jfunktor.core.rx.resource.impl;

import org.jfunktor.core.events.api.Event;
import org.jfunktor.core.rx.resource.api.Action;
import rx.Observer;
import rx.Subscription;

/**
 * Created by vj on 17/11/16.
 */
public class SubscriptionImpl implements org.jfunktor.core.rx.resource.api.Subscription {

    private Subscription subs;
    private Action<Event> action;
    private Observer<Event> subscriber;

    public  SubscriptionImpl(Action action, Subscription rxSubscription, Observer<Event> subscriber) {
        this.action = action;
        this.subscriber = subscriber;
        subs = rxSubscription;
    }


    @Override
    public boolean isUnsubscribed() {
        return subs.isUnsubscribed();
    }

    @Override
    public void unsubscribe() {

        action.unsubscribe(subscriber);
        subs.unsubscribe();
    }
}
