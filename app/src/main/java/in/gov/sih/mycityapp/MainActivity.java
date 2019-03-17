package in.gov.sih.mycityapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akhgupta.easylocation.EasyLocationAppCompatActivity;
import com.akhgupta.easylocation.EasyLocationRequest;
import com.akhgupta.easylocation.EasyLocationRequestBuilder;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.LocationRequest;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends EasyLocationAppCompatActivity {

    private final int REQ_CODE_SPEECH_INPUT = 2;

    private double lati, longi;
    private String address, district = "";
    static AutoCompleteTextView mAutoCompleteTextView;
    SharedPreferences mSharedPreferences;
    ArrayAdapter<String> mArrayAdapter;
    RecyclerView common_cities;

    private String mUsername;
    private Button goButton;
    private ImageView mSpeechButton;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private FirebaseUser firebaseUser;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ImageView loc = (ImageView) findViewById(R.id.location);
        mSpeechButton = (ImageView) findViewById(R.id.voice_search);
        goButton = (Button) findViewById(R.id.button);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        UpdateUI(firebaseUser);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id){
                    case R.id.sign_out:
                        AuthUI.getInstance().signOut(MainActivity.this);
                        Toast.makeText(MainActivity.this, "Signed out!", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });

        final DatabaseReference dref=FirebaseDatabase.getInstance().getReference(firebaseUser.getUid());
        dref.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if(!dataSnapshot.hasChild("karma"))
                 {
                     dref.getRef().child("karma").setValue(0);
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });

        mSpeechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });

        InputStream inputStream = getResources().openRawResource(R.raw.cities);
        ParseCity city = new ParseCity(inputStream);
        List<String> cities = city.getCity(MainActivity.this);
        mAutoCompleteTextView = findViewById(R.id.auto);
        Log.e("check", Integer.toString(cities.size()));
        mArrayAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1,
                cities);
        mAutoCompleteTextView.setThreshold(1);
        mAutoCompleteTextView.setAdapter(mArrayAdapter);

        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(5000);
        final EasyLocationRequest easyLocationRequest = new EasyLocationRequestBuilder()
                .setLocationRequest(locationRequest)
                .setFallBackToLastLocationTime(3000)
                .build();
        loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSingleLocationFix(easyLocationRequest);
            }
        });

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedCity = mAutoCompleteTextView.getText().toString();
                selectedCity = selectedCity.trim();
                if (selectedCity.length() == 0 || selectedCity == null)
                    Toast.makeText(MainActivity.this, "Entered Location cannot be empty", Toast.LENGTH_LONG).show();
                else {
                    SharedPreferences locShared = getSharedPreferences("locPrefs", MODE_PRIVATE);
                    String coordinates = locShared.getString(selectedCity, "0:0");
                    if (coordinates.equals("0:0"))
                        Toast.makeText(MainActivity.this, "Entered Location does not exist in database", Toast.LENGTH_LONG).show();
                    else {
                        longi = Double.parseDouble(coordinates.substring(0, 5));
                        lati = Double.parseDouble(coordinates.substring(6));
                        List<Address> addresss = null;
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        try {
                            addresss = geocoder.getFromLocation(lati, longi, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Log.e("this", coordinates);
                        mSharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putFloat("latitude", (float) lati);
                        editor.putFloat("longitude", (float) longi);
                        editor.putString("address", selectedCity);
                        if (addresss.get(0).getSubAdminArea() != null)
                            editor.putString("district", addresss.get(0).getSubAdminArea());
                        else
                            editor.putString("district", "Port Blair");
                        editor.apply();
                        Intent intent = new Intent(MainActivity.this, IntroductionActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });

        mAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    check();
                } else
                    goButton.setVisibility(View.GONE);
            }
        });
        int[] imgs = {R.drawable.bangalore, R.drawable.chennai, R.drawable.delhi, R.drawable.hyderabad, R.drawable.kolkata, R.drawable.mumbai2, R.drawable.pune2};

        String[] data = getResources().getStringArray(R.array.common_cities_name);

        common_cities = findViewById(R.id.common_cities);
        common_cities.setLayoutManager(new GridLayoutManager(this, 2));
        common_cities_adapter my_common_citiesadap = new common_cities_adapter(this, data, imgs);

        common_cities.setAdapter(my_common_citiesadap);


    }

    private void UpdateUI(FirebaseUser user) {
        TextView userName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.userName);
        TextView email = (TextView) navigationView.getHeaderView(0).findViewById(R.id.email);
        ImageView imageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView);

        mUsername = user.getDisplayName();
        userName.setText(mUsername);
        email.setText(user.getEmail());
        Glide.with(MainActivity.this).load(firebaseUser.getPhotoUrl()).into(imageView);
    }

    public void check() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!mAutoCompleteTextView.getText().toString().trim().equals("")) {
                    goButton.setVisibility(View.VISIBLE);

                } else {
                    goButton.setVisibility(View.GONE);
                }
                check();
            }
        };

        handler.postDelayed(runnable, 1000);
    }

    @Override
    public void onLocationPermissionGranted() {
        Toast.makeText(this, "Please wait while current location is fetched....", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationPermissionDenied() {
        Toast.makeText(this, "Enable Location for app to function properly", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationReceived(Location location) {
        lati = location.getLatitude();
        longi = location.getLongitude();
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lati, longi, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null) {
            address = addresses.get(0).getLocality();
            district = addresses.get(0).getSubAdminArea();
            mSharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putFloat("latitude", (float) lati);
            editor.putFloat("longitude", (float) longi);
            editor.putString("address", address);
            editor.putString("district", district);
            //Toast.makeText(this, district, Toast.LENGTH_SHORT).show();
            editor.commit();
            mAutoCompleteTextView.setText(address);

        } else
            Toast.makeText(this, "Location cannot be updated! Please enter location manually", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationProviderEnabled() {
        Toast.makeText(this, "Location On", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationProviderDisabled() {
        Toast.makeText(this, "Enable Location for app to function properly", Toast.LENGTH_LONG).show();
    }


    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.enter_journal_message));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception e) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mAutoCompleteTextView.setText(result.get(0));
                }
                break;
        }
    }


}

