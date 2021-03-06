package com.icdsp.tmap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapData.ConvertGPSToAddressListenerCallback;
import com.skt.Tmap.TMapData.FindPathDataListenerCallback;
import com.skt.Tmap.TMapData.TMapPathType;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapLabelInfo;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;


public class MainActivity extends BaseActivity implements TMapGpsManager.onLocationChangedCallback {

    TMapPoint Current_Point;
    double getCurrent_long;
    double getCurrent_lat;
    @Override
    public void onLocationChange(Location location) {
        LogManager.printLog("onLocationChange :::> " + location.getLatitude() +  " " +
                location.getLongitude() + " " + location.getSpeed() + " " + location.getAccuracy());
        if(m_bTrackingMode)
        {
            mMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
            mMapView.setCenterPoint(location.getLongitude(), location.getLatitude());

            getCurrent_long = location.getLongitude();
            getCurrent_lat = location.getLatitude();

            Current_Point = new TMapPoint(getCurrent_lat, getCurrent_long);

            LogManager.printLog("getCurrent_lat : " + getCurrent_lat);
            LogManager.printLog("getCurrent_long : " + getCurrent_long);
        }
    }

    /**
     *  Field declaration
     */
    private TMapView	mMapView = null;
    private Context 	mContext;
    private TMapMarkerItem CurrentMarker;

    private ArrayList<Bitmap> mOverlayList;
    //private ImageOverlay mOverlay;

    TMapGpsManager gps = null;

    TMapPoint Start_Point = null;
    TMapPoint Destination_Point = null;
    TMapPoint Place_Point = null;

    private String Address;






    public static String mApiKey = "38c7269d-5eb5-4739-b305-9886986b658f"; // ???????????? appKey

    private static final int[] mArrayMapButton = {
            R.id.btnClickDestination,
            R.id.btnSearchDestination,
            R.id.btnStartGuidance,
            R.id.btnSetCompassMode,
            R.id.btnSearchPlace
    };

    private 	int 		m_nCurrentZoomLevel = 0;
    private 	double 		m_Latitude  = 0;
    private     double  	m_Longitude = 0;
    private 	boolean 	m_bShowMapIcon = false;
    private 	boolean 	m_bTrafficeMode = false;
    private 	boolean 	m_bSightVisible = false;
    private 	boolean 	m_bTrackingMode = false;
    private 	boolean 	m_bOverlayMode = false;

    ArrayList<String> mArrayID;

    ArrayList<String>		mArrayLineID;
    private static 	int 	mLineID;

    ArrayList<String>       mArrayMarkerID;
    private static int 		mMarkerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mContext = this;

        // TmapView setting
        mMapView = new TMapView(this);
        configureMapView(); // API Key ??????
        vieweSetting();     // ????????? ?????? ??????
        initView();         // ????????? ?????????
        addView(mMapView);  // ?????? ??? ??????

        // ID arraylist
        mArrayID = new ArrayList<String>();
        mArrayLineID = new ArrayList<String>();
        mLineID = 0;
        mArrayMarkerID	= new ArrayList<String>();
        mMarkerID = 0;

