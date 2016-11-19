package org.jfunktor.core.rxresource.tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.suffix.SuffixTreeIndex;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by vj on 19/11/16.
 */
public class ItemDB {

    ConcurrentIndexedCollection<Item> items = new ConcurrentIndexedCollection<>();


    private static ObjectMapper objectMapper = new ObjectMapper();

    public ItemDB() {
    }

    public static ItemDB createItemDB(String testfile) throws IOException {
        System.out.println("ItemDB file URL : "+ItemDB.class.getResource(testfile));
        InputStream in = ItemDB.class.getResourceAsStream(testfile);

        List<Item> list = objectMapper.readValue(in, new TypeReference<List<Item>>(){});

        ItemDB itemDB = new ItemDB();
        itemDB.indexCollection(list);

        return itemDB;

    }

    private void indexCollection(List<Item> list) {
        items.addAll(list);
        items.addIndex(HashIndex.onAttribute(Item.ITEM_ID));
        items.addIndex(HashIndex.onAttribute(Item.ITEM_NAME));
        items.addIndex(SuffixTreeIndex.onAttribute(Item.ITEM_NAME));
        items.addIndex(SuffixTreeIndex.onAttribute(Item.ITEM_DESCRIPTION));
    }
}
