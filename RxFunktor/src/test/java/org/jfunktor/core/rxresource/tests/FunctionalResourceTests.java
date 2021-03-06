package org.jfunktor.core.rxresource.tests;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;

import static com.googlecode.cqengine.query.QueryFactory.*;

import com.googlecode.cqengine.resultset.ResultSet;
import org.jfunktor.core.events.api.Event;
import org.jfunktor.core.resource.api.ResourceException;
import org.jfunktor.core.rx.resource.api.Action;
import org.jfunktor.core.rx.resource.api.Resource;
import org.jfunktor.core.rx.resource.impl.RxResource;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * Created by vj on 11/11/16.
 */
public class FunctionalResourceTests {


    public static final String RESOURCE_ORDERS = "orders";
    public static final String ORDERS_VERSION = "1.0";

    private static final String GET = "get";
    private static final String POST = "post";
    private static final String PUT = "put";
    private static final String DELETE = "delete";
    private static final String ORDERS_EVENT = "Orders";
    private static final String NEW_ORDER_EVENT = "new-order";

    private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    private Resource<Event> createOrderResource() throws ResourceException, IOException {
        //List<Order> orderDB = createOrderDB();
        //List<Item> itemDB = createItemDB();

        OrderDB orderDB = createOrderDB();
        ItemDB itemDB = createItemDB();

        int orderIdCounter = 0;

        Resource<Event> orders = new RxResource(RESOURCE_ORDERS, ORDERS_VERSION);

        //orders resource actions
        orders.defineAction(GET,event->{

            Map details = event.getEventDetails();

            if(details.get(Event.EVENT_TYPE) == null || (!((String)details.get(Event.EVENT_TYPE)).equalsIgnoreCase("get-orders"))){
                Map<String,Object> errordetails = new HashMap();
                errordetails.put(Event.ERROR,new IllegalArgumentException("Invalid arguments for event"));
                Event evt = new Event(Event.ERROR_EVENT,errordetails);
                return evt;
            }

            String shippedTo = (String)details.get("shipped-to");
            String deliverOn = (String)details.getOrDefault("deliver-by",null);
            String deliverFrom = (String)details.getOrDefault("deliver-from",null);
            String deliverTo = (String)details.getOrDefault("deliver-to",null);

            assert (shippedTo != null || shippedTo.trim().length() > 0) : "shipped-to cannot be empty or null";

            Date deliverOnDate = null;
            Date deliverFromDate = null;
            Date deliverToDate = null;

            try {
                if (deliverOn != null) {
                    deliverOnDate = dateFormat.parse(deliverOn);
                }

                if (deliverFrom != null) {
                    deliverFromDate = dateFormat.parse(deliverFrom);
                }

                if (deliverTo != null) {
                    deliverToDate = dateFormat.parse(deliverTo);
                }
            }catch(ParseException e){
                Map<String,Object> errordetails = new HashMap();
                errordetails.put(Event.ERROR,e);
                Event evt = new Event(Event.ERROR_EVENT,errordetails);
                return evt;
            }



            if(deliverOn != null){
                deliverFromDate = deliverOnDate;
                deliverToDate = deliverToDate;
            }
            List<Order> results = getMatchingOrders(orderDB,shippedTo,deliverFromDate,deliverToDate);


            Map<String,Object> responseDetails = new HashMap();
            responseDetails.put(Event.EVENT_TYPE,ORDERS_EVENT);
            responseDetails.put(ORDERS_EVENT,results);
            responseDetails.put(Event.SOURCE_EVENT,event);
            Event resultEvent = new Event(ORDERS_EVENT,responseDetails);
            return resultEvent;
        });


        orders.defineAction(POST,event->{
            Map details = event.getEventDetails();

            if(details.get(Event.EVENT_TYPE) == null || (!((String)details.get(Event.EVENT_TYPE)).equalsIgnoreCase("new-order"))){
                Map<String,Object> errordetails = new HashMap();
                errordetails.put(Event.ERROR,new IllegalArgumentException("Invalid arguments for event"));
                Event evt = new Event(Event.ERROR_EVENT,errordetails);
                return evt;
            }

            Order newOrder = createNewOrder(orderDB, itemDB, orderIdCounter, details);

            Map<String,Object> responseDetails = new HashMap();
            responseDetails.put(Event.EVENT_TYPE,NEW_ORDER_EVENT);
            responseDetails.put(NEW_ORDER_EVENT,newOrder);
            responseDetails.put(Event.SOURCE_EVENT,event);
            Event resultEvent = new Event(NEW_ORDER_EVENT,responseDetails);

            return resultEvent;
        });

        return orders;
    }


