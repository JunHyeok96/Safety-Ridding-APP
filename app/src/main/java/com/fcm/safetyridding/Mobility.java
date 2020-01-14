package com.fcm.safetyridding;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.util.Iterator;
import java.util.Set;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
public class Mobility extends AppCompatActivity implements OnMapReadyCallback {
     BluetoothSPP bt;
     TextView receiveLatitude;
     TextView receiveLongitude;
    TextView receiveSpeed;
     double rawLatitude=0;
     double rawLongitude=0;
     String id, type;
    private GoogleMap mMap;
    Button mBtnDisConnect;
    MobilityInfo mobility = new MobilityInfo();
    String total = "";
    Marker marker;
    Circle circle;
    Circle circleMe;
    MarkerOptions makerOptions = new MarkerOptions();
    ArrayList<CarInfo> carInfos = new ArrayList<CarInfo>();
    ArrayList<Marker> carMarker = new ArrayList<Marker>();
    ArrayList<Circle> circles = new ArrayList<Circle>();

    MarkerOptions carOptions = new MarkerOptions();
    Bitmap carmarker;
    static BitmapDrawable carImage;
    double speed =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobility);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        id = getIntent().getStringExtra("id");
        type = getIntent().getStringExtra("type");
        Log.d("인텐트 수신", id + ", " +type);

        bt = new BluetoothSPP(this); //Initializing
        receiveLatitude = (TextView)findViewById(R.id.receiveLatitude);
        receiveLongitude = (TextView)findViewById(R.id.receiveLongitude) ;
        mBtnDisConnect = (Button)findViewById(R.id.btnDisConnect);
        receiveSpeed = (TextView)findViewById(R.id.speed);
        TextView idText = (TextView)findViewById(R.id.textView5);
        TextView typeText = (TextView)findViewById(R.id.type);
        idText.setText(id);
        typeText.setText(type);
        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }
       /*  아두이노 데이터 수신
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {
                total +=message;
                if(total.length()>21) {
                    //receiveLatitude.setText(total);
                    //Log.d("통신",  "받은 데이터 : "+ total);
                    //String array[] = total.split(",");
                    //mobility.latitude  = array[0];
                    //mobility.longitude =array[1];
                    Log.d("거리", mobility.id+","+mobility.type);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            POST(Main.url, mobility);
                        }

                    }).start();

                    total = "";
                }
            }
        });*/

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
                bt.send("연결됨", true);
                String array[] = name.split("_");
                mobility.id = array[0];
                mobility.type = array[1];

