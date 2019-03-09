package com.example.android.testradar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


//import com.example.android.testradar.mapfile.MapFileTileSource;

import org.oscim.android.MapView;
import org.oscim.backend.CanvasAdapter;
import org.oscim.core.MapPosition;
import org.oscim.layers.LocationLayer;
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
import java.security.Permission;

/**
 * A very basic Android app example.
 * <p>
 * You'll need a map with filename berlin.map from download.mapsforge.org in device storage.
 */
public class GettingStarted extends Activity implements LocationListener {
    // Name of the map file in device storage
    private static final String MAP_FILE = "janofa.map";

    private MapView mapView;
    private MapScaleBar mapScaleBar;

    private Map mMap;
    private LocationLayer theoLocationLayer;
    private LocationLayer locationLayer;
    private LocationManager locationManager;
    private final MapPosition mapPosition = new MapPosition();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

            // Scale bar
            mapScaleBar = new DefaultMapScaleBar(mapView.map());
            MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(mapView.map(), mapScaleBar);
            mapScaleBarLayer.getRenderer().setPosition(GLViewport.Position.BOTTOM_LEFT);
            mapScaleBarLayer.getRenderer().setOffset(5 * CanvasAdapter.getScale(), 0);
            mapView.map().layers().add(mapScaleBarLayer);
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mapView.map().setMapPosition(loc.getLatitude(),loc.getLongitude(), 1<<15);
            }
            // Note: this map position is specific to Berlin area
        }


        theoLocationLayer = new LocationLayer(mapView.map());
        theoLocationLayer.locationRenderer.setShader("location_1_reverse");
        theoLocationLayer.setEnabled(true);
        theoLocationLayer.setPosition(52.394 , 9.7417026 , 19.915);


        mapView.map().layers().add(theoLocationLayer);

        locationLayer = new LocationLayer(mapView.map());
        locationLayer.locationRenderer.setShader("location_1_reverse");
        locationLayer.setEnabled(false);
        mapView.map().layers().add(locationLayer);

    }

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
        locationLayer.setEnabled(true);
        locationLayer.setPosition(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        Log.d("andres_lat_long", ""+location.getLatitude()+" , "+location.getLongitude()+" , "+location.getAccuracy());

        // Follow location
//        mapView.map().getMapPosition(mapPosition);
//        mapPosition.setPosition(location.getLatitude(), location.getLongitude());
        //mapView.map().setMapPosition(mapPosition);

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
                locationManager.requestLocationUpdates(provider, 0, 0, this);
            }
        }
    }
}
