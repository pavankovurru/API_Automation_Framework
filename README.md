 # API AUTOMATION 
 
 This project uses REST ASSURED (Java DSL) to query an endpoint, Validate ResponseCode and to validate JSON payload.
 MySQL JDBC driver is then used to connect and query the database to validate accuracy of the API response.
 
 `SshUtlities` - will use jsch java library to multi hop into server via jump server, executes shell command and returns response.  
 
 `AeroSpikeUtility` - will return areospike result based on the unique key provided to it  
 
 
 ## FRAME WORK STACK 
 ```
 1. REST ASSURED 5.5.0
 2. JSON-SIMPLE 
 3. JDBC MYSQL DRIVER
 4. LOG4J 2.23.1  
 5. TestNG 7.10.2  
 6. Gradle 8.5
 ```
 
 ## DATA 
 ```
 1. Test environment related data is put in `testng xml's` so that that an environment related xml can be invoked by a gradle task which can be invoked from CI (Jenkins).
 2. JSON files used in POST AND PUT operations are at `/API_Automation_Framework/src/main/java/com/company/project/jsonFiles/*`.
 3. Data which is static not dependent on environment can be put in `/API_Automation_Framework/src/main/java/com/company/project/staticData/Constants.java`.
 ```
 
 
  ## TIPS & TRICKS 
  
  1. Make sure that you have these static imports in your test class file :
  ````   
     import static io.restassured.RestAssured.*;
     import static io.restassured.matcher.RestAssuredMatchers.*;
     import static org.hamcrest.Matchers.*;
     import io.restassured.response.Response;
````

  2. Capturing response after status code validation
  
  ```
 Response response =
                given()
                        .header("Content-Type", "application/json")
                        .header("Authorization", auth)
                        .queryParam("key", “value”)
                        .request()
                        .body(requestBody)
                        .get(requestURL)
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();
```

  3. Logging Response 

```
log.info("RESPONSE:" + response.asString());
```

  4. Capturing data from response
  
  ```
  String id = response.then().extract().path("data.id").toString();
  log.info(" ID - " + id);
  ```
  
  5. JSON Payload Validations.
  
  `NOTE : dot notation can be used to traverse down the json (EX: data.id , data.customers.statusId)`
    
  ````
  - response.then().body("data.Id", is(12345)); -- validating integer
  - response.then().body("data.Id", is(“12345”));  -- validating String
  - response.then().body("data.created", notNullValue());  -- validating that value is not null
  - response.then().body("id.length()",is(equalTo(36)));
   
  - Assert.assertTrue(response.then().extract().path("token").toString().length()==36);
  - Assert.assertEquals(response.then().extract().path("default").toString(),"false");
  - Assert.assertTrue(response.then().extract().path("name.text").toString().matches(".*?Test.*"));
  - Assert.assertTrue(response.then().extract().path("name.slot").toString().matches(".*?1.*"));
  
  
  NOTE : to validate a float value use  is(12345f) instead of  is(12345) since the later would assume that user 
  is trying to validate a double value 
  
  Matching a prefix
  - response.then().body("data.startDate", startsWith(startDate));


 ````
 
 
  ##### ARRAY VALIDATIONS
 ````
  - response.then().body("data.Ids", containsInAnyOrder(1,10,100,200,1000));  //array has values
  - response.then().body("data.deviceIds", is(empty()));    //empty array
  - response.then().body("flatten().any{it.containsKey('id')}",is(true));
  - response.then().extract().jsonPath().getList("data.size").size();  -- size of an array 
  
  EXAMPLE :
  
         for (int i = 0; i <= response.then().extract().jsonPath().getList("data.sixe").size() - 1; i++) {
              Assert.assertTrue(response.then().extract().path("data.size.id[" + i + "]").toString().equals(id.get(i)));
              Assert.assertTrue(response.then().extract().path("data.size.name[" + i + "]").toString().equals(name.get(i)));
          }

  - response.then().body("data.customers.statusId.flatten()", everyItem(not(1000)));  -- every statusID is not 1000    
  - response.then().body("data.customers.flatten().any {it.containsKey('name') }", is(true)); --  every element in data.customers array has key 'name'
  ````
  
  6.FORM Data
   
  When sending larger amount of data to the server it's common to use the multipart form data technique. Rest Assured provide methods called multiPart that allows you to specify a file, byte-array, input stream or text to upload. In its simplest form you can upload a file like this:
  
  ```given().
          multiPart(new File("/path/to/file")).
          post("/upload");
   ```
  
  The control name in this case is the name of the input tag with name "file". If you have a different control name then you need to specify it:
  
  ```given().
          multiPart("controlName", new File("/path/to/file")).
          post("/upload");
   ```
  
  It's also possible to supply multiple "multi-parts" entities in the same request:
  
  ```
  given().
          multiPart("controlName1", new File("/path/to/file")).
          multiPart("controlName2", "my_file_name.txt", someData).
          multiPart("controlName3", someJavaObject, "application/json").
  when().
          post("/upload");
   ```
  
  
  
  7.DATA BASE VALIDATIONS
  
  `dbResult.clear();  -- do this before every db connection to make sure that past data is removed from the arraylist dbResult.`
```
  -    String sqlQuery = "select name from customer where name=" + "\"" + randomvalue + "\"";
       dbResult = MySQL.query_Post_connection_To_MySQL_Via_JumpServer(sqlQuery, serviceEndPoint);
       Assert.assertTrue(
           dbResult.contains(randomvalue),
           "Customer table does not have an entry with name =" + randomvalue);
  ```
  
  ````
  -    String sqlQuery = "select count(name) from customer where not status_id=" + "\"" + 100 + "\"";
       dbResult = MySQL.query_Post_connection_To_MySQL_Via_JumpServer(sqlQuery, serviceEndPoint);
       Assert.assertTrue(Integer.valueOf(dbResult.get(0)) == size);
  ````
  ```
  -    String sqlQuery = "select count(name) from customer where not status_id=" + "\"" + 100 + "\"";
       dbResult = MySQL.query_Post_connection_To_MySQL_Via_JumpServer(sqlQuery, serviceEndPoint);

       if (Integer.valueOf(dbResult.get(0)) > limit || Integer.valueOf(dbResult.get(0)) == limit) {
       Assert.assertTrue(size == limit);
       }

       if (Integer.valueOf(dbResult.get(0)) < limit) {
       Assert.assertTrue(Integer.valueOf(dbResult.get(0)) == size);
       }
  ```
  ```
  -     String sqlQuery "select status_id from customer where id=" + "\"" + customerID + "\"";
        dbResult = MySQL.query_Post_connection_To_MySQL_Via_JumpServer(sqlQuery, serviceEndPoint);
        Assert.assertTrue(
               Integer.parseInt(dbResult.get(0)) == 100,
               "Customer " + customerID + " is not deleted");
  ```
  ```
  -   String sqlQuery_id = "select id from tablename";
      dbResult = MySQL.query_Post_connection_To_MySQL_Via_JumpServer(sqlQuery_id, serviceEndPoint);
      ArrayList<String> id = new ArrayList<>();
             
              for (String i : dbResult) {
                  id.add(i);
              }
      
              dbResult.clear();
      
              String sqlQuery_name = "select name from tablename";
              dbResult = MySQL.query_Post_connection_To_MySQL_Via_JumpServer(sqlQuery_name, serviceEndPoint);
              ArrayList<String> name = new ArrayList<>();
              for (String i : dbResult) {
                  name.add(i);
              }
      
      
   for (int i = 0; i <= response.then().extract().jsonPath().getList("data.statuses").size() - 1; i++) {
        Assert.assertTrue(response.then().extract().path("data.statuses.id[" + i + "]").toString().equals(id.get(i)));
       Assert.assertTrue(response.then().extract().path("data.statuses.name[" + i + "]").toString().equals(name.get(i)));
       }
  ```
  
  
  
