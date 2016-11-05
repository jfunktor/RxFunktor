package org.jfunktor.core.events.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.jfunktor.core.events.api.EventBus;

import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxBus<T> implements EventBus<T> {
	
	enum SubscribeAction{
		subscribe,unsubscribe;
	}
	
	private class WrappedEvent<T>{
		
		private String topic;
		private T event;
		
		WrappedEvent(String channel,T evt){
			topic = channel;
			event = evt;
		}

		public String getTopic() {
			return topic;
		}

		public T getEvent() {
			return event;
		}
		
	};
	
	private class WrappedSubscription implements Subscription{

		private Subscription delegate;
		private String topic;
		
		WrappedSubscription(Subscription subscription,String topic){
			delegate = subscription;
			this.topic = topic;
		}
		
		@Override
		public void unsubscribe() {
			System.out.println("Unsubscribe for topic "+topic);
			updateTopic(topic,SubscribeAction.unsubscribe);
			
			//now delegate to actual subscription
			delegate.unsubscribe();
		}

		@Override
		public boolean isUnsubscribed() {
			return delegate.isUnsubscribed();
		}
		
	};
	
	private String busName;
	private Subject<WrappedEvent<T>,WrappedEvent<T>> subject = new SerializedSubject<WrappedEvent<T>,WrappedEvent<T>>(PublishSubject.create());
	
	private Map<String,AtomicInteger> topicMap = new ConcurrentHashMap<>();
	
	public RxBus(String name){
		busName = name;
	}
	
	
	@Override
	public EventBus<T> unSubscribeAll() {
		subject.onCompleted();
		unSubscribeTopics();
		return this;
	}

	private void unSubscribeTopics() {
		topicMap.clear();
	}


	@Override
	public Subscription subscribe(String topic, Consumer<T> consumer) {
		return this.subscribe(topic, DeliveryMode.SYNC, consumer);
	}

	@Override
	public EventBus<T> publish(String topic, T evt) {
		WrappedEvent wrap = new WrappedEvent(topic,evt);
		subject.onNext(wrap);
		return this;
	}

	
	@Override
	public Subscription subscribe(String topic, DeliveryMode async, Consumer<T> consumer) {

		return this.subscribeWithObserver(topic,async,new Observer<T>() {

			@Override
			public void onCompleted() {
			}

			@Override
			public void onError(Throwable e) {
			}

			@Override
			public void onNext(T event) {
				consumer.accept(event);
			}


		});
	}


	private void updateTopic(String topic, SubscribeAction unsubscribe) {
		String topicLower = topic.toLowerCase();
		if(SubscribeAction.subscribe == unsubscribe){
			//subscription related logic
			topicMap.get(topicLower).incrementAndGet();
		}else{
			//unsubscribe related logic
			if(topicMap.get(topicLower).get() > 0){
				
				int val = topicMap.get(topicLower).decrementAndGet();
				System.out.println("Unsubscribe for topic "+topic+" with val post update "+val);
			}
		}
	}


	private void defineTopic(String topic) {
		topicMap.put(topic.toLowerCase(), new AtomicInteger(1));
	}


	private boolean isTopicDefined(String topic) {
		
		boolean retVal = false;
		String topicLower = topic.toLowerCase();
		if(topicMap.containsKey(topicLower))
			if(topicMap.get(topicLower).get() > 0)
				retVal = true;
		
		return retVal;
	}


	@Override
	public Subscription subscribeForDeadTopics(EventBus.DeliveryMode async,
			Consumer<T> consumer) {
		return this.subscribeForDeadTopicsWithObserver(async,new Observer<T>() {

			@Override
			public void onCompleted() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onError(Throwable e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onNext(T event) {
				consumer.accept(event);
			}


		});
	}


	@Override
	public Subscription subscribeForDeadTopicsWithObserver(EventBus.DeliveryMode async,
			Observer<T> consumer) {
		return subject.filter(event->{
			return !isTopicDefined(event.getTopic());
		})
		.map(evt->{return evt.getEvent();})
		.observeOn(async == DeliveryMode.ASYNC ? Schedulers.io() : Schedulers.immediate())
		.subscribe(consumer);
	}


	@Override
	public Subscription subscribeWithObserver(String topic, EventBus.DeliveryMode async,
			Observer<T> consumer) {
		Subscription subscription = subject.filter(event->{return event.getTopic().equalsIgnoreCase(topic)?true:false;})
		.map(evt->{return evt.getEvent();})
		.observeOn(async == DeliveryMode.ASYNC ? Schedulers.io() : Schedulers.immediate())
		.subscribe(consumer);
	
		//add the topic to the topic map
		if(isTopicDefined(topic)){
			updateTopic(topic,SubscribeAction.subscribe);
		}else{
			defineTopic(topic);
		}
		
		return new WrappedSubscription(subscription,topic);
	}
	
	

}
