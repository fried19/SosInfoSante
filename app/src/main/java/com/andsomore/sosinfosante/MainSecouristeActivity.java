package com.andsomore.sosinfosante;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andsomore.sosinfosante.dao.TraitementIncident;
import com.andsomore.sosinfosante.dao.TraitementIntervention;
import com.andsomore.sosinfosante.entite.Incident;
import com.andsomore.sosinfosante.entite.Intervention;
import com.andsomore.sosinfosante.idao.IResponse;
import com.andsomore.sosinfosante.idao.IResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
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
import com.mapbox.mapboxsdk.annotations.Marker;
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
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Future;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainSecouristeActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private MapboxMap mapboxMap;
    private FloatingActionButton FAB;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private SymbolManager symbolManager;
    private List<Symbol> symbols = new ArrayList<>();
    private Point originPosition;
    private Point destinationPosition;
    private NavigationMapRoute mapRoute;
    private DirectionsRoute currentRoute;
    private String code;
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);
    // Variables needed to add the location engine
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView tvLieu, tvPrevisualiser;
    private EditText etDescription;
    Intent intent;
    private LatLng incidentCurrentPosition;
    private LatLng incidentUpdatedPosition;
    private GeoJsonSource geoJsonSource;
    private ValueAnimator animator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapBox_key));
        setContentView(R.layout.activity_main_agent);
        Intent intent = getIntent();
        if (intent.hasExtra("idIncident")) {
            showAlertDialog();
        }
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        code = preference.getString("Code", "000");
        getSupportActionBar().setTitle("Sapeur pompier " + code);
        InitViews();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void InitViews() {
        mapView = findViewById(R.id.mapView);
        FAB = (FloatingActionButton) findViewById(R.id.myLocationButton);

    }

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
                        dialog.dismiss();
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
                    .accuracyColor(R.color.colorButtonRed)
                    .foregroundDrawable(R.drawable.ambulance)
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
                    Toast.makeText(MainSecouristeActivity.this, "GPS non activé", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(MainSecouristeActivity.this, "Location services not allowed", Toast.LENGTH_LONG).show();
                    }
                }
            });
            permissionsManager.requestLocationPermissions(MainSecouristeActivity.this);
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
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.accidente_marker);
                mapboxMap.getStyle().addImage("incident-marker", bm);
                mapboxMap.getStyle().addLayer(new SymbolLayer("layer-id", "source-id")
                        .withProperties(
                                PropertyFactory.iconImage("incident-marker"),
                                PropertyFactory.iconSize(1f),
                                PropertyFactory.textHaloColor("rgba(255, 255, 255, 100)"),
                                PropertyFactory.textAnchor(Property.TEXT_ANCHOR_TOP),
                                PropertyFactory.textField("Personne à secourir"),
                                PropertyFactory.textOffset((new Float[]{0f, 1.5f})),
                                PropertyFactory.iconOffset(new Float[]{0f, -1.5f}),
                                PropertyFactory.textHaloWidth(5.0f),
                                PropertyFactory.iconIgnorePlacement(true),
                                PropertyFactory.iconAllowOverlap(true)
                        ));

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
                            Toast.makeText(MainSecouristeActivity.this, "Aucun chemin trouvé. ", Toast.LENGTH_SHORT).show();
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

    private void showAlertDialog() {
        intent = getIntent();
        final String idIncident = intent.getStringExtra("idIncident");
        final String quartier = intent.getStringExtra("quartier");
        final String description = intent.getStringExtra("description");
        final View customLayout = getLayoutInflater().inflate(R.layout.secouriste_popup, null);
        tvLieu = customLayout.findViewById(R.id.tvLieu);
        tvPrevisualiser = customLayout.findViewById(R.id.tvPresualiser);
        etDescription = customLayout.findViewById(R.id.etDescription);
        tvLieu.setText(quartier);
        etDescription.setText(description);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Infos sur l'incident");
        // set the custom layout
        builder.setView(customLayout);
        builder.setCancelable(true);
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Secourir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TraitementIncident traitement = new TraitementIncident();
                traitement.checkIfResponded(idIncident, new IResponse() {
                    @Override
                    public void getResult(int val) {
                        if (val == 0) {
                            Toast.makeText(MainSecouristeActivity.this, "Cet incident à été supprimé. ", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void getResult(boolean ok, String code) {
                        if (ok) {
                            Toast.makeText(MainSecouristeActivity.this, "Le sapeur pompier " + code + " est déjà sur l'opération.)", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            String idIncident1 = intent.getStringExtra("idIncident");
                            Incident incident = new Incident();
                            incident.setIdIncident(idIncident1);
                            SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(MainSecouristeActivity.this);
                            String code1 = preference.getString("Code", "000");
                            incident.setCodeSecouriste(code1);
                            incident.setStatus("Répondu");
                            traitement.updateIncident(incident, new IResult() {
                                @Override
                                public void getResult(int val) {
                                    if (val == 1) {
                                        String date;
                                        Date DateIncident = null;
                                        try {
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", new Locale("fr", "FR"));
                                            date = sdf.format(new Date());
                                            DateIncident = sdf.parse(date);
                                        } catch (Exception e) {
                                            Log.e("Erreur", e.getMessage());
                                        }
                                        db = FirebaseFirestore.getInstance();
                                        GeoFirestore geo = new GeoFirestore(db.collection("INTERVENTION"));
                                        DocumentReference newInterventionRef = db.collection("INTERVENTION").document();
                                        String idIntervention = newInterventionRef.getId();
                                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainSecouristeActivity.this).edit();
                                        if (locationComponent.getLastKnownLocation() != null) {
                                            final double Longitude = locationComponent.getLastKnownLocation().getLongitude();
                                            final double Latitude = locationComponent.getLastKnownLocation().getLatitude();
                                            Intervention intervention = new Intervention();

                                            String id = preference.getString("idIntervention", null);
                                            intervention.setCodeAgent(code1);
                                            intervention.setDateItervention(DateIncident);
                                            intervention.setIdIncident(idIncident);
                                            intervention.setLongitude(Longitude);
                                            intervention.setLatitude(Latitude);
                                            intervention.setIdIntervention(idIntervention);
                                            dialog.dismiss();
                                            android.app.AlertDialog alert = new SpotsDialog(MainSecouristeActivity.this);
                                            alert.show();
                                            alert.setMessage("Création d'intervention en cours...");
                                            TraitementIntervention traitementInt = new TraitementIntervention();
                                            traitementInt.demarrerIntervention(intervention, new IResult() {
                                                @Override
                                                public void getResult(int val) {
                                                    if (val == 1) {
                                                        //L'enrégistrement d'intervention a marché
                                                        editor.putString("idIntervention", idIntervention);
                                                        editor.commit();
                                                        alert.dismiss();
                                                        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainSecouristeActivity.this);
                                                        builder1.setCancelable(true);
                                                        builder1.setMessage("Etes-vous prêt pour démarrer une nouvelle intervention? ");
                                                        builder1.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Toast.makeText(MainSecouristeActivity.this, "Veuillez donc clicker sur l'icon de la personne a secourir.Vous avez une vie à sauver!!", Toast.LENGTH_SHORT).show();
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                        builder1.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                //Getroute
                                                                GeoFirestore geo = new GeoFirestore(db.collection("INCIDENT"));
                                                                geo.getLocation(idIncident, new GeoFirestore.LocationCallback() {
                                                                    @Override
                                                                    public void onComplete(@Nullable GeoPoint geoPoint, @Nullable Exception e) {
                                                                        if (e == null) {
                                                                            originPosition = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(), locationComponent.getLastKnownLocation().getLatitude());
                                                                            destinationPosition = Point.fromLngLat(geoPoint.getLongitude(), geoPoint.getLatitude());
                                                                            getRoute(originPosition, destinationPosition, new IResult() {
                                                                                @Override
                                                                                public void getResult(int val) {

                                                                                }

                                                                                @Override
                                                                                public void getResult(boolean ok) {
                                                                                    if (ok) {
                                                                                        dialog.dismiss();
                                                                                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                                                                                .directionsRoute(currentRoute)
                                                                                                .shouldSimulateRoute(true)
                                                                                                .build();
                                                                                        NavigationLauncher.startNavigation(MainSecouristeActivity.this, options);
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });

                                                            }
                                                        });
                                                        final AlertDialog dialog1 = builder1.create();
                                                        dialog1.show();
                                                    } else {
                                                        alert.dismiss();
                                                        //L'enrégistrement d'intervention n'a pas marché
                                                        Toast.makeText(MainSecouristeActivity.this, "Une erreure s'est produite.Veuillez ressayer. ", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void getResult(boolean ok) {

                                                }
                                            });
                                        } else {
                                            dialog.dismiss();
                                            Toast.makeText(MainSecouristeActivity.this, "Veuillez activer votre position puis réssayer.", Toast.LENGTH_SHORT).show();
                                        }


                                    } else {
                                        Toast.makeText(MainSecouristeActivity.this, "Une erreure s'est produite.Veuillez ressayer. ", Toast.LENGTH_SHORT).show();

                                    }
                                }

                                @Override
                                public void getResult(boolean ok) {

                                }
                            });

                        }
                    }
                });
            }
        });
        // create and show the alert dialog
        final AlertDialog dialog = builder.create();

        tvPrevisualiser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationComponent.getLastKnownLocation() != null) {

                    Point origin = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(), locationComponent.getLastKnownLocation().getLatitude());
                    GeoFirestore geo = new GeoFirestore(db.collection("INCIDENT"));
                    geo.getLocation(idIncident, (geoPoint, e) -> {
                        if (e == null) {
                            dialog.dismiss();
                            Point destination = Point.fromLngLat(geoPoint.getLongitude(), geoPoint.getLatitude());
                            getRoute(origin, destination, new IResult() {
                                @Override
                                public void getResult(int val) {

                                }

                                @Override
                                public void getResult(boolean ok) {
                                    if (ok) dialog.hide();
                                }
                            });
                            onBackPressed();
                        } else {
                            e.printStackTrace();
                        }

                    });
                } else {
                    Toast.makeText(MainSecouristeActivity.this, "Veuillez activer votre position. ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }


    //Classe de Listener pour la position
    private class MainActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainSecouristeActivity> activityWeakReference;

        MainActivityLocationCallback(MainSecouristeActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            MainSecouristeActivity activity = activityWeakReference.get();

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
                        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(MainSecouristeActivity.this);
                        final String idIntervention = preference.getString("idIntervention", null);
                        if (idIntervention != null) {

                            final double Latitude = location.getLatitude();
                            final double Longitude = location.getLongitude();
                            db = FirebaseFirestore.getInstance();
                            GeoFirestore geo = new GeoFirestore(db.collection("INTERVENTION"));
                            geo.setLocation(idIntervention, new GeoPoint(Latitude, Longitude), null, new GeoFirestore.CompletionListener() {
                                @Override
                                public void onComplete(Exception e) {

                                }
                            });
                        }
                        getIncidentInitialPosition();
                        updateIncidentPosition();
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
            MainSecouristeActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateIncidentPosition() {
        intent = getIntent();
        if (intent.hasExtra("idIncident")) {
            db = FirebaseFirestore.getInstance();
            GeoFirestore geo = new GeoFirestore(db.collection("INCIDENT"));
            final String idIncident = intent.getStringExtra("idIncident");
            try {
                geo.getLocation(idIncident, new GeoFirestore.LocationCallback() {
                    @Override
                    public void onComplete(@Nullable GeoPoint geoPoint, @Nullable Exception e) {
                        if (e == null) {
                            incidentUpdatedPosition = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());

                            if (incidentCurrentPosition != incidentUpdatedPosition) {
                                if (animator != null && animator.isStarted()) {
                                    incidentCurrentPosition = (LatLng) animator.getAnimatedValue();
                                    animator.cancel();
                                }

                                animator = ObjectAnimator
                                        .ofObject(latLngEvaluator, incidentCurrentPosition, incidentUpdatedPosition)
                                        .setDuration(2000);
                                final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
                                        new ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                                LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
                                                geoJsonSource.setGeoJson(Point.fromLngLat(animatedPosition.getLongitude(), animatedPosition.getLatitude()));
                                            }
                                        };
                                animator.addUpdateListener(animatorUpdateListener);
                                animator.start();

                                incidentCurrentPosition = incidentUpdatedPosition;


                            }
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainSecouristeActivity.this, "Erreur de mise a jour de la position", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void getIncidentInitialPosition() {
        intent = getIntent();
        if (intent.hasExtra("idIncident") && mapboxMap.getStyle().getImage("incident-marker") == null) {
            db = FirebaseFirestore.getInstance();
            GeoFirestore geo = new GeoFirestore(db.collection("INCIDENT"));
            final String idIncident = intent.getStringExtra("idIncident");

            try {


                geo.getLocation(idIncident, new GeoFirestore.LocationCallback() {
                    @Override
                    public void onComplete(@Nullable GeoPoint geoPoint, @Nullable Exception e) {
                        if (e == null) {
                            incidentCurrentPosition = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                            geoJsonSource = new GeoJsonSource("source-id",
                                    Feature.fromGeometry(Point.fromLngLat(incidentCurrentPosition.getLongitude(),
                                            incidentCurrentPosition.getLatitude())));
                            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    style.addSource(geoJsonSource);
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

    @Override
    public void onBackPressed() {
        intent = getIntent();
        if (intent.hasExtra("idIncident")) {
            showAlertDialog();
        } else super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {

        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(MainSecouristeActivity.this);
        final String idIntervention = preference.getString("idIntervention", null);
        if (idIntervention != null) {

            GeoFirestore geo = new GeoFirestore(db.collection("INTERVENTION"));
            geo.removeLocation(idIntervention);
        }
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.secouriste_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainSecouristeActivity.this);
        SharedPreferences.Editor editor = preferences.edit();
        intent = getIntent();
        final String idIncident,idIntervention;

        int id = item.getItemId();
        if (id == R.id.action_deconnexion) {
            editor.clear();
            editor.commit();
            startActivity(new Intent(MainSecouristeActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        if (id == R.id.action_terminer) {
            if(intent.hasExtra("idIncident")){
                idIncident = intent.getStringExtra("idIncident");
                idIntervention = intent.getStringExtra("idIntervention");
                TraitementIncident traitement = new TraitementIncident();
                traitement.endIncident(idIncident, new IResult() {
                    @Override
                    public void getResult(int val) {
                        if(val == 1){
                            Toast.makeText(MainSecouristeActivity.this, "Intervention terminée avec succès", Toast.LENGTH_SHORT).show();
                            GeoFirestore geo = new GeoFirestore(db.collection("INTERVENTION"));
                            geo.removeLocation(idIntervention);
                            finish();
                            startActivity(getIntent());
                        }
                    }

                    @Override
                    public void getResult(boolean ok) {

                    }
                });
              return true;
            }else{
                Toast.makeText(MainSecouristeActivity.this, "Aucun traitement n'est en cours", Toast.LENGTH_SHORT).show();
                return false;
            }

        }

        return super.onOptionsItemSelected(item);
    }
}
