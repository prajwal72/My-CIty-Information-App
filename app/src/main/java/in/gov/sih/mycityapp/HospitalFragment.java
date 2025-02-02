package in.gov.sih.mycityapp;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HospitalFragment extends Fragment {

    DatabaseReference dref;
    ArrayList<HospitalModel> hospitalModels;
    RecyclerView recyclerView;
    HospitalAdapter hospitalAdapter;

    public static int scrollPos = 0;

    public HospitalFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_hospital, container, false);
        recyclerView=view.findViewById(R.id.recycler);

        hospitalModels=new ArrayList<>();
        dref= FirebaseDatabase.getInstance().getReference("hospitals");
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hospitalModels.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren())
                {
                    HospitalModel att=ds.getValue(HospitalModel.class);
                    hospitalModels.add(att);

                }
                Log.e("tag",""+hospitalModels.size());
                hospitalAdapter=new HospitalAdapter(hospitalModels);
                recyclerView.setAdapter(hospitalAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.scrollToPosition(scrollPos);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
}
