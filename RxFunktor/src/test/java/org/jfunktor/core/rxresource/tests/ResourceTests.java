package org.jfunktor.core.rxresource.tests;

import org.jfunktor.core.events.api.Event;
import org.jfunktor.core.resource.api.ResourceException;
import org.jfunktor.core.rx.resource.api.Action;
import org.jfunktor.core.rx.resource.api.Resource;
import org.jfunktor.core.rx.resource.impl.RxResource;
import static org.jfunktor.core.rx.resource.impl.RxResource.safely;

import org.junit.Test;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.Subscription;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResourceTests {
	
	

	@Test
	public void test_simple_resource() throws ResourceException {
		
		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		Resource<Event> resource = new RxResource("Flight","1.0");
		
		resource.defineAction("find",event->{return event;}).subscribe(subscriber);


		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
	
			
	}

	@Test
	public void test_simple_resource_immutable_event() throws ResourceException {
		
		TestSubscriber subscriber = new TestSubscriber();
		
		Resource<Event> resource = new RxResource("Flight","1.0");
		
		resource.defineAction("find",event->{ event.getEventDetails().put("REQUESTOR", "RAM");return event;}).subscribe(subscriber);
		
		Map params = new HashMap();
		params.put("REQUESTOR", "SAM");

		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		
		
		Event event2 = new Event("find",params);

		resource.onNext(event2);

		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Error Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);

			
	}

	/*@Test(expected=UnsupportedOperationException.class)
	public void test_simple_resource_immutable_event_on_error_return() throws Throwable {
		
		TestSubscriber subscriber = new TestSubscriber();
		
		Resource<Event> resource = new RxResource("Flight","1.0");
		
		Action<Event> findAction = resource.defineAction("find", event->{
		
			event.getEventDetails().put("REQUESTOR", "RAM");
			return event;
			
		});


		findAction.onErrorReturn(error->{
			HashMap dataMap = new HashMap();
			dataMap.put("Error", error);
			Event errorEvent = new Event("Error",dataMap);
			return errorEvent;
		}).subscribe(subscriber);
		
		Map params = new HashMap();
		params.put("REQUESTOR", "SAM");

		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		
		
		Event event2 = new Event("find",params);

		resource.onNext(event2);

		
		subscriber.assertNoErrors();
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
		
		//check whether we received any error events
		Event errorEvent = responseEvents.get(0);
		
		assertTrue(String.format("Event Response does not match expected event Actual %s, Expected %s", "Error",errorEvent.getEventName()),errorEvent.getEventName().equals("Error"));

		//here get the error and throw it to make the test pass as expected
		throw (Throwable)errorEvent.getEventDetails().get("Error");
			
	}*/

	/*@Test
	public void test_simple_resource_immutable_event_on_error_resume() throws Throwable {
		
		TestSubscriber subscriber = new TestSubscriber();
		
		Resource<Event> resource = new RxResource("Flight","1.0");
		
		resource.defineAction("find",event->{
		
			event.getEventDetails().put("REQUESTOR", "RAM");
			return event;
			
		});

        resource.onErrorResumeNext("find",error->{
			return Observable.create(subs->{
				HashMap dataMap = new HashMap();
				dataMap.put("Error", error);
				Event errorEvent = new Event("Error",dataMap);
				subs.onNext(errorEvent);
			});
		}).subscribe(subscriber);
		
		Map params = new HashMap();
		params.put("REQUESTOR", "SAM");

		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		
		
		Event event2 = new Event("find",params);

		resource.onNext(event2);

		
		subscriber.assertNoErrors();
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
		
		//check whether we received any error events
		Event errorEvent = responseEvents.get(0);
		
		assertTrue(String.format("Event Response does not match expected event Actual %s, Expected %s", "Error",errorEvent.getEventName()),errorEvent.getEventName().equals("Error"));

			
	}*/

	@Test
	public void test_simple_resource_immutable_event_on_exception_resume() throws Throwable {

		TestSubscriber subscriber = new TestSubscriber();

		Resource<Event> resource = new RxResource("Flight","1.0");



		resource.defineAction("find",event->{
            try {
                event.getEventDetails().put("REQUESTOR", "RAM");
                return event;
            }catch(Throwable error){
                HashMap dataMap = new HashMap();
                dataMap.put("Error", error);
                Event errorEvent = new Event("Error",dataMap);
                return errorEvent;
            }

		}).subscribe(subscriber);

		Map params = new HashMap();
		params.put("REQUESTOR", "SAM");


		Event event = new Event("find",params);

		resource.onNext(event);


		Event event2 = new Event("find",params);

		resource.onNext(event2);


		subscriber.assertNoErrors();
		List<Event> responseEvents = subscriber.getOnNextEvents();

		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);


		//check whether we received any error events
        responseEvents.forEach(errorEvent->{
            assertTrue(String.format("Event Response does not match expected event Actual %s, Expected %s", "Error",errorEvent.getEventName()),errorEvent.getEventName().equals("Error"));
        });


	}

    @Test
    public void test_simple_resource_immutable_event_on_exception_resume_with_safely() throws Throwable {

        TestSubscriber subscriber = new TestSubscriber();

        Resource<Event> resource = new RxResource("Flight","1.0");



        resource.defineAction("find",safely(event->{
            event.getEventDetails().put("REQUESTOR", "RAM");
            return event;
        })).subscribe(subscriber);

        Map params = new HashMap();
        params.put("REQUESTOR", "SAM");


        Event event = new Event("find",params);

        resource.onNext(event);


        Event event2 = new Event("find",params);

        resource.onNext(event2);


        subscriber.assertNoErrors();
        List<Event> responseEvents = subscriber.getOnNextEvents();

        assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);


        //check whether we received any error events
        responseEvents.forEach(errorEvent->{
            assertTrue(String.format("Event Response does not match expected event Actual %s, Expected %s", "Error",errorEvent.getEventName()),errorEvent.getEventName().equals("Error"));
        });


    }

	@Test
	public void test_simple_resource_response() throws ResourceException{

		TestSubscriber subscriber = new TestSubscriber();
		
		Resource resource = new RxResource("Flight","1.0");
		
		resource.defineAction("find",event->{return event;}).subscribe(subscriber);
		
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
		Action<Event> findAction = resource.defineAction("find",event->{return event;});

        findAction.subscribe(subscriber);

		//resource.deactivateAction("find");
        findAction.activate(false);
		
		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),0), responseEvents.size() == 0);

		assertFalse("Resource action not deactivated",findAction.isActive());
		
		//resource.activateAction("find");

        findAction.activate(true);

		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);

		//assertTrue("Resource action not activated",resource.isActionActive("find"));
        assertTrue("Resource action not activated",findAction.isActive());
	}

	@Test
	public void test_simple_resource_undefine() throws ResourceException{

		TestSubscriber<Event> subscriber = new TestSubscriber();
		TestSubscriber<Event> defaultSubscriber = new TestSubscriber();
		
		Resource resource = new RxResource("Flight","1.0");
        org.jfunktor.core.rx.resource.api.Subscription findActionSubscription = resource.defineAction("find", event -> {
            return event;
        }).subscribe(subscriber);

        org.jfunktor.core.rx.resource.api.Subscription defaultSubscription = resource.getDefaultAction().subscribe(defaultSubscriber);

		Map params = new HashMap();
		params.put("id","1");
		
		Event event = new Event("find",params);
		
		resource.onNext(event);

		params = new HashMap();
		params.put("id","2");

		event = new Event("find",params);

		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);

		assertTrue("Resource action is not activated",resource.getAction("find").isActive());
		assertTrue("Resource action is not defined",resource.isActionDefined("find"));
		
		//subscriber.unsubscribe();
		resource.undefineAction("find");

		params = new HashMap();
		params.put("id","3");

		event = new Event("find",params);
		
		resource.onNext(event);

		params = new HashMap();
		params.put("id","4");

		event = new Event("find",params);

		resource.onNext(event);
		
		subscriber.assertNoErrors();
		defaultSubscriber.assertNoErrors();

		responseEvents = subscriber.getOnNextEvents();

		List<Event> defaultResponseEvents = defaultSubscriber.getOnNextEvents();

		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
		assertTrue(String.format("Default Response does not match expected Actual %d, Expected %d", defaultResponseEvents.size(),2), defaultResponseEvents.size() == 2);

		assertFalse("Resource action is defined",resource.isActionDefined("find"));
		
		
		//redefine the action
        findActionSubscription = resource.defineAction("find",evt->{return evt;}).subscribe(subscriber);

		params = new HashMap();
		params.put("id","5");

		event = new Event("find",params);
		
		resource.onNext(event);

		params = new HashMap();
		params.put("id","6");

		event = new Event("find",params);

		resource.onNext(event);
		
		subscriber.assertNoErrors();
		defaultSubscriber.assertNoErrors();

		responseEvents = subscriber.getOnNextEvents();

		defaultResponseEvents = defaultSubscriber.getOnNextEvents();

		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
		assertTrue(String.format("Default Response does not match expected Actual %d, Expected %d", defaultResponseEvents.size(),2), defaultResponseEvents.size() == 2);
		assertTrue("Resource action is not activated",resource.getAction("find").isActive());
		assertTrue("Resource action is not defined",resource.isActionDefined("find"));

        findActionSubscription.unsubscribe();
		defaultSubscription.unsubscribe();
	}

	@Test
	public void test_simple_resource_activate_deactivate() throws ResourceException{

		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		Resource resource = new RxResource("Flight","1.0");
		org.jfunktor.core.rx.resource.api.Subscription subscription = resource.defineAction("find", event->{return event;}).subscribe(subscriber);

		Map params = new HashMap();
		
		Event event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		List<Event> responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);

		assertTrue("Resource action is not activated",resource.getAction("find").isActive());
		assertTrue("Resource action is not defined",resource.isActionDefined("find"));
		
		//subscriber.unsubscribe();
		resource.getAction("find").activate(false);

		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
		
		assertFalse("Resource action is activated",resource.getAction("find").isActive());
		assertTrue("Resource action is defined",resource.isActionDefined("find"));
		
		
		//redefine the action
		resource.getAction("find").activate(true);
		
		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),4), responseEvents.size() == 4);
		assertTrue("Resource action is not activated",resource.getAction("find").isActive());
		assertTrue("Resource action is not defined",resource.isActionDefined("find"));
		
	}

	@Test
	public void test_simple_resource_activate_deactivate_async() throws ResourceException{

		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		Resource resource = new RxResource("Flight","1.0");
		resource.defineAction("find",event->{return event;}).subscribe(subscriber);

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

		assertTrue("Resource action is not activated",resource.getAction("find").isActive());
		assertTrue("Resource action is not defined",resource.isActionDefined("find"));
		
		//subscriber.unsubscribe();
		resource.getAction("find").activate(false);

		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),2), responseEvents.size() == 2);
		
		assertFalse("Resource action is activated",resource.getAction("find").isActive());
		assertTrue("Resource action is defined",resource.isActionDefined("find"));
		
		
		//redefine the action
        resource.getAction("find").activate(true);
		
		event = new Event("find",params);
		
		resource.onNext(event);
		resource.onNext(event);
		resource.onNext(event);
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),5), responseEvents.size() == 5);
		assertTrue("Resource action is not activated",resource.getAction("find").isActive());
		assertTrue("Resource action is not defined",resource.isActionDefined("find"));
		
	}
	
	@Test
	public void test_simple_resource_multiple_actions_response() throws ResourceException{

		TestSubscriber<Event> subscriber = new TestSubscriber();
		
		RxResource resource = new RxResource("Flight","1.0");
		
		resource.defineAction("find",event->{return event;}).subscribe(subscriber);
		
		resource.defineAction("create",event->{return event;}).subscribe(subscriber);
		resource.defineAction("update",event->{return event;}).subscribe(subscriber);
		resource.defineAction("delete",event->{return event;}).subscribe(subscriber);
		
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
		
		resource.defineAction("find",event->{return event;}).subscribe(subscriber);
		
		resource.defineAction("create",event->{return event;}).subscribe(subscriber);
		resource.defineAction("update",event->{return event;}).subscribe(subscriber);
		resource.defineAction("delete",event->{return event;}).subscribe(subscriber);
		
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
		
		resource.defineAction("find",event->{return event;}).subscribe(subscriber);
		
		resource.defineAction("create",event->{return event;}).subscribe(subscriber);
		resource.defineAction("update",event->{return event;}).subscribe(subscriber);
		resource.defineAction("delete",event->{return event;}).subscribe(subscriber);
		
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
		resource.getAction("find").activate(false);
		
		event = new Event("find",params);
		
		resource.onNext(event); //this should not fire the requests since find is de activated
		
		
		subscriber.assertNoErrors();
		
		responseEvents = subscriber.getOnNextEvents();
		
		assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),5), responseEvents.size() == 5);


        resource.getAction("find").activate(true);
		
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
		flightResource.defineAction("GET",event->{
			
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

        //Observable<Event> flight_get = flightResource.getAction("GET");

                //now we can attach as many observers we want to the flight get
		org.jfunktor.core.rx.resource.api.Subscription flight_subscription = flightResource.getAction("GET").subscribe(flightGetSubscriber);
		
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
		flightResource.defineAction("GET",event->{
			
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


        //Observable<Event> flight_get = flightResource.getAction("GET");


                //now we can attach as many observers we want to the flight get
        org.jfunktor.core.rx.resource.api.Subscription flight_subscription = flightResource.getAction("GET").subscribe(flightGetSubscriber1);
		
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
		flight_subscription = flightResource.getAction("GET").subscribe(flightGetSubscriber2);
		
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
	
/*	@Test
	public void test_Rest_Flight_Resource_GET_multiple_flatmap() throws ResourceException{
		
		TestSubscriber flightGetSubscriber1 = new TestSubscriber();
		TestSubscriber flightGetSubscriber2 = new TestSubscriber();
		
		Resource<Event> flightResource = new RxResource("Flight","1.0");
		
		
		Observable<Event> flight_get = flightResource.defineAction("GET",event->{
			
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
		
		//flightGetSubscriber1.onCompleted();
		
		//flightGetSubscriber1.assertCompleted();
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
		flightResource.onNext(requestEvent);
		//once the request is completed the observable will stop emitting
		//flightResource.onCompleted();
		
		//flightGetSubscriber2.onCompleted();
		//now lets validate the responses
		//flightGetSubscriber2.assertCompleted();
		
		flightGetSubscriber2.assertNoErrors();
		
		responseList = flightGetSubscriber2.getOnNextEvents();
		
		assertTrue(String.format("Expected response does not match actual : %s, Expected : %d",responseList.size(),2),responseList.size() == 2);
		
		responseList.forEach(evt->{
			assertTrue(String.format("Event Name is not as expected %s", evt.getEventName()),evt.getEventName().equalsIgnoreCase("RESPONSE"));
			System.out.println("Response Event "+evt);
		});
		
		
		
	}

	@Test
	public void test_Rest_Flight_Resource_GET_multiple_flatmap_onComplete() throws ResourceException{

		TestSubscriber flightGetSubscriber1 = new TestSubscriber();
		TestSubscriber flightGetSubscriber2 = new TestSubscriber();

		Resource<Event> flightResource = new RxResource("Flight","1.0");


		Observable<Event> flight_get = flightResource.defineAction("GET").flatMap(event->{

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

		//flightGetSubscriber1.onCompleted();

		//flightGetSubscriber1.assertCompleted();
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
		flightResource.onNext(requestEvent);
		//once the request is completed the observable will stop emitting
		//flightResource.onCompleted();

		//flightGetSubscriber2.onCompleted();
		//now lets validate the responses
		//flightGetSubscriber2.assertCompleted();

		flightGetSubscriber2.assertNoErrors();

		responseList = flightGetSubscriber2.getOnNextEvents();

		assertTrue(String.format("Expected response does not match actual : %s, Expected : %d",responseList.size(),2),responseList.size() == 2);

		responseList.forEach(evt->{
			assertTrue(String.format("Event Name is not as expected %s", evt.getEventName()),evt.getEventName().equalsIgnoreCase("RESPONSE"));
			System.out.println("Response Event "+evt);
		});

		flightResource.onCompleted();
		flightGetSubscriber2.assertCompleted();
		flightGetSubscriber1.assertCompleted();

	}

	@Test
	public void test_Rest_Flight_Resource_default_actions() throws ResourceException {

		TestSubscriber defaultSubscriber = new TestSubscriber();

		TestSubscriber flightPostSubscriber = new TestSubscriber();

		Resource<Event> flightResource = new RxResource("Flight","1.0");

		Observable<Event> flight_post = flightResource.defineAction("POST").flatMap(event->{

			return Observable.create(new OnSubscribe<Event>(){

				@Override
				public void call(Subscriber<? super Event> subscriber) {
					System.out.println("Received Event : "+event.getEventName()+" subscriber "+subscriber);

					System.out.println("Details : "+event.getEventDetails());

					Map<String,Object> responseDetails = new HashMap();
					List<String> flights = new ArrayList<String>();
					flights.add("SK2345 Created");
					flights.add("1345 Created");
					responseDetails.put("flight", flights);

					Event responseEvent = new Event("RESPONSE",responseDetails);
					subscriber.onStart();
					subscriber.onNext(responseEvent);

					subscriber.onCompleted();


				}

			});
		});

		Subscription flightPostSubscription = flight_post.subscribe(flightPostSubscriber);

		Observable<Event> flight_defaults = flightResource.getDefaultAction();

		Subscription subscription = flight_defaults.subscribe(defaultSubscriber);

		//this will usually be during the request call
		Map<String,Object> requestDetails = new HashMap();
		requestDetails.put("query", "all flights");

		Event requestEvent1 = new Event("undefined_action",requestDetails);
		Event requestEvent2 = new Event("GET",requestDetails);
		Event requestEvent3 = new Event("PUT",requestDetails);

		Event requestEvent4 = new Event("POST",requestDetails);
		//this is now simulating the actual request made
		flightResource.onNext(requestEvent1);
		flightResource.onNext(requestEvent2);
		flightResource.onNext(requestEvent3);
		flightResource.onNext(requestEvent1);
		flightResource.onNext(requestEvent4); //publish the POST event as well

		flightPostSubscriber.assertNoErrors();

		List<Event> postResponses = flightPostSubscriber.getOnNextEvents();

		assertTrue(String.format("Expected response does not match actual : %s, Expected : %d",postResponses.size(),1),postResponses.size() == 1);

		defaultSubscriber.assertNoErrors();

		List<Event> defaultEvents = defaultSubscriber.getOnNextEvents();

		assertTrue(String.format("Expected response does not match actual : %s, Expected : %d",defaultEvents.size(),4),defaultEvents.size() == 4);


	}*/
}
