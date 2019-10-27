package com.graphqljava.tutorial.bookdetails;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GraphQLDataFetchers {

    private static List<Book> bookList = new ArrayList<>();
    private static List<Author> authorList = new ArrayList<>();
    private static List<Address> addressList = new ArrayList<>();

    private static List<String> bookParams = new ArrayList<>(
            Arrays.asList("bookId", "bookName", "pageCount"));

    private static List<String> authorParams = new ArrayList<>(
            Arrays.asList("authorId", "authorName"));

    private static List<String> addressParams = new ArrayList<>(
            Arrays.asList("zipcode"));

    public GraphQLDataFetchers(){
        bookList.add(new Book("book1", "book-John1", 223, Arrays.asList("auth1", "auth2", "auth3")));
        bookList.add(new Book("book2", "book-John1", 223, Arrays.asList("auth1", "auth2")));
        bookList.add(new Book("book3", "book-John1", 223, Arrays.asList("auth1", "auth4")));
        bookList.add(new Book("book4", "book-John2", 223, Arrays.asList("auth2")));
        bookList.add(new Book("book5", "book-John2", 223, Arrays.asList("auth4")));
        bookList.add(new Book("book6", "book6", 200, Arrays.asList("auth4")));

        authorList.add(new Author("auth1", "John", "addr1"));
        authorList.add(new Author("auth2", "John", "addr2"));
        authorList.add(new Author("auth3", "Bill", "addr3"));
        authorList.add(new Author("auth4", "Amy", "addr2"));

        addressList.add(new Address("addr1", "Beijing", "ChaoYang", "1234"));
        addressList.add(new Address("addr2", "Beijing", "ChaoYang", "1122"));
        addressList.add(new Address("addr3", "Shenzhen", "lu", "1234"));

    }

    public DataFetcher getAllBooksDataFetcher(){
        return environment -> {
            Map<String, Object> arguments = environment.getArguments();
            DataFetchingFieldSelectionSet selectionSet = environment.getSelectionSet();
            List<SelectedField> nodeFields = selectionSet.getFields();
            if(arguments == null || arguments.size() == 0){
                if(nodeFields == null || nodeFields.size() == 0)
                    return bookList;
            }

            List<Book> bookResultList = bookList;

            //这一步filter是为了去掉结果中的null项
            if(arguments != null && arguments.size() > 0)
                filterBookList(bookList, bookResultList, arguments);

            nodeFields.forEach(selectedField -> {
                if(selectedField.getArguments().size() > 0) {
                    Map<String, Object> authorArguments = selectedField.getArguments();
                    //去掉当前层无法接收的参数
                    for(String key : authorArguments.keySet()){
                        if(!authorParams.contains(key)){
                            authorArguments.remove(key);
                        }
                    }
                    filterAuthorList(bookResultList, authorArguments);
                }

//                DataFetchingFieldSelectionSet innerSelectionSet = selectedField.getSelectionSet();
//                List<SelectedField> innerNodeFields = innerSelectionSet.getFields();

//                innerNodeFields.forEach(innerSelectedField -> {
//                    if(innerSelectedField.getArguments().size() > 0) {
//                        Map<String, Object> addressArguments = innerSelectedField.getArguments();
//
//                        for(String key : addressArguments.keySet()){
//                            if(!addressParams.contains(key)){
//                                addressArguments.remove(key);
//                            }
//                        }
//                        filterAddressList(bookResultList, addressArguments);
//                    }
//                });

            });

            return bookResultList;
        };
    }


    public DataFetcher getAuthorDataFetcher() {
        return environment -> {
            //这里因为是通过Book查询Author数据的子查询，所以dataFetchingEnvironment.getSource()中封装了Book对象的全部信息
            //即GraphQL中每个字段的Datafetcher都是以自顶向下的方式调用的，父字段的结果是子Datafetcherenvironment的source属性。
            Map<String, Object> arguments = environment.getArguments();
            System.out.println("author fetche arguments: " + arguments);

            Book book = environment.getSource();
            List<Author> authors = getAuthorByBook(book);

            if(authors == null){
                return null;
            }

            //查询参数为空，则返回每本book对应的author
            if(arguments == null || arguments.size() == 0){
                return authors;
            }

            Iterator<Author> authorIterator = authors.iterator();

            DataFetchingFieldSelectionSet selectionSet = environment.getSelectionSet();
            List<SelectedField> nodeFields = selectionSet.getFields();
            nodeFields.forEach(selectedField -> {
                if(selectedField.getArguments().size() > 0){
                    Map<String, Object> nextLevelArguments = selectedField.getArguments();

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
            });
            //查询参数不为空，返回按参数过滤后的作者信息
            while (authorIterator.hasNext()){
                Author author = authorIterator.next();
                for(String key : arguments.keySet()){
                    if(!author.get(key).equals(arguments.get(key))){
                        authorIterator.remove();
                        break;
                    }
                }
            }

            return authors;
        };
    }

    public DataFetcher getAuthorAddressDataFetcher(){
        return environment -> {
            Map<String, Object> arguments = environment.getArguments();
            Author author = environment.getSource();
            Address address = addressList.stream().filter(a -> a.getId().equals(author.getAddressId())).findAny().orElse(null);

            if(address == null){
                return null;
            }

            if(arguments == null || arguments.size() == 0){
                return address;
            }

            for(String key : arguments.keySet()){
                return addressList
                        .stream()
                        .filter(a -> a.get(key).equals(address.get(key)))
                        .findAny()
                        .orElse(null);
            }

            return null;
        };
    }

    private void filterBookList(List<Book> bookList, List<Book> bookResultList, Map<String, Object> arguments) {
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

    private void filterAuthorList(List<Book> bookResultList, Map<String, Object> arguments) {
        Iterator<Book> bookIterator = bookResultList.iterator();
        while (bookIterator.hasNext()){
            Book book = bookIterator.next();
            List<Author> authors = getAuthorByBook(book);

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

    private void filterAddressList(List<Book> bookResultList, Map<String, Object> addressArguments) {
        Iterator<Book> bookIterator = bookResultList.iterator();
        while (bookIterator.hasNext()){
            Book book = bookIterator.next();
            List<Author> authors = getAuthorByBook(book);
            Map<Author, Address> author2Address = getAddressByAuthor(authors);

            if(author2Address == null){
                bookIterator.remove();
                return;
            }

            for(String key : addressArguments.keySet()){
                for(Author author : authors){
                    if(!author2Address.get(author).get(key).equals(addressArguments.get(key))){
                        bookIterator.remove();
                        break;
                    }
                }
            }
        }
    }

    private List<Author> getAuthorByBook(Book book){
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

    private Map<Author, Address> getAddressByAuthor(List<Author> authors){
        Map<Author, Address> res = new HashMap<>();
        for(Author author : authors){
            String authorId = author.getId();
            addressList.stream().filter(a -> a.getId().equals(authorId)).findFirst().ifPresent(address -> res.put(author, address));
        }
        return res;
    }


    public DataFetcher getBookByPageCount() {
        return environment -> {
            Integer p = environment.getArgument("p");
            if(p != null){
                return null;
            }
            return bookList;
        };
    }
}
