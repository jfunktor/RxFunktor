package org.jfunktor.core.events.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfunktor.core.events.api.Event;
import org.jfunktor.core.events.api.EventBus;
import org.jfunktor.core.events.api.EventBus.DeliveryMode;
import org.jfunktor.core.events.impl.RxBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rx.Subscription;
import rx.observers.TestSubscriber;

public class EventBusTests {
	
	private EventBus eventBus = new RxBus("bus-1");

	@Before
	public void setUp() throws Exception {
		
		//eventBus.topic("channel").subscribe(event->{System.out.println("Received Event "+event);});
		
		eventBus.subscribe("channel",event->{System.out.println("Received Event "+event);});
		
			

	}

	@After
	public void tearDown() throws Exception {
		
		eventBus.unSubscribeAll();
	}

	@Test
	public void test_event_subscription_single_topic_single() {
		
		TestConsumer<Object> consumer = new TestConsumer<Object>();
		
		Event evt = new Event("Test",new HashMap());		
		
		eventBus.subscribe("channel",consumer);
		
		eventBus.publish("channel",evt);
		
		List events = consumer.getAccepted();
		
		assertTrue("No useful events received",events.size() > 0);
	}
	
	@Test
	public void test_event_subscription_multiple_topic_single(){

		TestConsumer<Object> consumer1 = new TestConsumer<Object>();
		TestConsumer<Object> consumer2 = new TestConsumer<Object>();
		TestConsumer<Object> consumer3 = new TestConsumer<Object>();
		TestConsumer<Object> consumer4 = new TestConsumer<Object>();
		
		Event evt = new Event("Test",new HashMap());		
		
		eventBus.subscribe("channel",consumer1);
		eventBus.subscribe("channel",consumer2);
		eventBus.subscribe("channel",consumer3);
		eventBus.subscribe("channel",consumer4);
		
		eventBus.publish("channel",evt);
		
		List events1 = consumer1.getAccepted();
		List events2 = consumer2.getAccepted();
		List events3 = consumer3.getAccepted();
		List events4 = consumer4.getAccepted();
		
		
		assertTrue("1 : No useful events received",events1.size() > 0);
		assertTrue("2 : No useful events received",events2.size() > 0);
		assertTrue("3 : No useful events received",events3.size() > 0);
		assertTrue("4 : No useful events received",events4.size() > 0);
		
	}
	
	
	@Test
	public void test_event_subscription_multiple_topic_multiple(){
		TestConsumer<Object> consumer1 = new TestConsumer<Object>();
		TestConsumer<Object> consumer2 = new TestConsumer<Object>();
		TestConsumer<Object> consumer3 = new TestConsumer<Object>();
		TestConsumer<Object> consumer4 = new TestConsumer<Object>();
		
		Event evt = new Event("Test",new HashMap());		
		
		eventBus.subscribe("channel1",consumer1);
		eventBus.subscribe("channel2",consumer2);
		eventBus.subscribe("channel3",consumer3);
		eventBus.subscribe("channel4",consumer4);
		
		eventBus.publish("channel1",evt);
		eventBus.publish("channel2",evt);
		eventBus.publish("channel3",evt);
		eventBus.publish("channel4",evt);
		
		List events1 = consumer1.getAccepted();
		List events2 = consumer2.getAccepted();
		List events3 = consumer3.getAccepted();
		List events4 = consumer4.getAccepted();
		
		
		assertTrue("1 : No useful events received",events1.size() > 0);
		assertTrue("2 : No useful events received",events2.size() > 0);
		assertTrue("3 : No useful events received",events3.size() > 0);
		assertTrue("4 : No useful events received",events4.size() > 0);
		
	}
	
	
	@Test
	public void test_event_subscription_single_topic_single_events_multiple() {
		
		TestConsumer<Object> consumer = new TestConsumer<Object>();
		
		Event evt1 = new Event("Test1",new HashMap());		
		Event evt2 = new Event("Test2",new HashMap());
		Event evt3 = new Event("Test3",new HashMap());
		
		eventBus.subscribe("channel",consumer);
		
		eventBus.publish("channel",evt1);
		eventBus.publish("channel",evt2);
		eventBus.publish("channel",evt3);
		
		List events = consumer.getAccepted();
		
		assertTrue("Some events were not received",events.size() == 3);
	}
	

