package com.example.android.testradar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
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
    private void syncSingleVisit(){

        JSONObject params=new JSONObject();
        try {
            params.put("id", "sample value");

        }catch (JSONException e){
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest;
        jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, "blabla/visit_insert.php", params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        MySingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest);
    }





}
