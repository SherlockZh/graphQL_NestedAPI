package com.graphqljava.tutorial.bookdetails;

import java.util.*;

public class Utils {
    public void filterBookList(List<Book> bookList, List<Book> bookResultList, Map<String, Object> arguments) {
        for(Book book : bookList){
            boolean isValid = true;
            for(String key : arguments.keySet()){
                if(isValid && !book.get(key).equals(arguments.get(key))){
                    isValid = false;
                }
            }
            if(isValid)
                bookResultList.add(book);
        }
    }

    public void filterAuthorList(List<Book> bookResultList, Map<String, Object> arguments, List<Author> authorList) {
        Iterator<Book> bookIterator = bookResultList.iterator();
        while (bookIterator.hasNext()){
            Book book = bookIterator.next();
            List<Author> authors = getAuthorByBook(book, authorList);

            if(authors == null){
                bookIterator.remove();
                return;
            }
            boolean flag = false;
            for(String key : arguments.keySet()){
                for(Author author : authors){
                    if(author.get(key).equals(arguments.get(key))){
                        flag = true;
                    }
                }
                if(!flag)
                    bookIterator.remove();
            }

        }
    }

    public void filterAddressList(Iterator<Author> authorIterator, Map<String, Object> nextLevelArguments, List<Address> addressList) {
        while (authorIterator.hasNext() && !nextLevelArguments.isEmpty()){
            Author author = authorIterator.next();
            Address address = addressList.stream().filter(a -> a.getId().equals(author.getAddressId())).findFirst().orElse(null);
            if(address == null){
                break;
            }
            boolean flag = false;
            for(String key : nextLevelArguments.keySet()){
                if(address.get(key).equals(nextLevelArguments.get(key))){
                    flag = true;
                }
            }
            if(!flag)
                authorIterator.remove();
        }
    }

    public List<Author> getAuthorByBook(Book book, List<Author>authorList){
        List<Author> res = new ArrayList<>();
        List<String> authorIds = book.getAuthorIds();

        for(String id : authorIds){
            for(Author author : authorList){
                if(author.getId().equals(id)){
                    res.add(author);
                }
            }
        }
        return res;
    }

    public Map<Author, Address> getAddressByAuthor(List<Author> authors, List<Address> addressList){
        Map<Author, Address> res = new HashMap<>();
        for(Author author : authors){
            String authorId = author.getId();
            addressList.stream().filter(a -> a.getId().equals(authorId)).findFirst().ifPresent(address -> res.put(author, address));
        }
        return res;
    }

    public void remainCurrentLayerArguments(Map<String, Object> authorArguments, List<String> authorParams) {
        for(String key : authorArguments.keySet()){
            if(!authorParams.contains(key)){
                authorArguments.remove(key);
            }
        }
    }

    public void removeInvalidAuthor(Iterator<Author> authorIterator, Map<String, Object> arguments) {
        while (authorIterator.hasNext()){
            Author author = authorIterator.next();
            for(String key : arguments.keySet()){
                if(!author.get(key).equals(arguments.get(key))){
                    authorIterator.remove();
                    break;
                }
            }
        }
    }
}
