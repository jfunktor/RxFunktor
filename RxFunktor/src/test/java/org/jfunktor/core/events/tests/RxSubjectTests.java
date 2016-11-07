package org.jfunktor.core.events.tests;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

import org.jfunktor.core.events.api.Event;

public class RxSubjectTests {

	private class WrappedEvent<T>{
		
		private String channel;
		private T event;
		
		WrappedEvent(String chnl,T evt){
			channel = chnl;
			event = evt;
		}

		public String getChannel() {
			return channel;
		}

		public T getEvent() {
			return event;
		}
		
	};
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Subject<WrappedEvent<Event>,WrappedEvent<Event>> subject = new SerializedSubject<WrappedEvent<Event>,WrappedEvent<Event>>(PublishSubject.create());
		
		subject.filter(event->{
			String channel = event.getChannel();
			if((channel != null) && "Test1".equalsIgnoreCase(channel))
				return true;
			return false;}).map(evt->{return evt.getEvent();}).observeOn(Schedulers.computation()).subscribe(event->{
				System.out.println(String.format("1 : Received %s on Thread %s", event.getEventName(),Thread.currentThread().getName()));
			});

		subject.filter(event->{
			String channel = event.getChannel();
			if((channel != null) && "Test2".equalsIgnoreCase(channel))
				return true;
			return false;}).map(evt->{return evt.getEvent();}).observeOn(Schedulers.io()).subscribe(event->{
				System.out.println(String.format("2 : Received %s on Thread %s", event.getEventName(),Thread.currentThread().getName()));
			});
		
		subject.filter(event->{
			String channel = event.getChannel();
			if((channel != null) && "Test1".equalsIgnoreCase(channel))
				return true;
			return false;}).map(evt->{return evt.getEvent();}).subscribe(event->{
				System.out.println(String.format("3 : Received %s on Thread %s", event.getEventName(),Thread.currentThread().getName()));
			});
		
		Map<String, Object> params = new HashMap();
		
		Event evt = new Event("TestEvent",params);
		
		WrappedEvent wrappedEvt = new WrappedEvent("Test2",evt);

		subject.onNext(wrappedEvt);

		evt = new Event("TestEvent",params);
		
		wrappedEvt = new WrappedEvent("Test1",evt);
		
		subject.onNext(wrappedEvt);
		
	}

}
