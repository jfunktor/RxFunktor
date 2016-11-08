package org.jfunktor.core.rxresource.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfunktor.core.events.api.Event;
import org.jfunktor.core.resource.api.ResourceException;
import org.jfunktor.core.rx.resource.api.Resource;
import org.jfunktor.core.rx.resource.impl.RxResource;
import org.junit.Test;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.observables.AsyncOnSubscribe;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class ResourceTests {
	
	

	@Test
	public void test_simple_resource() throws ResourceException {
		
		TestSubscriber subscriber = new TestSubscriber();
		
		Resource<Event> resource = new RxResource("Flight","1.0");
		
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
	public void test_simple_resource_response() throws ResourceException{

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
	public void test_simple_resource_deactivation() throws ResourceException{

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

		assertFalse("Resource action not deactivated",resource.isActionActive("find"));
		
		resource.activateAction("find");

		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);

		assertTrue("Resource action not activated",resource.isActionActive("find"));
		
	}

	@Test
	public void test_simple_resource_undefine() throws ResourceException{

		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		Resource resource = new RxResource("Flight","1.0");
		Subscription subscription = resource.defineAction("find").map(event->{return event;}).subscribe(subscriber);

		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);

		assertTrue("Resource action is not activated",resource.isActionActive("find"));
		assertTrue("Resource action is not defined",resource.isActionDefined("find"));
		
		//subscriber.unsubscribe();
		resource.undefineAction("find");

		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
		
		assertFalse("Resource action is activated",resource.isActionActive("find"));
		assertFalse("Resource action is defined",resource.isActionDefined("find"));
		
		
		//redefine the action
		subscription = resource.defineAction("find").map(evt->{return evt;}).subscribe(subscriber);
		
		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
		assertTrue("Resource action is not activated",resource.isActionActive("find"));
		assertTrue("Resource action is not defined",resource.isActionDefined("find"));
		
	}

	@Test
	public void test_simple_resource_activate_deactivate() throws ResourceException{

		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		Resource resource = new RxResource("Flight","1.0");
		Subscription subscription = resource.defineAction("find").map(event->{return event;}).subscribe(subscriber);

		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);

		assertTrue("Resource action is not activated",resource.isActionActive("find"));
		assertTrue("Resource action is not defined",resource.isActionDefined("find"));
		
		//subscriber.unsubscribe();
		resource.deactivateAction("find");

		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
		
		assertFalse("Resource action is activated",resource.isActionActive("find"));
		assertTrue("Resource action is defined",resource.isActionDefined("find"));
		
		
		//redefine the action
		resource.activateAction("find");
		
		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),4), responseEvents.size() == 4);
		assertTrue("Resource action is not activated",resource.isActionActive("find"));
		assertTrue("Resource action is not defined",resource.isActionDefined("find"));
		
	}

	@Test
	public void test_simple_resource_activate_deactivate_async() throws ResourceException{

		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		Resource resource = new RxResource("Flight","1.0");
		resource.defineAction("find").observeOn(Schedulers.immediate()).map(event->{return event;}).subscribe(subscriber);

		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		/*try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		subscriber.assertNoErrors();
		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);

		assertTrue("Resource action is not activated",resource.isActionActive("find"));
		assertTrue("Resource action is not defined",resource.isActionDefined("find"));
		
		//subscriber.unsubscribe();
		resource.deactivateAction("find");

		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
		
		assertFalse("Resource action is activated",resource.isActionActive("find"));
		assertTrue("Resource action is defined",resource.isActionDefined("find"));
		
		
		//redefine the action
		resource.activateAction("find");
		
		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),5), responseEvents.size() == 5);
		assertTrue("Resource action is not activated",resource.isActionActive("find"));
		assertTrue("Resource action is not defined",resource.isActionDefined("find"));
		
	}
	
	@Test
	public void test_simple_resource_multiple_actions_response() throws ResourceException{

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
	public void test_simple_resource_undefine_multiple_actions_response() throws ResourceException{

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
	public void test_simple_resource_deactivate_multiple_actions_response() throws ResourceException{

		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		Resource<Event> resource = new RxResource("Flight","1.0");
		
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
	
	@Test
	public void test_Rest_Flight_Resource_GET() throws ResourceException{
		
		TestSubscriber flightGetSubscriber = new TestSubscriber();
		
		Resource<Event> flightResource = new RxResource("Flight","1.0");
		
		//add the actions
		Observable<Event> flight_get = flightResource.defineAction("GET").map(event->{
			
			//write the logic here
			System.out.println("Received Event : "+event.getEventName());
			
			System.out.println("Details : "+event.getEventDetails());
			
			Map<String,Object> responseDetails = new HashMap();
			List<String> flights = new ArrayList<String>();
			flights.add("SK2345");
			flights.add("1345");
			responseDetails.put("flight", flights);
			
			Event responseEvent = new Event("RESPONSE",responseDetails);
			return responseEvent;
		});
		
		
		
		//now we can attach as many observers we want to the flight get
		Subscription flight_subscription = flight_get.subscribe(flightGetSubscriber);
		
		//this will usually be during the request call
		Map<String,Object> requestDetails = new HashMap();
		requestDetails.put("query", "all flights");
		
		Event requestEvent = new Event("GET",requestDetails);
		
		//this is now simulating the actual request made
		flightResource.onNext(requestEvent);
		
		//once the request is completed the observable will stop emitting
		flightResource.onCompleted();
		
		//now lets validate the responses
		flightGetSubscriber.assertCompleted();
		
		flightGetSubscriber.assertNoErrors();
		
		List<Event> responseList = flightGetSubscriber.getOnNextEvents();
		
		assertTrue(String.format("Expected response does not match actual : %s, Expected : %d",responseList.size(),1),responseList.size() == 1);
		
		responseList.forEach(evt->{
			assertTrue(String.format("Event Name is not as expected %s", evt.getEventName()),evt.getEventName().equalsIgnoreCase("RESPONSE"));
			System.out.println("Response Event "+evt);
		});
		
		
		
		
	}
	

	@Test
	public void test_Rest_Flight_Resource_GET_multiple() throws ResourceException{
		
		TestSubscriber flightGetSubscriber1 = new TestSubscriber();
		TestSubscriber flightGetSubscriber2 = new TestSubscriber();
		
		Resource<Event> flightResource = new RxResource("Flight","1.0");
		
		//add the actions
		Observable<Event> flight_get = flightResource.defineAction("GET").map(event->{
			
			//write the logic here
			System.out.println("Received Event : "+event.getEventName());
			
			System.out.println("Details : "+event.getEventDetails());
			
			Map<String,Object> responseDetails = new HashMap();
			List<String> flights = new ArrayList<String>();
			flights.add("SK2345");
			flights.add("1345");
			responseDetails.put("flight", flights);
			
			Event responseEvent = new Event("RESPONSE",responseDetails);
			return responseEvent;
		});
		
		
		
		
		
		//now we can attach as many observers we want to the flight get
		Subscription flight_subscription = flight_get.subscribe(flightGetSubscriber1);
		
		//this will usually be during the request call
		Map<String,Object> requestDetails = new HashMap();
		requestDetails.put("query", "all flights");
		
		Event requestEvent = new Event("GET",requestDetails);
		
		//this is now simulating the actual request made
		flightResource.onNext(requestEvent);
		
		//once the request is completed the observable will stop emitting
		//flightResource.onCompleted();
		
		//now lets validate the responses
		
		flightGetSubscriber1.onCompleted();
		
		flightGetSubscriber1.assertCompleted();
		flightGetSubscriber1.assertNoErrors();
		
		List<Event> responseList = flightGetSubscriber1.getOnNextEvents();
		
		assertTrue(String.format("Expected response does not match actual : %s, Expected : %d",responseList.size(),1),responseList.size() == 1);
		
		responseList.forEach(evt->{
			assertTrue(String.format("Event Name is not as expected %s", evt.getEventName()),evt.getEventName().equalsIgnoreCase("RESPONSE"));
			System.out.println("Response Event "+evt);
		});

		//simulating second request immediately
		//now we can attach as many observers we want to the flight get
		flight_subscription = flight_get.subscribe(flightGetSubscriber2);
		
		//this will usually be during the request call
		requestDetails = new HashMap();
		requestDetails.put("query", "EK1234");
		
		requestEvent = new Event("GET",requestDetails);
		
		//this is now simulating the actual request made
		flightResource.onNext(requestEvent);
		
		//once the request is completed the observable will stop emitting
		//flightResource.onCompleted();
		
		flightGetSubscriber2.onCompleted();
		//now lets validate the responses
		flightGetSubscriber2.assertCompleted();
		
		flightGetSubscriber2.assertNoErrors();
		
		responseList = flightGetSubscriber2.getOnNextEvents();
		
		assertTrue(String.format("Expected response does not match actual : %s, Expected : %d",responseList.size(),1),responseList.size() == 1);
		
		responseList.forEach(evt->{
			assertTrue(String.format("Event Name is not as expected %s", evt.getEventName()),evt.getEventName().equalsIgnoreCase("RESPONSE"));
			System.out.println("Response Event "+evt);
		});
		
		
		
	}
	
	@Test
	public void test_Rest_Flight_Resource_GET_multiple_flatmap() throws ResourceException{
		
		TestSubscriber flightGetSubscriber1 = new TestSubscriber();
		TestSubscriber flightGetSubscriber2 = new TestSubscriber();
		
		Resource<Event> flightResource = new RxResource("Flight","1.0");
		
		
		Observable<Event> flight_get = flightResource.defineAction("GET").concatMap(event->{
			
			return Observable.create(new OnSubscribe<Event>(){

				@Override
				public void call(Subscriber<? super Event> subscriber) {
					System.out.println("Received Event : "+event.getEventName()+" subscriber "+subscriber);
					
					System.out.println("Details : "+event.getEventDetails());
					
					Map<String,Object> responseDetails = new HashMap();
					List<String> flights = new ArrayList<String>();
					flights.add("SK2345");
					flights.add("1345");
					responseDetails.put("flight", flights);
					
					Event responseEvent = new Event("RESPONSE",responseDetails);
					subscriber.onStart();
					subscriber.onNext(responseEvent);
					
					subscriber.onCompleted();
					
						
				}
				
			});
		});
		
		
		//now we can attach as many observers we want to the flight get
		Subscription flight_subscription = flight_get.subscribe(flightGetSubscriber1);
		
		//this will usually be during the request call
		Map<String,Object> requestDetails = new HashMap();
		requestDetails.put("query", "all flights");
		
		Event requestEvent = new Event("GET",requestDetails);
		
		//this is now simulating the actual request made
		flightResource.onNext(requestEvent);
		
		//once the request is completed the observable will stop emitting
		//flightResource.onCompleted();
		
		//now lets validate the responses
		
		System.out.println("flightGetSubscriber1 "+flightGetSubscriber1);
		
		flightGetSubscriber1.onCompleted();
		
		flightGetSubscriber1.assertCompleted();
		flightGetSubscriber1.assertNoErrors();
		
		List<Event> responseList = flightGetSubscriber1.getOnNextEvents();
		
		assertTrue(String.format("Expected response does not match actual : %s, Expected : %d",responseList.size(),1),responseList.size() == 1);
		
		responseList.forEach(evt->{
			assertTrue(String.format("Event Name is not as expected %s", evt.getEventName()),evt.getEventName().equalsIgnoreCase("RESPONSE"));
			System.out.println("Response Event "+evt);
		});

		//simulating second request immediately
		//now we can attach as many observers we want to the flight get
		flight_subscription = flight_get.subscribe(flightGetSubscriber2);
		
		//this will usually be during the request call
		requestDetails = new HashMap();
		requestDetails.put("query", "EK1234");
		
		requestEvent = new Event("GET",requestDetails);
		
		//this is now simulating the actual request made
		flightResource.onNext(requestEvent);
		
		//once the request is completed the observable will stop emitting
		//flightResource.onCompleted();
		
		flightGetSubscriber2.onCompleted();
		//now lets validate the responses
		flightGetSubscriber2.assertCompleted();
		
		flightGetSubscriber2.assertNoErrors();
		
		responseList = flightGetSubscriber2.getOnNextEvents();
		
		assertTrue(String.format("Expected response does not match actual : %s, Expected : %d",responseList.size(),1),responseList.size() == 1);
		
		responseList.forEach(evt->{
			assertTrue(String.format("Event Name is not as expected %s", evt.getEventName()),evt.getEventName().equalsIgnoreCase("RESPONSE"));
			System.out.println("Response Event "+evt);
		});
		
		
		
	}
	
}
