package in.gov.sih.mycityapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Iterator;

public class AttractionFragment extends Fragment {

    public static int scrollpos = 0;
    private static String LOG_TAG = "AttractionFragment";
    private DatabaseReference databaseReference;
    private ArrayList<Attraction> attraction;
    private ProgressBar progressBar;
    private String city;
    private String location;
    private String latitude, longitude;

    public AttractionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_attraction, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler);

        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        Context context = getContext();
        SharedPreferences sharedPreferences= context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences statePreferences= context.getSharedPreferences("statePrefs",Context.MODE_PRIVATE);

        attraction = new ArrayList<>();
        city = sharedPreferences.getString("address"," ");
        String state = statePreferences.getString(city, " ");
        latitude = (sharedPreferences.getFloat("latitude", 0.0f)) + "";
        longitude = (sharedPreferences.getFloat("longitude", 0.0f)) + "";
        location = city+","+ state;

        databaseReference = FirebaseDatabase.getInstance().getReference();

        AttractionAdapter attractionAdapter = new AttractionAdapter(attraction);
        recyclerView.setAdapter(attractionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.smoothScrollToPosition(scrollpos);

        getAttraction();

        return view;
    }

    private void getAttraction() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("cities").child(location).hasChild("attractions")){
                    ScrapingTask task = new ScrapingTask();
                    task.execute();
                }
                else{
                    attraction.clear();
                  /*  for(DataSnapshot ds: dataSnapshot.getChildren())
                    {
                        Attraction att = ds.getValue(Attraction.class);
                        attraction.add(att);
                    }*/
                    Log.d(LOG_TAG,"Attractions Written in database");
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(LOG_TAG,databaseError.getMessage());
            }
        });
    }

    private class ScrapingTask extends AsyncTask{

        @Override
        protected void onPostExecute(Object object) {
            if((Integer)object < 1)
                getAttractionsByFallback();
            else
                getAttraction();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String url = "https://www.google.com/search?q=places+to+visit+in+" + location;
            int count = 0;
            try{
                Document html = Jsoup.connect(url).get();
                Elements places = html.select("table[class=jktSrf]").select("td");

                Elements place = places.select("a[class=mCovnb]").eq(0);
                url = place.attr("href");

                url = "https://www.google.com" + url;
                html = Jsoup.connect(url).get();

                places = html.select("ol").eq(0).select("li");
                for(int i = 0;i < places.size(); i++){
                    place = places.select("li").eq(i);
                    String title = place.select("h2").eq(0).text();
                    String imageURL = place.select("img").eq(0).attr("src");
                    String description = place.select("p").eq(0).text();

                    if(description == null || description.trim().equals("")){
                        description = title;
                        if (title.contains("Park"))
                            description = "Park";
                        if (title.contains("Garden"))
                            description = "Garden";
                        if (title.contains("Waterfall"))
                            description = "Waterfall";
                        if (title.contains("Dam"))
                            description = "Dam";
                        if (title.contains("Lake"))
                            description = "Lake";
                        if (title.contains("Temple") || title.contains("Mandir"))
                            description = "Temple";
                        if (title.contains("Road"))
                            description = "Road";
                        if (title.contains("Masjid") || title.contains("Mosque"))
                            description = "Mosque";
                        if (title.contains("Church"))
                            description = "Church";
                    }

                    databaseReference.child("cities").child(location).child("attractions").child(Integer.toString(count)).child("name").setValue(title);
                    databaseReference.child("cities").child(location).child("attractions").child(Integer.toString(count)).child("description").setValue(description);
                    databaseReference.child("cities").child(location).child("attractions").child(Integer.toString(count)).child("imageURL").setValue(imageURL);
                    databaseReference.child("cities").child(location).child("attractions").child(Integer.toString(count)).child("rating").setValue(0);
                    databaseReference.child("cities").child(location).child("attractions").child(Integer.toString(count)).child("numberOfReviews").setValue(0);

                    count++;
                }

            }catch (Exception e){
                Log.e(LOG_TAG,e.getMessage());
            }

            return count;

        }
    }

    private void getAttractionsByFallback(){
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=coordinates" +
                "|pageimages|pageterms&colimit=50&piprop=thumbnail&pithumbsize=360&pilimit=50&" +
                "wbptterms=description&generator=geosearch&ggscoord=" +
                latitude + "|" + longitude +
                "&ggsradius=10000&ggslimit=50";

        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(LOG_TAG, "Fallback Response Obtained");
                addAttractions(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error.getMessage());
            }
        });

        queue.add(stringRequest);
    }

    private void addAttractions(String response){
        try{
            JSONObject rootJsonObject = new JSONObject(response);

            JSONObject query = rootJsonObject.getJSONObject("query");
            JSONObject pages = query.getJSONObject("pages");

            Iterator<String> keys = pages.keys();
            int count = 0;
            while (keys.hasNext()){
                String key = keys.next();
                JSONObject attraction = pages.getJSONObject(key);

                String title, imageURL = null, description = null;

                title = attraction.getString("title");

                if(attraction.has("thumbnail")) {
                    JSONObject JSONthumbnail = attraction.getJSONObject("thumbnail");
                    imageURL = JSONthumbnail.getString("source");
                }

                if(attraction.has("terms")) {
                    JSONObject JSONdescription = attraction.getJSONObject("terms");
                    description = JSONdescription.getJSONArray("description").getString(0);
                }

                Attraction att = new Attraction(title, description, imageURL, 0, 0);
                if(filter(att)){
                    databaseReference.child("cities").child(location).child("attractions").child(Integer.toString(count)).child("name").setValue(title);
                    databaseReference.child("cities").child(location).child("attractions").child(Integer.toString(count)).child("description").setValue(description);
                    databaseReference.child("cities").child(location).child("attractions").child(Integer.toString(count)).child("imageURL").setValue(imageURL);
                    databaseReference.child("cities").child(location).child("attractions").child(Integer.toString(count)).child("rating").setValue(0);
                    databaseReference.child("cities").child(location).child("attractions").child(Integer.toString(count)).child("numberOfReviews").setValue(0);
                    count++;
                }
            }

        }catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private boolean filter(Attraction attraction){
        if(attraction.getDescription() == null)
            return false;

        String description = attraction.getDescription();
        String title = attraction.getName();

        if(description.contains("district") || description.contains("District"))
            return false;

        if(description.contains("human settlement"))
            description = "A settlement of the city";

        if(title.contains("district") || title.contains("District"))
            return false;

        if(title.contains("constituency") || title.contains("Constituency"))
            return false;

        if(title.contains("division") || title.contains("Division"))
            return false;

        if(title.equals(city))
            return false;

        description = description.substring(0, 1).toUpperCase() + description.substring(1);
        attraction.setDescription(description);

        title = title.substring(0, 1).toUpperCase() + title.substring(1);
        attraction.setName(title);

        return true;
    }




}



