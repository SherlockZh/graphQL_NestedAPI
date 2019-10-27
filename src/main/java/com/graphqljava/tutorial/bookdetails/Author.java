package com.graphqljava.tutorial.bookdetails;

public class Author {
    private String id;
    private String name;
    private String addressId;

    public Object get(String param){
        switch (param) {
            case "authorId":
                return getId();
            case "authorName":
                return getName();
            case "addressId":
                return getAddressId();
        }
        return null;
    }

    public Author(String id, String name, String addressId) {
        this.id = id;
        this.name = name;
        this.addressId = addressId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setaddressId(String addressId) {
        this.addressId = addressId;
    }

    public String toString(){
        return "AuthorId: " + getId() + ", AuthorName: " + getName() + ", AddressId: " + getAddressId();
    }
}