	@Test
	public void test_event_subscription_multiple_topic_multiple_events_multiple(){
		TestConsumer<Object> consumer1 = new TestConsumer<Object>();
		TestConsumer<Object> consumer2 = new TestConsumer<Object>();
		TestConsumer<Object> consumer3 = new TestConsumer<Object>();
		TestConsumer<Object> consumer4 = new TestConsumer<Object>();
		TestConsumer<Object> consumer5 = new TestConsumer<Object>();
		
		Event evt1 = new Event("Test1",new HashMap());		
		Event evt2 = new Event("Test2",new HashMap());
		Event evt3 = new Event("Test3",new HashMap());
		
		eventBus.subscribe("channel1",consumer1);
		eventBus.subscribe("channel2",consumer2);
		eventBus.subscribe("channel3",consumer3);
		eventBus.subscribe("channel4",consumer4);
		eventBus.subscribe("channel5",consumer5);
		
		eventBus.publish("channel1",evt1);
		eventBus.publish("channel1",evt2);
		
		eventBus.publish("channel2",evt2);
		eventBus.publish("channel2",evt3);
		
		eventBus.publish("channel3",evt2);
		eventBus.publish("channel3",evt3);

		eventBus.publish("channel4",evt3);
		
		List events1 = consumer1.getAccepted();
		List events2 = consumer2.getAccepted();
		List events3 = consumer3.getAccepted();
		List events4 = consumer4.getAccepted();
		List events5 = consumer5.getAccepted();
		
		assertTrue("1 : Some events were not received",events1.size() == 2);
		assertTrue("2 : Some events were not received",events2.size() == 2);
		assertTrue("3 : Some events were not received",events3.size() == 2);
		assertTrue("4 : Some events were not received",events4.size() == 1);
		assertTrue("5 : Some events were received",events5.size() == 0);
		
	}

	@Test
	public void test_event_subscription_single_topic_single_synchronous() {
		
		TestThreadedConsumer<Object> consumer = new TestThreadedConsumer<Object>();
		
		Event evt = new Event("Test",new HashMap());		
		
		eventBus.subscribe("channel",consumer);
		
		eventBus.publish("channel",evt);
		
		List events = consumer.getAccepted();
		
		Map<String,List<Object>> eventMap = consumer.getEventMap();
		
		assertTrue("No useful events received",events.size() == 1);
		
		eventMap.keySet().forEach(threadName -> {assertTrue(String.format("Event was received asynchronous creation : %s, received : %s", consumer.getCreationThread(),threadName),threadName.equals(consumer.getCreationThread()));});
		
	}

	@Test
	public void test_event_subscription_single_topic_single_asynchronous() throws InterruptedException {
		
		TestThreadedConsumer<Object> consumer = new TestThreadedConsumer<Object>();
		
		Event evt = new Event("TestAsync",new HashMap());		
		
		eventBus.subscribe("channel",EventBus.DeliveryMode.ASYNC,consumer);
		
		eventBus.publish("channel",evt);
		
	
		Thread.sleep(1000);
		
		List events = consumer.getAccepted();
		
		Map<String,List<Object>> eventMap = consumer.getEventMap();
		
		assertTrue("No useful events received",events.size() == 1);
		
		eventMap.keySet().forEach(threadName -> {assertFalse(String.format("Event was received synchronous creation : %s, received : %s", consumer.getCreationThread(),threadName),threadName.equals(consumer.getCreationThread()));});
		
	}
	
	
	@Test
	public void test_event_subscription_single_topic_single_asynchronous_dead_topics() throws InterruptedException {
		
		TestThreadedConsumer<Object> consumer = new TestThreadedConsumer<Object>();
		
		Event evt = new Event("TestAsync",new HashMap());		
		
		eventBus.subscribeForDeadTopics(EventBus.DeliveryMode.ASYNC,consumer);
		
		eventBus.publish("Topic1",evt);
		
	
		Thread.sleep(1000);
		
		List events = consumer.getAccepted();
		
		Map<String,List<Object>> eventMap = consumer.getEventMap();
		
		assertTrue("No useful events received",events.size() == 1);
		
		eventMap.keySet().forEach(threadName -> {assertFalse(String.format("Event was received synchronous creation : %s, received : %s", consumer.getCreationThread(),threadName),threadName.equals(consumer.getCreationThread()));});
		
	}

