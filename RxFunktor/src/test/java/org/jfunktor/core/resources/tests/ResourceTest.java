package org.jfunktor.core.resources.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.jfunktor.core.events.api.Event;
import org.jfunktor.core.resource.api.Resource;
import org.jfunktor.core.resource.api.ResourceException;
import org.junit.After;
import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

public class ResourceTest {

	private Resource sampleResource;
	
	@Before
	public void setUp() throws Exception {
		sampleResource = new Resource("flight","1.0")
							.withAction("find",(context,event)->{
								return Observable.create(subscriber->{
									
									Map requestMap = (Map)context.get("Request");
									
									if(requestMap.containsKey("subaction")){
										subscriber.onError(new ResourceException("Unable to process request..."));	
									}else{
										Observable.just("One", "Two", "Three").subscribe(item->{subscriber.onNext(item);});
										
										//subscriber.onNext("find response");
										subscriber.onCompleted();
									}
								
								}).subscribeOn(Schedulers.io());
								
							})
							.withAction("depart",(context,event)->{
								return Observable.create(subscriber->{
									
									subscriber.onNext("depart response");
									subscriber.onCompleted();
								
								});
								
							})
							.withAction("arrive",(context,event)->{
								return Observable.create(subscriber->{
									
									subscriber.onNext("arrive response");
									subscriber.onCompleted();
								
								});
								
							});
		
	}

	@After
	public void tearDown() throws Exception {
		sampleResource = null;
	}

	@Test
	public void test_query_sample_resource() {
		assertTrue("Find action not supported!", sampleResource.isSupported("find"));
		assertTrue("Depart action not supported!", sampleResource.isSupported("depart"));
		assertTrue("Arrive action not supported!", sampleResource.isSupported("arrive"));
	}

	@Test
	public void test_query_casesensitivity_sample_resource() {
		assertTrue("Find action not supported!", sampleResource.isSupported("Find"));
		assertTrue("DEPART action not supported!", sampleResource.isSupported("DEPART"));
		assertTrue("ArrIVE action not supported!", sampleResource.isSupported("arrIVE"));
	}
	
	@Test
	public void test_action_count_on_sample_resource(){
		int actual = sampleResource.getActionCount();
		int expected = 3;
		assertEquals(String.format("Action count %d does not match expected %d",actual,expected), expected, actual);
		
	}
	
	@Test(expected = ResourceException.class)
	public void test_mutation_test_on_sample_resource() throws ResourceException{
		//try to redefine the action with a different one
		sampleResource.withAction("Find",(context,event)->{
			return Observable.create(subscriber->{
				
				subscriber.onNext("find response");
				subscriber.onCompleted();
			
			});		
		});
	}
	
	
	@Test
	public void test_process_sample_resource() throws Exception{
		HashMap<String, Object> context = new HashMap();
		HashMap<String,String> params = new HashMap();
		params.put("name", "vijay");
		//params.put("subaction", "birthplace");
		context.put("action", "find");
		context.put("Request", params);
		Event requestEvent = new Event("find",params);
		
		
		System.out.println("Subscribing Thread : " + Thread.currentThread());
		TestSubscriber testSubscriber = new TestSubscriber();
		
		/*sampleResource.process(context,requestEvent).subscribe(new Observer(){

			@Override
			public void onCompleted() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onError(Throwable e) {
				
				e.printStackTrace();
			}

			@Override
			public void onNext(Object result) {
				System.out.println("Observing Thread : " + Thread.currentThread());
				System.out.println("onNext process sample resource" + result);
				assertNotEquals("Resource response was empty!", null, result);
				
			}
			
		});*/
		
		sampleResource.process(context, requestEvent).subscribe(testSubscriber);
		
		testSubscriber.assertNoErrors();
		
		List events = testSubscriber.getOnNextEvents();
		
		
		events.forEach(event ->{System.out.println(event);});
		
	}
	
	
	
	@Test
	public void test_process_sample_resource_with_exception() throws Exception{
		HashMap<String, Object> context = new HashMap();
		HashMap<String,String> params = new HashMap();
		params.put("name", "vijay");
		params.put("subaction", "birthplace");
		context.put("action", "find");
		context.put("Request", params);
		Event requestEvent = new Event("find",params);
		
		
		System.out.println("Subscribing Thread : " + Thread.currentThread());
		TestSubscriber testSubscriber = new TestSubscriber();
		
		/*sampleResource.process(context,requestEvent).subscribe(new Observer(){

			@Override
			public void onCompleted() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onError(Throwable e) {
				
				e.printStackTrace();
			}

			@Override
			public void onNext(Object result) {
				System.out.println("Observing Thread : " + Thread.currentThread());
				System.out.println("onNext process sample resource" + result);
				assertNotEquals("Resource response was empty!", null, result);
				
			}
			
		});*/
		
		sampleResource.process(context, requestEvent).observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(testSubscriber);
		
	
		testSubscriber.assertError(ResourceException.class);
		
		List events = testSubscriber.getOnNextEvents();
		
		
		events.forEach(event ->{System.out.println(event);});
		
	}
	

}
