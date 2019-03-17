package in.gov.sih.mycityapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Information extends Fragment {

        private ArrayList<Info> info;
        private ArrayAdapter arrayAdapter;
        private ListView list;
        private Context context;
        private DatabaseReference databaseReference;
        private String city;
        private String state;
        private String location;
        private RelativeLayout progressBar;

        public Information() {
                // Required empty public constructor
        }

        public static Information newInstance() {
                Information fragment = new Information();
                return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
                // Inflate the layout for this fragment
                View returnView = inflater.inflate(R.layout.activity_information, container,
                        false);
                list = (ListView)returnView.findViewById(R.id.list);
                context = getContext();
                SharedPreferences sharedPreferences=context.getSharedPreferences("MyPrefs",Context.MODE_PRIVATE);
                SharedPreferences statePreferences=context.getSharedPreferences("statePrefs",Context.MODE_PRIVATE);

                info = new ArrayList<>();
                city = sharedPreferences.getString("address"," ");
                state = statePreferences.getString(city," ");
                location = city+","+state;

                databaseReference = FirebaseDatabase.getInstance().getReference();

                arrayAdapter = new InfoAdapter( getActivity(),1,new ArrayList<Info>());
                arrayAdapter.addAll(info);
                list.setAdapter(arrayAdapter);

                progressBar = (RelativeLayout)returnView.findViewById(R.id.progress_bar);

                getInfo();

                return returnView;
        }

        private class Info {
                private String key, value;

                public Info(){
                        key = "";
                        value = "";
                }

                public Info(String key, String value){
                        this.key = key;
                        this.value = value;
                }

                public String getValue() {
                        return value;
                }

                public String getKey() {
                        return key;
                }

                public void setValue(String value) {
                        this.value = value;
                }

                public void setKey(String key) {
                        this.key = key;
                }

        }

        private class InfoAdapter extends ArrayAdapter<Info>{

                public InfoAdapter(@NonNull Context context, int resource,
                                   @NonNull List<Info> objects) {
                        super(context, resource, objects);
                }

                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        if(convertView == null) {
                                convertView = LayoutInflater.from(getActivity()).
                                        inflate(R.layout.info_item, null);
                        }

                        TextView keyView = (TextView)convertView.findViewById(R.id.key);
                        TextView valueView = (TextView)convertView.findViewById(R.id.value);

                        final Info info = getItem(position);
                        keyView.setText(info.getKey());
                        valueView.setText(info.getValue());

                        if(info.getValue().trim().equals("")){
                                valueView.setVisibility(View.GONE);
                                keyView.setTextSize(22);
                                keyView.setPadding(0, 60, 0, 0);
                        }
                        else {
                                valueView.setVisibility(View.VISIBLE);
                                keyView.setTextSize(18);
                                if(position != 0 && getItem(position - 1).getKey().trim().charAt(0) == '•'
                                        && info.getKey().trim().charAt(0) != '•'){
                                        //Log.e("jk", "" + first_char);
                                        keyView.setPadding(0, 200, 0, 200);
                                }
                                else {
                                        keyView.setPadding(0, 0, 0, 0);
                                }
                        }

                        return convertView;
                }
        }

        private void rearrangeDetails(ArrayList<Info> infos){
                infos.add(0, new Info("General", ""));
                int count = 1;
                for(int i = 1;i < infos.size();i ++){
                        Info info = infos.get(i);
                        if(!info.getValue().trim().equals("") && !(info.getKey().trim().charAt(0) == '•')){
                                infos.remove(i);
                                info.setKey("• " + info.getKey());
                                infos.add(count, info);
                                count ++;
                        }
                }
        }

        private void getInfo(){
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.child("cities").child(location).hasChild("info")){
                        ScrapingTask task = new ScrapingTask();
                        task.execute();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        private class ScrapingTask extends AsyncTask {

                @Override
                protected void onPostExecute(Object o) {
                        ArrayList<Info> infos = (ArrayList<Info>)o;
                        rearrangeDetails(infos);
                        addInfo(infos);
                }

                @Override
                protected Object doInBackground(Object[] objects) {
                        int q=1,k=0,l;
                        ArrayList<Info> infos = new ArrayList<>();
                        String query = city +' '+state;
                        query=query.replaceAll(" ","+");
                        String url = "https://www.google.com/search?q=Wikipedia+" + query;
                        try{
                                Document html = Jsoup.connect(url).get();
                                Elements places = html.select("div[class=r]");

                                Elements place = places.select("a").eq(0);
                                url = place.attr("href");

                                html = Jsoup.connect(url).get();
                                places = html.select("table[class=infobox geography vcard]");
                                int f=0;
                                String country="Country",nation="Nation";
                                Elements placex =places.select("tr[class^=merged]");
                                String header = "General";
                                int gen_count = 1;
                                int head_count = 1;
                                int size = placex.size();
                                for(int i = 0;i < size;i ++){
                                        try{
                                                String string = places.select("tr[class^=merged]").eq(i).text();
                                                placex =places.select("tr[class^=merged]").eq(i);

                                                if(f==0&&(string.contains(country)||string.contains(nation))) {
                                                        f = 1;
                                                        continue;
                                                }
                                                if (f==0)
                                                    continue;
                                                String key, value;
                                                key = placex.select("th").text();
                                                value = placex.select("td").text();
                                                q=1;
                                                while(q==1){
                                                        q=0;
                                                        l=value.length();
                                                        for(int j=0;j<l;j++){
                                                                if(value.charAt(j)=='[')
                                                                        k=j;
                                                                if(j!=l-1&&value.charAt(j)==']'&& value.charAt(j+1)=='[')
                                                                        continue;
                                                                if(value.charAt(j)==']') {
                                                                        value = value.substring(0, k) + value.substring(j + 1);
                                                                        q = 1;
                                                                }
                                                        }
                                                }
                                                if (key.contains("Website")){
                                                    value = placex.select("a").attr("href");
                                                }
                                                q=1;
                                                while(q==1){
                                                        q=0;
                                                        l=key.length();
                                                        for(int j=0;j<l;j++){
                                                                if(key.charAt(j)=='[')
                                                                        k=j;
                                                                if(j!=l-1&&key.charAt(j)==']'&& key.charAt(j+1)=='[')
                                                                        continue;
                                                                if(key.charAt(j)==']') {
                                                                        key = key.substring(0, k) + key.substring(j + 1);
                                                                        q = 1;
                                                                }
                                                        }
                                                }
                                                key = key.trim();
                                                value = value.trim();
                                                if (value.length() == 0){
                                                    header = key;
                                                    head_count = 1;
                                                }
                                                else if(key.trim().charAt(0) == '•'){
                                                    l=key.length();
                                                    key = Integer.toString(head_count) + "_ " + key.substring(2,l);
                                                    databaseReference.child("cities").child(location).child("info")
                                                            .child(header).child(key).setValue(value);
                                                    head_count++;
                                                }
                                                else{
                                                    key = Integer.toString(gen_count) + "_ " + key;
                                                    databaseReference.child("cities").child(location).child("info")
                                                            .child("General").child(key).setValue(value);
                                                    gen_count++;
                                                }
                                                infos.add(new Info(key,value));
                                        }
                                        catch (Exception e){
                                                continue;
                                        }
                                }

                        }catch (IOException e){

                        }

                        return infos;

                };
        }

        private void addInfo(ArrayList<Info> info){
                arrayAdapter.clear();
                arrayAdapter.addAll(info);
                progressBar.setVisibility(View.GONE);
        }


}
