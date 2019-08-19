package com.example.android.testradar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
//import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.oscim.android.MapView;
import org.oscim.backend.CanvasAdapter;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.map.Map;
import org.oscim.renderer.GLViewport;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.MapScaleBar;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import java.io.File;

import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.marker.ItemizedLayer;

import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.marker.MarkerSymbol.HotspotPlace;
//import org.oscim.tiling.source.OkHttpEngine;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

//import com.example.android.testradar.mapfile.MapFileTileSource;

/**
 * A very basic Android app example.
 * <p>
 * You'll need a map with filename berlin.map from download.mapsforge.org in device storage.
 */
public class GettingStartedMarkerFused extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ItemizedLayer.OnItemGestureListener<TaxiMarker>, SensorEventListener, GestureListener {
    // Name of the map file in device storage
    private static final String MAP_FILE = "nicaragua.map";
    //private static final String MAP_FILE = "janofa.map";

    private MapView mapView;
    private MapScaleBar mapScaleBar;
    private Context mContext;
    private Location mLocation;
    private Compass mCompass;
    private ImageView compassImage;
    private ImageView backToCenterImage;


    private float mTilt;
    private double mScale;

    private float mRotation;
    MarkerSymbol symbol;
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetometer;

    MarkerSymbol mFocusMarker;
    ItemizedLayer<TaxiMarker> mMarkerLayer;

    //    private LocationLayer andresLocationLayer;
//    private LocationLayer locationLayer;
    private LocationManager locationManager;
    private final MapPosition mapPosition = new MapPosition();
    MarkerSymbol[] mClickAnimationSymbols = new MarkerSymbol[19];
    Drawable[] mClickAnimationDrawables = new Drawable[19];
    private MarkerSymbol[] mScaleSymbols = new MarkerSymbol[100];

    DataConnect dataConnect;


    //experiment
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // we build google api client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(500);


