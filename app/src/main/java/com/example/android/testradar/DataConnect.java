package com.example.android.testradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oscim.layers.LocationLayer;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;

public class DataConnect {

    Context mContext;
    public DataConnect(Context context) {
        mContext=context;
    }

    public  boolean hasInternetConnectivity(){
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return ((netInfo != null));
    }

    //ckeck host availability
    public  boolean hasHostAccess(){
        try {
            InetAddress ip= InetAddress.getByName("server name");
            int port=80;
            SocketAddress socketAddress = new InetSocketAddress(ip,port);
            Socket socket = new Socket();
            int timeoutMs = 5000;   // 5 seconds
            socket.connect(socketAddress, timeoutMs);
            return true;
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //sync single client record to server
    public void syncPosition(PositionObject positionObject){

        JSONObject params=new JSONObject();
        try {
            params.put("userid", positionObject.getUserid());
            params.put("lat", positionObject.getLat());
            params.put("lon", positionObject.getLon());
            params.put("acc", positionObject.getAcc());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = simpleDateFormat.format(positionObject.getTime());
            params.put("time", currentTime);

        }catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest;
        jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, "https://www.awicon.de/theostaxi/php/posttest.php", params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    Log.d("andres_json_write", "OK");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        MySingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
    }

    public void findAndres(final LocationLayer locationLayer){
        //JSONObject params=new JSONObject();
//        try {
//            params.put("userid", positionObject.getUserid());
//            params.put("lat", positionObject.getLat());
//            params.put("lon", positionObject.getLon());
//            params.put("acc", positionObject.getAcc());
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String currentTime = simpleDateFormat.format(positionObject.getTime());
//            params.put("time", currentTime);
//
//        }catch (JSONException e){
//            e.printStackTrace();
//        }

        JsonArrayRequest jsonArrayRequest;
        jsonArrayRequest = new JsonArrayRequest
                (Request.Method.POST, "https://www.awicon.de/theostaxi/php/gettest.php", null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for(int i=0; i<response.length(); i++){
                                JSONObject jresponse = response.getJSONObject(i);
                                if (jresponse.getInt("userid") == 2){
                                    locationLayer.setPosition(jresponse.getDouble("lat"),jresponse.getDouble("lon"),jresponse.getDouble("acc"));
                                }
                            }

                        }catch (JSONException e) {
                            e.printStackTrace();
                        }


                        //locationLayer.setPosition();

                        Log.d("andres_json_response", "OK");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        MySingleton.getInstance(mContext).addToRequestQueue(jsonArrayRequest);
    }





}