	@Test
	public void test_event_subscription_single_topic_single_synchronous_dead_topics() throws InterruptedException {
		
		TestThreadedConsumer<Object> consumer = new TestThreadedConsumer<Object>();
		
		Event evt = new Event("TestSync",new HashMap());		
		
		eventBus.subscribeForDeadTopics(EventBus.DeliveryMode.SYNC,consumer);
		
		eventBus.publish("Topic2",evt);
		eventBus.publish("Channel",evt);
		
		//Thread.sleep(1000);
		
		List events = consumer.getAccepted();
		
		Map<String,List<Object>> eventMap = consumer.getEventMap();
		
		System.out.println("Consumed Events "+events.size());
		
		assertTrue("No useful events or more events received",events.size() <= 1);
		
		eventMap.keySet().forEach(threadName -> {assertTrue(String.format("Event was received asynchronous creation : %s, received : %s", consumer.getCreationThread(),threadName),threadName.equals(consumer.getCreationThread()));});
		
	}
	
	@Test
	public void test_event_subscription_multiple_topics_asynchronous_dead_topics_post_subscription() throws InterruptedException {
		
		TestThreadedConsumer<Object> consumer = new TestThreadedConsumer<Object>();
		
		Event evt = new Event("TestAsync",new HashMap());		
		
		eventBus.subscribeForDeadTopics(EventBus.DeliveryMode.ASYNC,consumer);
		
		eventBus.subscribe("Topic1", event->{System.out.println("Topic1 : received event "+event);});
		
		eventBus.publish("Topic1",evt);
		
	
		Thread.sleep(1000);
		
		List events = consumer.getAccepted();
		
		Map<String,List<Object>> eventMap = consumer.getEventMap();
		
		assertTrue("Some events received",events.size() == 0);
		
		eventMap.keySet().forEach(threadName -> {assertFalse(String.format("Event was received synchronous creation : %s, received : %s", consumer.getCreationThread(),threadName),threadName.equals(consumer.getCreationThread()));});
		
	}
	
	
	@Test
	public void test_event_subscription_multiple_topics_asynchronous_dead_topics_post_subscription_rx_observer() throws InterruptedException {
		
		TestSubscriber consumer = new TestSubscriber();
		
		Event evt = new Event("TestAsync",new HashMap());		
		
		eventBus.subscribeForDeadTopicsWithObserver(EventBus.DeliveryMode.ASYNC,consumer);
		
		eventBus.subscribe("Topic1", event->{System.out.println("Topic1 : received event "+event);});
		
		eventBus.publish("Topic1",evt);
		eventBus.publish("Channel", evt);
		
		consumer.assertNoErrors();
		
		List events = consumer.getOnNextEvents();
		
		assertTrue("Some events received",events.size() == 0);
		
	}
	
	@Test
	public void test_multiple_topics_asynchronous_dead_topics_post_subscription_multiple_notifications_rx_observer() throws InterruptedException {
		
		TestSubscriber consumer = new TestSubscriber();
		
		Event evt = new Event("TestAsync",new HashMap());		

		eventBus.subscribe("Topic1", event->{System.out.println("Topic1 : received event "+event);});
		eventBus.subscribe("Topic2", event->{System.out.println("Topic2 : received event "+event);});
		
		eventBus.subscribeForDeadTopicsWithObserver(EventBus.DeliveryMode.ASYNC,consumer);

		eventBus.publish("Topic1",evt); //NO
		eventBus.publish("Topic3",evt); //Yes
		eventBus.publish("Channel", evt); //NO
		
		eventBus.subscribe("Topic3", event->{System.out.println("Topic3 : received event "+event);});
		eventBus.subscribe("Topic4", event->{System.out.println("Topic4 : received event "+event);});
		eventBus.subscribe("Topic5", event->{System.out.println("Topic5 : received event "+event);});
		eventBus.subscribe("Topic6", event->{System.out.println("Topic6 : received event "+event);});
		
		eventBus.publish("Topic2",evt); //NO
		eventBus.publish("Topic7",evt); //YES
		eventBus.publish("Topic3",evt); //NO
		eventBus.publish("Channel2", evt); //YES
		eventBus.publish("Channel3", evt); //YES
		eventBus.publish("Channel3", evt); //YES
		eventBus.publish("Channel3", evt); //YES

		eventBus.subscribe("Topic7", event->{System.out.println("Topic7 : received event "+event);});
		eventBus.subscribe("Topic4", event->{System.out.println("Topic4 : received event "+event);});
		eventBus.subscribe("Topic5", event->{System.out.println("Topic5 : received event "+event);});
		eventBus.subscribe("Topic6", event->{System.out.println("Topic6 : received event "+event);});

		eventBus.publish("Topic1",evt); //NO
		eventBus.publish("Topic7",evt); //NO 
		eventBus.publish("Channel7", evt); //YES
		eventBus.publish("Channel6", evt); //YES
		eventBus.publish("Channel7", evt); //YES
		eventBus.publish("Channel5", evt); //YES
		eventBus.publish("Channel7", evt); //YES
		eventBus.publish("Channel4", evt); //YES
		
		consumer.assertNoErrors();
		
		List events = consumer.getOnNextEvents();
		
		assertTrue(String.format("Received events %d do not match with expected %d",events.size(),12),events.size() == 12);
		
	}
	