        mContext = getApplicationContext();

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        mSensorAccelerometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD);

        setContentView(R.layout.activity_tilemap);
        // Map view
        mapView = (MapView) findViewById(R.id.mapView);

        compassImage = (ImageView) findViewById(R.id.compass);
        backToCenterImage = (ImageView) findViewById(R.id.back_to_center);
        mCompass = new Compass(this, mapView.map(), compassImage);


        // Tile source
        MapFileTileSource tileSource = new MapFileTileSource();

        String mapPath = new File(Environment.getExternalStorageDirectory(), MAP_FILE).getAbsolutePath();

        tileSource.setMapFile(mapPath);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (tileSource.setMapFile(mapPath)) {

            // Vector layer
            VectorTileLayer tileLayer = mapView.map().setBaseMap(tileSource);

            // Building layer
            mapView.map().layers().add(new BuildingLayer(mapView.map(), tileLayer));

            // Label layer
            //mapView.map().layers().add(new LabelLayer(mapView.map(), tileLayer));

            // Render theme
            mapView.map().setTheme(VtmThemes.DEFAULT);
            //add set pivot
            mapView.map().viewport().setMapViewCenter(0.75f);


            // Scale bar
            mapScaleBar = new DefaultMapScaleBar(mapView.map());
            MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(mapView.map(), mapScaleBar);
            mapScaleBarLayer.getRenderer().setPosition(GLViewport.Position.BOTTOM_LEFT);
            mapScaleBarLayer.getRenderer().setOffset(5 * CanvasAdapter.getScale(), 0);
            mapView.map().layers().add(mapScaleBarLayer);

            mTilt = mapView.map().viewport().getMinTilt();
            mScale = 1 << 17;

//            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
//                mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                mapView.map().setMapPosition(mLocation.getLatitude(),mLocation.getLongitude(),mScale);
//
//            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    mLocation = location;
                                    mapView.map().setMapPosition(mLocation.getLatitude(), mLocation.getLongitude(), mScale);
                                }
                            }
                        });
            }

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    endLocation = locationResult.getLastLocation();
                    mCompass.setCurrLocation(endLocation);
                    if (mCurrLocation != null && mLocation != null && !mClicked) {
                        smoothenMapMovement(mCurrLocation, mLocation, endLocation);
                        mLocation = endLocation;
                    }
                }

                ;
            };


        }
        mCompass.setEnabled(true);
        mCompass.setMode(Compass.Mode.C2D);

        mapView.map().layers().add(mCompass);

        mapView.map().layers().add(new MapEventsReceiver(mapView));
        Drawable icon = getResources().getDrawable(R.drawable.frame00);
        Bitmap bitmapPoi = AndroidGraphicsCustom.drawableToBitmap(icon, 200);

        for (int i = 0; i <= 18; i++) {
            String name = (i < 10) ? "frame0" : "frame";
            Drawable drawable = getResources().getDrawable(this.getResources().getIdentifier(name + i, "drawable", this.getPackageName()));
            mClickAnimationDrawables[i] = drawable;
            mClickAnimationSymbols[i] = new MarkerSymbol(AndroidGraphicsCustom.drawableToBitmap(drawable, 200), HotspotPlace.CENTER, false);
        }

        //prepare bitmaps
        for (int i = 0; i < 100; i++) {
            mScaleSymbols[i] = new MarkerSymbol(AndroidGraphicsCustom.drawableToBitmap(icon, 101 + i), HotspotPlace.CENTER, false);
        }

        //TextureItem t=new TextureItem(bitmapPoi);
        //TextureRegion tr=new TextureRegion(t,new TextureAtlas.Rect(0,0,10,10));

        symbol = new MarkerSymbol(bitmapPoi, HotspotPlace.CENTER, false);

        mMarkerLayer = new ItemizedLayer<>(mapView.map(), new ArrayList<TaxiMarker>(), symbol, this);
        mapView.map().layers().add(mMarkerLayer);

        //mMarkerLayer.addItem(new TaxiMarker(1,"theos id", "", new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude()),1,"uno"));

        //response from callback
        dataConnect = new DataConnect(mContext);
        dataConnect.setOnTaxiDataListener(new DataConnect.OnTaxiDataListener() {
            @Override
            public void onTaxiDataArrived(TaxiMarker[] taxiArray) {
                Toast.makeText(mContext, "your data just arrived" + taxiArray.length + ", " + taxiArray[0].getTaxiId(), Toast.LENGTH_LONG).show();
            }
        });

        backToCenterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToCenter();
            }
        });
        backToCenterImage.setVisibility(ImageView.INVISIBLE);

        Drawable d = backToCenterImage.getDrawable();
        if (d instanceof AnimatedVectorDrawableCompat) {
            advCompat = (AnimatedVectorDrawableCompat) d;
        } else if (d instanceof AnimatedVectorDrawable) {
            adv = (AnimatedVectorDrawable) d;

        }


    }

    AnimatedVectorDrawableCompat advCompat;
    AnimatedVectorDrawable adv;

    public void prepareClickFrames(double scale) {
        for (int i = 0; i <= 18; i++) {
            String name = (i < 10) ? "frame0" : "frame";
            Drawable drawable = getResources().getDrawable(this.getResources().getIdentifier(name + i, "drawable", this.getPackageName()));
            //mClickAnimationSymbols[i]=addCircleOnClick(drawable);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();


        // Listeners for the sensors are registered in this callback and
        // can be unregistered in onStop().
        //
        // Check to ensure sensors are available before registering listeners.
        // Both listeners are registered with a "normal" amount of delay
        // (SENSOR_DELAY_NORMAL).
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorMagnetometer != null) {
            mSensorManager.registerListener(this, mSensorMagnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unregister all sensor listeners in this callback so they don't
        // continue to use resources when the app is stopped.
        mSensorManager.unregisterListener(this);
    }

    TaxiMarker taxiMarker;

    void createLayers(ItemizedLayer<TaxiMarker> itemizedLayer, Location location, float rotation) {
        if (itemizedLayer.getItemList().size() > 0) {
            taxiMarker = itemizedLayer.getItemList().get(0);
            Log.d("testR", "right place");
        } else {
            taxiMarker = new TaxiMarker(1, "theos id", "", new GeoPoint(location.getLatitude(), location.getLongitude()), 1, "uno");
            Log.d("testR", "wrong place");
        }
        taxiMarker.geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        taxiMarker.setMarker(symbol);
        taxiMarker.setRotation(rotation);
        if (itemizedLayer.getItemList().size() > 0) {
            itemizedLayer.getItemList().set(0, taxiMarker);
        } else {
            itemizedLayer.addItem(taxiMarker);
        }
        itemizedLayer.populate();//???

//        itemizedLayer.removeAllItems();
//        List<TaxiMarker> arrayOfOne = new ArrayList<>();
//
//        taxiMarker=new TaxiMarker(1,"theos id", "", new GeoPoint(location.getLatitude(), location.getLongitude()),1,"uno");
//        taxiMarker.setMarker(symbol);
//        taxiMarker.setRotation(rotation);
//
//        arrayOfOne.add(taxiMarker);
//
//        itemizedLayer.addItems(arrayOfOne);
        //mapView.map().updateMap(false);
    }

    //test  fhfg gn
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
//        enableAvailableProviders();

    }


    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
//        locationManager.removeUpdates(this);
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);


    }

    @Override
    protected void onDestroy() {
        if (mapScaleBar != null)
            mapScaleBar.destroy();
        mapView.onDestroy();
        super.onDestroy();
    }

    boolean mClicked=false;
    private Location mCurrLocation=new Location("");
