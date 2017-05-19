package com.company.project.Utilities;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by pavankovurru on 5/10/17.
 */
public class JSON_Utilities {

    public static String jsonToString(String filepath) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;

        try {
            Object obj = parser.parse(new FileReader(filepath));
            jsonObject = (JSONObject) obj;
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        catch (ParseException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
     }


    //  ************   READ FROM JSON EXAMPLES - JSON SIMPLE   ************  //

    //    {
    //        "age":29,
    //            "name":"test",
    //            "messages":["msg 1","msg 2","msg 3"]
    //    }


    //    Object obj = parser.parse(new FileReader("JSON FILE PATH"));
    //
    //    JSONObject jsonObject = (JSONObject) obj;
    //            System.out.println(jsonObject);   //prints json object

    //    String name = (String) jsonObject.get("name");
    //    long age = (Long) jsonObject.get("age");

    //    JSONArray msg = (JSONArray) jsonObject.get("messages");
    //    Iterator<String> iterator = msg.iterator();
    //            while (iterator.hasNext()) {
    //        System.out.println(iterator.next());
    //    }


    //  ************  WRITE TO JSON EXAMPLES -- gson jsonWriter   ************  //

//import com.google.gson.stream.JsonWriter;

//    JsonWriter writer;
//     try {
//        writer = new JsonWriter(new FileWriter("c:\\user.json"));
//
//        writer.beginObject(); // {
//        writer.name("name").value("pavan"); // "name" : "pavan"
//        writer.name("age").value(29); // "age" : 29
//
//        writer.name("messages"); // "messages" :
//        writer.beginArray(); // [
//        writer.value("msg 1"); // "msg 1"
//        writer.value("msg 2"); // "msg 2"
//        writer.value("msg 3"); // "msg 3"
//        writer.endArray(); // ]
//
//        writer.endObject(); // }
//        writer.close();
//
//        System.out.println("Done");
//
//    } catch (IOException e) {
//        e.printStackTrace();
//    }








}
