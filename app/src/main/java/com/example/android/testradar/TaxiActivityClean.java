package com.example.android.testradar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import org.oscim.android.MapView;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerLayer;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.renderer.GLViewport;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.MapScaleBar;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import java.io.File;
import java.util.ArrayList;

import static org.oscim.android.canvas.AndroidGraphics.drawableToBitmap;

public class TaxiActivityClean extends Activity implements LocationListener, ItemizedLayer.OnItemGestureListener<TaxiMarker>, SensorEventListener, Map.UpdateListener {
    //name of map file to be used
    private static final String MAP_FILE = "nicaragua.map";

    //map render relevant variables
    private MapView mapView;
    private MapScaleBar mapScaleBar;
    private Context mContext;
    private MarkerLayer mMarkerLayer;
    private LocationManager locationManager;
    private Location mLocation;

    //animation and bitmap variables
    MarkerSymbol[] mAnimationSymbols=new MarkerSymbol[18];
    MarkerSymbol mSymbol;

    //data connect variables
    DataConnect mDataConnect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

//        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
//
//        mSensorAccelerometer = mSensorManager.getDefaultSensor(
//                Sensor.TYPE_ACCELEROMETER);
//        mSensorMagnetometer = mSensorManager.getDefaultSensor(
//                Sensor.TYPE_MAGNETIC_FIELD);
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //TELL ACTIVITY THAT MAP FILLS THE CONTENT VIEW
        // Map view
        mapView = new MapView(this);
        setContentView(mapView);

        //SET UP MAP VIEW
        // Tile source
        MapFileTileSource tileSource = new MapFileTileSource();
        //map file path
        String mapPath = new File(Environment.getExternalStorageDirectory(), MAP_FILE).getAbsolutePath();
        //set path of tile source
        tileSource.setMapFile(mapPath);

        if (tileSource.setMapFile(mapPath)) {
            // Vector layer
            VectorTileLayer tileLayer = mapView.map().setBaseMap(tileSource);
            // Building layer !!!consider deleting as there are too few buildings mapped in Nicaragua
            mapView.map().layers().add(new BuildingLayer(mapView.map(), tileLayer));
            // Label layer !!!modify for custom layers
            mapView.map().layers().add(new LabelLayer(mapView.map(), tileLayer));
            // Render theme
            mapView.map().setTheme(VtmThemes.DEFAULT);
            // Scale bar
            mapScaleBar = new DefaultMapScaleBar(mapView.map());
            MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(mapView.map(), mapScaleBar);
            mapScaleBarLayer.getRenderer().setPosition(GLViewport.Position.BOTTOM_LEFT);
            mapScaleBarLayer.getRenderer().setOffset(5 * CanvasAdapter.getScale(), 0);
            mapView.map().layers().add(mapScaleBarLayer);
            //placing map view centered on own location
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mapView.map().setMapPosition(loc.getLatitude(), loc.getLongitude(), 1 << 10);
            }
        }
        //add event receiver in order to react to map events such as zoom
        mapView.map().layers().add(new MapEventsReceiver(mapView));



        //prepares animation frame sequence of on taxi click event
        for (int i = 0; i < 18; i++) {
            String name = (i < 10) ? "frame0" : "frame";
            Drawable drawable = getResources().getDrawable(this.getResources().getIdentifier(name + i, "drawable", this.getPackageName()));
            mAnimationSymbols[i] = new MarkerSymbol(drawableToBitmap(drawable), MarkerSymbol.HotspotPlace.CENTER,false);
        }


        //set up graphics and marker symbol
        Bitmap bitmapPoi = drawableToBitmap(getResources().getDrawable(R.drawable.frame00));
        mSymbol = new MarkerSymbol(bitmapPoi, MarkerSymbol.HotspotPlace.CENTER, false);


        //add itemized layer to the mapview
        mMarkerLayer = new ItemizedLayer<>(mapView.map(), new ArrayList<TaxiMarker>(), mSymbol, this);
        mapView.map().layers().add(mMarkerLayer);

        //this is a callback method from data connect in order to update taxi position whenever the data arrives (so far only toast)
        mDataConnect = new DataConnect(mContext);
        mDataConnect.setOnTaxiDataListener(new DataConnect.OnTaxiDataListener() {
            @Override
            public void onTaxiDataArrived(TaxiMarker[] taxiArray) {
                Toast.makeText(mContext, "your data just arrived" + taxiArray.length + ", " + taxiArray[0].getTaxiId(), Toast.LENGTH_LONG).show();
            }
        });


    }

    class MapEventsReceiver extends Layer implements GestureListener, Map.UpdateListener {

        MapEventsReceiver(MapView mapView) {
            super(mapView.map());
        }

        @Override
        public boolean onGesture(Gesture g, MotionEvent e) {

            return false;
        }
        //test to enable resizing on zoom
        @Override
        public void onMapEvent(Event e, MapPosition mapPosition) {
            if(e == Map.SCALE_EVENT || e == Map.ANIM_END) {
                double scale = mapPosition.getZoom();
                Log.d("scale_test", "scale:" + scale);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

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

    @Override
    public boolean onItemSingleTapUp(int index, TaxiMarker item) {
        return false;
    }

    @Override
    public boolean onItemLongPress(int index, TaxiMarker item) {
        return false;
    }

    @Override
    public void onMapEvent(Event e, MapPosition mapPosition) {

    }
}