//    @Override
//    public void onLocationChanged(Location location) {
//        endLocation=location;
//        if (mCurrLocation!=null && mLocation!=null && !mClicked) {
//            smoothenMapMovement(mCurrLocation, mLocation, location);
//            mLocation = location;
//        }
//
//
//        //mapView.map().viewport().setMapPosition(new MapPosition(location.getLatitude(),location.getLongitude(),1<<17));
//        //dataConnect.getTaxiData();
//    }
//
//    @Override
//    public void onStatusChanged(String s, int i, Bundle bundle) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String s) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String s) {
//
//    }
//
//    private void enableAvailableProviders() {
//        locationManager.removeUpdates(this);
//
//        for (String provider : locationManager.getProviders(true)) {
//            if (LocationManager.GPS_PROVIDER.equals(provider)) {
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
//                    locationManager.requestLocationUpdates(provider, 300, 0, this);
//            }
//        }
//    }

    //    private float[] mAccelerometerData = new float[3];
//    private float[] mMagnetometerData = new float[3];
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // The sensor type (as defined in the Sensor class).
//        int sensorType = sensorEvent.sensor.getType();
//
//        // The sensorEvent object is reused across calls to onSensorChanged().
//        // clone() gets a copy so the data doesn't change out from under us
//        switch (sensorType) {
//            case Sensor.TYPE_ACCELEROMETER:
//                mAccelerometerData = sensorEvent.values.clone();
//                break;
//            case Sensor.TYPE_MAGNETIC_FIELD:
//                mMagnetometerData = sensorEvent.values.clone();
//                break;
//            default:
//                return;
//        }
//        // Compute the rotation matrix: merges and translates the data
//        // from the accelerometer and magnetometer, in the device coordinate
//        // system, into a matrix in the world's coordinate system.
//        //
//        // The second argument is an inclination matrix, which isn't
//        // used in this example.
//        float[] rotationMatrix = new float[9];
//        boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
//                null, mAccelerometerData, mMagnetometerData);
//
//        float orientationValues[] = new float[3];
//        if (rotationOK) {
//            SensorManager.getOrientation(rotationMatrix,
//                    orientationValues);
//        }
//
//        // Pull out the individual values from the array.
//        float azimuth = orientationValues[0];
//        float pitch = orientationValues[1];
//        float roll = orientationValues[2];

        if (mLocation!=null && !mClicked) {
            if (taxiMarker != null) {
                mMarkerLayer.removeAllItems();
                taxiMarker.setRotation(mCompass.getRotation());
                mMarkerLayer.addItem(taxiMarker);
                mapView.map().updateMap(false);
            }

        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    int mCurrSize;

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    class MapEventsReceiver extends Layer implements GestureListener, Map.UpdateListener {

        MapEventsReceiver(MapView mapView) {
            super(mapView.map());
        }

        @Override
        public boolean onGesture(Gesture g, MotionEvent e) {

//            mCurrLocation.setLatitude(mapView.map().getMapPosition().getLatitude());
//            mCurrLocation.setLatitude(mapView.map().getMapPosition().getLongitude());
//            if (g instanceof Gesture.Tap) {
//                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
//                Toast.makeText(mContext, "Map tap\n" + p, Toast.LENGTH_SHORT).show();
//                return true;
//            }
//            if (g instanceof Gesture.LongPress) {
//                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
//                Toast.makeText(mContext, "Map long press\n" + p, Toast.LENGTH_SHORT).show();
//                return true;
//            }
//            if (g instanceof Gesture.TripleTap) {
//                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
//                Toast.makeText(mContext, "Map triple tap\n" + p, Toast.LENGTH_SHORT).show();
//                return true;
//            }
            return false;
        }

        @Override
        public void onMapEvent(Event e, MapPosition mapPosition) {
            if (e == Map.POSITION_EVENT || e == Map.SCALE_EVENT || e == Map.ANIM_END|| e==Map.ROTATE_EVENT) {

                    //Toast.makeText(mContext,"currPos: "+mapPosition.getLatitude(),Toast.LENGTH_LONG).show();
                    mCurrLocation.setLatitude(mapView.map().getMapPosition().getLatitude());
                    mCurrLocation.setLongitude(mapView.map().getMapPosition().getLongitude());
                    // Toast.makeText(mContext,"currPos: "+mapPosition.getLatitude()+" "+mCurrLocation.getLatitude(),Toast.LENGTH_LONG).show();



            }
            if (e==Map.MOVE_EVENT || e == Map.SCALE_EVENT || e==Map.ROTATE_EVENT){
                backToCenterImage.setVisibility(ImageView.VISIBLE);
                bullseyeAnim();
                //mCompass.setMode(Compass.Mode.OFF);
                wasMoved=true;
                mCompass.controlView(false);
                rescheduleTimer();
            }
            if(e == Map.SCALE_EVENT || e == Map.ANIM_END|| e==Map.ROTATE_EVENT) {
                double scale = mapPosition.getZoom();
                Log.d("scale_test", "scale:" + scale);
                mScale=mapPosition.getScale();
                int currSize;
                if (scale>16 && scale<17){
                    currSize=(int) (100*(scale-16));
                }else if (scale>=17){
                    currSize=99;
                }else{
                    currSize=0;
                }
                scaleIcon(currSize);
                scaleAnimation(currSize);

            }
            if (e==Map.TILT_EVENT){
                mTilt=mapPosition.getTilt();
            }
        }

    }
    public void scaleIcon(int scale){
        symbol=mScaleSymbols[scale];
        taxiMarker = mMarkerLayer.getItemList().get(0);
        taxiMarker.setMarker(symbol);
        taxiMarker.setRotation(mCompass.getRotation());
        mMarkerLayer.getItemList().set(0,taxiMarker);
        mapView.map().updateMap(false);
    }

    public void scaleAnimation(final int currSize){
        if (currSize!=mCurrSize) {
            mCurrSize=currSize;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currSize == mCurrSize) {
                        for (int i = 0; i < mClickAnimationSymbols.length; i++) {
                            mClickAnimationSymbols[i] = new MarkerSymbol(AndroidGraphicsCustom.drawableToBitmap(mClickAnimationDrawables[i], 101 + currSize), HotspotPlace.CENTER, false);
                        }
                    }
                }
            }, 100);
        }

    }

    public void rescheduleTimer(){
        mTimer.cancel();
        mTimer=new Timer("movementTimer",true);
        MyTimerClass timerTask=new MyTimerClass();
        mCompass.setMode(Compass.Mode.OFF);
        mTimer.schedule(timerTask,15000);
    }

    private Timer mTimer=new Timer("movementTimer",true);
    private class MyTimerClass extends TimerTask{
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    backToCenter();
                    //mCompass.setEnabled(true);

                }
            });

        }
    };

    private void backToCenter(){
        wasMoved=false;
        mCompass.setMapRotation(-mapView.map().getMapPosition().getBearing());
        smoothenMapMovement(mCurrLocation,mLocation,endLocation);
        backToCenterImage.setVisibility(ImageView.INVISIBLE);
        //mCompass.setMode(Compass.Mode.C2D);
        mTimer.cancel();
    }



    private void bullseyeAnim(){
        Drawable d = backToCenterImage.getDrawable();
        if (d instanceof AnimatedVectorDrawableCompat){
            //AnimatedVectorDrawableCompat advCompat = (AnimatedVectorDrawableCompat) d;
            advCompat.stop();
            advCompat.start();
        } else if (d instanceof AnimatedVectorDrawable){
            //AnimatedVectorDrawable adv = (AnimatedVectorDrawable) d;
            adv.stop();
            adv.start();
        }
    }

