package com.android.wastemainframe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.wastemainframe.utils.CameraUtility;
import com.chakra.clocationfinder.CLocationHelper;
import com.chakra.clocationfinder.LocationListenerCallBack;
import com.chakra.volleyjar.CVolleyHelper;
import com.chakra.volleyjar.IResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.isapanah.awesomespinner.AwesomeSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainPageActivity extends FragmentActivity implements OnMapReadyCallback, IResult, LocationListenerCallBack {

    private CVolleyHelper mVolleyService;
    private ProgressDialog pDialog;
    private CardView btnSelect;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask;
    int COMPRESSION_QUALITY = 50;
    int IMAGEHEIGHT = 640;
    int IMAGEWIDTH = 480;
    Bitmap.CompressFormat IMAGEFORMAT = Bitmap.CompressFormat.JPEG;
    private CLocationHelper CLocationWrapper;
    public static final String TAG_STR = "LOC_BROADCASTER";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private CardView locationBtn;
    private TextView mLat;
    private TextView mLng;
    private TextView acc;
    private TextView pro;
    private TextView altitude;
    private TextView speed;
    private TextView bearing;
    private TextView time;
    private Button mapView;
    private String Address1;
    private String Address2;
    private String City;
    private String State;
    private String Country;
    private String County;
    private String PIN;
    private String Area;
    private GoogleMap mMap;
    private CardView imageViewCardAdaptor;
    private ImageView imageViewAttach;
    private TextView addressTv;
    private String route;
    private AwesomeSpinner my_spinner;
    private String encodedImage;
    private CardView submitImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_form);

        pDialog = new ProgressDialog(this);
        btnSelect = findViewById(R.id.btnSelectPhoto);
        locationBtn = (CardView) findViewById(R.id.locbtn);
        mLat = (TextView) findViewById(R.id.lat);
        mLng = (TextView) findViewById(R.id.lng);
        acc = (TextView) findViewById(R.id.acc);
        pro = (TextView) findViewById(R.id.pro);
        altitude = (TextView) findViewById(R.id.altitude);
        speed = (TextView) findViewById(R.id.speed);
        bearing = (TextView) findViewById(R.id.bearing);
        time = (TextView) findViewById(R.id.time);
        addressTv = (TextView) findViewById(R.id.addressTv);
        mapView = (Button) findViewById(R.id.mapView);
        mapView = (Button) findViewById(R.id.mapView);
        submitImage = (CardView) findViewById(R.id.submit_image);
        imageViewAttach = (ImageView) findViewById(R.id.imageViewAttach);
        imageViewCardAdaptor = (CardView) findViewById(R.id.imageViewCardAdaptor);

        initSprinner();

        mVolleyService = new CVolleyHelper(this, this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        submitImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageProcessRequest(encodedImage);
            }
        });
        mapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainPageActivity.this, MapsActivity.class));
            }
        });

        //Create instance for CLocationHelper
        CLocationWrapper = CLocationHelper.getInstance(this);

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check permission is available or not
                //Request Location
                requestLocation();

                //UI Update Empty all textView and Change button text and color
                mLat.setText("");
                mLng.setText("");
                acc.setText("");
                pro.setText("");
                altitude.setText("");
                speed.setText("");
                bearing.setText("");
                time.setText("");
            }
        });
        //Check permission is available or not
        //Request Location
        requestLocation();

    }

    private void initSprinner() {

        AwesomeSpinner wasteTypeSpinner = findViewById(R.id.wasteTypeSpinner);
        AwesomeSpinner wasteCharSpinner = findViewById(R.id.wasteCharSpinner);
        AwesomeSpinner locTypeSpinner = findViewById(R.id.Loc_typeSpinner);
        AwesomeSpinner wasteShapeSpinner = findViewById(R.id.wasteShapeSpinner);
        AwesomeSpinner sourceTypeSpinner = findViewById(R.id.sourceTypeSpinner);

//wasteTypeSpinner
        List<String> wasteTypeSpinnercategories = new ArrayList<String>();
        wasteTypeSpinnercategories.add("Solid plastic");
        wasteTypeSpinnercategories.add("Plastic cover");
        wasteTypeSpinnercategories.add("Plastic Bottle");
        wasteTypeSpinnercategories.add("Others");

        ArrayAdapter<String> categoriesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, wasteTypeSpinnercategories);
        wasteTypeSpinner.setAdapter(categoriesAdapter);
        wasteTypeSpinner.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
            @Override
            public void onItemSelected(int position, String itemAtPosition) {
                Toast.makeText(MainPageActivity.this, "**********position=" + position + "**********itemAtPosition=" + itemAtPosition, Toast.LENGTH_SHORT).show();
            }
        });

