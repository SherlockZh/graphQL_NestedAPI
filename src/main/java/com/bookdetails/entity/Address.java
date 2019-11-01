package com.bookdetails.entity;

public class Address {

    private String id;
    private String city;
    private String road;
    private String zipcode;

    public Address(String id, String city, String road, String zipcode) {
        this.id = id;
        this.city = city;
        this.road = road;
        this.zipcode = zipcode;
    }

    public Object get(String param){
        switch (param) {
            case "zipcode":
                return getZipcode();
            case "addressId":
                return getId();
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
}
