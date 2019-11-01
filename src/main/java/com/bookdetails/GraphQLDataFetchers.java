package com.bookdetails;

import com.bookdetails.util.Utils;
import com.bookdetails.entity.Address;
import com.bookdetails.entity.Author;
import com.bookdetails.entity.Book;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class GraphQLDataFetchers {

    private static List<Book> bookList = new ArrayList<>();
    private static List<Author> authorList = new ArrayList<>();
    private static List<Address> addressList = new ArrayList<>();
    private Utils utils = new Utils();

    private static List<String> bookParams = new ArrayList<>(
            Arrays.asList("bookId", "bookName", "pageCount"));

    private static List<String> authorParams = new ArrayList<>(
            Arrays.asList("authorId", "authorName"));

    private static List<String> addressParams = new ArrayList<>(
            Arrays.asList("zipcode"));

    public GraphQLDataFetchers(){
        bookList.add(new Book("book1", "book-John1", 223, Arrays.asList("auth1", "auth2", "auth3")));
        bookList.add(new Book("book2", "book-John1", 223, Arrays.asList("auth1", "auth2")));
        bookList.add(new Book("book3", "book-John1", 123, Arrays.asList("auth1", "auth4")));
        bookList.add(new Book("book4", "book-John2", 223, Arrays.asList("auth2")));
        bookList.add(new Book("book5", "book5-name", 223, Arrays.asList("auth4")));
        bookList.add(new Book("book6", "book6-name", 200, Arrays.asList("auth4")));

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
            List<Book> bookResultList = new ArrayList<>();

            if(arguments == null || arguments.size() == 0){
                bookResultList.addAll(bookList); //
                if(nodeFields == null || nodeFields.size() == 0)
                    return bookList;
            }

            //去掉结果中的null项
            if(arguments != null && arguments.size() > 0) {
                 utils.filterBookList(bookList, bookResultList, arguments);
            }
            nodeFields.forEach(selectedField -> {
                if(selectedField.getArguments().size() > 0) {
                    Map<String, Object> authorArguments = selectedField.getArguments();
                    //去掉当前层无法接收的参数
                    utils.remainCurrentLayerArguments(authorArguments, authorParams);
                    utils.filterAuthorList(bookResultList, authorArguments, authorList);
                }
            });

            return bookResultList;
        };
    }


    public DataFetcher getAuthorDataFetcher() {
        return environment -> {
            //这里因为是通过Book查询Author数据的子查询，所以dataFetchingEnvironment.getSource()中封装了Book对象的全部信息
            //即GraphQL中每个字段的DataFetcher都是以自顶向下的方式调用的，父字段的结果是子DataFetcherEnvironment的source属性。
            Map<String, Object> preLevelArguments = environment.getArguments();

            Book book = environment.getSource();
            //首先取到所有book的authors
            List<Author> authors = utils.getAuthorByBook(book, authorList);

            if(authors == null){
                return null;
            }

            Iterator<Author> authorIterator = authors.iterator();

            DataFetchingFieldSelectionSet selectionSet = environment.getSelectionSet();
            List<SelectedField> nodeFields = selectionSet.getFields();
            nodeFields.forEach(selectedField -> {
                if(selectedField.getArguments().size() > 0){
                    Map<String, Object> nextLevelArguments = selectedField.getArguments();
                    //匹配与下层参数对应的author
                    utils.filterAddressList(authorIterator, nextLevelArguments, addressList);
                }
            });
            //查询参数不为空，返回按参数过滤后的作者信息
            utils.removeInvalidAuthor(authorIterator, preLevelArguments);

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