    private ItemDB createItemDB() throws IOException {
        return ItemDB.createItemDB("/Items1.json");
    }

    private Order createNewOrder(OrderDB orderdb,ItemDB itemsdb,int orderIdCounter,Map<String,Object> details){
        String shipperName = (String)details.get("shipperName");
        Map<String,String> addressMap = (Map<String,String>)details.get("shipperAddress");
        String deliverBy = (String)details.get("deliverBy");
        List<String> itemsList = (List<String>)details.get("items");

        List<LineItem> lineItems = getLineItems(itemsdb,itemsList);

        Order newOrder = new Order(generateOrderId(orderIdCounter),shipperName,
                getAddress(addressMap),lineItems,Calendar.getInstance().getTime(),deliverBy);

        //insert the order in to orderdb
        orderdb.add(newOrder);
        return newOrder;

    }

    private List<LineItem> getLineItems(ItemDB itemsdb,List<String> itemsList) {
        /*Stream<Item> itemStream = itemsdb.stream().filter(item -> {
            boolean retVal = false;
            if(itemsList.contains(item.getItemId())){
                retVal = true;
            }
            return retVal;
        });*/

        Query itemQuery = in(Item.ITEM_ID,itemsList);
        ResultSet<Item> items = itemsdb.retrieve(itemQuery);

        ArrayList<LineItem> lineItems = new ArrayList();
        items.forEach(item->{
            lineItems.add(new LineItem(item.getItemId(),item.getItemName(),item.getItemDescription(),1));
        });


        return lineItems;
        /*Stream<LineItem> lineItemStream = itemsList.stream().map(itemid->{
            Stream<Item> items = itemsdb.stream().filter(item -> {
                boolean retVal = false;
                if(item.getItemId().equalsIgnoreCase(itemid)){
                    retVal = true;
                }
                return retVal;
            });
            if(items.findFirst().isPresent()){
                Item i = items.findFirst().get();
                LineItem litem = new LineItem(i.getItemId(),i.getItemName(),i.getItemDescription(),1);
                return litem;
            }else{
                return null;
            }
        });


        return lineItemStream.collect(Collectors.toList());*/
    }

    private Address getAddress(Map<String, String> addressMap) {
        String address1 = addressMap.get("address1");
        String address2 = addressMap.get("address2");
        String city = addressMap.get("city");
        String country = addressMap.get("country");

        return new Address(address1,address2,city,country);

    }

    private String generateOrderId(int counter){
        return (new Integer(counter + 1)).toString();
    }


    /*private List<Order> createOrderDB() {
        ArrayList<Order> orders = new ArrayList();
        return orders;
    }*/

    private OrderDB createOrderDB() throws IOException {
        OrderDB orderDB = OrderDB.createOrderDB("/Orders1.json");
        return orderDB;
    }

    private List<Order> getMatchingOrders(OrderDB orderDB,String shippedTo, Date deliverFromDate, Date deliverToDate) {

        Query<Order> query = and(between(Order.DELIVER_BY,deliverFromDate,true,deliverToDate,true),equal(Order.SHIPPER_NAME,shippedTo));

        ArrayList<Order> results = new ArrayList();

        ResultSet<Order> retrievedResults = orderDB.retrieve(query);
        retrievedResults.forEach(order -> {
            results.add(order);
        });

        return results;

        /*Stream<Order> orderStream = orderDB.stream().filter(order -> {
            boolean retVal = false;
            if ((order.getDeliverBy().after(deliverFromDate) && order.getDeliverBy().before(deliverToDate))
                    || order.getDeliverBy().equals(deliverFromDate)) {
                if(!shippedTo.equals("*")){
                    if(order.getShipperName().equals(shippedTo)){
                        retVal = true;
                    }
                }else{
                    retVal = true;
                }
            }
            return retVal;
        });
        return orderStream.collect(Collectors.toList());*/
    }