        // Gps Open
        gps = new TMapGpsManager(MainActivity.this);
        gps.setMinTime(1000);
        gps.setMinDistance(2);
        gps.setProvider(gps.NETWORK_PROVIDER);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1); //???????????? ?????? ?????? ?????? ??????
            }
            return;
        }
        gps.OpenGps();

        // Insert A SKT Logo on the Tmap.
        mMapView.setTMapLogoPosition(TMapView.TMapLogoPositon.POSITION_BOTTOMRIGHT);
    }


    /**
     * setSKPMapApiKey()??? ApiKey??? ?????? ??????.
     */
    private void configureMapView() {
        mMapView.setSKPMapApiKey(mApiKey);
    }


    // set ths Map's properties
    private void vieweSetting() {
        setMapIcon();       // ?????? ??? ?????? ????????? ????????? ??????
        mMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        mMapView.setZoomLevel(15);
        mMapView.setMapType(TMapView.MAPTYPE_STANDARD);
    }

    /**
     * setMapIcon
     * ??????????????? ????????? ???????????? ????????????.
     */
    public void setMapIcon() {
        CurrentMarker = new TMapMarkerItem();

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.poi_here);
        mMapView.setIcon(bitmap);
        mMapView.addMarkerItem("CurrentMarker", CurrentMarker);

        mMapView.setIconVisibility(true);
    }

    /**
     * initView - ????????? ?????? ???????????? ????????????.
     */
    private void initView() {
        for (int btnMapView : mArrayMapButton) {
            Button ViewButton = (Button)findViewById(btnMapView);
            ViewButton.setOnClickListener(this);
        }

        mMapView.setOnApiKeyListener(new TMapView.OnApiKeyListenerCallback() {
            @Override
            public void SKPMapApikeySucceed() {
                LogManager.printLog("MainActivity SKPMapApikeySucceed");
            }

            @Override
            public void SKPMapApikeyFailed(String errorMsg) {
                LogManager.printLog("MainActivity SKPMapApikeyFailed " + errorMsg);
            }
        });


        mMapView.setOnEnableScrollWithZoomLevelListener(new TMapView.OnEnableScrollWithZoomLevelCallback() {
            @Override
            public void onEnableScrollWithZoomLevelEvent(float zoom, TMapPoint centerPoint) {
                LogManager.printLog("MainActivity onEnableScrollWithZoomLevelEvent " + zoom + " " + centerPoint.getLatitude() + " " + centerPoint.getLongitude());
            }
        });

        mMapView.setOnDisableScrollWithZoomLevelListener(new TMapView.OnDisableScrollWithZoomLevelCallback() {
            @Override
            public void onDisableScrollWithZoomLevelEvent(float zoom, TMapPoint centerPoint) {
                LogManager.printLog("MainActivity onDisableScrollWithZoomLevelEvent " + zoom + " " + centerPoint.getLatitude() + " " + centerPoint.getLongitude());
            }
        });

        mMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> markerlist,ArrayList<TMapPOIItem> poilist, TMapPoint point, PointF pointf) {
                LogManager.printLog("MainActivity onPressUpEvent " + markerlist.size());
                return false;
            }

            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> markerlist,ArrayList<TMapPOIItem> poilist, TMapPoint point, PointF pointf) {
                LogManager.printLog("MainActivity onPressEvent " + markerlist.size());

                for (int i = 0; i < markerlist.size(); i++) {
                    TMapMarkerItem item = markerlist.get(i);
                    LogManager.printLog("MainActivity onPressEvent " + item.getName() + " " + item.getTMapPoint().getLatitude() + " " + item.getTMapPoint().getLongitude());
                }
                return false;
            }
        });

        mMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {
            @Override
            public void onLongPressEvent(ArrayList<TMapMarkerItem> markerlist, ArrayList<TMapPOIItem> poilist, TMapPoint point) {
                LogManager.printLog("MainActivity onLongPressEvent " + markerlist.size());
            }
        });

        mMapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem markerItem) {
                String strMessage = "";
                strMessage = "ID: " + markerItem.getID() + " " + "Title " + markerItem.getCalloutTitle();
                Common.showAlertDialog(MainActivity.this, "Callout Right Button", strMessage);
            }
        });

        mMapView.setOnClickReverseLabelListener(new TMapView.OnClickReverseLabelListenerCallback() {
            @Override
            public void onClickReverseLabelEvent(TMapLabelInfo findReverseLabel) {
                if(findReverseLabel != null) {
                    LogManager.printLog("MainActivity setOnClickReverseLabelListener " + findReverseLabel.id + " / " + findReverseLabel.labelLat
                            + " / " + findReverseLabel.labelLon + " / " + findReverseLabel.labelName);

                }
            }
        });

        m_nCurrentZoomLevel = -1;
        m_bShowMapIcon = true;
        m_bTrafficeMode = false;
        m_bSightVisible = true;
        m_bTrackingMode = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gps.CloseGps();
        mMapView.removeTMapPath();
        if(mOverlayList != null){
            mOverlayList.clear();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClickDestination   :   ClickDestination();  break;
            case R.id.btnSearchDestination  :   SearchDestination(); break;
            case R.id.btnStartGuidance      :   StartGuidance();     break;
            case R.id.btnSetCompassMode     : 	setCompassMode();    break;
            case R.id.btnSearchPlace        :   getRoundPoint();     break;
        }
    }

    /////////////////////////////////Each BUtton's Method//////////////////////////////////////////////////////////////
    /**
     *  ??????????????? ?????? ?????????
     */
    public void ClickDestination() {
        Toast.makeText(MainActivity.this, "???????????? ?????? ????????? ????????? ??? ????????? ??????????????? ???????????????.", Toast.LENGTH_SHORT).show();

        mMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList,
                                        ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {

                TMapData tMapData = new TMapData();
                tMapData.convertGpsToAddress(tMapPoint.getLatitude(), tMapPoint.getLongitude(),
                        new ConvertGPSToAddressListenerCallback() {
                            @Override
                            public void onConvertToGPSToAddress(String strAddress) {
                                Address = strAddress;
                                LogManager.printLog("????????? ????????? ????????? " + strAddress);
                            }
                        });

                Toast.makeText(MainActivity.this, "???????????? ????????? ????????? " + Address + " ?????????.", Toast.LENGTH_SHORT).show();

                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList,
                                          ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                Destination_Point = tMapPoint;

                return false;
            }
        });

    }

    /** SearchDestination
     *  ??????????????? ?????? ?????????
     */
    public void SearchDestination() {
        // ???????????? ????????????
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("POI ?????? ??????");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strData = input.getText().toString();
                TMapData tMapData = new TMapData();

                tMapData.findAllPOI(strData, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                        for(int i=0; i<poiItem.size(); i++){
                            TMapPOIItem item = poiItem.get(i);

                            LogManager.printLog("POI Name: " + item.getPOIName().toString() + ", " +
                                    "Address: " + item.getPOIAddress().replace("null", "")  + ", " +
                                    "Point: " + item.getPOIPoint().toString());

                            Address = item.getPOIAddress();
                            Destination_Point = item.getPOIPoint();
                        }
                    }
                });
            }
        });

        Toast.makeText(this, "???????????? ????????? " + Address + " ?????????.", Toast.LENGTH_SHORT).show();
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    /** StartGuidance()
     *  ?????? ????????? ?????????
     */
    public void StartGuidance() {
        mMapView.removeAllMarkerItem();
        mMapView.removeTMapPath();

        setTrackingMode();

        TMapPoint point1 = mMapView.getLocationPoint();
        TMapPoint point2 = Destination_Point;

        TMapData tmapdata = new TMapData();

        tmapdata.findPathDataWithType(TMapPathType.CAR_PATH, point1, point2, new FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                polyLine.setLineColor(Color.BLUE);
                mMapView.addTMapPath(polyLine);
            }
        });

        Bitmap start = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.poi_start);
        Bitmap end = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.poi_end);
        mMapView.setTMapPathIcon(start, end);

        mMapView.zoomToTMapPoint(point1, point2);
    }

    /**
     * setCompassMode
     * ????????? ????????? ?????? ???????????? ?????????????????? ????????????.
     */
    public void setCompassMode() {mMapView.setCompassMode(!mMapView.getIsCompass());}
    ///////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * getIsCompass
     * ?????????????????? ??????????????? ????????????.
     */
    public void getIsCompass() {
        Boolean bGetIsCompass = mMapView.getIsCompass();
        Common.showAlertDialog(this, "", "?????? ????????? ????????? : " + bGetIsCompass.toString() );
    }

    /**
     * getLocationPoint
     * ??????????????? ????????? ????????? ??????, ????????? ????????????.
     */
    public void getLocationPoint() {
        TMapPoint point = mMapView.getLocationPoint();

        double Latitude = point.getLatitude();
        double Longitude = point.getLongitude();

        m_Latitude  = Latitude;
        m_Longitude = Longitude;

        LogManager.printLog("Latitude " + Latitude + " Longitude " + Longitude);

        String strResult = String.format("Latitude = %f Longitude = %f", Latitude, Longitude);

        Common.showAlertDialog(this, "", strResult);
    }

    /**
     * setTrackingMode
     * ??????????????? ????????? ??????????????? ?????????????????? ?????????????????? ????????????.
     */
    public void setTrackingMode() {
        mMapView.setTrackingMode(mMapView.getIsTracking());
    }

    /**
     * getIsTracking
     * ?????????????????? ??????????????? ????????????.
     */
    public void getIsTracking() {
        Boolean bIsTracking = mMapView.getIsTracking();
        Common.showAlertDialog(this, "", "?????? ??????????????? ?????? ??????  : " + bIsTracking.toString() );
    }
// place
    public void getRoundPoint(){
        // ????????? ??????
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("?????? ?????? ??????");
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_pin_red);


        final EditText input = new EditText(this);
        builder.setView(input);
        //?????? ?????? ??????
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strData = input.getText().toString();
                TMapData tMapData = new TMapData();

                tMapData.findAllPOI(strData, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                        for(int i=0; i<poiItem.size(); i++){
                            TMapMarkerItem mapMarkerItem = new TMapMarkerItem();

                            TMapPOIItem item = poiItem.get(i);
                            String locate = item.getPOIPoint().toString();
                            String lat_s = locate.substring(4,14);
                            String lon_s = locate.substring(20, 30);
                            Double lat = Double.parseDouble(lat_s);
                            Double lon = Double.parseDouble(lon_s);
                            TMapPoint tMapPoint = new TMapPoint(lat, lon);
                            // ????????? ?????? ?????? ??????
                            mapMarkerItem.setIcon(bitmap);
                            mapMarkerItem.setPosition(0.5f, 0.5f);
                            mapMarkerItem.setTMapPoint(tMapPoint);
                            mMapView.addMarkerItem("marker" + i, mapMarkerItem);
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }




}
