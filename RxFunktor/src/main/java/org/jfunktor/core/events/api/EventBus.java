package org.jfunktor.core.events.api;

import java.util.function.Consumer;

import rx.Observer;
import rx.Subscription;



public interface EventBus<T> {
	
	public enum DeliveryMode{
		SYNC,
		ASYNC
	}


	EventBus<T> unSubscribeAll();

	Subscription subscribe(String string, Consumer<T> consumer);

	EventBus<T> publish(String string, T evt);

	Subscription subscribe(String string, DeliveryMode async, Consumer<T> consumer);

	
	Subscription subscribeForDeadTopics(DeliveryMode async,Consumer<T> consumer);

	Subscription subscribeForDeadTopicsWithObserver(DeliveryMode async, Observer<T> consumer);

	Subscription subscribeWithObserver(String topic, DeliveryMode async, Observer<T> consumer);

	

}