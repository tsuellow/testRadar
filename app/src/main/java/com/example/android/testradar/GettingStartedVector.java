package com.example.android.testradar;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;

import android.support.v4.app.ActivityCompat;


import org.oscim.android.MapView;
import org.oscim.backend.CanvasAdapter;
import org.oscim.core.MapPosition;

import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;

import org.oscim.renderer.GLViewport;

import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.MapScaleBar;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import java.io.File;

import android.widget.Toast;

import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.marker.MarkerSymbol.HotspotPlace;

import java.util.ArrayList;
import java.util.List;

import static org.oscim.android.canvas.AndroidGraphics.drawableToBitmap;

public class GettingStartedVector extends Activity implements LocationListener, ItemizedLayer.OnItemGestureListener<MarkerItem>, SensorEventListener {
    // Name of the map file in device storage
    private static final String MAP_FILE = "nica.map";

    private MapView mapView;
    private MapScaleBar mapScaleBar;
    private Context mContext;
    private Location mLocation;

    private  float mRotation;
    MarkerSymbol symbol;
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetometer;

    MarkerSymbol mFocusMarker;
    ItemizedLayer<MarkerItem> mMarkerLayer;

    //    private LocationLayer andresLocationLayer;
//    private LocationLayer locationLayer;
    private LocationManager locationManager;
    private final MapPosition mapPosition = new MapPosition();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext= getApplicationContext();

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        mSensorAccelerometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD);

        // Map view
        mapView = new MapView(this);
        setContentView(mapView);




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
            mapView.map().layers().add(new LabelLayer(mapView.map(), tileLayer));

            // Render theme
            mapView.map().setTheme(VtmThemes.DEFAULT);
            //add markers


            // Scale bar
            mapScaleBar = new DefaultMapScaleBar(mapView.map());
            MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(mapView.map(), mapScaleBar);
            mapScaleBarLayer.getRenderer().setPosition(GLViewport.Position.BOTTOM_LEFT);
            mapScaleBarLayer.getRenderer().setOffset(5 * CanvasAdapter.getScale(), 0);
            mapView.map().layers().add(mapScaleBarLayer);
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mapView.map().setMapPosition(52.35,9.7, 1<<10);
            }


        }
        mapView.map().layers().add(new MapEventsReceiver(mapView));
        Bitmap bitmapPoi = drawableToBitmap(getResources().getDrawable(R.drawable.taxi_icon));
//        TextureItem t=new TextureItem(bitmapPoi);
//        TextureRegion tr=new TextureRegion(t,new TextureAtlas.Rect(0,0,100,100));

        symbol = new MarkerSymbol(bitmapPoi, HotspotPlace.CENTER,false);

        mMarkerLayer = new ItemizedLayer<>(mapView.map(), new ArrayList<MarkerItem>(), symbol, this);
        mapView.map().layers().add(mMarkerLayer);


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

    void createLayers(Location location, float rotation) {


        mMarkerLayer.removeAllItems();


        List<MarkerItem> pts = new ArrayList<>();

        MarkerItem markerItem=new MarkerItem("theos id"+rotation, "", new GeoPoint(location.getLatitude(), location.getLongitude()));
        markerItem.setMarker(symbol);
        markerItem.setRotation(rotation);

        pts.add(markerItem);

        mMarkerLayer.addItems(pts);
        mapView.map().updateMap(true);
    }
    //test  fhfg gn
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        enableAvailableProviders();

    }


    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onDestroy() {
        if (mapScaleBar != null)
            mapScaleBar.destroy();
        mapView.onDestroy();
        super.onDestroy();
    }


    @Override
    public void onLocationChanged(Location location) {
        mLocation=location;
        createLayers(mLocation,mRotation);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void enableAvailableProviders() {
        locationManager.removeUpdates(this);

        for (String provider : locationManager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER.equals(provider)
                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(provider, 5000, 0, this);
            }
        }
    }

    private float[] mAccelerometerData = new float[3];
    private float[] mMagnetometerData = new float[3];
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // The sensor type (as defined in the Sensor class).
        int sensorType = sensorEvent.sensor.getType();

        // The sensorEvent object is reused across calls to onSensorChanged().
        // clone() gets a copy so the data doesn't change out from under us
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData = sensorEvent.values.clone();
                break;
            default:
                return;
        }
        // Compute the rotation matrix: merges and translates the data
        // from the accelerometer and magnetometer, in the device coordinate
        // system, into a matrix in the world's coordinate system.
        //
        // The second argument is an inclination matrix, which isn't
        // used in this example.
        float[] rotationMatrix = new float[9];
        boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix,
                null, mAccelerometerData, mMagnetometerData);

        float orientationValues[] = new float[3];
        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrix,
                    orientationValues);
        }

        // Pull out the individual values from the array.
        float azimuth = orientationValues[0];
        float pitch = orientationValues[1];
        float roll = orientationValues[2];

        if (mLocation!=null) {

            mRotation = 180 * (azimuth / 3.1416f);
            createLayers(mLocation, mRotation);
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

//    private void smoothen(Location oldLoc, final Location newLoc){
//        int fraction=20;
//        double latDiff=newLoc.getLatitude()-oldLoc.getLatitude();
//        double lonDiff=newLoc.getLongitude()-oldLoc.getLongitude();
//        float accDiff=newLoc.getAccuracy()-oldLoc.getAccuracy();
//
//        Handler handler1 = new Handler();
//        for (int a = 1; a<=fraction ;a++) {
//            final double lat=oldLoc.getLatitude()+a*latDiff/fraction;
//            final double lon=oldLoc.getLongitude()+a*lonDiff/fraction;
//            final float acc=oldLoc.getAccuracy()+a*accDiff/fraction;
//            handler1.postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//                    if (newLoc==newLocation) {
//                        locationLayer.setPosition(lat, lon, acc);
//                        oldLocation = new Location("");
//                        oldLocation.setLatitude(lat);
//                        oldLocation.setLongitude(lon);
//                        oldLocation.setAccuracy(acc);
//                    }
//                }
//            }, 25*a);
//        }
//    }

    class MapEventsReceiver extends Layer implements GestureListener {

        MapEventsReceiver(MapView mapView) {
            super(mapView.map());
        }

        @Override
        public boolean onGesture(Gesture g, MotionEvent e) {
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
    }


    @Override
    public boolean onItemSingleTapUp(int index, MarkerItem item) {
        Toast.makeText(this,item.getTitle(),Toast.LENGTH_LONG).show();
        Bitmap longshot=drawableToBitmap(getResources().getDrawable(R.drawable.anim));
        MarkerSymbol newsym= new MarkerSymbol(longshot, HotspotPlace.CENTER,false);
        item.setMarker(newsym);
        return false;
    }

    @Override
    public boolean onItemLongPress(int index, MarkerItem item) {
        return false;
    }
}

