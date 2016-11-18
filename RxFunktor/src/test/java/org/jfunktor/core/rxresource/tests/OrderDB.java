package org.jfunktor.core.rxresource.tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.engine.CollectionQueryEngine;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.suffix.SuffixTreeIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by vj on 18/11/16.
 */
public class OrderDB {

    ConcurrentIndexedCollection<Order> orders = new ConcurrentIndexedCollection<>();

    private static ObjectMapper objectMapper = new ObjectMapper();
    OrderDB(){

    }
    public static OrderDB createOrderDB(String testfile) throws IOException {

        System.out.println("OrderDB file URL : "+OrderDB.class.getResource(testfile));
        InputStream in = OrderDB.class.getResourceAsStream(testfile);

        List<Order> list = objectMapper.readValue(in, new TypeReference<List<Order>>(){});

        OrderDB orderDB = new OrderDB();
        orderDB.indexCollection(list);

        return orderDB;
    }

    private void indexCollection(List<Order> list) {
        orders.addAll(list);
        orders.addIndex(HashIndex.onAttribute(Order.ORDER_ID));
        orders.addIndex(HashIndex.onAttribute(Order.SHIPPER_CITY));
        orders.addIndex(HashIndex.onAttribute(Order.SHIPPER_COUNTRY));
        orders.addIndex(SuffixTreeIndex.onAttribute(Order.SHIPPER_NAME));
    }


    public ResultSet<Order> retrieve(Query<Order> query){
        ResultSet<Order> resultSet = orders.retrieve(query);
        return resultSet;
    }


}