//private MarkerSymbol addCircleOnClick(Drawable drawable){
//    Bitmap circle = drawableToBitmap(drawable);
//    MarkerSymbol circleSym= new MarkerSymbol(circle, HotspotPlace.CENTER,false);
//    return  circleSym;
//}

    @Override
    public boolean onItemSingleTapUp(int index, TaxiMarker item) {
        //Toast.makeText(this,item.getTaxiComment(),Toast.LENGTH_LONG).show();

        taxiClickAnimation(item);
        return false;
    }

    private void taxiClickAnimation(final TaxiMarker item){
        mClicked=true;
        final int size= mClickAnimationSymbols.length;
        for (int a = 0; a<size ;a++) {
            final int i=a;
            mapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    item.setMarker(mClickAnimationSymbols[i]);
                    item.getMarker().setRotation(mCompass.getRotation());
                    mMarkerLayer.update();
                    mapView.map().updateMap(false);
                    Log.d("marker_error","loop "+i);
                    mClicked=i!=size-1;

                }
            }, 40*i);
        }
        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setMarker(mClickAnimationSymbols[0]);
                item.getMarker().setRotation(mCompass.getRotation());
                mMarkerLayer.update();
                mapView.map().updateMap(false);
            }
        }, 40*size);
    }

    private Location endLocation=new Location("");
    private boolean wasMoved=false;

    private void smoothenMapMovement(Location initialMap, Location initialTaxi, final Location end){
        double latDiffMap=end.getLatitude()-initialMap.getLatitude();
        double lonDiffMap=end.getLongitude()-initialMap.getLongitude();
        //float rotDiffMap=-mCompass.getRotation()-mapView.map().getMapPosition().getBearing();
        //mCompass.setMapRotation(mapView.map().getMapPosition().getBearing());

        double latDiff=end.getLatitude()-initialTaxi.getLatitude();
        double lonDiff=end.getLongitude()-initialTaxi.getLongitude();
        final int frames=19;
        for (int a = 0; a<frames ;a++) {
            final double latMap = initialMap.getLatitude() + latDiffMap * (a + 1) / frames;
            final double lonMap = initialMap.getLongitude() + lonDiffMap * (a + 1) / frames;
            final int i=a;

            final double lat = initialTaxi.getLatitude() + latDiff * (a + 1) / frames;
            final double lon = initialTaxi.getLongitude() + lonDiff * (a + 1) / frames;
            final Location loc=new Location(initialTaxi);
            loc.setLatitude(lat);
            loc.setLongitude(lon);

            mapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (end==endLocation) {

                        if (!wasMoved) {
                            //move map
                            mapView.map().viewport().setMapPosition(new MapPosition(latMap, lonMap, mScale));
                            mapView.map().viewport().setTilt(mTilt);
                            mapView.map().viewport().setRotation(-mCompass.getMapRotation());
                            //reset anim initial values
                            mCurrLocation.setLatitude(latMap);
                            mCurrLocation.setLongitude(lonMap);
                            if(i==frames-1){
                                mCompass.controlView(true);
                                mCompass.setMode(Compass.Mode.C2D);
                            }
                        }

                        //move taxi
                        createLayers(mMarkerLayer, loc, mCompass.getRotation());
                        Log.d("marker_error", "potential issue location changed"+end.getTime()+" acc:"+end.getAccuracy());
                        mapView.map().updateMap(true);

                        //reset anim initial values
                        mLocation=loc;

                    }

                }
            }, 25 * a);
        }
    }

    private void smoothenTaxiMovement(Location initial, Location end){
        double latDiff=end.getLatitude()-initial.getLatitude();
        double lonDiff=end.getLongitude()-initial.getLongitude();
        int frames=18;
        for (int a = 0; a<frames ;a++) {
            final double lat = initial.getLatitude() + latDiff * (a + 1) / frames;
            final double lon = initial.getLongitude() + lonDiff * (a + 1) / frames;
            final Location loc=new Location(initial);
            loc.setLatitude(lat);
            loc.setLongitude(lon);
            mapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    createLayers(mMarkerLayer,loc,mCompass.getRotation());
                    mapView.map().updateMap(false);

                }
            }, 25 * a);
        }
    }

    @Override
    public boolean onItemLongPress(int index, TaxiMarker item) {
        return false;
    }

    @Override
    public boolean onGesture(Gesture g, MotionEvent e) {
        return false;
    }




}

