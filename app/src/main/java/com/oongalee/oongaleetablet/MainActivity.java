package com.oongalee.oongaleetablet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayAdapter arrayAdapter;
    ArrayList<String> arr_messages = new ArrayList<>();
    ArrayList<String> arr_ids = new ArrayList<>();
    Button refresh_button;
    ListView listView;
    RequestQueue requestQueue;

    final String RESTAURANT_NAME = "Boston Pizza Westmount";
    final String ADDRESS = "http://54.244.183.131:1337/messages";

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 30*1000; //Delay for 30 seconds.  One second = 1000 milliseconds.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        refresh_button = findViewById(R.id.refresh_button);
        listView = findViewById(R.id.listView);

        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                refreshList();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Repeat every 15 seconds
        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                refreshList();
                handler.postDelayed(runnable, delay);
            }
        }, delay);
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(runnable);
    }

    private void refreshList() {
        String url = ADDRESS + "/?restaurant_name=" + RESTAURANT_NAME;
        Log.d("uh", url);
        get_request(url);
    }

    private void get_request(String url) {
        JsonArrayRequest arrReq = new JsonArrayRequest(Request.Method.GET, url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        arr_messages = new ArrayList<>();
                        arr_ids = new ArrayList<>();
                        if (response.length() > 0) {
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    try {
                                        JSONObject jsonObj = response.getJSONObject(i);
                                        Log.d("yuh", String.valueOf(jsonObj.getInt("id")));
                                        arr_ids.add(String.valueOf(jsonObj.getInt("id")));
                                        arr_messages.add(jsonObj.getString("message"));
                                    } catch (Exception e) {
                                        continue;
                                    }
                                }
                                arrayAdapter = new ArrayAdapter(MainActivity.this, R.layout.custom_textview, arr_messages);
                                listView.setAdapter(arrayAdapter);
                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        String message_id = arr_ids.get(position);
                                        String url = ADDRESS + "/" + message_id;
                                        delete_request(url);
                                        arr_messages.remove(position);
                                        arr_ids.remove(position);
                                        arrayAdapter = new ArrayAdapter(MainActivity.this, R.layout.custom_textview, arr_messages);
                                        listView.setAdapter(arrayAdapter);
                                    }
                                });
                            } catch (Exception e) {
                                return;
                            }
                        } else {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // If there a HTTP error then add a note to our repo list.
                        Log.e("Volley", error.toString());
                    }
                }
        );
        requestQueue.add(arrReq);
    }

    private void delete_request(final String url) {
        StringRequest req = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // If there a HTTP error then add a note to our repo list.
                        Log.e("Volley", error.toString());
                    }
                }
        );
        requestQueue.add(req);
    }
}