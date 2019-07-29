package com.example.android.testradar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.oscim.android.MapView;
import org.oscim.backend.CanvasAdapter;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.layers.LocationLayer;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.atlas.TextureAtlas;
import org.oscim.renderer.atlas.TextureRegion;
import org.oscim.renderer.bucket.TextureItem;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.MapScaleBar;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import java.io.File;
import java.util.Date;

import android.os.Bundle;
import android.widget.Toast;

import com.caverock.androidsvg.SVG;

import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.marker.ItemizedLayer;

import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.marker.MarkerSymbol.HotspotPlace;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.map.Map;
import org.oscim.tiling.TileSource;
//import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.bitmap.DefaultSources;

import java.util.ArrayList;
import java.util.List;

import static org.oscim.android.canvas.AndroidGraphics.drawableToBitmap;

//import com.example.android.testradar.mapfile.MapFileTileSource;

/**
 * A very basic Android app example.
 * <p>
 * You'll need a map with filename berlin.map from download.mapsforge.org in device storage.
 */
public class GettingStartedMarker extends Activity implements LocationListener, ItemizedLayer.OnItemGestureListener<TaxiMarker>, SensorEventListener, Map.UpdateListener {
    // Name of the map file in device storage
    private static final String MAP_FILE = "nicaragua.map";

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
    ItemizedLayer<TaxiMarker> mMarkerLayer;

//    private LocationLayer andresLocationLayer;
//    private LocationLayer locationLayer;
    private LocationManager locationManager;
    private final MapPosition mapPosition = new MapPosition();
    MarkerSymbol[] animationSymbols=new MarkerSymbol[18];

    DataConnect dataConnect;

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
                mapView.map().setMapPosition(13.5,-86.8, 1<<10);
            }


        }
        mapView.map().layers().add(new MapEventsReceiver(mapView));
        Bitmap bitmapPoi = drawableToBitmap(getResources().getDrawable(R.drawable.frame00));

        for(int i=0;i<18;i++){
            String name=(i<10)?"frame0":"frame";
            Drawable drawable=getResources().getDrawable(this.getResources().getIdentifier(name+i, "drawable", this.getPackageName()));
            animationSymbols[i]=addCircleOnClick(drawable);
        }

        //TextureItem t=new TextureItem(bitmapPoi);
        //TextureRegion tr=new TextureRegion(t,new TextureAtlas.Rect(0,0,10,10));

        symbol = new MarkerSymbol(bitmapPoi, HotspotPlace.CENTER,false);

        mMarkerLayer = new ItemizedLayer<>(mapView.map(), new ArrayList<TaxiMarker>(), symbol, this);
        mapView.map().layers().add(mMarkerLayer);


        dataConnect= new DataConnect(mContext);
        dataConnect.setOnTaxiDataListener(new DataConnect.OnTaxiDataListener() {
            @Override
            public void onTaxiDataArrived(TaxiMarker[] taxiArray) {


                Toast.makeText(mContext, "your data just arrived"+taxiArray.length+", "+taxiArray[0].getTaxiId(),Toast.LENGTH_LONG).show();
            }
        });



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


        List<TaxiMarker> pts = new ArrayList<>();

        TaxiMarker TaxiMarker=new TaxiMarker(1,"theos id"+rotation, "", new GeoPoint(location.getLatitude(), location.getLongitude()),1,"uno");
        TaxiMarker.setMarker(symbol);
        TaxiMarker.setRotation(rotation);

        pts.add(TaxiMarker);

        mMarkerLayer.addItems(pts);
        mapView.map().updateMap(false);
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

    boolean mClicked=false;
    @Override
    public void onLocationChanged(Location location) {
        if(!mClicked) {
            mLocation = location;
            createLayers(mLocation, mRotation);
        }
        dataConnect.getTaxiData();
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

        if (mLocation!=null && !mClicked) {

            mRotation = 180 * (azimuth / 3.1416f);
            createLayers(mLocation, mRotation);
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onMapEvent(Event e, MapPosition mapPosition) {
    if(e == Map.SCALE_EVENT) {
         double scale = mapPosition.getScale();
         Log.d("scale_test", "scale:" + scale);
    }
    }


    class MapEventsReceiver extends Layer implements GestureListener, Map.UpdateListener {

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

        @Override
        public void onMapEvent(Event e, MapPosition mapPosition) {
            if(e == Map.SCALE_EVENT || e == Map.ANIM_END) {
                double scale = mapPosition.getZoom();
                Log.d("scale_test", "scale:" + scale);
            }
        }

    }

private MarkerSymbol addCircleOnClick(Drawable drawable){
    Bitmap circle = drawableToBitmap(drawable);
    MarkerSymbol circleSym= new MarkerSymbol(circle, HotspotPlace.CENTER,false);
    return  circleSym;
}

    @Override
    public boolean onItemSingleTapUp(int index, TaxiMarker item) {
        Toast.makeText(this,item.getTaxiComment(),Toast.LENGTH_LONG).show();

        anime(item);
        return false;
    }

    private void anime(final TaxiMarker item){
        mClicked=true;
        final int size=animationSymbols.length;
        for (int a = 0; a<size ;a++) {
            final int i=a;
            mapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    item.setMarker(animationSymbols[i]);
                    item.getMarker().setRotation(mRotation);
                    mMarkerLayer.update();
                    mapView.map().updateMap(true);
                    Log.d("marker_error","loop "+i);
                    mClicked=i!=size-1;

                }
            }, 25*i);
        }
    }

    @Override
    public boolean onItemLongPress(int index, TaxiMarker item) {
        return false;
    }
}