     private Order findOrderById(List<Order> orderDB,String orderid){
        Stream<Order> orderStream = orderDB.stream().filter(order -> {
            boolean retVal = false;
            if(order.getOrderId().equals(orderid)){
                retVal = true;
            }
            return retVal;

        });
        return orderStream.findFirst().get();
    }

    @Test
    public void test_order_create() throws ResourceException, IOException {
        Resource orders = createOrderResource();
        TestSubscriber<Event> subscriber = new TestSubscriber<>();
        Action<Event> createAction = orders.getAction(POST);
        createAction.subscribe(subscriber);

        Event createEvent = createNewOrderEvent("Raul Suberix");

        orders.onNext(createEvent);

        subscriber.assertNoErrors();
        List<Event> responseEvents = subscriber.getOnNextEvents();

        assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),1), responseEvents.size() == 1);

        Event evt = responseEvents.get(0);
        assertTrue(String.format("Response event name is not as expected. Action %s, Expected %s",evt.getEventName(),"new-order"),evt.getEventName().equalsIgnoreCase("new-order"));
        System.out.println("Event details : "+evt.getEventDetails());
    }

    @Test
    public void test_orderdb() throws IOException {
        OrderDB orderDB = OrderDB.createOrderDB("/Orders1.json");
        Query<Order> query = equal(Order.ORDER_ID,"1");
        for (Order order : orderDB.retrieve(query)) {
            System.out.println("Order "+order);
            assertTrue(String.format("Order id %s is not what was expected %s",order.getOrderId(),"1"),order.getOrderId().equals("1"));
        }
    }

    private Event createNewOrderEvent(String shippedto) {

        Map<String,String> address = new HashMap<>();
        address.put("address1","New York");
        address.put("address2","Washington");
        address.put("city","New York");
        address.put("country","USA");

        List<String> itemlist = new ArrayList();
        itemlist.add("2");
        itemlist.add("6");

        Map<String,Object> details = new HashMap<>();
        details.put(Event.EVENT_TYPE,"new-order");
        details.put("shipperName",shippedto);
        details.put("shipperAddress",address);
        details.put("deliverBy","10-12-2016");
        details.put("items",itemlist);

        Event createevent = new Event("POST",details);
        return createevent;
    }




    @Test
    public void test_order_update(){

    }

    @Test
    public void test_order_query() throws ResourceException, IOException {
        Resource orders = createOrderResource();
        TestSubscriber<Event> subscriber = new TestSubscriber<>();
        Action<Event> getAction = orders.getAction(GET);
        getAction.subscribe(subscriber);

        Event createEvent = createQueryOrderEvent("Vijay");

        orders.onNext(createEvent);

        subscriber.assertNoErrors();
        List<Event> responseEvents = subscriber.getOnNextEvents();

        assertTrue(String.format("Response does not match expected Actual %d, Expected %d", responseEvents.size(),1), responseEvents.size() == 1);

        Event evt = responseEvents.get(0);
        assertTrue(String.format("Response event name is not as expected. Action %s, Expected %s",evt.getEventName(),"Orders"),evt.getEventName().equalsIgnoreCase("Orders"));
        System.out.println("Event details : "+evt.getEventDetails());
    }

    private Event createQueryOrderEvent(String shippername) {
        Map<String,String> address = new HashMap<>();
        address.put("address1","New York");
        address.put("address2","Washington");
        address.put("city","New York");
        address.put("country","USA");

        Map<String,Object> details = new HashMap<>();
        details.put(Event.EVENT_TYPE,"get-orders");
        details.put("shipped-to",shippername);
        details.put("deliver-from","10-10-2017");
        details.put("deliver-to","10-11-2017");

        Event getevent = new Event("GET",details);
        return getevent;
    }
}

class OrderNotFoundException extends Exception{
    public OrderNotFoundException() {
    }

    public OrderNotFoundException(String s) {
        super(s);
    }

    public OrderNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public OrderNotFoundException(Throwable throwable) {
        super(throwable);
    }

    public OrderNotFoundException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}

