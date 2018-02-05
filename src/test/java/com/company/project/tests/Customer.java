package com.company.project.tests;

import com.company.project.utilities.JSON_Utilities;
import com.company.project.utilities.MySQL;
import com.company.project.staticData.Constants;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/** Created by pavankovurru on 5/19/17. */
public class Customer {

  // Initiating Logger Object
  private static final Logger log = LogManager.getLogger();

  private static String randomvalue;
  private static String customerID;
  private static String serviceEndPoint;
  private static String auth;
  private static String customerRequestEndPoint;

  private ArrayList<String> dbResult = new ArrayList<>(); // this arraylist stores DB query result

  @BeforeClass(alwaysRun = true)
  @Parameters({"serviceEndPoint", "auth", "customerRequestEndPoint"})
  public void preTestSteps(String serviceEndPoint) {

    // Initializing data
    this.serviceEndPoint = serviceEndPoint;
    this.auth = auth;
    this.customerRequestEndPoint = customerRequestEndPoint;
    randomvalue = String.valueOf(Instant.now().getEpochSecond());
  }

  //  **** CREATING A CUSTOMER **** //

  @Test(priority = 1)
  public void verify_CreateCustomer() {

    // Request Details
    String requestURL = serviceEndPoint + customerRequestEndPoint;
    String requestBody =
        JSON_Utilities.jsonToString(
                System.getProperty("user.dir")
                    + "/src/main/java/com/company/project/jsonFiles/customer.json")
            .replaceAll("nameToBeChanged", randomvalue);

    // Printing Request Details
    log.debug("REQUEST-URL:POST-" + requestURL);
    log.debug("REQUEST-BODY:" + requestBody);

    // Extracting response after status code validation
    Response response =
        given()
            .header("Content-Type", "application/json")
            .header("Authorization", auth)
            .request()
            .body(requestBody)
            .post(requestURL)
            .then()
            .statusCode(201)
            .extract()
            .response();

    // printing response
    log.info("RESPONSE:" + response.asString());

    // capturing created customer ID
    customerID = response.then().extract().path("data.id").toString();
    log.info("Created New Customer - ID - " + customerID);

    // JSON response Pay load validations
    response.then().body("data.id", is(20));
    response.then().body("data.created", notNullValue());
    response.then().body("data.name", is(randomvalue));

    // MySQL DB Validation
    dbResult.clear();

    String sqlQuery = "select name from customer where name=" + "\"" + randomvalue + "\"";
    dbResult = MySQL.query_Post_connection_To_MySQL_Via_JumpServer(sqlQuery, serviceEndPoint);
    Assert.assertTrue(
        dbResult.contains(randomvalue),
        "Customer table does not have an entry name =" + randomvalue);
  }

  // **** CREATING A CUSTOMER - NEGATIVE TEST CASES **** //

  @Test(priority = 2)
  @Parameters({"serviceEndPoint", "customerRequestEndPoint"})
  public void verify_CreateCustomer_InvalidAuth(
      String serviceEndPoint, String customerRequestEndPoint) {

    // Request Details
    String requestURL = serviceEndPoint + customerRequestEndPoint;
    String requestBody =
        JSON_Utilities.jsonToString(
                System.getProperty("user.dir")
                    + "/src/main/java/com/company/project/jsonFiles/customer.json")
            .replaceAll("nameToBeChanged", randomvalue);

    // Printing Request Details
    log.debug("REQUEST-URL:POST-" + requestURL);
    log.debug("REQUEST-BODY:" + requestBody);

    // Extracting response after status code validation
    Response response =
        given()
            .header("Content-Type", "application/json")
            .header("Authorization", Constants.INVALID_AUTH)
            .request()
            .body(requestBody)
            .post(requestURL)
            .then()
            .statusCode(401)
            .extract()
            .response();

    // printing response
    log.info("RESPONSE:" + response.asString());

    // JSON response Pay load validations
    response.then().body("message", is("INVALID TOKEN"));
  }

  @Test(priority = 3)
  @Parameters({"serviceEndPoint", "auth", "customerRequestEndPoint"})
  public void verify_CreateCustomer_WithoutRequestBody(
      String serviceEndPoint, String auth, String customerRequestEndPoint) {

    // Request Details
    String requestURL = serviceEndPoint + customerRequestEndPoint;

    // Printing Request Details
    log.debug("REQUEST-URL:POST-" + requestURL);

    // Extracting response after status code validation
    Response response =
        given()
            .header("Content-Type", "application/json")
            .header("Authorization", auth)
            .request()
            .post(requestURL)
            .then()
            .statusCode(400)
            .extract()
            .response();

    // printing response
    log.info("RESPONSE:" + response.asString());
  }

  //  **** GETTING ALL CUSTOMERS **** //

  @Test(priority = 4)
  @Parameters({"serviceEndPoint", "auth", "customerRequestEndPoint"})
  public void verify_GetAllCustomers(
      String serviceEndPoint, String auth, String customerRequestEndPoint) {

    // Request Details
    String requestURL = serviceEndPoint + customerRequestEndPoint;

    // Printing Request Details
    log.debug("REQUEST-URL:GET-" + requestURL);

    // Extracting response after status code validation
    Response response =
        given()
            .header("Content-Type", "application/json")
            .header("Authorization", auth)
            .request()
            .get(requestURL)
            .then()
            .statusCode(200)
            .extract()
            .response();

    // printing response
    log.info("RESPONSE:" + response.asString());

    // MySQL DB data capture
    dbResult.clear();

    String sqlQuery_id = "select id from customer";
    dbResult = MySQL.query_Post_connection_To_MySQL_Via_JumpServer(sqlQuery_id, serviceEndPoint);
    ArrayList<String> id = new ArrayList<>();
    for (String i : dbResult) {
      id.add(i);
    }

    dbResult.clear();

    String sqlQuery_name = "select name from customer";
    dbResult = MySQL.query_Post_connection_To_MySQL_Via_JumpServer(sqlQuery_name, serviceEndPoint);
    ArrayList<String> name = new ArrayList<>();
    for (String i : dbResult) {
      name.add(i);
    }

    // JSON response Pay load validations
    for (int i = 0;
        i <= response.then().extract().jsonPath().getList("data.statuses").size() - 1;
        i++) {
      Assert.assertTrue(
          response
              .then()
              .extract()
              .path("data.statuses.id[" + i + "]")
              .toString()
              .equals(id.get(i)));
      Assert.assertTrue(
          response
              .then()
              .extract()
              .path("data.statuses.name[" + i + "]")
              .toString()
              .equals(name.get(i)));
    }
  }
}
