package com.example.moslah_hamza.apprentissagefouille1;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.SettingsApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private Button b;
    private String url = "http://192.168.1.4/";
    private Spinner spinner;
    private Map<Integer, String> services = new HashMap<Integer, String>();
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager locationManager;
    private LocationRequest mLocationRequest;
    private List<Post> posts = new ArrayList<>();
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private double longitude, latitude;
    private String file = "posts.arff";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPostes();

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonArrayRequest jsonRequest = new JsonArrayRequest
                (url + "get_services.php", new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(0);
                                services.put(jsonObject.getInt("id"), jsonObject.getString("label"));
                            }
                            Log.d("service ", services.get(1));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(MainActivity.this).add(jsonRequest);  // adding the request to the Volley request queue

//        if (services.size() == 0) {
//            services.put(1, "Post-finance");
//            services.put(2, "Postassurance");
//            services.put(3, "Courrier");
//            services.put(4, "Rapid-poste");
//            services.put(5, "Poste-colis");
//        }

        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.service_arrays, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationManager locationManager1 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager1.isProviderEnabled(LocationManager.GPS_PROVIDER))
            Log.d("GPS ", "enabled");

        if (mGoogleApiClient != null && locationManager != null)
            Log.d("", "google client initalized successfully ");

        b = (Button) findViewById(R.id.tickbut);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Post bestOf = getNearestBestPost(spinner.getSelectedItemPosition() + 1);
                if (bestOf != null) {
                    StringRequest request = new StringRequest(Request.Method.POST, url + "add_ticket.php",
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // response
                                    Log.d("Response", response);
                                    try {
                                        //Do it with this it will work
                                        JSONArray jsonArray = new JSONArray(response);
                                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                                        int num = jsonObject.getInt("message");
                                        Toast.makeText(getApplicationContext(), "Le numéro de votre ticket : " + num, Toast.LENGTH_LONG).show();
                                        Toast.makeText(getApplicationContext(), "Le meilleur bureau : " + bestOf.getLabel()
                                                + " " + bestOf.getAdress(), Toast.LENGTH_LONG).show();
                                        Toast.makeText(getApplicationContext(), "Vous pouvez etre servi aprés : " + bestOf.getWait() / 60
                                                + " minutes", Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    Log.d("Error.Response", error.toString());
                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("service", String.valueOf(spinner.getSelectedItemPosition() + 1));
                            params.put("poste", String.valueOf(bestOf.getId()));
                            return params;
                        }
                    };
                    Volley.newRequestQueue(MainActivity.this).add(request);  // adding the request to the Volley request queue
                }
            }
        });
    }

    private Post getNearestBestPost(int i) {
        //Post[] posts2 = (Post[]) posts.toArray();
        List<Post> best = new ArrayList<>();
        for (Post post : posts) {
            Log.d("best services: ", "" + post.getId() + " " + post.getWait() + " " + post.getCluster() + " " + post.getService());
            if ((post.getService() == i) && (post.getCluster() == 0)) {
                float dist = distFrom((float) latitude, (float) longitude, (float) post.getLat(), (float) post.getLon());
                post.setDistance(dist);
                best.add(post);
            }
        }

        Collections.sort(best, new Comparator<Post>() {
            @Override
            public int compare(Post z1, Post z2) {
                if (z1.getDistance() > z2.getDistance())
                    return 1;
                if (z1.getDistance() < z2.getDistance())
                    return -1;
                return 0;
            }
        });

        if (best.size() == 0) {
            return null;
        }
        return best.get(0);
    }

    public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    private void getPostes() {
        JsonArrayRequest jsonRequest = new JsonArrayRequest
                (url + "get_avg_time.php", new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            Log.d("get avg time length ", "" + response.length());
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                Post post = new Post(jsonObject.getDouble("wait"), jsonObject.getDouble("lon"),
                                        jsonObject.getDouble("lat"), jsonObject.getInt("poste"), jsonObject.getInt("service"),
                                        jsonObject.getInt("code"), jsonObject.getString("adresse"), jsonObject.getString("label"));
                                posts.add(post);
                            }
                            classPosts();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(MainActivity.this).add(jsonRequest);  // adding the request to the Volley request queue
    }

    private void writePostsFile() {
        BufferedWriter bw = null;
        FileWriter fw = null;
        FileOutputStream fOut;
        String separator = System.getProperty("line.separator");
        file = getFilesDir() + "/" + file;

        try {
            fOut = new FileOutputStream(file);
            fOut.write("@relation poste".getBytes());
            fOut.write(separator.getBytes());
            fOut.write(separator.getBytes());
            fOut.write("@attribute wait real".getBytes());
            fOut.write(separator.getBytes());
            fOut.write(separator.getBytes());
            fOut.write("@data".getBytes());
            fOut.write(separator.getBytes());
            //fOut = openFileOutput(file, Context.MODE_PRIVATE);

            for (Post post1 : posts) {
                String content = post1.getWait() + "\n";
                fOut.write(content.getBytes());
            }
            Log.d("writes ", "file");
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            fw = new FileWriter(file);
//            bw = new BufferedWriter(fw);
//
//            for (Post post1 : posts) {
//                String content = posts.indexOf(post1) + " " + post1.getWait() + "\n";
//                bw.write(content);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (bw != null)
//                    bw.close();
//                if (fw != null)
//                    fw.close();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
    }

    private void classPosts() {
        SimpleKMeans kmeans = new SimpleKMeans();
        kmeans.setSeed(10);

        //important parameter to set: preserver order, number of cluster.
        kmeans.setPreserveInstancesOrder(true);
        try {
            kmeans.setNumClusters(2);
        } catch (Exception e) {
            Log.e("kmeans num cluster : ", e.toString());
        }

        writePostsFile();
        BufferedReader datafile = readDataFile(file);
        Instances data = null;
        try {
            data = new Instances(datafile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            kmeans.buildClusterer(data);
        } catch (Exception e) {
            Log.e("kmeans build : ", e.toString());
        }

        // This array returns the cluster number (starting with 0) for each instance
        // The array has as many elements as the number of instances
        int[] assignments = new int[0];
        try {
            assignments = kmeans.getAssignments();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Post> psts = new ArrayList<>();
        psts.addAll(posts);
        Collections.sort(psts, new Comparator<Post>() {
            @Override
            public int compare(Post z1, Post z2) {
                if (z1.getWait() > z2.getWait())
                    return 1;
                if (z1.getWait() < z2.getWait())
                    return -1;
                return 0;
            }
        });
        int last = posts.indexOf(psts.get(psts.size() - 1));
        int first = posts.indexOf(psts.get(0));
        int lastcls = assignments[last];
        int firstcls = assignments[first];

        int i = 0;
        for (int clusterNum : assignments) {
            //Post post = posts.get(i);
            Post post = psts.get(i);
            if (clusterNum == lastcls) {
                post.setCluster(1);
            } else if (clusterNum == firstcls) {
                post.setCluster(0);
            }
            //System.out.println(""+post.getCluster());
            Log.d("cluster : ", "" + post.getCluster());
            posts.set(i, post);
            i++;
        }
    }

    public BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;
        StringBuilder sb = new StringBuilder();

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            Log.e("File not found: ", filename);
        }


//        try {
//            File file = new File(filename);
//            FileInputStream fis = new FileInputStream(file);
//                    //getApplicationContext().openFileInput(filename);
//            InputStreamReader isr = new InputStreamReader(fis);
//            inputReader = new BufferedReader(isr);
//            String line;
//            int l=0;
//            while ((line = inputReader.readLine()) != null) {
//                sb.append(line);
//                l++;
//            }
//            Log.d("lines: ", l+"");
//            Log.d("lines: ", inputReader.toString());
//        } catch (FileNotFoundException ex) {
//            Log.d("File not found: ", filename);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return inputReader;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation == null) {
            startLocationUpdates();
        }
        if (mLocation != null) {
            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();
            Toast.makeText(this, "Location Detected lat: " + latitude + ", lon: " + longitude, Toast.LENGTH_LONG).show();
            Log.d("", "Location Detected lat: " + latitude + ", lon: " + longitude);
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_LONG).show();
            Log.d("", "Location not Detected ");
        }


    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("", "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("", "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.d("", "start method ");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}
