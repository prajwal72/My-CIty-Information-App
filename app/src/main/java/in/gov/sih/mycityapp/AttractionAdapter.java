package in.gov.sih.mycityapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.ViewHolder>{


    private ArrayList<Attraction> attractionModels;
    private Context context;
    private String city;
    private String location;


    public AttractionAdapter(ArrayList<Attraction> attractionModels)
    {
        this.attractionModels = attractionModels;
    }



    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        SharedPreferences sharedPreferences= context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences statePreferences= context.getSharedPreferences("statePrefs",Context.MODE_PRIVATE);
        city = sharedPreferences.getString("address"," ");
        String state = statePreferences.getString(city, " ");
        location = city + "," + state;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.atttraction_item,parent,false);
        return new ViewHolder(view,parent.getContext(),attractionModels);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Glide.with(context).load(attractionModels.get(position).getImageURL()).into(holder.img);
        holder.name.setText(attractionModels.get(position).getName());
        final String hotelName = attractionModels.get(position).getName();
        final String uid = FirebaseAuth.getInstance().getUid();
        holder.address.setText(attractionModels.get(position).getDescription());
        int reviews = (int) attractionModels.get(position).getNumberOfReviews();
        holder.rev.setText(reviews + " reviews");
        holder.rat.setRating(attractionModels.get(position).getRating());
        holder.ret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final RatingBar ratingBar = new RatingBar(context);
                ratingBar.setPadding(100, 100, 100, 0);
                final DatabaseReference dref = FirebaseDatabase.getInstance().getReference();
                final SharedPreferences sharedPreferences = context.getSharedPreferences("Ratings",Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = sharedPreferences.edit();
                final float rating = sharedPreferences.getFloat(uid+hotelName,0.0f);
                builder.setView(ratingBar);
                if(rating != 0.0f)
                    ratingBar.setRating(rating);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    AttractionFragment.scrollpos = position;

                        if(rating==0.0f) {
                            dref.child("cities").child(location).child("attractions").child(String.valueOf(position)).child("rating").setValue(((attractionModels.get(position).getRating() * attractionModels.get(position).getNumberOfReviews() - rating + ratingBar.getRating()) / (attractionModels.get(position).getNumberOfReviews()+1)));
                            dref.child("cities").child(location).child("attractions").child(String.valueOf(position)).child("numberOfReviews").setValue(attractionModels.get(position).getNumberOfReviews()+1);
                        }

                        if(rating!=0.0f) {
                            dref.child("cities").child(location).child("attractions").child(String.valueOf(position)).child("rating").setValue(((attractionModels.get(position).getRating() * attractionModels.get(position).getNumberOfReviews() - rating + ratingBar.getRating()) / (attractionModels.get(position).getNumberOfReviews())));
                        }
                        editor.putFloat(uid+hotelName,ratingBar.getRating());
                        editor.commit();
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                Dialog dialog=builder.create();
                dialog.show();
            }
        });


    }
    @Override
    public int getItemCount() {
        return attractionModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView name,address,rev,ret;
        RatingBar rat;
        ImageView img;
        ArrayList<Attraction> attractionModels;

        public ViewHolder(@NonNull View itemView, Context context,ArrayList<Attraction> attractionModels) {
            super(itemView);
            this.attractionModels = attractionModels;
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            rev = itemView.findViewById(R.id.rev);
            rat = itemView.findViewById(R.id.rat);
            img = itemView.findViewById(R.id.img);
            ret = itemView.findViewById(R.id.rate);
             name.setOnClickListener(this);
             address.setOnClickListener(this);
             img.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Intent intent=new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://maps.google.com/maps?q="+attractionModels.get(getAdapterPosition()).getName()+attractionModels.get(getAdapterPosition()).getDescription()));
            context.startActivity(intent);
        }
    }
}
