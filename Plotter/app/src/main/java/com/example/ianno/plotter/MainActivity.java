package com.example.ianno.plotter;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class MainActivity extends FragmentActivity implements OnClickListener,OnMapReadyCallback{

    GoogleMap map;
    ArrayList<LatLng> markerPoints;
//    public void sendMessage(View view) {
//        Intent intent = new Intent(this, barcodenGPS.class);
////        EditText editText = (EditText) findViewById(R.id.editText);
////        String message = editText.getText().toString();
////        intent.putExtra(EXTRA_MESSAGE, message);
//        startActivity(intent);
//    }

    private void initializeMap() {
        if (map == null) {
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
     //  setUpMap();// do your map stuff here
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initializeMap();

        // Initializing
        markerPoints = new ArrayList<LatLng>();

        // Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);


        // Getting reference to Button
        Button btnDraw = (Button)findViewById(R.id.btn_draw);

        // Getting Map for the SupportMapFragment
        map = fm.getMap();

        // Enable MyLocation Button in the Map
        map.setMyLocationEnabled(true);

        // Setting onclick event listener for the map
        map.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                // Already 10 locations with 8 waypoints and 1 start location and 1 end location.
                // Upto 8 waypoints are allowed in a query for non-business users
                if(markerPoints.size()>=10){
                    return;
                }

                // Adding new item to the ArrayList
                markerPoints.add(point);

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(point);

                /**
                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is RED and
                 * for the rest of markers, the color is AZURE
                 */
                if(markerPoints.size()==1){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }else if(markerPoints.size()==2){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }else{
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                }

                // Add new marker to the Google Map Android API V2
                map.addMarker(options);

            }
        });


        // The map will be cleared on long click
        map.setOnMapLongClickListener(new OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                // Removes all the points from Google Map
                map.clear();

                // Removes all the points in the ArrayList
                markerPoints.clear();

            }
        });


        // Click event handler for Button btn_draw
        btnDraw.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Checks, whether start and end locations are captured
                if(markerPoints.size() >= 2){
                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }

            }
        });


    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "";
        for(int i=2;i<markerPoints.size();i++){
            LatLng point  = (LatLng) markerPoints.get(i);
            if(i==2)
                waypoints = "waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }


        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+waypoints;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;


        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exceptiondownloadingurl", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }



    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            map.addPolyline(lineOptions);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private Button scanBtn;
    private TextView formatTxt, contentTxt;
    public void onClick(View v){
//respond to clicks
        scanBtn = (Button)findViewById(R.id.scan_button);
        formatTxt = (TextView)findViewById(R.id.scan_format);
        contentTxt = (TextView)findViewById(R.id.scan_content);
        scanBtn.setOnClickListener(this);
        if(v.getId()==R.id.scan_button){
//scan
            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
            scanIntegrator.initiateScan();
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();
            formatTxt.setText("FORMAT: " + scanFormat);
            contentTxt.setText("CONTENT: " + scanContent);
//we have a result
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static final String UPLOAD_URL = "http://192.168.208.81:8080/getimage";

    private void uploadImage(final Bitmap bitmap){
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        final String TAG = "";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {


                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Log.d(TAG,"Listener");
                        //converts base64 to bitmap
//                        byte[] decodedString = Base64.decode(s, Base64.DEFAULT);
//                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//
//                        imgTakenPhoto.setImageBitmap(decodedByte);
                        // Toast.makeText(MainActivity.this, s , Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Log.e(TAG,"ErrorListener"+volleyError.getMessage());
                        //Showing toast
                        Toast.makeText(MainActivity.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
//                String image = getStringImage(bitmap);

                //Getting Image Name
                String name = "Image";

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                String KEY_IMAGE = "image";
                String KEY_NAME = "name";

                //Adding parameters
                params.put(KEY_IMAGE, image);
                params.put(KEY_NAME, name);

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
}


//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import org.json.JSONObject;
//
//import android.content.pm.PackageManager;
//import android.graphics.Color;
//import android.location.Location;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.FragmentActivity;
//import android.support.v4.content.ContextCompat;
//import android.util.Log;
//import android.view.Menu;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationListener;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
//import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
//import com.google.android.gms.maps.MapFragment;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.model.PolylineOptions;
//
//import com.google.android.gms.location.LocationServices;
//import android.Manifest;
//
//public  class MainActivity extends FragmentActivity implements OnMapReadyCallback ,
//        LocationListener,
//        GoogleApiClient.ConnectionCallbacks,
//        GoogleApiClient.OnConnectionFailedListener {
//    GoogleApiClient mGoogleApiClient;
//    Location mCurrentLocation;
//    LocationRequest mLocationRequest;
//    LatLng LatLng;
//
//
//    @Override
//    public void onConnected(Bundle bundle) {
//        System.out.println("onConnected");
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//        }
//        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        if (location == null) {
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
//        }
//        else {
//            handleNewLocation(location);
//        }
//
//    }
//
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(ConnectionResult connectionResult) {
//
//    }
//
//
//    @Override
//    public void onLocationChanged(Location location) {
//
//    }
//    GoogleMap map;
//    ArrayList<LatLng> markerPoints;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        super.onCreate(savedInstanceState);
//        LocationRequest mLocationRequest;
//        GoogleApiClient mGoogleApiClient;
//        Location mCurrentLocation;
//
//
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(LocationServices.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();
//
//        setContentView(R.layout.activity_main);
//
//
//        // Initializing
//        markerPoints = new ArrayList<LatLng>();
//
//        // Getting reference to SupportMapFragment of the activity_main
//        //SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map).getMap();
//        SupportMapFragment fm = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
//
//        // Getting reference to Button
//        Button btnDraw = (Button)findViewById(R.id.btn_draw);
//
//        // Getting Map for the SupportMapFragment
//        fm.getMapAsync(this);
//        //if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
//
//        //ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
//        // LocationServices.MY_PERMISSION_ACCESS_COURSE_LOCATION );
//
//        //);
//
//
//        // Enable MyLocation Button in the Map
//        //map.setMyLocationEnabled(true);
//
//        // Setting onclick event listener for the map
//        map.setOnMapClickListener(new OnMapClickListener() {
//
//            @Override
//            public void onMapClick(LatLng point) {
//
//                // Already 10 locations with 8 waypoints and 1 start location and 1 end location.
//                // Upto 8 waypoints are allowed in a query for non-business users
//                if(markerPoints.size()>=10){
//                    return;
//                }
//
//                // Adding new item to the ArrayList
//                markerPoints.add(point);
//
//                // Creating MarkerOptions
//                MarkerOptions options = new MarkerOptions();
//
//                // Setting the position of the marker
//                options.position(point);
//
//                /**
//                 * For the start location, the color of marker is GREEN and
//                 * for the end location, the color of marker is RED and
//                 * for the rest of markers, the color is AZURE
//                 */
//                if(markerPoints.size()==1){
//                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//                }else if(markerPoints.size()==2){
//                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//                }else{
//                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//                }
//
//                // Add new marker to the Google Map Android API V2
//                map.addMarker(options);
//
//            }
//        });
//
//
//        // The map will be cleared on long click
//        map.setOnMapLongClickListener(new OnMapLongClickListener() {
//
//            @Override
//            public void onMapLongClick(LatLng point) {
//                // Removes all the points from Google Map
//                map.clear();
//
//                // Removes all the points in the ArrayList
//                markerPoints.clear();
//
//            }
//        });
//
//
//        // Click event handler for Button btn_draw
//        btnDraw.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // Checks, whether start and end locations are captured
//                if(markerPoints.size() >= 2){
//                    LatLng origin = markerPoints.get(0);
//                    LatLng dest = markerPoints.get(1);
//
//                    // Getting URL to the Google Directions API
//                    String url = getDirectionsUrl(origin, dest);
//
//                    DownloadTask downloadTask = new DownloadTask();
//
//                    // Start downloading json data from Google Directions API
//                    downloadTask.execute(url);
//                }
//
//            }
//        });
//
//
//    }
//
//    private void setUpMapIfNeeded() {
//        // Do a null check to confirm that we have not already instantiated the map.
//        if (map == null) {
//            // Try to obtain the map from the SupportMapFragment.
//            SupportMapFragment map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
//
//           // map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
//            // Check if we were successful in obtaining the map.
//            map.getMapAsync(this);
////            if (map != null) {
////                setUpMap();
////            }
//        }
//    }
//    private void handleNewLocation(Location location) {
////        Log.d(TAG, location.toString());
//
//        double currentLatitude = location.getLatitude();
//        double currentLongitude = location.getLongitude();
//
//        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
//
//        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
//        MarkerOptions options = new MarkerOptions()
//                .position(latLng)
//                .title("I am here!");
//        map.addMarker(options);
//        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//    }
//    private String getDirectionsUrl(LatLng origin,LatLng dest){
//
//        // Origin of route
//        String str_origin = "origin="+origin.latitude+","+origin.longitude;
//
//        // Destination of route
//        String str_dest = "destination="+dest.latitude+","+dest.longitude;
//
//        // Sensor enabled
//        String sensor = "sensor=false";
//
//        // Waypoints
//        String waypoints = "";
//        for(int i=2;i<markerPoints.size();i++){
//            LatLng point  = (LatLng) markerPoints.get(i);
//            if(i==2)
//                waypoints = "waypoints=";
//            waypoints += point.latitude + "," + point.longitude + "|";
//        }
//
//
//        // Building the parameters to the web service
//        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+waypoints;
//
//        // Output format
//        String output = "json";
//
//        // Building the url to the web service
//        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
//
//
//        return url;
//    }
//
//    /** A method to download json data from url */
//    private String downloadUrl(String strUrl) throws IOException{
//        String data = "";
//        InputStream iStream = null;
//        HttpURLConnection urlConnection = null;
//        try{
//            URL url = new URL(strUrl);
//
//            // Creating an http connection to communicate with url
//            urlConnection = (HttpURLConnection) url.openConnection();
//
//            // Connecting to url
//            urlConnection.connect();
//
//            // Reading data from url
//            iStream = urlConnection.getInputStream();
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
//
//            StringBuffer sb  = new StringBuffer();
//
//            String line = "";
//            while( ( line = br.readLine())  != null){
//                sb.append(line);
//            }
//
//            data = sb.toString();
//
//            br.close();
//
//        }catch(Exception e){
//            Log.d("Exceptiondownloadingurl", e.toString());
//        }finally{
//            iStream.close();
//            urlConnection.disconnect();
//        }
//        return data;
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//
//    }
//
//
//    // Fetches data from url passed
//    private class DownloadTask extends AsyncTask<String, Void, String>{
//
//        // Downloading data in non-ui thread
//        @Override
//        protected String doInBackground(String... url) {
//
//            // For storing data from web service
//            String data = "";
//
//            try{
//                // Fetching the data from web service
//                data = downloadUrl(url[0]);
//            }catch(Exception e){
//                Log.d("Background Task",e.toString());
//            }
//            return data;
//        }
//
//        // Executes in UI thread, after the execution of
//        // doInBackground()
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//
//            ParserTask parserTask = new ParserTask();
//
//            // Invokes the thread for parsing the JSON data
//            parserTask.execute(result);
//
//        }
//    }
//
//    /** A class to parse the Google Places in JSON format */
//    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
//
//        // Parsing the data in non-ui thread
//        @Override
//        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
//
//            JSONObject jObject;
//            List<List<HashMap<String, String>>> routes = null;
//
//            try{
//                jObject = new JSONObject(jsonData[0]);
//                DirectionsJSONParser parser = new DirectionsJSONParser();
//
//                // Starts parsing data
//                routes = parser.parse(jObject);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//            return routes;
//        }
//
//        // Executes in UI thread, after the parsing process
//        @Override
//        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
//
//            ArrayList<LatLng> points = null;
//            PolylineOptions lineOptions = null;
//
//            // Traversing through all the routes
//            for(int i=0;i<result.size();i++){
//                points = new ArrayList<LatLng>();
//                lineOptions = new PolylineOptions();
//
//                // Fetching i-th route
//                List<HashMap<String, String>> path = result.get(i);
//
//                // Fetching all the points in i-th route
//                for(int j=0;j<path.size();j++){
//                    HashMap<String,String> point = path.get(j);
//
//                    double lat = Double.parseDouble(point.get("lat"));
//                    double lng = Double.parseDouble(point.get("lng"));
//                    LatLng position = new LatLng(lat, lng);
//
//                    points.add(position);
//                }
//
//                // Adding all the points in the route to LineOptions
//                lineOptions.addAll(points);
//                lineOptions.width(2);
//                lineOptions.color(Color.RED);
//            }
//
//            // Drawing polyline in the Google Map for the i-th route
//            map.addPolyline(lineOptions);
//        }
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//}