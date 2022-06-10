package com.example.lab1_aanchalbansal_c0846324_android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.lab1_aanchalbansal_c0846324_android.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener {

    private static final int REQUEST_CODE = 1;
    private static final String TAG = "MapsActivity";
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    //    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_LIGHT_GREEN_ARGB = 0xff81C784;
    private static final int COLOR_DARK_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_LIGHT_ORANGE_ARGB = 0xffF9A825;
    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);
    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DASH, GAP);
    private final ArrayList<MarkerOptions> markerArray = new ArrayList<MarkerOptions>();
    private boolean polylineASelected = false;
    private boolean polylineBSelected = false;
    private boolean polylineCSelected = false;
    private boolean polylineDSelected = false;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient mClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public static LatLng midPoint(LatLng start, LatLng dest) {
        if(start.latitude < dest.latitude) {
            LatLngBounds bounds = new LatLngBounds(start, dest);
            return bounds.getCenter();
        }
        return start;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SearchView searchView = findViewById(R.id.idSearchView);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // on below line we are getting the
                // location name from search view.
                String location = searchView.getQuery().toString();

                // below line is to create a list of address
                // where we will store the list of all address.
                List<Address> addressList = null;

                // checking if the entered location is null or not.
                if (location != null || location.equals("")) {
                    // on below line we are creating and initializing a geo coder.
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        // on below line we are getting location from the
                        // location name and adding that location to address list.
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(!addressList.isEmpty()) {
                        // on below line we are getting the location
                        // from our list a first position.
                        Address address = addressList.get(0);

                        // on below line we are creating a variable for our location
                        // where we will add our locations latitude and longitude.
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(location);
                        // on below line we are adding marker to that position.
                        if (markerArray.size() >= 4) {
                            mMap.clear(); // To clear all previous markers
                            markerArray.clear();
                        }
                        mMap.addMarker(markerOptions);
                        markerArray.add(markerOptions);
                        drawPolyLine();
                        searchView.clearFocus();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        // at last we calling our map fragment to update.
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment.getMapAsync(this);

        mClient = LocationServices.getFusedLocationProviderClient(this);
    }

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

        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.zoomIn());

        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();

        this.mMap.setOnMapClickListener((GoogleMap.OnMapClickListener) this);
    }

    @SuppressLint("MissingPermission")
    private void startUpdateLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