//                mobility.type = array[1];
                if (Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Mobility.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            0);
                } else {

                    Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(location !=null){
                        rawLatitude = location.getLatitude();
                        rawLongitude = location.getLongitude();
                        mobility.longitude = String.valueOf(rawLongitude);
                        mobility.latitude = String.valueOf(rawLatitude);

                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                200,
                                0,
                                gpsLocationListener);
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                200,
                                0,
                                gpsLocationListener);
                        LatLng loc = new LatLng(rawLatitude, rawLongitude);
                       mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 19));
                       //marker.setPosition(loc);
                        circleMe.setCenter(loc);
                       circle.setCenter(loc);
                    }}
            }
            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
                lm.removeUpdates(gpsLocationListener);

                finish();
            }
            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        mBtnDisConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt.send("연결종료",true);
                bt.disconnect();
                POST(Main.url, mobility, carInfos);
                finish();
            }
        });
        /*
        carImage=(BitmapDrawable)getResources().getDrawable(R.drawable.moto);
        Bitmap car=carImage.getBitmap();
        carmarker = Bitmap.createScaledBitmap(car, 80, 80, false);
        */
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double dist = distance(rawLatitude ,rawLongitude, latitude, longitude);
            speed =  location.getSpeed() * 3.6;
            Log.d("속도", String.valueOf(speed));
            receiveSpeed.setText(String.valueOf(speed));
            LatLng loc = new LatLng(rawLatitude, rawLongitude);
            //marker.setPosition(loc);
            circleMe.setCenter(loc);
            circle.setCenter(loc);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 19));
            Log.d("거리", String.valueOf(dist));
            if(dist<30) {
                Log.d("거리", "출력");
                rawLatitude = latitude;
                rawLongitude = longitude;
                mobility.latitude = String.valueOf(latitude);
                mobility.longitude = String.valueOf(longitude);
                receiveLatitude.setText(mobility.latitude);
                receiveLongitude.setText(mobility.longitude);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        POST(Main.url, mobility, carInfos);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < circles.size(); i++) {
                                            circles.get(i).remove();
                                        }
                                        circles.clear();
                                        for (int i = 0; i < carInfos.size(); i++) {
                                            double latitude = Double.parseDouble(carInfos.get(i).latitude);
                                            double longitude = Double.parseDouble(carInfos.get(i).longitude);
                                            LatLng loc = new LatLng(latitude, longitude);
                                                /*carOptions = new MarkerOptions()
                                                        .position(loc)
                                                        .title("현 위치")
                                                        .anchor(0.5f, 1.0f)
                                                        .icon(BitmapDescriptorFactory.fromBitmap(carmarker));
                                                carMarker.add(mMap.addMarker(carOptions));*/
                                            CircleOptions myLocationCircle = new CircleOptions().center(loc) //원점
                                                    .radius(5)      //반지름 단위 : m
                                                    .strokeWidth(0f)  //선너비 0f : 선없음
                                                    .fillColor(Color.parseColor("#28FF2819")); //배경색
                                            circles.add(mMap.addCircle(myLocationCircle));
                                        }
                                    }
                                });
                            }
                        }).start();
                    }

                }).start();

            }
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
            }
        }
        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            bt.disconnect();
        } else {
            BluetoothAdapter ap2 = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> devices = ap2.getBondedDevices();
            if (devices.size() > 0) {

                Iterator<BluetoothDevice> iter = devices.iterator();
                int i =0;
                while (iter.hasNext()) {
                    BluetoothDevice d = iter.next();
                    Log.d("이름 조회",  d.getName() + "-" +id);
                    if(d.getName().equals(id)){
                        String address = d.getAddress();
                        bt.connect(address);
                        Log.d("이름 조회",  "일치!");
                        break;
                    }

                }

            }
        }



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


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public static String POST(String url, MobilityInfo mobilityInfo, ArrayList<CarInfo> carInfo) throws  IllegalStateException{
        String inputLine = null;
        StringBuffer outResult  = new StringBuffer();

        String result = "";
        try {
            URL urlCon = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) urlCon.openConnection();

            String json = "";
            // build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", mobilityInfo.getId());
            jsonObject.put("type", mobilityInfo.getType());
            jsonObject.put("latitude", mobilityInfo.getLatitude());
            jsonObject.put("longitude", mobilityInfo.getLongitude());
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
            carInfo.clear();
            String response = outResult.toString();
            // Log.d("통신 응답  ", response);

            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0 ; i<jsonArray.length(); i++){
                JSONObject jsonObject_res = jsonArray.getJSONObject(i);
                String id = jsonObject_res.getString("id");
                String type = jsonObject_res.getString("type");
                String latitude = jsonObject_res.getString("latitude");
                String longitude = jsonObject_res.getString("longitude");
                carInfo.add(new CarInfo(id, type, latitude, longitude));
                Log.d("거리",id+ " " + type);
            }

            httpCon.disconnect();
            //    Log.d("통신", "종료");
        }
        catch(Exception e){
            e.printStackTrace();
            Log.d("통신 에러", e.getLocalizedMessage());

        }

        return outResult.toString();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng location = new LatLng(rawLatitude, rawLongitude);

        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.me);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 50, 50, false);

        makerOptions = new MarkerOptions()
                .position(location)
                .title("현 위치")
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

        CircleOptions myLocationCircleMe = new CircleOptions().center(location) //원점
                .radius(2)      //반지름 단위 : m
                .strokeWidth(0f)  //선너비 0f : 선없음
                .fillColor(Color.parseColor("#FFFF2819")); //배경색

        CircleOptions myLocationCircle = new CircleOptions().center(location) //원점
                .radius(50)      //반지름 단위 : m
                .strokeWidth(0f)  //선너비 0f : 선없음
                .fillColor(Color.parseColor("#2003A9F4")); //배경색
        circle = mMap.addCircle(myLocationCircle);
        circleMe = mMap.addCircle(myLocationCircleMe);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,19));
        //marker = mMap.addMarker(makerOptions);

    }
}