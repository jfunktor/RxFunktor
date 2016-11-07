package org.jfunktor.core.rxresource.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfunktor.core.events.api.Event;
import org.jfunktor.core.rx.resource.api.Resource;
import org.jfunktor.core.rx.resource.impl.RxResource;
import org.junit.Test;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class ResourceTests {
	
	

	@Test
	public void test_simple_resource() {
		
		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		Resource<Event> resource = new RxResource("Flight","1.0");
		
		resource.defineAction("find").map(event->{return event;}).subscribe(subscriber);
		
		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
			
	}
	
	@Test
	public void test_simple_resource_response(){

		TestSubscriber subscriber = new TestSubscriber();
		
		Resource resource = new RxResource("Flight","1.0");
		
		resource.defineAction("find").map(event->{return event;}).subscribe(subscriber);
		
		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
		
		
	}

	@Test
	public void test_simple_resource_deactivation(){

		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		Resource resource = new RxResource("Flight","1.0");
		resource.defineAction("find").map(event->{return event;}).subscribe(subscriber);

		resource.deactivateAction("find");
		
		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),0), responseEvents.size() == 0);

		assertTrue("Resource action not deactivated",resource.isActionActive());
		
		resource.activateAction("find");

		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);

		assertTrue("Resource action not activated",resource.isActionActive());
		
	}

	@Test
	public void test_simple_resource_undefine(){

		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		Resource resource = new RxResource("Flight","1.0");
		resource.defineAction("find").map(event->{return event;}).subscribe(subscriber);

		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);

		assertTrue("Resource action is not activated",resource.isActionActive());
		assertTrue("Resource action is not defined",resource.isActionDefined());
		
		resource.undefineAction("find");

		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
		
		assertFalse("Resource action is activated",resource.isActionActive());
		assertFalse("Resource action is defined",resource.isActionDefined());
		
		//redefine the action
		resource.defineAction("find").map(evt->{return evt;}).subscribe(subscriber);
		
		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),4), responseEvents.size() == 4);
		assertTrue("Resource action is not activated",resource.isActionActive());
		assertTrue("Resource action is not defined",resource.isActionDefined());
		
	}
	
	@Test
	public void test_simple_resource_multiple_actions_response(){

		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		RxResource resource = new RxResource("Flight","1.0");
		
		resource.defineAction("find").map(event->{return event;}).subscribe(subscriber);
		
		resource.defineAction("create").map(event->{return event;}).subscribe(subscriber);
		resource.defineAction("update").map(event->{return event;}).subscribe(subscriber);
		resource.defineAction("delete").map(event->{return event;}).subscribe(subscriber);
		
		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		event = new Event("create",params);
		
		resource.onNext(event);
		
		event = new Event("update",params);
		
		resource.onNext(event);
		
		event = new Event("delete",params);
		
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),5), responseEvents.size() == 5);
		
		
	}
	
	
	@Test
	public void test_simple_resource_undefine_multiple_actions_response(){

		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		Resource resource = new RxResource("Flight","1.0");
		
		resource.defineAction("find").map(event->{return event;}).subscribe(subscriber);
		
		resource.defineAction("create").map(event->{return event;}).subscribe(subscriber);
		resource.defineAction("update").map(event->{return event;}).subscribe(subscriber);
		resource.defineAction("delete").map(event->{return event;}).subscribe(subscriber);
		
		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		event = new Event("create",params);
		
		resource.onNext(event);
		
		event = new Event("update",params);
		
		resource.onNext(event);
		
		event = new Event("delete",params);
		
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),5), responseEvents.size() == 5);
		

		//now undefine some of the actions
		resource.undefineAction("find");
		
		event = new Event("find",params);
		
		resource.onNext(event); //this should not fire the requests since find is undefined
		
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),5), responseEvents.size() == 5);
		
	}
	
	
	@Test
	public void test_simple_resource_deactivate_multiple_actions_response(){

		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		Resource resource = new RxResource("Flight","1.0");
		
		resource.defineAction("find").map(event->{return event;}).subscribe(subscriber);
		
		resource.defineAction("create").map(event->{return event;}).subscribe(subscriber);
		resource.defineAction("update").map(event->{return event;}).subscribe(subscriber);
		resource.defineAction("delete").map(event->{return event;}).subscribe(subscriber);
		
		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		event = new Event("create",params);
		
		resource.onNext(event);
		
		event = new Event("update",params);
		
		resource.onNext(event);
		
		event = new Event("delete",params);
		
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),5), responseEvents.size() == 5);
		

		//now deactivate some of the actions
		resource.deactivateAction("find");
		
		event = new Event("find",params);
		
		resource.onNext(event); //this should not fire the requests since find is de activated
		
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),5), responseEvents.size() == 5);
		

		resource.activateAction("find");
		
		event = new Event("find",params);
		
		resource.onNext(event); //this should again fire the requests since find is activated back again
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),6), responseEvents.size() == 6);
		
	}
	
	

}