//                mMap.clear();
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                    mMap.addMarker(new MarkerOptions().position(userLocation).title("your location!"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
                    mClient.removeLocationUpdates(locationCallback);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mClient.requestLocationUpdates(locationRequest, locationCallback, null);

    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage("The permission is mandatory")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                            }
                        }).create().show();
            } else
                startUpdateLocation();
        }
    }

    private void drawPolyLine(){
        if (markerArray.size() > 1) {
            Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .add(
                            new LatLng(markerArray.get(0).getPosition().latitude, markerArray.get(0).getPosition().longitude),
                            new LatLng(markerArray.get(1).getPosition().latitude, markerArray.get(1).getPosition().longitude)));

            // Store a data object with the polyline, used here to indicate an arbitrary type.
            polyline1.setTag("A");
            polyline1.setColor(R.color.red);
            stylePolyline(polyline1);
        }

        if (markerArray.size() > 2) {
            Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .add(
                            new LatLng(markerArray.get(1).getPosition().latitude, markerArray.get(1).getPosition().longitude),
                            new LatLng(markerArray.get(2).getPosition().latitude, markerArray.get(2).getPosition().longitude)));

            // Store a data object with the polyline, used here to indicate an arbitrary type.
            polyline1.setTag("B");
            polyline1.setColor(R.color.red);
            stylePolyline(polyline1);
        }

        if (markerArray.size() > 3) {
            Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .add(
                            new LatLng(markerArray.get(2).getPosition().latitude, markerArray.get(2).getPosition().longitude),
                            new LatLng(markerArray.get(3).getPosition().latitude, markerArray.get(3).getPosition().longitude)));

            // Store a data object with the polyline, used here to indicate an arbitrary type.
            polyline1.setTag("C");
            polyline1.setColor(R.color.red);
            stylePolyline(polyline1);

            // Add polygons to indicate areas on the map.
            Polygon polygon1 = mMap.addPolygon(new PolygonOptions()
                    .clickable(true)
                    .add(
                            new LatLng(markerArray.get(0).getPosition().latitude, markerArray.get(0).getPosition().longitude),
                            new LatLng(markerArray.get(1).getPosition().latitude, markerArray.get(1).getPosition().longitude),
                            new LatLng(markerArray.get(2).getPosition().latitude, markerArray.get(2).getPosition().longitude),
                            new LatLng(markerArray.get(3).getPosition().latitude, markerArray.get(3).getPosition().longitude)));
            // Store a data object with the polygon, used here to indicate an arbitrary type.
            polygon1.setTag("alpha");
            // Style the polygon.
            stylePolygon(polygon1);
        }

        // Set listeners for click events.
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

        if (markerArray.size() >= 4) {
            mMap.clear(); // To clear all previous markers
            markerArray.clear();
        }

        String[] markerNames = new String[]{"A", "B", "C", "D"};
        Integer count = markerArray.size();
        MarkerOptions marker = new MarkerOptions()
                .position(latLng).title(markerNames[count]);
        mMap.addMarker(marker);
        markerArray.add(marker);
        drawPolyLine();

    }

    /**
     * Listens for clicks on a polyline.
     *
     * @param polyline The polyline object that the user has clicked.
     */
    @Override
    public void onPolylineClick(Polyline polyline) {

        switch (polyline.getTag().toString()) {

            case "A":

                // Working here currently - app crashing here

                LatLng midPoint = midPoint(markerArray.get(0).getPosition(), markerArray.get(1).getPosition());
                Double distance = distance(markerArray.get(0).getPosition(), markerArray.get(1).getPosition());

                HashMap<String, Marker> hashMapMarker = new HashMap<>();

                if (polylineASelected) {
                    MarkerOptions marker = new MarkerOptions()
                            .position(midPoint).title(String.valueOf(distance.shortValue()) + " mi");
                    Marker marker1 = this.mMap.addMarker(marker);
                    hashMapMarker.put("A", marker1);

                } else {
                    Marker marker = hashMapMarker.get("A");
                    if(marker!=null) {
                        marker.remove();
                        hashMapMarker.remove("A");
                    }
                }

                if(polylineASelected){
                    polylineASelected = false;
                }else{
                    polylineASelected = true;
                }

                Toast.makeText(this,
                        "Distance from A to B is " + (distance.intValue()) + " mi",
                        Toast.LENGTH_LONG).show();
                break;
            case "B":

                LatLng midPoint1 = midPoint(markerArray.get(1).getPosition(), markerArray.get(2).getPosition());
                Double distance1 = distance(markerArray.get(1).getPosition(), markerArray.get(2).getPosition());

                HashMap<String, Marker> hashMapMarker1 = new HashMap<>();

                if (polylineBSelected) {
                    MarkerOptions marker = new MarkerOptions()
                            .position(midPoint1).title(distance1 + " mi");
                    Marker marker2 = this.mMap.addMarker(marker);
                    hashMapMarker1.put("B", marker2);

                } else {
                    Marker marker = hashMapMarker1.get("B");
                    if(marker!=null) {
                        marker.remove();
                        hashMapMarker1.remove("B");
                    }
                }

                if(polylineBSelected){
                    polylineBSelected = false;
                }else{
                    polylineBSelected = true;
                }
                Toast.makeText(this,
                        "Distance from B to C is " + (distance1.intValue()) + " mi",
                        Toast.LENGTH_LONG).show();
                break;
            case "C":
                LatLng midPoint2 = midPoint(markerArray.get(2).getPosition(), markerArray.get(3).getPosition());
                Double distance2 = distance(markerArray.get(2).getPosition(), markerArray.get(3).getPosition());

                HashMap<String, Marker> hashMapMarker2 = new HashMap<>();

                if (polylineBSelected) {
                    MarkerOptions marker = new MarkerOptions()
                            .position(midPoint2).title(distance2 + " mi");
                    Marker marker3 = this.mMap.addMarker(marker);
                    hashMapMarker2.put("C", marker3);

                } else {
                    Marker marker = hashMapMarker2.get("C");
                    if(marker!=null) {
                        marker.remove();
                        hashMapMarker2.remove("C");
                    }
                }
                if(polylineCSelected){
                    polylineCSelected = false;
                }else{
                    polylineCSelected = true;
                }
                Toast.makeText(this,
                        "Distance from C to D is " + (distance2.intValue()) + " mi",
                        Toast.LENGTH_LONG).show();
                break;
            case "D":
                LatLng midPoint3 = midPoint(markerArray.get(3).getPosition(), markerArray.get(0).getPosition());
                Double distance3 = distance(markerArray.get(3).getPosition(), markerArray.get(0).getPosition());

                HashMap<String, Marker> hashMapMarker3 = new HashMap<>();

                if (polylineBSelected) {
                    MarkerOptions marker = new MarkerOptions()
                            .position(midPoint3).title(distance3 + " mi");
                    Marker marker4 = this.mMap.addMarker(marker);
                    hashMapMarker3.put("D", marker4);

                } else {
                    Marker marker = hashMapMarker3.get("D");
                    if(marker!=null) {
                        marker.remove();
                        hashMapMarker3.remove("D");
                    }
                }
                if(polylineDSelected){
                    polylineDSelected = false;
                }else{
                    polylineDSelected = true;
                }
                Toast.makeText(this,
                        "Distance from D to A " + (distance3.intValue()) + " mi",
                        Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }

        // Previous code
        // Flip from solid stroke to dotted stroke pattern.
//        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
//            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
//        } else {
//            // The default pattern is a solid stroke.
//            polyline.setPattern(null);
//        }
//        Toast.makeText(this, "Route type " + polyline.getTag().toString(),
//                Toast.LENGTH_SHORT).show();
    }

    /**
     * Listens for clicks on a polygon.
     *
     * @param polygon The polygon object that the user has clicked.
     */
    @Override
    public void onPolygonClick(Polygon polygon) {
        double dis1 = distance(polygon.getPoints().get(0), polygon.getPoints().get(1));
        double dis2 = distance(polygon.getPoints().get(1), polygon.getPoints().get(2));
        double dis3 = distance(polygon.getPoints().get(2), polygon.getPoints().get(3));
        double dis4 = distance(polygon.getPoints().get(3), polygon.getPoints().get(0));

        Toast.makeText(this, "Total distance  " + (int)(dis1 + dis2 + dis3 + dis4) + " mi", Toast.LENGTH_SHORT).show();
    }

    /**
     * Styles the polyline, based on type.
     *
     * @param polyline The polyline object that needs styling.
     */
    private void stylePolyline(Polyline polyline) {
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "A":
                // Use a custom bitmap as the cap at the start of the line.
//                polyline.setStartCap(
//                        new CustomCap(
//                                BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10));
                break;
            case "B":
                // Use a round cap at the start of the line.
                polyline.setStartCap(new RoundCap());
                break;
        }

        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(getResources().getColor(R.color.red));
        polyline.setJointType(JointType.ROUND);
    }

    /**
     * Styles the polygon, based on type.
     *
     * @param polygon The polygon object that needs styling.
     */
    private void stylePolygon(Polygon polygon) {
        String type = "";
        // Get the data object stored with the polygon.
        if (polygon.getTag() != null) {
            type = polygon.getTag().toString();
        }

        List<PatternItem> pattern = null;
        int strokeColor = getResources().getColor(R.color.red);
        int fillColor = getResources().getColor(R.color.translucent_green);

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "alpha":
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA;
                strokeColor = getResources().getColor(R.color.red);
                fillColor = getResources().getColor(R.color.translucent_green);
                break;
            case "beta":
                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
                pattern = PATTERN_POLYGON_BETA;
                strokeColor = getResources().getColor(R.color.red);
                fillColor = getResources().getColor(R.color.translucent_green);
                break;
        }

//        polygon.setStrokePattern(pattern);
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(strokeColor);
        polygon.setFillColor(fillColor);
    }

    /*
    To find distance between two coordinates
     */
    private double distance(LatLng latlon1, LatLng latLng2) {
        double theta = latLng2.longitude - latlon1.longitude;
        double dist = Math.sin(deg2rad(latlon1.latitude))
                * Math.sin(deg2rad(latLng2.latitude))
                + Math.cos(deg2rad(latlon1.latitude))
                * Math.cos(deg2rad(latLng2.latitude))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}