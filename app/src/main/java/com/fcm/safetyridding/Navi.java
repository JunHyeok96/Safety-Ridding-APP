package com.fcm.safetyridding;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Navi extends FragmentActivity implements OnMapReadyCallback {
    double longitude,latitude;
    Marker marker;
    ArrayList<Marker> mobilityMarker = new ArrayList<Marker>();
    MarkerOptions makerOptions = new MarkerOptions();
    MarkerOptions bikeOptions = new MarkerOptions();
    MarkerOptions kickOptions = new MarkerOptions();
    MarkerOptions motoOptions = new MarkerOptions();
    double rawLatitude=0;
    double rawLongitude=0;
    private GoogleMap mMap;
    Circle circle;
    Circle circleMe;
    CarInfo car = new CarInfo();
    static BitmapDrawable bikeImage;
    static BitmapDrawable kickImage;
    static BitmapDrawable motoImage;
    Bitmap bikemarker;
    Bitmap kickmarker;
    Bitmap motomarker;
    ArrayList<MobilityInfo> mobilityInfos = new ArrayList<MobilityInfo>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navy);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Navi.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        } else {
            Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location !=null){
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            rawLatitude=latitude;
            rawLongitude= longitude;
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    200,
                    0,
                    gpsLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    200,
                    0,
                    gpsLocationListener);
        }}
        bikeImage=(BitmapDrawable)getResources().getDrawable(R.drawable.bike);
        kickImage=(BitmapDrawable)getResources().getDrawable(R.drawable.kick);
        motoImage=(BitmapDrawable)getResources().getDrawable(R.drawable.moto);
        Bitmap bike=bikeImage.getBitmap();
        Bitmap kick=kickImage.getBitmap();
        Bitmap moto=motoImage.getBitmap();
        bikemarker = Bitmap.createScaledBitmap(bike, 80, 80, false);
        kickmarker = Bitmap.createScaledBitmap(kick, 100, 100, false);
        motomarker = Bitmap.createScaledBitmap(moto, 80, 80, false);

    }

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            Log.d("통신", "얻어온 GPS 위도 : " + latitude + "," + "경도 : " + longitude);
            double dist = distance(rawLatitude ,rawLongitude, latitude, longitude);
            Log.d("거리", String.valueOf(dist));
            if(dist<30) {
                rawLatitude = latitude;
                rawLongitude= longitude;
                LatLng loc = new LatLng(latitude, longitude);
                Log.d("거리", "출력");
                circleMe.setCenter(loc);
                //marker.setPosition(loc);
                circle.setCenter(loc);
                car.id = "RUSH_CAR";
                car.type = "car";
                car.latitude = Double.toString(rawLatitude);
                car.longitude = Double.toString(rawLongitude);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        POST(Main.url, car, mobilityInfos);
                        //Log.d("통신", String.valueOf(mobilityInfos.size()));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < mobilityMarker.size(); i++) {
                                            mobilityMarker.get(i).remove();
                                        }
                                        mobilityMarker.clear();
                                        for (int i = 0; i < mobilityInfos.size(); i++) {
                                            double latitude = Double.parseDouble(mobilityInfos.get(i).latitude);
                                            double longitude = Double.parseDouble(mobilityInfos.get(i).longitude);
                                            LatLng loc = new LatLng(latitude, longitude);
                                            if (mobilityInfos.get(i).type.equals("BIKE")) {
                                                bikeOptions = new MarkerOptions()
                                                        .position(loc)
                                                        .title("현 위치")
                                                        .icon(BitmapDescriptorFactory.fromBitmap(bikemarker));
                                                mobilityMarker.add(mMap.addMarker(bikeOptions));
                                            } else if (mobilityInfos.get(i).type.equals("KICK")) {
                                                kickOptions = new MarkerOptions()
                                                        .position(loc)
                                                        .title("현 위치")
                                                        .icon(BitmapDescriptorFactory.fromBitmap(kickmarker));
                                                mobilityMarker.add(mMap.addMarker(kickOptions));
                                            } else if (mobilityInfos.get(i).type.equals("MOTO")) {
                                                motoOptions = new MarkerOptions()
                                                        .position(loc)
                                                        .title("현 위치")
                                                        .icon(BitmapDescriptorFactory.fromBitmap(motomarker));
                                                mobilityMarker.add(mMap.addMarker(motoOptions));
                                            }
                                        }
                                    }
                                });
                            }
                        }).start();
                    }
                }).start();
                ;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,18));
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng location = new LatLng(latitude, longitude);

        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.me);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 50, 50, false);

        makerOptions = new MarkerOptions()
                .position(location)
                .title("현 위치")
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        CircleOptions myLocationCircle = new CircleOptions().center(location) //원점
                .radius(100)      //반지름 단위 : m
                .strokeWidth(0f)  //선너비 0f : 선없음
                .fillColor(Color.parseColor("#2003A9F4")); //배경색

        CircleOptions myLocationCircleme = new CircleOptions().center(location) //원점
                .radius(4)      //반지름 단위 : m
                .strokeWidth(0f)  //선너비 0f : 선없음
                .fillColor(Color.parseColor("#FFFF2819")); //배경색
        circle = mMap.addCircle(myLocationCircle);
        circleMe = mMap.addCircle(myLocationCircleme);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,18));
        //marker = mMap.addMarker(makerOptions);

    }

    public static String POST(String url, CarInfo carInfo, ArrayList<MobilityInfo> mobilityInfos) throws  IllegalStateException{
        String inputLine = null;
        StringBuffer outResult  = new StringBuffer();

        String result = "";
        try {
            URL urlCon = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) urlCon.openConnection();

            String json = "";
            // build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", carInfo.getId());
            jsonObject.put("type", carInfo.getType());
            jsonObject.put("latitude", carInfo.getLatitude());
            jsonObject.put("longitude", carInfo.getLongitude());
            // convert JSONObject to JSON to String
            json = jsonObject.toString();
            // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            httpCon.setDoOutput(true);
            // InputStream으로 서버로 부터 응답을 받겠다는 옵션.
            httpCon.setDoInput(true);
            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);
            // Set some headers to inform server about the type of the content
            httpCon.setRequestMethod("POST");
            httpCon.setRequestProperty("Content-type", "application/json");
            httpCon.setConnectTimeout(10000);
            httpCon.setReadTimeout(10000);

            OutputStream os = httpCon.getOutputStream();
            os.write(json.getBytes("euc-kr"));
            os.flush();
            // receive response as inputStream

            BufferedReader in = new BufferedReader(new InputStreamReader(httpCon.getInputStream(), "UTF-8"));
            while ((inputLine = in.readLine()) != null) {
                outResult.append(inputLine);
            }

            mobilityInfos.clear();
            String response = outResult.toString();
            Log.d("통신 응답  ", response);

            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0 ; i<jsonArray.length(); i++){
                JSONObject jsonObject_res = jsonArray.getJSONObject(i);
                String id = jsonObject_res.getString("id");
                String type = jsonObject_res.getString("type");
                String latitude = jsonObject_res.getString("latitude");
                String longitude = jsonObject_res.getString("longitude");
                mobilityInfos.add(new MobilityInfo(id, type, latitude, longitude));
            }

            httpCon.disconnect();
            Log.d("통신", "종료");
        }
        catch(Exception e){
            e.printStackTrace();
            Log.d("통신 에러", e.getLocalizedMessage());

        }
        return outResult.toString();
    }
    double distance(double rawLatitude, double rawLongitude, double latitude, double longitude){
        Location a = new Location("a");
        Location b = new Location("b");
        a.setLatitude(rawLatitude);
        a.setLongitude(rawLongitude);
        b.setLatitude(latitude);
        b.setLongitude(longitude);
        return a.distanceTo(b);
    }
}

