package com.example.thu.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thu.taxinhanh.MainActivity;
import com.example.thu.taxinhanh.MapsActivity;
import com.example.thu.taxinhanh.R;
import com.example.thu.utils.DirectionFinder;
import com.example.thu.utils.DirectionFinderListener;
import com.example.thu.utils.GPSTracker;
import com.example.thu.utils.LatLngInterpolator;
import com.example.thu.utils.MarkerAnimation;
import com.example.thu.utils.Route;
import com.example.thu.utils.Utils;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by thu on 6/12/2017.
 * Guide at http://manishkpr.webheavens.com/android-navigation-drawer-example-using-fragments/
 */

public class BookFragment extends Fragment implements OnMapReadyCallback, DirectionFinderListener {
    private boolean isBookAvailable = false;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private GPSTracker gpsTracker;
    MapView mMapView;

    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    private static final String[] LOCATION_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int LOCATION_REQUEST = 1340;


    public static Fragment newInstance(Context context) {
        BookFragment f = new BookFragment();
        return f;
    }

    ViewGroup root = null;
    Marker mMarker = null;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://thesisk13.ddns.net:3002/");
        } catch (URISyntaxException e) {}
    }

    //https://stackoverflow.com/questions/13756261/how-to-get-the-current-location-in-google-maps-android-api-v2
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(final Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            if (mMarker != null) {
                mMarker.remove();
            }

            mMarker = mMap.addMarker(new MarkerOptions().position(loc));
            if(mMap != null){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f));
            }

                ArrayList<LatLng> a = new ArrayList<LatLng>();
                a.add(new LatLng(10.763001338925134,106.675278721788));
                a.add(new LatLng(10.763001338925134,106.69027566331943));
                a.add(new LatLng(10.756531587882872,106.69027566331943));
                a.add(new LatLng(10.756531587882872,106.675278721788));

                LatLng b = new LatLng(location.getLatitude(), location.getLongitude());
                if (com.google.maps.android.PolyUtil.containsLocation(b, a, true)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Vào vùng ahihi", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        }
    };

    private ArrayList<Marker> lstVehicles = new ArrayList<Marker>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.activity_book, null);

        final TextView tvPickUp = (TextView) root.findViewById(R.id.tvPickUp);
        final TextView tvDropOff = (TextView) root.findViewById(R.id.tvDropOff);
        tvPickUp.setSelected(true);
        tvDropOff.setSelected(true);

        Button btnDropOff = (Button) root.findViewById(R.id.btnDropOff);

        btnDropOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoadPlaceAutocomplete().execute();


            }
        });

        ImageButton btnClearPickUp = (ImageButton) root.findViewById(R.id.btnClearDropOff);
        btnClearPickUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDropOff.setText(getResources().getText(R.string.please_choose_dropoff));
                tvDropOff.setTypeface(null, Typeface.ITALIC);
            }
        });

        final ImageButton btnBook = (ImageButton) root.findViewById(R.id.btnBook);
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(getContext(), String.valueOf(mMap.getCameraPosition().tilt),Toast.LENGTH_LONG).show();

                if (isBookAvailable) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Thông tin")
                            .setMessage(getResources().getString(R.string.go_to_queue_zone))
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.cancel();
                                }
                            }).create().show();
                    isBookAvailable = false;
                    String currentAddress = getAddress(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                    sendRequest(currentAddress, tvPickUp.getText().toString());
                }

                btnBook.setImageResource(R.drawable.book_invisible);
            }
        });

        mMapView = (MapView) root.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                mMap = map;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
                    return;
                }
                mMap.setMyLocationEnabled(true);

                //gpsTracker = new GPSTracker(getActivity());

                TextView tvPickUp = (TextView) root.findViewById(R.id.tvPickUp);
                tvPickUp.setText(getAddress(10.7622739,106.6822471));
                //tvPickUp.setText(getAddress(gpsTracker.getLatitude(),gpsTracker.getLongitude()));

                // For dropping a marker at a point on the Map
                //LatLng sydney = new LatLng(gpsTracker.getLatitude(),gpsTracker.getLongitude());
                //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                //CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(15).build();

                //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                mMap.setOnMyLocationChangeListener(myLocationChangeListener);