class Order{
    private String orderId;
    private String shipperName;
    private Address shipperAddress;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="dd/MM/yyyy")
    private Date deliverBy;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="dd/MM/yyyy")
    private Date orderDate;

    private List<LineItem> items;

    static final Attribute<Order,String> ORDER_ID = attribute("orderId",order->{return order.orderId;});

    static final Attribute<Order,String> SHIPPER_NAME = attribute("shipperName",order->{return order.shipperName;});

    static final Attribute<Order,String> SHIPPER_CITY = attribute("shipperCity",order->{return order.shipperAddress.getCity();});

    static final Attribute<Order,String> SHIPPER_COUNTRY = attribute("shipperCountry",order->{return order.shipperAddress.getCountry();});

    static final Attribute<Order,Date> DELIVER_BY = attribute("deliverBy",order->{return order.deliverBy;});

    private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public Order(){}

    public Order(String orderId, String shipperName, Address shipperAddress, List<LineItem> items) {
        this.orderId = orderId;
        this.shipperName = shipperName;
        this.shipperAddress = shipperAddress;
        this.items = items;
    }

    public Order(String orderId, String shipperName, Address shipperAddress, List<LineItem> items,String orderdate,String deliverby) {
        this.orderId = orderId;
        this.shipperName = shipperName;
        this.shipperAddress = shipperAddress;
        this.items = items;
        try {
            orderDate = dateFormat.parse(orderdate);
            deliverBy = dateFormat.parse(deliverby);
        }catch(ParseException e){
            //default to today
            orderDate = Calendar.getInstance().getTime(); //default to current
            deliverBy = orderDate;
        }
    }

    public Order(String orderId, String shipperName, Address shipperAddress, List<LineItem> items,Date orderdate,Date deliverby) {
        this.orderId = orderId;
        this.shipperName = shipperName;
        this.shipperAddress = shipperAddress;
        this.items = items;
        orderDate = orderdate;
        deliverBy = deliverby;
    }

    public Order(String orderId, String shipperName, Address shipperAddress, List<LineItem> items,Date orderdate,String deliverby) {
        this.orderId = orderId;
        this.shipperName = shipperName;
        this.shipperAddress = shipperAddress;
        this.items = items;
        this.orderDate = orderdate;
        try {
            deliverBy = dateFormat.parse(deliverby);
        }catch(ParseException e){
            deliverBy = orderDate;
        }
    }


    public String getOrderId() {
        return orderId;
    }

    public String getShipperName() {
        return shipperName;
    }

    public Address getShipperAddress() {
        return shipperAddress;
    }

    public List<LineItem> getLineItems() {
        return items;
    }

    public Date getDeliverBy() {
        return deliverBy;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setShipperName(String shipperName) {
        this.shipperName = shipperName;
    }

    public void setShipperAddress(Address shipperAddress) {
        this.shipperAddress = shipperAddress;
    }

    public void setDeliverBy(Date deliverBy) {
        this.deliverBy = deliverBy;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public void setItems(List<LineItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", shipperName='" + shipperName + '\'' +
                ", shipperAddress=" + shipperAddress +
                ", lineItems=" + items +
                '}';
    }
}

class Address{
    private String address1;
    private String address2;
    private String city;
    private String country;

    public Address(){};

    public Address(String address1, String address2, String city, String country) {
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.country = country;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "Address{" +
                "address1='" + address1 + '\'' +
                ", address2='" + address2 + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
class LineItem{
    private String itemId;
    private String itemName;
    private String itemDescription;
    private int quantity;

    public LineItem(String itemId, String itemName, String itemDescription, int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
    }

    public LineItem(){}

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "LineItem{" +
                "itemId='" + itemId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}

class Item{
    private String itemId;
    private String itemName;
    private String itemDescription;
    private int quantity;

    static final Attribute<Item,String> ITEM_ID = attribute("itemId",item->{return item.itemId;});

    static final Attribute<Item,String> ITEM_NAME = attribute("itemName",item->{return item.itemName;});

    static final Attribute<Item,String> ITEM_DESCRIPTION = attribute("itemDescription",item->{return item.itemDescription;});

    static final Attribute<Item,Integer> ITEM_QUANTITY = attribute("quantity",item->{return item.quantity;});

    public Item(){}

    public Item(String itemId, String itemName, String itemDescription, int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemId='" + itemId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                ", quantity=" + quantity +
                '}';
    }


}