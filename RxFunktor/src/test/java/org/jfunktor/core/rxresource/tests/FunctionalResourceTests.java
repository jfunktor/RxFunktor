package org.jfunktor.core.rxresource.tests;

import org.jfunktor.core.events.api.Event;
import org.jfunktor.core.resource.api.ResourceException;
import org.jfunktor.core.rx.resource.api.Resource;
import org.jfunktor.core.rx.resource.impl.RxResource;
import org.junit.Test;
import rx.Observable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.jfunktor.core.rx.resource.impl.RxResource.safely;

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
    private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    private Resource<Event> createOrderResource() throws ResourceException {
        ArrayList<Order> orderArrayList = new ArrayList();

        Resource orders = new RxResource(RESOURCE_ORDERS, ORDERS_VERSION);

        //orders resource actions
        Observable<Event> getAction = orders.defineAction(GET).map(safely(event->{
            Map details = event.getEventDetails();
            String shippedTo = (String)details.get("shipped-to");
            String deliverOn = (String)details.getOrDefault("deliver-by",null);
            String deliverFrom = (String)details.getOrDefault("deliver-from",null);
            String deliverTo = (String)details.getOrDefault("deliver-to",null);

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
                //Event e = new Event();
            }

            return event;
        }));


        Observable<Event> putAction = orders.defineAction(PUT).map(safely(event->{
            return event;
        }));

        Observable<Event> postAction = orders.defineAction(POST).map(safely(event->{
            return event;
        }));

        Observable<Event> deleteAction = orders.defineAction(DELETE).map(safely(event->{
            return event;
        }));

        return orders;
    }

    @Test
    public void test_order_create()throws ResourceException{
        Resource orders = createOrderResource();
        //orders.getAction(GET)

    }

    @Test
    public void test_order_update(){

    }

    @Test
    public void test_order_query(){

    }
}

class Order{
    private String orderId;
    private String shipperName;
    private Address shipperAddress;
    private Date deliverBy;
    private Date orderDate;
    private List<Item> items;

    private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public Order(String orderId, String shipperName, Address shipperAddress, List<Item> items) {
        this.orderId = orderId;
        this.shipperName = shipperName;
        this.shipperAddress = shipperAddress;
        this.items = items;
    }

    public Order(String orderId, String shipperName, Address shipperAddress, List<Item> items,String orderdate,String deliverby) {
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

    public String getOrderId() {
        return orderId;
    }

    public String getShipperName() {
        return shipperName;
    }

    public Address getShipperAddress() {
        return shipperAddress;
    }

    public List<Item> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", shipperName='" + shipperName + '\'' +
                ", shipperAddress=" + shipperAddress +
                ", items=" + items +
                '}';
    }
}

class Address{
    private String address1;
    private String address2;
    private String city;
    private String country;

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

class Item{
    private String itemId;
    private String itemName;
    private String itemDescription;
    private int quantity;

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