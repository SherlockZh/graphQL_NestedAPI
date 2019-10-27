#Nested API using GraphQL and integrated with Springboot.

Query:
```Json
{
  books(pageCount: 223){
    id
    name
    pageCount
    author(authorName: "John"){
      id
      name
      address(zipcode: "1234"){
        id
        city
        road
        zipcode
      }
    }
  }
}
```

Output
```Json
{
  "data": {
    "books": [
      {
        "id": "book1",
        "name": "book-John1",
        "pageCount": 223,
        "author": [
          {
            "id": "auth1",
            "name": "John",
            "address": {
              "id": "addr1",
              "city": "Beijing",
              "road": "ChaoYang",
              "zipcode": "1234"
            }
          },
          {
            "id": "auth3",
            "name": "Bill",
            "address": {
              "id": "addr1",
              "city": "Beijing",
              "road": "ChaoYang",
              "zipcode": "1234"
            }
          }
        ]
      },
      {
        "id": "book2",
        "name": "book-John1",
        "pageCount": 223,
        "author": [
          {
            "id": "auth1",
            "name": "John",
            "address": {
              "id": "addr1",
              "city": "Beijing",
              "road": "ChaoYang",
              "zipcode": "1234"
            }
          }
        ]
      },
      {
        "id": "book3",
        "name": "book-John1",
        "pageCount": 223,
        "author": [
          {
            "id": "auth1",
            "name": "John",
            "address": {
              "id": "addr1",
              "city": "Beijing",
              "road": "ChaoYang",
              "zipcode": "1234"
            }
          }
        ]
      },
      {
        "id": "book4",
        "name": "book-John2",
        "pageCount": 223,
        "author": []
      }
    ]
  }
}
```