//                mMap.addMarker(new MarkerOptions()
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
//                        .title("59A-1234")
//                        .position(new LatLng(10.7622739,106.6822471)));


                mSocket.on("INIT_VEHICELS", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        //JSONObject objVehicles = (JSONObject) args[0];
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "connected", Toast.LENGTH_SHORT);
                            }
                        });
                    }
                }).on("VEHICLE_UPDATE", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        final JSONObject objUpdate = (JSONObject) args[0];
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Marker vehicle = findVehicleMarker(objUpdate.getString("licensePlate"));
                                    LatLng newLocation = new LatLng(objUpdate.getDouble("lat"),objUpdate.getDouble("lng"));
                                    LatLng oldLocation = new LatLng(objUpdate.getDouble("latOld"),objUpdate.getDouble("lngOld"));

                                    String licensePlate = objUpdate.getString("licensePlate");
                                    if (null == vehicle) {
                                        lstVehicles.add(mMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                                .title(licensePlate)
                                                .position(newLocation)));
                                    } else {
                                        Location prevLoc = new Location("");
                                        prevLoc.setLatitude(oldLocation.latitude);
                                        prevLoc.setLongitude(oldLocation.longitude);

                                        Location nextLoc = new Location("");
                                        nextLoc.setLatitude(newLocation.latitude);
                                        nextLoc.setLongitude(newLocation.longitude);

                                        float bearing = prevLoc.bearingTo(nextLoc) ;


                                        vehicle.setRotation(bearing);
                                        MarkerAnimation.animateMarkerToICS(vehicle, newLocation, new LatLngInterpolator.Spherical());
                                        //vehicle.setPosition();

                                    }

                                    ;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    }
                });

                mSocket.connect();


//                ArrayList<LatLng> a = new ArrayList<LatLng>();
//                a.add(new LatLng(10.763001338925134,106.675278721788));
//                a.add(new LatLng(10.763001338925134,106.69027566331943));
//                a.add(new LatLng(10.756531587882872,106.69027566331943));
//                a.add(new LatLng(10.756531587882872,106.675278721788));
//
//                LatLng b = new LatLng(10.7622739,106.6822471);
//                final String res = String.valueOf((Utils.isPointInPolygon(b, a)));
//
//                boolean c = com.google.maps.android.PolyUtil.containsLocation(b, a, true);
//
//
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getActivity(), res, Toast.LENGTH_SHORT);
//                    }
//                });
            }
        });



        return root;
    }

    private class LoadPlaceAutocomplete extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Intent intent =
                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .build(getActivity());
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            } catch (GooglePlayServicesRepairableException e) {
                // TODO: Handle the error.
            } catch (GooglePlayServicesNotAvailableException e) {
                // TODO: Handle the error.
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((LinearLayout)getActivity().findViewById(R.id.llLoading)).setVisibility(View.GONE);
                }
            });
        }

        @Override
        protected void onPreExecute() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((LinearLayout)getActivity().findViewById(R.id.llLoading)).setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        protected void onProgressUpdate(Void... values) { }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        TextView tvDropOff = (TextView) getActivity().findViewById(R.id.tvDropOff);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                tvDropOff.setText(place.getAddress());
                TextView tvPickUp = (TextView) getActivity().findViewById(R.id.tvPickUp);
                isBookAvailable = true;
                sendRequest(tvPickUp.getText().toString(), place.getAddress().toString());
                //isBookAvailable = false;

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                tvDropOff.setText(getResources().getText(R.string.please_choose_dropoff));
                tvDropOff.setTypeface(null, Typeface.ITALIC);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                // Do nothing
            }
        }
    }

    private void sendRequest(String origin, String destination) {
        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0) +  ", " + obj.getAddressLine(2) +  ", " + obj.getAddressLine(3);
            return add;
            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return "NOT FOUND";
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(getActivity(), "Loading...",
                "Đang lấy thông tin địa điểm..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }

        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
    }

    private Marker findVehicleMarker (String licensePlate) {
        for (int i = 0; i < lstVehicles.size(); i++) {
            if (lstVehicles.get(i).getTitle().toLowerCase().equals(licensePlate.toLowerCase())) {
                return lstVehicles.get(i);
            }
        }
        return null;
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));

            //distance = route.distance.text;
            String[] km = route.distance.text.split(" ");

            double distance = Double.parseDouble(km[0]);
            updatePriceUI(distance);


            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.empty_flag_40))
                    .title(route.startAddress)
                    .position(route.startLocation)));

            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.empty_flag_40))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(6);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(route.startLocation);
            builder.include(route.endLocation);
            LatLngBounds bounds = builder.build();

            //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 15));
        }
    }

    public void updatePriceUI(double distance) {
        if (isBookAvailable) {
            ((ImageButton) getActivity().findViewById(R.id.btnBook)).setImageResource(R.drawable.book_visible);
            getActivity().findViewById(R.id.tiPrice).setVisibility(View.VISIBLE);
            TextView tvBook = (TextView)getActivity().findViewById(R.id.tvFare);
            tvBook.setText("Giá cước dự tính: " + String.format("%,.0f VNĐ", distance * 11000));
            //((ImageButton) root.findViewById(R.id.btnBook)).setImageResource(R.drawable.book_visible);
        } else {
            //((ImageButton) getActivity().findViewById(R.id.btnBook)).setImageResource(R.drawable.book_invisible);
        }
    }
}
