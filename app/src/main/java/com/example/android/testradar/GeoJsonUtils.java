package com.example.android.testradar;

import android.graphics.Color;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.simple.parser.JSONParser;
import org.oscim.core.GeoPoint;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.utils.ColorUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class GeoJsonUtils {


//    public JSONObject loadGeoJson(InputStream is){
//        JSONObject geoJson=new JSONObject();
//        JSONParser jsonParser = new JSONParser();
//        try {
//            geoJson = (JSONObject) jsonParser.parse(
//                    new InputStreamReader(is, StandardCharsets.UTF_8));
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return geoJson;
//    }


    static Style.Builder sb = org.oscim.layers.vector.geometries.Style.builder()
            .fillColor(Color.RED)
            .strokeColor(Color.RED)
            .strokeWidth(3)
            .fillAlpha(0.5f);

    static Style mStyle = sb.build();

    public static FeatureCollection loadFeatureCollection(InputStream is) {
        try {
            FeatureCollection fc = new ObjectMapper().readValue(is,FeatureCollection.class);
            return fc;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void addSingleBarrio(BarriosLayer barriosLayer, Feature feature) {
            //barrio identifying data
            String title = feature.getProperty("name");
            //int id = feature.getProperty("id");
            int color = Color.parseColor((String)feature.getProperty("fill"));
            //coordinates
            Polygon geom = (Polygon) feature.getGeometry();
            List<LngLatAlt> coords = geom.getExteriorRing();
            //set style
            Style style=sb.fillColor(color).strokeColor(Color.BLACK).build();
            //add barrio
            barriosLayer.add(new BarrioPolygonDrawable(GeoJsonUtils.convertToGeo(coords), style, title,0));

    }

    public static void addBarrios(BarriosLayer barriosLayer, FeatureCollection features){
        for (int i=0;i<features.getFeatures().size();i++){
            if (features.getFeatures().get(i).getGeometry() instanceof Polygon) {
                addSingleBarrio(barriosLayer,features.getFeatures().get(i));
            }
        }
    }




    public static List<GeoPoint> convertToGeo(List<LngLatAlt> lnglatList){
        List<GeoPoint> result=new ArrayList<GeoPoint>();
        for (int i=0;i<lnglatList.size();i++){
            LngLatAlt lnglat=lnglatList.get(i);
            result.add(new GeoPoint(lnglat.getLatitude(),lnglat.getLongitude()));
        }
        return result;
    }




//    public BarrioPolygonDrawable[] createBarriosArray(JSONObject geoJson){
//        BarrioPolygonDrawable[] result;
//        try {
//            JSONArray features = geoJson.getJSONArray("features");
//            for (int i=0; i<features.length(); i++){
//                BarrioPolygonDrawable barrio;
//                List<GeoPoint> coords=new ArrayList<GeoPoint>();
//                JSONObject feature=features.getJSONObject(i);
//                JSONArray jsonCoords=feature.getJSONObject("geometry").getJSONArray("coordinates");
//                JSONArray jsonCoords1=(JSONArray)jsonCoords.get(0);
//                JSONArray jsonCoords2=(JSONArray)jsonCoords1.get(0);
//
//
//            }
//
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//    }

}