//wasteCharSpinner
        List<String> wasteCharSpinnercategories = new ArrayList<String>();
        wasteCharSpinnercategories.add("Polyethylene Terephthalate (PETE or PET)");
        wasteCharSpinnercategories.add("High-Density Polyethylene (HDPE)");
        wasteCharSpinnercategories.add("Low-Density Polyethylene (LDPE)");

        ArrayAdapter<String> wasteCharSpinnercategoriesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, wasteCharSpinnercategories);
        wasteCharSpinner.setAdapter(wasteCharSpinnercategoriesAdapter);
        wasteCharSpinner.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
            @Override
            public void onItemSelected(int position, String itemAtPosition) {
                Toast.makeText(MainPageActivity.this, "**********position=" + position + "**********itemAtPosition=" + itemAtPosition, Toast.LENGTH_SHORT).show();
            }
        });

//locTypeSpinner
        List<String> locTypeSpinnerCategories = new ArrayList<String>();
        locTypeSpinnerCategories.add("Land");
        locTypeSpinnerCategories.add("Water");
        locTypeSpinnerCategories.add("Space");

        ArrayAdapter<String> locTypeSpinnerCategoriesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, locTypeSpinnerCategories);
        locTypeSpinner.setAdapter(locTypeSpinnerCategoriesAdapter);
        locTypeSpinner.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
            @Override
            public void onItemSelected(int position, String itemAtPosition) {
                Toast.makeText(MainPageActivity.this, "**********position=" + position + "**********itemAtPosition=" + itemAtPosition, Toast.LENGTH_SHORT).show();
            }
        });

//wasteShapeSpinner
        List<String> wasteShapeSpinnerCategories = new ArrayList<String>();
        wasteShapeSpinnerCategories.add("Bottle");
        wasteShapeSpinnerCategories.add("Cover");
        wasteShapeSpinnerCategories.add("Cup");
        wasteShapeSpinnerCategories.add("Bucket");
        wasteShapeSpinnerCategories.add("Sheet");
        wasteShapeSpinnerCategories.add("Rod");
        wasteShapeSpinnerCategories.add("Tube");
        wasteShapeSpinnerCategories.add("Film");

        ArrayAdapter<String> wasteShapeSpinnerCategoriesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, wasteShapeSpinnerCategories);
        wasteShapeSpinner.setAdapter(wasteShapeSpinnerCategoriesAdapter);
        wasteShapeSpinner.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
            @Override
            public void onItemSelected(int position, String itemAtPosition) {
                Toast.makeText(MainPageActivity.this, "**********position=" + position + "**********itemAtPosition=" + itemAtPosition, Toast.LENGTH_SHORT).show();
            }
        });

