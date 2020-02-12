package com.andsomore.sosinfosante;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.andsomore.sosinfosante.idao.IResult;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.sdsmdg.harjot.vectormaster.VectorMasterView;
import com.sdsmdg.harjot.vectormaster.models.PathModel;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    private CurvedBottomNavigationView bottomNavigationView;
    private VectorMasterView fab, fab1, fab2;
    private FloatingActionButton FAB;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView nv;
    RelativeLayout lin_id;
    PathModel outline;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private SymbolManager symbolManager;
    private List<Symbol> symbols = new ArrayList<>();
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);
    private Point originPosition;
    private Point destinationPosition;
    private NavigationMapRoute mapRoute;
    private DirectionsRoute currentRoute;
    public static boolean isLocationEnabled = false;
    // Variables needed to add the location engine
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private static final String DOT_SOURCE_ID = "dot-source-id";
    private static final String LINE_SOURCE_ID = "line-source-id";
    private GeoPoint Gpoint;
    Intent intent;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    static MainActivity instance;
    private LatLng secouristeCurrentPosition;
    private LatLng secouristeUpdatedPosition;
    private GeoJsonSource geoJsonSource;
    private ValueAnimator animator;
    private static int count=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapBox_key));
        setContentView(R.layout.activity_main);
        instance = this;
        InitViews();
        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorWhite));

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        bottomNavigationView.inflateMenu(R.menu.main_menu);
        bottomNavigationView.setSelectedItemId(R.id.action_home);

        bottomNavigationView.getMenu().getItem(1).setIcon(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

    }

    private void InitViews() {
        mapView = findViewById(R.id.mapView);
        bottomNavigationView = (CurvedBottomNavigationView) findViewById(R.id.bottom_nav);
        fab = (VectorMasterView) findViewById(R.id.fab);
        fab1 = (VectorMasterView) findViewById(R.id.fab1);
        fab2 = (VectorMasterView) findViewById(R.id.fab2);
        lin_id = (RelativeLayout) findViewById(R.id.lin_id);
        FAB = (FloatingActionButton) findViewById(R.id.myLocationButton);
    }

    private void draw() {

        bottomNavigationView.mFirstCurveStartPoint.set((bottomNavigationView.mNavigationBarWidth * 10 / 12)
                - (bottomNavigationView.CURVE_CIRCLE_RADIUS * 2)
                - (bottomNavigationView.CURVE_CIRCLE_RADIUS / 3), 0);

        bottomNavigationView.mFirstCurveEndPoint.set(bottomNavigationView.mNavigationBarWidth * 10 / 12,
                bottomNavigationView.CURVE_CIRCLE_RADIUS
                        + (bottomNavigationView.CURVE_CIRCLE_RADIUS / 4));

        bottomNavigationView.mSecondCurveStartPoint = bottomNavigationView.mFirstCurveEndPoint;
        bottomNavigationView.mSecondCurveEndPoint.set((bottomNavigationView.mNavigationBarWidth * 10 / 12)
                + (bottomNavigationView.CURVE_CIRCLE_RADIUS * 2) + (bottomNavigationView.CURVE_CIRCLE_RADIUS / 3), 0);

        bottomNavigationView.mFirstCurveControlPoint1.set(bottomNavigationView.mFirstCurveStartPoint.x
                        + bottomNavigationView.CURVE_CIRCLE_RADIUS + (bottomNavigationView.CURVE_CIRCLE_RADIUS / 4),
                bottomNavigationView.mFirstCurveStartPoint.y);

        bottomNavigationView.mFirstCurveControlPoint2.set(bottomNavigationView.mFirstCurveEndPoint.x
                        - (bottomNavigationView.CURVE_CIRCLE_RADIUS * 2) + bottomNavigationView.CURVE_CIRCLE_RADIUS,
                bottomNavigationView.mFirstCurveEndPoint.y);

        //pour le 2nd
        bottomNavigationView.mSecondCurveControlPoint1.set(bottomNavigationView.mSecondCurveStartPoint.x
                        + (bottomNavigationView.CURVE_CIRCLE_RADIUS * 2) - bottomNavigationView.CURVE_CIRCLE_RADIUS,
                bottomNavigationView.mSecondCurveStartPoint.y);
        bottomNavigationView.mSecondCurveControlPoint2.set(bottomNavigationView.mSecondCurveEndPoint.x -
                        (bottomNavigationView.CURVE_CIRCLE_RADIUS + (bottomNavigationView.CURVE_CIRCLE_RADIUS / 4)),
                bottomNavigationView.mSecondCurveEndPoint.y);
    }


    private void draw(int i) {
        bottomNavigationView.mFirstCurveStartPoint.set((bottomNavigationView.mNavigationBarWidth / i)
                - (bottomNavigationView.CURVE_CIRCLE_RADIUS * 2) - (bottomNavigationView.CURVE_CIRCLE_RADIUS / 3), 0);

        bottomNavigationView.mFirstCurveEndPoint.set(bottomNavigationView.mNavigationBarWidth / i, bottomNavigationView.CURVE_CIRCLE_RADIUS
                + (bottomNavigationView.CURVE_CIRCLE_RADIUS / 4));

        bottomNavigationView.mSecondCurveStartPoint = bottomNavigationView.mFirstCurveEndPoint;
        bottomNavigationView.mSecondCurveEndPoint.set((bottomNavigationView.mNavigationBarWidth / i)
                + (bottomNavigationView.CURVE_CIRCLE_RADIUS * 2) + (bottomNavigationView.CURVE_CIRCLE_RADIUS / 3), 0);

        bottomNavigationView.mFirstCurveControlPoint1.set(bottomNavigationView.mFirstCurveStartPoint.x
                        + bottomNavigationView.CURVE_CIRCLE_RADIUS + (bottomNavigationView.CURVE_CIRCLE_RADIUS / 4),
                bottomNavigationView.mFirstCurveStartPoint.y);

        bottomNavigationView.mFirstCurveControlPoint2.set(bottomNavigationView.mFirstCurveEndPoint.x
                        - (bottomNavigationView.CURVE_CIRCLE_RADIUS * 2) + bottomNavigationView.CURVE_CIRCLE_RADIUS,
                bottomNavigationView.mFirstCurveEndPoint.y);

        //pour le 2nd
        bottomNavigationView.mSecondCurveControlPoint1.set(bottomNavigationView.mSecondCurveStartPoint.x
                        + (bottomNavigationView.CURVE_CIRCLE_RADIUS * 2) - bottomNavigationView.CURVE_CIRCLE_RADIUS,
                bottomNavigationView.mSecondCurveStartPoint.y);
        bottomNavigationView.mSecondCurveControlPoint2.set(bottomNavigationView.mSecondCurveEndPoint.x -
                        (bottomNavigationView.CURVE_CIRCLE_RADIUS + (bottomNavigationView.CURVE_CIRCLE_RADIUS / 4)),
                bottomNavigationView.mSecondCurveEndPoint.y);
    }

    private void drawAnimation(final VectorMasterView fab) {
        outline = fab.getPathModelByName("outline");
        outline.setStrokeColor(Color.WHITE);
        outline.setTrimPathEnd(0.0f);
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                outline.setTrimPathEnd((Float) valueAnimator.getAnimatedValue());
                fab.update();
            }
        });
        animator.start();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_conso:
                draw(6);
                lin_id.setX(bottomNavigationView.mFirstCurveControlPoint1.x);
                fab.setVisibility(View.VISIBLE);
                fab1.setVisibility(View.GONE);
                fab2.setVisibility(View.GONE);
                bottomNavigationView.getMenu().getItem(0).setIcon(null);
                bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.ic_home);
                bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.ic_account);
                drawAnimation(fab);
                startActivityForResult(new Intent(MainActivity.this, IncidentActivity.class), 0);
                break;

            case R.id.action_home:
                // startActivity(new Intent(MainActivity.this,ReservationActivity.class));
                draw(2);
                lin_id.setX(bottomNavigationView.mFirstCurveControlPoint1.x);
                fab.setVisibility(View.GONE);
                fab1.setVisibility(View.VISIBLE);
                fab2.setVisibility(View.GONE);
                bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.ic_add_incident);
                bottomNavigationView.getMenu().getItem(1).setIcon(null);
                bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.ic_account);
                drawAnimation(fab1);
                break;

            case R.id.action_budget:
                // startActivity(new Intent(MainActivity.this,ReservationActivity.class));
                draw();
                lin_id.setX(bottomNavigationView.mFirstCurveControlPoint1.x);
                fab.setVisibility(View.GONE);
                fab1.setVisibility(View.GONE);
                fab2.setVisibility(View.VISIBLE);
                bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.ic_add_incident);
                bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.ic_home);
                bottomNavigationView.getMenu().getItem(2).setIcon(null);
                drawAnimation(fab2);
                //showAlertSialog();

                startActivityForResult(new Intent(MainActivity.this, CompteActivity.class), 0);
                break;
        }
        return true;
    }

    //Debut MapBox


    //Fonction pour actualiser la position de l'utilisateur
    private void initMapStuff() {
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (locationComponent.getLastKnownLocation() != null) {
                        mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory
                                .newLatLngZoom(new LatLng(mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude()
                                        , mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude()), 14));

                    }
                } else {
                    showGPSDisabledAlertToUser();
                }

            }
        });
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Le GPS est désactivé.Voulez-vous l'activer?")
                .setCancelable(false)
                .setPositiveButton("Activer",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                                if (locationComponent.getLastKnownLocation() != null) {
                                    mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory
                                            .newLatLngZoom(new LatLng(mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude()
                                                    , mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude()), 14));

                                }
                            }
                        });
        alertDialogBuilder.setNegativeButton("Annuler",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    //Fonction pour initialiser les objet du MapBox
    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Create and customize the LocationComponent's options
            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
                    .elevation(5)
                    .accuracyAlpha(.6f)
                    .accuracyColor(Color.WHITE)
                    .foregroundDrawable(R.drawable.accidente_marker)
                    .build();
            //Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();
            LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, loadedMapStyle)
                    .locationComponentOptions(customLocationComponentOptions)
                    .useDefaultLocationEngine(false)
                    .build();
            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            initMapStuff();
            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(new PermissionsListener() {
                @Override
                public void onExplanationNeeded(List<String> permissionsToExplain) {
                    Toast.makeText(MainActivity.this, "location not enabled", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onPermissionResult(boolean granted) {
                    if (granted) {

                        mapboxMap.getStyle(new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                enableLocationComponent(style);
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "Location services not allowed", Toast.LENGTH_LONG).show();
                    }
                }
            });
            permissionsManager.requestLocationPermissions(MainActivity.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
               /* mapboxMap.getStyle().addImage(("incident-marker"), BitmapFactory.decodeResource(
                        getResources(), R.drawable.accidente_marker));*/
//                intent = getIntent();
//                if (intent.hasExtra("idIncident")) {
//                    final String code = intent.getStringExtra("code").toString();
                //}
              //initSources(style);
              //initSymbolLayer(style);

            }
        });


    }


    private void getRoute(Point origin, Point destination, IResult result) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null) {
                            Log.e("Erreur: ", "Token incorrect");
                            result.getResult(false);
                            return;
                        } else if (response.body().routes().size() == 0) {
                            Toast.makeText(MainActivity.this, "Aucun chemin trouvé. ", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e("Erreur:", "Aucune route");
                            result.getResult(false);
                            return;
                        }
                        currentRoute = response.body().routes().get(0);
                        if (mapRoute != null) {
                            mapRoute.updateRouteVisibilityTo(false);
                            mapRoute.updateRouteArrowVisibilityTo(false);
                            result.getResult(false);

                        } else {
                            mapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        mapRoute.addRoute(currentRoute);
                        result.getResult(true);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e("Erreur: ", t.getMessage());
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        isLocationEnabled = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        isLocationEnabled = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
        intent = getIntent();
        if (intent.hasExtra("idIncident")) {

            GeoFirestore geo = new GeoFirestore(db.collection("INCIDENT"));
            geo.removeLocation(intent.getStringExtra("idIncident"));
        }
    }

    public List<Double> getPosition() {
        List<Double> list = new ArrayList<>();
        list.add(0, locationComponent.getLastKnownLocation().getLongitude());
        list.add(1, locationComponent.getLastKnownLocation().getLatitude());
        return list;
    }

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

    //Classe de Listener pour la position
    private class MainActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainActivity> activityWeakReference;

        MainActivityLocationCallback(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            MainActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                // Create a Toast which displays the new location's coordinates
               /* Toast.makeText(activity, String.format(activity.getString(R.string.new_location),
                        String.valueOf(result.getLastLocation().getLatitude()), String.valueOf(result.getLastLocation().getLongitude())),
                        Toast.LENGTH_SHORT).show();*/

                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                    intent = getIntent();
                    if (intent.hasExtra("idIncident")) {
                        final String idIncident = intent.getStringExtra("idIncident").toString();
                        final double Latitude = location.getLatitude();
                        final double Longitude = location.getLongitude();
                        GeoFirestore geo = new GeoFirestore(db.collection("INCIDENT"));
                        geo.setLocation(idIncident, new GeoPoint(Latitude, Longitude), null, new GeoFirestore.CompletionListener() {
                            @Override
                            public void onComplete(Exception e) {

                            }
                        });
                        getInitialSecouristePosition();
                        getUpdatedSecouristePosition();
                    }

                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception mess.36666666
         *                  age
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getUpdatedSecouristePosition() {
        intent = getIntent();
        if (intent.hasExtra("idIncident")) {
            final String idIntervention = intent.getStringExtra("idIntervention");
            GeoFirestore geo = new GeoFirestore(db.collection("INTERVENTION"));
            try {
                geo.getLocation(idIntervention, new GeoFirestore.LocationCallback() {
                    @Override
                    public void onComplete(@Nullable GeoPoint geoPoint, @Nullable Exception e) {
                        if (e == null) {
                            getInterventionPosition(new IResult() {
                                @Override
                                public void getResult(int val) {

                                }

                                @Override
                                public void getResult(boolean ok) {
                                    if (ok) {
                                        secouristeUpdatedPosition = new LatLng(Gpoint.getLatitude(),Gpoint.getLongitude());
                                        Log.d("Coordonnées Gpoin",Gpoint.toString());

                                        if (secouristeCurrentPosition != secouristeUpdatedPosition) {
                                            if (animator != null && animator.isStarted()) {
                                                secouristeCurrentPosition = (LatLng) animator.getAnimatedValue();
                                                Log.d("Detail:","Position differente");
                                                animator.cancel();
                                            }
                                            Log.d("Detail:","Position differente");

                                            animator = ObjectAnimator
                                                    .ofObject(latLngEvaluator, secouristeCurrentPosition, secouristeUpdatedPosition)
                                                    .setDuration(2000);

                                            animator.addUpdateListener(animatorUpdateListener);
                                            animator.start();
                                            secouristeCurrentPosition = secouristeUpdatedPosition;
                                        }
                                    }
                                }
                            });
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Erreur de mise a jour de la position", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getInitialSecouristePosition() {
        intent = getIntent();
        if (intent.hasExtra("idIncident") && count == 0) {
            final String idIntervention = intent.getStringExtra("idIntervention");
            final String code = intent.getStringExtra("code").toString();
            db = FirebaseFirestore.getInstance();
            GeoFirestore geo = new GeoFirestore(db.collection("INTERVENTION"));
            try {
                getInterventionPosition(new IResult() {
                    @Override
                    public void getResult(int val) {

                    }

                    @Override
                    public void getResult(boolean ok) {
                        if (ok) {
                            MainActivity.this.mapboxMap.getStyle(new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    initSources(style);
                                    initSymbolLayer(style);
                                }
                            });
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("Erreur:", e.getMessage());
            }
        }

    }

    public synchronized void getInterventionPosition(IResult result) {
        intent = getIntent();
        final String idIntervention = intent.getStringExtra("idIntervention");
        db = FirebaseFirestore.getInstance();
        GeoFirestore geo = new GeoFirestore(db.collection("INTERVENTION"));
        geo.getLocation(idIntervention, new GeoFirestore.LocationCallback() {
            @Override
            public void onComplete(@Nullable GeoPoint geoPoint, @Nullable Exception e) {
                if (e == null) {
                    Gpoint = geoPoint;
                    result.getResult(true);
                }
            }
        });

    }

    // Class is used to interpolate the marker animation.
    private static final TypeEvaluator<LatLng> latLngEvaluator = new TypeEvaluator<LatLng>() {
        private final LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    };

    private void initSources(@NonNull Style loadedMapStyle) {

        intent = getIntent();
        if (intent.hasExtra("idIntervention") && count == 0) {
            getInterventionPosition(new IResult() {
                @Override
                public void getResult(int val) {

                }
                @Override
                public void getResult(boolean ok) {
                  if(ok){
                      secouristeCurrentPosition = new LatLng(Gpoint.getLatitude(), Gpoint.getLongitude());
                      geoJsonSource = new GeoJsonSource(DOT_SOURCE_ID,
                              Feature.fromGeometry(Point.fromLngLat(secouristeCurrentPosition.getLongitude(),
                                      secouristeCurrentPosition.getLatitude())));
                      loadedMapStyle.addSource(geoJsonSource);
                      count = 1;
                  }
                }
            });
        }
    }

    private void initSymbolLayer(@NonNull Style loadedMapStyle) {
        intent = getIntent();
        if (intent.hasExtra("idIntervention") && count !=1 ) {
            count = 1;
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ambulance);
            loadedMapStyle.addImage("secouriste-marker", bm);
            final String code = intent.getStringExtra("code").toString();
            loadedMapStyle.addLayer(new SymbolLayer("symbol-layer-id", DOT_SOURCE_ID)
                    .withProperties(
                            PropertyFactory.iconImage("secouriste-marker"),
                            PropertyFactory.iconSize(1f),
                            PropertyFactory.textHaloColor("rgba(255, 255, 255, 100)"),
                            PropertyFactory.textAnchor(Property.TEXT_ANCHOR_TOP),
                            PropertyFactory.textField("Sapeur pompier " + code),
                            PropertyFactory.textOffset((new Float[]{0f, 1.5f})),
                            PropertyFactory.iconOffset(new Float[]{0f, -1.5f}),
                            PropertyFactory.textHaloWidth(5.0f),
                            PropertyFactory.iconIgnorePlacement(true),
                            PropertyFactory.iconAllowOverlap(true)
                    ));
        }


    }

    private void initDotLinePath(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addLayer(new LineLayer("line-layer-id", LINE_SOURCE_ID).withProperties(
                lineColor(Color.parseColor("#F13C6E")),
                lineWidth(4f)
        ));
    }

 private  final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
         new ValueAnimator.AnimatorUpdateListener() {
             @Override
             public void onAnimationUpdate(ValueAnimator valueAnimator) {
                 LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
                 geoJsonSource.setGeoJson(Point.fromLngLat(animatedPosition.getLongitude(), animatedPosition.getLatitude()));
             }
         };
}
