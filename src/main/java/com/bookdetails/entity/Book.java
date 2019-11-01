package com.bookdetails.entity;

import java.util.List;

public class Book {

    private String id;
    private String name;
    private Integer pageCount;
    private List<String> authorIds;

    public Book(String id, String name, int pageCount, List<String> authorIds) {
        this.id = id;
        this.name = name;
        this.pageCount = pageCount;
        this.authorIds = authorIds;
    }

    //统一判断查询条件
    public Object get(String param){
        switch (param) {
            case "bookId":
                return getId();
            case "bookName":
                return getName();
            case "pageCount":
                return getPageCount();
            case "authorIds":
                return getAuthorIds();
        }
        return null;
    }

    public List<String> getAuthorIds() {
        return authorIds;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPageCount() {
        return pageCount;
    }

    public String toString(){
        return "id: " + getId() + ", name: " + getName() + ", pageCount: " + getPageCount() + ", authorId: " + getAuthorIds();
    }
}
