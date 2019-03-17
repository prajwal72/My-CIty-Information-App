package in.gov.sih.mycityapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class common_cities_adapter extends RecyclerView.Adapter<common_cities_adapter.ViewHolder> {

    private String[] mData;
    private int[] imgs;
    private LayoutInflater mInflater;


    // data is passed into the constructor
    common_cities_adapter(Context context, String[] data, int[] imgs) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.imgs = imgs;

    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.common_cities_item, parent, false);
        return new ViewHolder(view, parent.getContext());
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.myTextView.setText(mData[position]);
        holder.myImage.setImageResource(imgs[position]);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mAutoCompleteTextView.setText(mData[position]);
            }
        });
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;
        ImageView myImage;

        ViewHolder(View itemView, Context context) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.city_name);
            myImage = itemView.findViewById(R.id.common_cities_image);

        }
    }
}