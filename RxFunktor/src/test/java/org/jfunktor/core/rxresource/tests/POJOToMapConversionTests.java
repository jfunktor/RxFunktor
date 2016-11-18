package org.jfunktor.core.rxresource.tests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by vj on 18/11/16.
 */
public class POJOToMapConversionTests {

    @Test
    public void testSimpleMapToPojoConversion(){
        ObjectMapper mapper = new ObjectMapper();
        Asset asset1 = new Asset("1","Vessel");
        asset1.setQuantity(500);
        asset1.setIntroductionDate(java.util.Calendar.getInstance().getTime());
        Location location = new Location();
        location.setLocationName("SIN");
        location.setLocationType("City");
        location.setAddress(new Address("Gant Street","St. Elizabeth Garden","Singapore","Singapore"));
        asset1.setLocation(location);
        Map convertedMap = mapper.convertValue(asset1, Map.class);

        System.out.println("Converted Map : "+convertedMap);

        assertTrue("ConvertedMap does not contain the expected attribute 'id'",convertedMap.containsKey("id"));
        assertTrue("ConvertedMap does not contain the expected attribute 'type'",convertedMap.containsKey("type-name"));

        Asset asset = mapper.convertValue(convertedMap, Asset.class);

        System.out.println("Converted Asset : "+asset);
        assertTrue("Converted Asset does not contain the expected attribute 'id'",asset.getId() != null);
        assertTrue("Converted Asset does not contain the expected attribute 'type'",asset.getType() != null);
    }
}


class Asset{

    private String id;

    @JsonProperty("type-name")
    private String type;

    private Integer quantity;
    private Map attributes;
    private Location location;
    private Date introductionDate;

    Asset(){};

    Asset(String id,String type){
        this.id = id;
        this.type = type;
        attributes = new HashMap<String,Object>();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map getAttributes() {
        return attributes;
    }

    public void setAttributes(Map attributes) {
        this.attributes = attributes;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Date getIntroductionDate() {
        return introductionDate;
    }

    public void setIntroductionDate(Date introductionDate) {
        this.introductionDate = introductionDate;
    }

    @Override
    public String toString() {
        return "Asset{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", quantity=" + quantity +
                ", attributes=" + attributes +
                ", location=" + location +
                ", introductionDate=" + introductionDate +
                '}';
    }
}

class Location{
    private String locationName;
    private String locationType;
    private Address address;

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Location{" +
                "locationName='" + locationName + '\'' +
                ", locationType='" + locationType + '\'' +
                ", address=" + address +
                '}';
    }
}
