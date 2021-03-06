package hkp.logimap;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by kryształ on 03.12.2015.
 */
public class Delivery implements Serializable{
    Driver driver;
    Vehicle vehicle;
    Route route;
    Integer id, state;
    ArrayList<Package> packages;
    ArrayList<LatLng> directions;
    ArrayList<LatLng> gps;
    HashMap<Integer, Location> locations;
    Boolean finished, history;


    Delivery(JSONObject delivery) {
        packages  = new ArrayList<>();
        directions = new ArrayList<>();
        gps = new ArrayList<>();
        locations = new HashMap<>();

        try {
            this.id = delivery.getInt("id");
            this.driver = new Driver(delivery.getJSONObject("driver"));
            this.vehicle = new Vehicle(delivery.getJSONObject("vehicle"));
            this.route = new Route(delivery.getJSONObject("route"), locations);
            this.state = delivery.getInt("status");
            this.finished = false;
            this.history = false;

            JSONArray _packages = delivery.getJSONArray("package");

            for(int i=0; i < _packages.length(); ++i) {
                this.packages.add(new Package(_packages.getJSONObject(i)));
            }

            //Minimum deadline for packages in location
            ArrayList<Time> times = new ArrayList<>();
            for(Location l : locations.values()) {
                for (Package p : packages)
                    if (p.destination == l.id)
                        times.add(p.deadline);
                Collections.sort(times);
                l.deadline = times.get(0);
                times.clear();
            }

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }
    }

    public Package getPackage(Integer id) {
        for(Package p: this.packages)
            if(p.id == id)
                return p;

        return  null;
    }

    public void checkLocations() {
        this.finished = true;

        for(Location l : this.locations.values())
            if(!l.finished) {
                this.finished = false;
                break;
            }
    }

    public void finish(MyApplication application) {
        this.state = 3;

        String new_json = getJSON();
        application.addPUT(new PUT_Request("orders/" + this.id, new_json));

        saveDeliveryToFile("", "delivery" + this.id, application);
    }

    public String getJSON() {
        try {
            JSONObject delivery_json = new JSONObject();

            delivery_json.put("id", this.id);
            delivery_json.put("driver", this.driver.getJSON());
            delivery_json.put("vehicle", this.vehicle.getJSON());
            delivery_json.put("route", route.getJSON(this.locations.values()));
                JSONArray packages_json = new JSONArray();
                for(Package p : this.packages)
                    packages_json.put(p.getJSON());
            delivery_json.put("package", packages_json);
//            // google directions to json
//            JSONArray directions_json = new JSONArray();
//            for(LatLng p : this.directions) {
//                JSONObject point = new JSONObject();
//                point.put("lat", p.latitude);
//                point.put("lng", p.longitude);
//                directions_json.put(point);
//            }
//            delivery_json.put("directions",delivery_json);
//            // gps points to json
//            JSONArray gps_json = new JSONArray();
//            for(LatLng p : this.gps) {
//                JSONObject point = new JSONObject();
//                point.put("lat", p.latitude);
//                point.put("lng", p.longitude);
//               gps_json.put(point);
//            }
//            delivery_json.put("waypoints",gps_json);
            delivery_json.put("status", state);

            return delivery_json.toString();
        }catch (Exception e) {
            Log.e("ERROR", e.getMessage(),e);
            return null;
        }
    }

    public void saveDeliveryToFile(String json, String filename, Context context) {
        if(filename=="")
            filename = "delivery.json";

        if(json=="")
            json = getJSON();
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, context.MODE_PRIVATE));
            outputStreamWriter.write(json);
            outputStreamWriter.close();
        }


        catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }
    }
}