//sourceTypeSpinner
        List<String> sourceTypeSpinnerCategories = new ArrayList<String>();
        sourceTypeSpinnerCategories.add("Image by Camera");
        sourceTypeSpinnerCategories.add("Video by Drone");
        sourceTypeSpinnerCategories.add("Video by CCTV");

        ArrayAdapter<String> sourceTypeSpinnerCategoriesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sourceTypeSpinnerCategories);
        sourceTypeSpinner.setAdapter(sourceTypeSpinnerCategoriesAdapter);
        sourceTypeSpinner.setOnSpinnerItemClickListener(new AwesomeSpinner.onSpinnerItemClickListener<String>() {
            @Override
            public void onItemSelected(int position, String itemAtPosition) {
                Toast.makeText(MainPageActivity.this, "**********position=" + position + "**********itemAtPosition=" + itemAtPosition, Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = CameraUtility.checkPermission(MainPageActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");


        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        assert thumbnail != null;
        Bitmap device = Bitmap.createScaledBitmap(thumbnail, IMAGEWIDTH, IMAGEHEIGHT, true);
        device.compress(IMAGEFORMAT, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        imageViewCardAdaptor.setVisibility(View.VISIBLE);
        imageViewAttach.setImageBitmap(device);
        byte[] image = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(image, Base64.DEFAULT);

//        Bitmap photoView = (Bitmap) data.getExtras().get("data");
//        photoView.compress(IMAGEFORMAT, COMPRESSION_QUALITY, byteArrayBitmapStream);
//        previewCapturedImage(photoView);
//
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(image);
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // ticketMgsType 1->Text mgs, 2->Image mgs
        //    ivImage.setImageBitmap(thumbnail);
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
                assert bm != null;
                Bitmap device = Bitmap.createScaledBitmap(bm, IMAGEWIDTH, IMAGEHEIGHT, true);
                device.compress(IMAGEFORMAT, COMPRESSION_QUALITY,
                        byteArrayBitmapStream);
                byte[] image = byteArrayBitmapStream.toByteArray();
                encodedImage = Base64.encodeToString(image, Base64.DEFAULT);
                // ticketMgsType 1->Text mgs, 2->Image mgs
                imageViewCardAdaptor.setVisibility(View.VISIBLE);
                imageViewAttach.setImageBitmap(device);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void imageProcessRequest(String encodedImage) {
        pDialog.setMessage(this.getString(R.string.pleasewait));
        pDialog.setCancelable(false);
        pDialog.show();

        JSONObject js_validation = new JSONObject();
        try {
            js_validation.put("source_type", 2);
            js_validation.put("waste_type", 1);
            js_validation.put("loc_type", 2);
            js_validation.put("img_raw", encodedImage);
            js_validation.put("img_processed", "L3yetLycKOvehsluDinJ8u40AOX26Dim7fvU");
            js_validation.put("time_date", "2019-05-08 15,19,23");
            js_validation.put("waste_char", 2);
            js_validation.put("waste_shape", 5);
            js_validation.put("waste_status", 3);
            js_validation.put("waste_prod_name", "starbucks");
            js_validation.put("waste_prod_address", "92/5, Ground Floor, MGN Splendor, Off Chamiers Road, Alwarpet,Ch-600067");
            js_validation.put("other", "null");
            js_validation.put("latitude", 12.983732);
            js_validation.put("longitude", 79.968442);
            js_validation.put("country", "India");
            js_validation.put("state", "Tamil Nadu");
            js_validation.put("district", "Kanchipuram");
            js_validation.put("region", "Kanchipuram");
            js_validation.put("city", "Kanchipuram");
            js_validation.put("street", "Sivanthangal");
            js_validation.put("pincode", 602105);
            System.out.println("**getSoReportRequest , " + js_validation);
        } catch (JSONException js) {
            js.printStackTrace();
        }
        mVolleyService.sendRequest(this, Request.Method.POST, this.getString(R.string.ip_port) + ConstURL.URL_IMAGE_PROCESS_FUN, js_validation, "getSOReportRequest", "application/json");

    }

    private void getImageProcessResponse(JSONObject resp) {
        try {
            System.out.println("** getImageProcessResponse= " + resp);
            String statuscode = resp.getString("statuscode");
            String statustype = resp.getString("statusType");

            if (statuscode.trim().equalsIgnoreCase("200") && statustype.trim().equalsIgnoreCase("Success")) {
                messageDialog("success", resp.getString("statusmessage").trim(), getString(R.string.failureLabel));

            } else if (statuscode.trim().equalsIgnoreCase("400") && statustype.trim().equalsIgnoreCase("Failure")) {
                messageDialog("Fail", resp.getString("statusmessage").trim(), getString(R.string.failureLabel));
            } else {
                try {
                    messageDialog("warning", resp.getString("statusmessage").trim(), getString(R.string.failureLabel));
                    System.out.println("Get Vin Detail error response : " + resp.toString());
                } catch (JSONException js) {
                    js.printStackTrace();
                }

            }
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            if (pDialog != null) {
                if (pDialog.isShowing()) {
                    pDialog.dismiss();
                }
            }
        }
    }

    public void connectTimeoutLayoutDialog(String message) {
        final Dialog dialogForMessage = new Dialog(this);
        dialogForMessage.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogForMessage.setContentView(R.layout.alert_for_no_internet);
        dialogForMessage.setCancelable(false);
        TextView m = dialogForMessage.findViewById(R.id.message);
        final Button ok = dialogForMessage.findViewById(R.id.ok);
        m.setText(message);
        dialogForMessage.show();
        ok.setText(getString(R.string.ok));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialogForMessage.dismiss();
            }
        });
    }

    public void messageDialog(String s, String message, String title) {
        final Dialog dialogForMessage = new Dialog(this);
        dialogForMessage.requestWindowFeature(Window.FEATURE_NO_TITLE);
        TextView m = null;
        TextView t = null;
        Button ok = null;
        if ("success".equalsIgnoreCase(s)) {
            dialogForMessage.setContentView(R.layout.alert_for_msg_success);
            m = dialogForMessage.findViewById(R.id.message);
            t = dialogForMessage.findViewById(R.id.title);
            ok = dialogForMessage.findViewById(R.id.ok);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    dialogForMessage.dismiss();
                }
            });
        } else if ("warning".equalsIgnoreCase(s)) {
            dialogForMessage.setContentView(R.layout.alert_for_msg_warning);
            m = dialogForMessage.findViewById(R.id.message);
            t = dialogForMessage.findViewById(R.id.title);
            ok = dialogForMessage.findViewById(R.id.ok);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    dialogForMessage.dismiss();
                }
            });
        } else {
            dialogForMessage.setContentView(R.layout.alert_for_msg_fail);
            m = dialogForMessage.findViewById(R.id.message);
            t = dialogForMessage.findViewById(R.id.title);
            ok = dialogForMessage.findViewById(R.id.ok);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    dialogForMessage.dismiss();
                }
            });
        }
        dialogForMessage.setCancelable(false);
        if (title != null)
            t.setText(title);
        m.setText(message);
        dialogForMessage.show();
        ok.setText(this.getString(R.string.ok));
    }


    @Override
    public String notifySuccess(String s, JSONObject jsonObject) {
        if (pDialog != null) {
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
        if (s.equalsIgnoreCase("geocodeRequest")) {
            getAddress(jsonObject);
//            System.out.println("******************************jsonObject = " + jsonObject);
        } else {
            getImageProcessResponse(jsonObject);
        }
        return null;
    }

    @Override
    public String notifyError(String s, com.android.volley.VolleyError volleyError) {
        if (pDialog != null) {
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
        connectTimeoutLayoutDialog(getString(R.string.noInternetConnectionMgs));
        return null;
    }

    private void requestLocation() {
        //Check permission is Available or Not
        if (!checkPermission()) {
            //Request for Permission
            requestPermission();
        } else {
            //Add Location Listener CallBack
            CLocationWrapper.addLocationListener(this);

            //Request location
            CLocationWrapper.requestLocation();
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission Granted, Now you can access location data", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, "Permission Denied, You cannot access location data.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    //Requested Location callback of Fused location object
    @Override
    public void updateLocation(Location location) {
        Log.i(TAG_STR, "Inside updateLocation " + location.getAltitude() + "*************");

        //Display location Object values in TextViews
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = sdf.format(new Date(location.getTime()));
        time.setText("" + dateTime);
        altitude.setText("" + location.getAltitude() + " m");
        speed.setText("" + location.getSpeed() + " m/sec");
        bearing.setText("" + location.getBearing() + "°");
        acc.setText("" + location.getAccuracy() + " m");
        pro.setText(location.getProvider());
        mLat.setText("" + location.getLatitude());
        mLng.setText("" + location.getLongitude());
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        CameraUpdate center =
                CameraUpdateFactory.newLatLng(sydney);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(8);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

        mVolleyService.sendRequest(this, Request.Method.GET, "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + location.getLatitude() + "," + location.getLongitude() + "&key=AIzaSyBhDoMmm8hJNWr0XRFSMGN2T5spmJSqegQ", null, "geocodeRequest", "application/json");

        //Remove Location Listener is mandatory
        CLocationWrapper.removeLocationListener(this);
    }

    public void getAddress(JSONObject jsonObj) {
        Address1 = "";
        Address2 = "";
        City = "";
        State = "";
        Country = "";
        County = "";
        PIN = "";
        Area = "";
        route = "";


        try {

            String Status = jsonObj.getString("status");
            if (Status.equalsIgnoreCase("OK")) {
                JSONArray Results = jsonObj.getJSONArray("results");
                JSONObject zero = Results.getJSONObject(0);
                JSONArray address_components = zero.getJSONArray("address_components");

                for (int i = 0; i < address_components.length(); i++) {
                    JSONObject zero2 = address_components.getJSONObject(i);
                    String long_name = zero2.getString("long_name");
                    JSONArray mtypes = zero2.getJSONArray("types");
                    String Type = mtypes.getString(0);

                    if (!TextUtils.isEmpty(long_name) || !long_name.equals(null) || long_name.length() > 0 || !long_name.equals("")) {
                        if (Type.equalsIgnoreCase("street_number")) {
                            Address1 = long_name + " ";
                            System.out.println("Address1 = " + Address1);
                        } else if (Type.equalsIgnoreCase("route")) {
                            Address1 = Address1 + long_name;
                            System.out.println("Address1 = " + Address1);
                        } else if (Type.equalsIgnoreCase("sublocality")) {
                            Address2 = long_name;
                            System.out.println("Address2= " + Address2);
                        } else if (Type.equalsIgnoreCase("locality")) {
                            City = long_name;
                            System.out.println("City = " + City);
                        } else if (Type.equalsIgnoreCase("administrative_area_level_2")) {
                            County = long_name;
                            System.out.println("Country = " + Country);
                        } else if (Type.equalsIgnoreCase("administrative_area_level_1")) {
                            State = long_name;
                            System.out.println("Status = " + Status);
                        } else if (Type.equalsIgnoreCase("country")) {
                            Country = long_name;
                            System.out.println("Country = " + Country);
                        } else if (Type.equalsIgnoreCase("postal_code")) {
                            PIN = long_name;
                            System.out.println("PIN = " + PIN);
                        } else if (Type.equalsIgnoreCase("neighborhood")) {
                            Area = long_name;
                            System.out.println("Area = " + Area);
                        }
                        addressTv.setText(Address1 + " " + Address2 + " " + Area + " " + City + " " + State + " Pin Code-" + PIN + ", " + Country);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }
}
