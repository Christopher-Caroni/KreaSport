package com.ccaroni.kreasport.view.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.ccaroni.kreasport.R;
import com.ccaroni.kreasport.data.RealmHelper;
import com.ccaroni.kreasport.data.realm.DownloadedArea;
import com.ccaroni.kreasport.map.models.MapOptions;
import com.ccaroni.kreasport.map.viewmodels.MapVM;
import com.ccaroni.kreasport.map.views.CustomMapView;
import com.ccaroni.kreasport.utils.Constants;
import com.ccaroni.kreasport.utils.CustomCacheManagerCallback;
import com.ccaroni.kreasport.utils.Utils;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.modules.SqliteArchiveTileWriter;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.threeten.bp.OffsetDateTime;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class AreaSelectionActivity extends AppCompatActivity implements CustomMapView.MapViewCommunication {

    private static final String LOG = AreaSelectionActivity.class.getSimpleName();

    private static final String KEY_BASE = Utils.getBaseString(AreaSelectionActivity.class.getSimpleName()) + "keys.";
    public static final String KEY_AREA_ID = KEY_BASE + "area_id";


    private CustomMapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_area_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupMap();

        setButtonActions();
    }

    private void setupMap() {

        MapOptions mMapOptions = new MapOptions()
                .setEnableLocationOverlay(true)
                .setEnableMultiTouchControls(true)
                .setEnableScaleOverlay(true);

        MapVM mMapVM = new MapVM(new GeoPoint(50.633621, 3.0651845), 9);

        // needs to be called before the MapView is created to enable hw acceleration
        Configuration.getInstance().setMapViewHardwareAccelerated(true);

        mMapView = new CustomMapView(this, mMapOptions, mMapVM);


        mMapView.setMinZoomLevel(Constants.DOWNLOAD_MIN_ZOOM);
        mMapView.setMaxZoomLevel(Constants.DOWNLOAD_MAX_ZOOM);
        /*
            From levels 11 to 17, the max download size is approx. 150MB
            From levels 11 to max, the max download size is approx. 25000MB
            OSM policy prohibits mass downloading and downloading past zoom level 17 without special access.
            See https://operations.osmfoundation.org/policies/tiles/
         */

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.map_relative_layout);
        relativeLayout.addView(mMapView);
    }

    private void setButtonActions() {
        Button cancel = (Button) findViewById(R.id.btn_cancel_area_download);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, null);
                finish();
            }
        });

        Button download = (Button) findViewById(R.id.btn_confirm_area_download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });
    }

    /**
     * Initiates the download process by getting the download path, size, notificaiton...
     */
    private void startDownload() {
        BoundingBox currentBB = mMapView.getBoundingBox();

        String locationName = getLocationName();
        String absoluteFilePath = getUniquePath(locationName);

        Log.d(LOG, "Selected area: " + locationName);
        Log.d(LOG, "Will download to: " + absoluteFilePath);

        SqliteArchiveTileWriter writer = null;
        try {
            writer = new SqliteArchiveTileWriter(absoluteFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CacheManager mgr = new CacheManager(mMapView, writer);

        CustomCacheManagerCallback customCacheManagerCallback = new CustomCacheManagerCallback(this, writer, locationName);

        mgr.downloadAreaAsyncNoUI(this, currentBB, mMapView.getZoomLevel(), mMapView.getMaxZoomLevel(), customCacheManagerCallback);

        closeActivity(currentBB, locationName, absoluteFilePath);
    }

    /**
     * Puts the necessary data in an Intent for {@link OfflineAreasActivity} and calls {@link #finish()}
     *
     * @param currentBB
     * @param locationName
     * @param absoluteFilePath
     */
    private void closeActivity(BoundingBox currentBB, String locationName, String absoluteFilePath) {
        double estimatedSize = Utils.getDownloadSizeForBoundingBox(currentBB, mMapView);
        int roundedSize = (int) Math.round(estimatedSize);

        RealmHelper.getInstance(this).beginTransaction();

        DownloadedArea area = RealmHelper.getInstance(this).createRealmObject(DownloadedArea.class)
                .setDateDownloaded(OffsetDateTime.now().toString())
                .setName(locationName)
                .setPath(absoluteFilePath)
                .setSize(estimatedSize);

        RealmHelper.getInstance(this).commitTransaction();

        Intent response = new Intent();
        response.putExtra(KEY_AREA_ID, area.getId());

        setResult(RESULT_OK, response);
        finish();
    }

    /**
     * @return either "New Area" or {@link Address#getLocality()} for the address associated to the center of the map.
     */
    private String getLocationName() {
        String locationName = null;
        GeoPoint center = (GeoPoint) mMapView.getMapCenter();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            Address address = geocoder.getFromLocation(center.getLatitude(), center.getLongitude(), 1).get(0);

            locationName = address.getLocality();

        } catch (IOException e) {
            locationName = "New Area";
        }
        return locationName;
    }

    /**
     * Gets a unique filename like <b>sdcard/osmdroid/ouputName_GENERATED_NUMBER.sqlite</b>.<br>
     * Uses {@link File#createTempFile(String, String, File)}
     *
     * @param locationName the original name of what we want to download
     * @return either a unique filename or sdcard/osmdroid/locationName.sqlite
     */
    private String getUniquePath(final String locationName) {
        final String outputName = locationName.replaceAll("\\s", "_");
        final String extension = ".sqlite";

        String baseDirectoryString = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "osmdroid" + File.separator;
        File baseDirectory = new File(baseDirectoryString);

        try {
            File file = File.createTempFile(outputName, extension, baseDirectory);
            String absolutePath = file.getAbsolutePath();
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            return absolutePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baseDirectoryString + outputName + extension;
    }

    @Override
    public void onMapBackgroundTouch() {

    }
}