package in.gov.sih.mycityapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AttractionFragment extends Fragment {

    public static int scrollpos = 0;
    private DatabaseReference dref;
    private ArrayList<Attraction> attraction;
    private RecyclerView recyclerView;
    private AttractionAdapter attractionAdapter;
    private RelativeLayout progressBar;

    public AttractionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_attraction, container, false);
        recyclerView =view.findViewById(R.id.recycler);

        progressBar = (RelativeLayout) view.findViewById(R.id.progress_bar);

        attraction = new ArrayList<>();
        dref = FirebaseDatabase.getInstance().getReference("mainattraction");
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                attraction.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    Attraction att = ds.getValue(Attraction.class);
                    attraction.add(att);
                }

                 attractionAdapter = new AttractionAdapter(attraction);
                 recyclerView.setAdapter(attractionAdapter);
                 recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                 recyclerView.smoothScrollToPosition(scrollpos);

                 progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

}