	@Test
	public void test_event_subscription_rx_observer(){

		TestSubscriber consumer = new TestSubscriber();
		
		Event evt = new Event("TestAsync",new HashMap());		
		
		eventBus.subscribeWithObserver("Channel1",EventBus.DeliveryMode.SYNC,consumer);
		
	
		eventBus.publish("Channel1", evt);
		
		eventBus.publish("Channel1", evt);
		
		eventBus.publish("Channel2", evt);
		
		consumer.assertNoErrors();
		
		List events = consumer.getOnNextEvents();
		
		assertTrue(String.format("Received events %d do not match with expected %d",events.size(),2),events.size() == 2);
		
	}
	
	
	@Test
	public void test_consumer_unsubscription_single(){

		TestSubscriber consumer = new TestSubscriber();
		
		Event evt = new Event("TestAsync",new HashMap());		
		
		Subscription subscription = eventBus.subscribeWithObserver("Channel1",EventBus.DeliveryMode.SYNC,consumer);
		
	
		eventBus.publish("Channel1", evt);
		
		eventBus.publish("Channel1", evt);
		
		eventBus.publish("Channel2", evt);
		
		//now unsubscribe
		//we should no longer receive events
		subscription.unsubscribe();

		eventBus.publish("Channel1", evt);

		consumer.assertNoErrors();
		
		List events = consumer.getOnNextEvents();
		
		
		assertTrue(String.format("Received events %d do not match with expected %d",events.size(),2),events.size() == 2);		
	}
	
	
	@Test
	public void test_consumer_unsubscription_all(){

		TestSubscriber consumer = new TestSubscriber();
		
		TestSubscriber consumer1 = new TestSubscriber();
		
		Event evt = new Event("TestAsync",new HashMap());	
		
		EventBus localEventBus = new RxBus("bus-2");
		
		Subscription subscription1 = localEventBus.subscribeWithObserver("Channel1",EventBus.DeliveryMode.SYNC,consumer);
		Subscription subscription2 = localEventBus.subscribeWithObserver("Channel2",EventBus.DeliveryMode.SYNC,consumer);
		Subscription subscription3 = localEventBus.subscribeWithObserver("Channel3",EventBus.DeliveryMode.SYNC,consumer);

		Subscription subscription4 = localEventBus.subscribeWithObserver("Channel1",EventBus.DeliveryMode.SYNC,consumer1);
		Subscription subscription5 = localEventBus.subscribeWithObserver("Channel2",EventBus.DeliveryMode.SYNC,consumer1);
		Subscription subscription6 = localEventBus.subscribeWithObserver("Channel3",EventBus.DeliveryMode.SYNC,consumer1);
		
		localEventBus.publish("Channel1", evt);
		
		localEventBus.publish("Channel1", evt);
		
		localEventBus.publish("Channel2", evt);

		localEventBus.publish("Channel3", evt);

		localEventBus.publish("Channel3", evt);

		//now unsubscribe
		//we should no longer receive events
		localEventBus.unSubscribeAll();

		localEventBus.publish("Channel1", evt);
		localEventBus.publish("Channel2", evt);
		localEventBus.publish("Channel3", evt);
		localEventBus.publish("Channel4", evt);

		consumer.assertNoErrors();
		
		consumer1.assertNoErrors();
		
		List events = consumer.getOnNextEvents();
		List events1 = consumer1.getOnNextEvents();
		
		
		assertTrue(String.format("Consumer Received events %d do not match with expected %d",events.size(),5),events.size() == 5);
		assertTrue(String.format("Consumer 1 Received events %d do not match with expected %d",events1.size(),5),events1.size() == 5);		

	}
	
	
	@Test
	public void test_consumer_unsubscription_multiple(){

		TestSubscriber consumer1 = new TestSubscriber();
		TestSubscriber consumer2 = new TestSubscriber();
		TestSubscriber deadTopicConsumer = new TestSubscriber();
		
		Event evt = new Event("TestAsync",new HashMap());		
		
		EventBus localEventBus = new RxBus("bus-2");
		
		Subscription subscription1 = localEventBus.subscribeWithObserver("Channel1",EventBus.DeliveryMode.SYNC,consumer1);
		Subscription subscription2 = localEventBus.subscribeWithObserver("Channel1",EventBus.DeliveryMode.SYNC,consumer2);
		Subscription deadTopicSubscription = localEventBus.subscribeForDeadTopicsWithObserver(DeliveryMode.ASYNC, deadTopicConsumer);
		
	
		localEventBus.publish("Channel1", evt);
		
		localEventBus.publish("Channel1", evt);
		
		localEventBus.publish("Channel2", evt);
		
		consumer1.assertNoErrors();
		consumer2.assertNoErrors();
		deadTopicConsumer.assertNoErrors();
		
		List events1 = consumer1.getOnNextEvents();
		List events2 = consumer2.getOnNextEvents();
		List deadEvents = deadTopicConsumer.getOnNextEvents();
		
		assertTrue(String.format("Consumer 1 Received events %d do not match with expected %d",events1.size(),2),events1.size() == 2);
		assertTrue(String.format("Consumer 2 Received events %d do not match with expected %d",events2.size(),2),events2.size() == 2);
		assertTrue(String.format("DeadTopicConsumer Received events %d do not match with expected %d",deadEvents.size(),1),deadEvents.size() == 1);
		
		//now unsubscribe 1
		subscription1.unsubscribe();

		localEventBus.publish("Channel1", evt);
		
		localEventBus.publish("Channel1", evt);

		events1 = consumer1.getOnNextEvents();
		events2 = consumer2.getOnNextEvents();
		deadEvents = deadTopicConsumer.getOnNextEvents();

		//Consumer 1 has been unsubscribed so no new events for it
		//Consumer 2 is still on so new events should flow to it
		//DeadTopicConsumer is still on , but no new dead events should be on now
		assertTrue(String.format("Consumer 1 Received events %d do not match with expected %d",events1.size(),2),events1.size() == 2);
		assertTrue(String.format("Consumer 2 Received events %d do not match with expected %d",events2.size(),4),events2.size() == 4);
		assertTrue(String.format("DeadTopicConsumer Received events %d do not match with expected %d",deadEvents.size(),1),deadEvents.size() == 1);
		
		
		//now unsubscribe 2 also
		//we should no longer receive events
		subscription2.unsubscribe();

		localEventBus.publish("Channel1", evt);
		
		localEventBus.publish("Channel1", evt);

		consumer1.assertNoErrors();
		consumer2.assertNoErrors();
		deadTopicConsumer.assertNoErrors();
		
		events1 = consumer1.getOnNextEvents();
		events2 = consumer2.getOnNextEvents();
		deadEvents = deadTopicConsumer.getOnNextEvents();
		
		
		//Consumer 1 has been unsubscribed so no new events for it
		//Consumer 2 has been unsubscribed so no new events for it
		//DeadTopicConsumer is still on , so additional dead events should be routed to it...
		assertTrue(String.format("Consumer 1 Received events %d do not match with expected %d",events1.size(),2),events1.size() == 2);
		assertTrue(String.format("Consumer 2 Received events %d do not match with expected %d",events2.size(),4),events2.size() == 4);
		assertTrue(String.format("DeadTopicConsumer Received events %d do not match with expected %d",deadEvents.size(),3),deadEvents.size() == 3);
	}
		
}
