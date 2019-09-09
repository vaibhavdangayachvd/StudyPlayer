package com.example.studyplayer;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public final class VideoLoader extends ViewModel {
    MutableLiveData<Boolean> hasLoaded=new MutableLiveData<>();
    ArrayList<Video> videoList = new ArrayList<>();
    Context context;
    public ArrayList<Video>getVideos()
    {
        return videoList;
    }
    public VideoLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    public LiveData<Boolean> getVideoObserver() {
        return hasLoaded;
    }

    public void loadVideos() {
        final String URL = "https://studyplayer.000webhostapp.com/getvideos.php";
        StringRequest request = new StringRequest(StringRequest.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (parseResponse(response))
                    hasLoaded.setValue(true);
                else
                    hasLoaded.setValue(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
        queue.start();
    }

    private boolean parseResponse(String response) {
        boolean hasChanged = false;
        try {
            JSONObject obj = new JSONObject(response);
            String status = obj.getString("status");
            if (status.equals("DATA_FOUND")) {
                JSONArray ids = obj.getJSONArray("id");
                JSONArray titles = obj.getJSONArray("title");
                JSONArray descriptions = obj.getJSONArray("description");
                JSONArray filenames = obj.getJSONArray("filename");
                JSONArray sizes = obj.getJSONArray("size");
                JSONArray createds = obj.getJSONArray("created");
                if (videoList.size() < ids.length()) {
                    hasChanged = true;
                    for (int i = videoList.size(); i < ids.length(); ++i)
                        videoList.add(new Video(ids.getString(i), titles.getString(i), descriptions.getString(i), filenames.getString(i), sizes.getString(i), createds.getString(i)));
                }
            }
        } catch (Exception e) {
            hasChanged = false;
            Toast.makeText(context,"Network Response Error",Toast.LENGTH_SHORT).show();
        }
        return hasChanged;
    }
}

