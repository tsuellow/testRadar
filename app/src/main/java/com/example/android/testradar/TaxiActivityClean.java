package com.example.android.testradar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


import org.geojson.FeatureCollection;

import org.oscim.android.MapView;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Color;
import org.oscim.core.MapPosition;
import org.oscim.event.Event;

import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;

import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.renderer.GLViewport;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.MapScaleBar;
import org.oscim.scalebar.MapScaleBarLayer;

import org.oscim.theme.VtmThemes;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.mapfile.MapFileTileSource;




import java.io.File;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import java.util.List;

import java.util.Timer;
import java.util.TimerTask;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import static org.oscim.android.canvas.AndroidGraphics.drawableToBitmap;

public class TaxiActivityClean extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ItemizedLayer.OnItemGestureListener<TaxiMarker>, SensorEventListener, GestureListener {

    // Name of the map file in device storage
    private static final String MAP_FILE = "nicaragua.map";
    private MapView mapView;

    private MapScaleBar mapScaleBar;
    private Context mContext;
    private Location mLocation;
    private Compass mCompass;
    private ImageView compassImage;
    private ImageView backToCenterImage;

    private AlertDialog dialogCalibrate;
    Vibrator mVibrator;


    private float mTilt;
    private double mScale;


    MarkerSymbol symbol;
    private SensorManager mSensorManager;

    MarkerSymbol mFocusMarker;
    ItemizedLayer<TaxiMarker> mMarkerLayer;

    private final MapPosition mapPosition = new MapPosition();
    MarkerSymbol[] mClickAnimationSymbols = new MarkerSymbol[19];
    Drawable[] mClickAnimationDrawables = new Drawable[19];
    private MarkerSymbol[] mScaleSymbols = new MarkerSymbol[100];

    DataConnect dataConnect;


    //experiment
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private InputStream geoJsonIs;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onGesture(Gesture g, MotionEvent e) {
        return false;
    }

    @Override
    public boolean onItemSingleTapUp(int index, TaxiMarker item) {
        return false;
    }

    @Override
    public boolean onItemLongPress(int index, TaxiMarker item) {
        return false;
    }
}

