package com.ccaroni.kreasport.race;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.ccaroni.kreasport.BR;
import com.ccaroni.kreasport.data.RealmHelper;
import com.ccaroni.kreasport.data.realm.RealmCheckpoint;
import com.ccaroni.kreasport.data.realm.RealmRace;
import com.ccaroni.kreasport.data.realm.RealmRiddle;
import com.ccaroni.kreasport.map.views.CustomOverlayItem;
import com.ccaroni.kreasport.race.interfaces.IRaceVM;
import com.ccaroni.kreasport.utils.Constants;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

/**
 * Created by Master on 20/08/2017.
 */

public class RaceVM extends IRaceVM {

    private static final String TAG = RaceVM.class.getSimpleName();


    private List<CustomOverlayItem> overlayItems;


    public RaceVM(Context context) {
        if (context instanceof IRaceView) {
            this.raceView = (IRaceView) context;
        } else {
            throw new RuntimeException(context + " must implement " + IRaceView.class.getSimpleName());
        }

        raceActive = false;

        changeVisibilitiesOnRaceState();
    }

    /**
     * Updates the bindings for the whole bottom sheet visibilities and fab visibilities. NOT the data in the bottom sheet
     */
    protected void changeVisibilitiesOnRaceState() {
        passiveInfoVisibility = raceActive ? View.GONE : View.VISIBLE;
        activeInfoVisibility = raceActive ? View.VISIBLE : View.GONE;

        fabStartVisibility = !raceActive && RaceHolder.getInstance().isRaceSelected() ? View.VISIBLE : View.GONE;
        bottomSheetVisibility = RaceHolder.getInstance().isRaceSelected() ? View.VISIBLE : View.GONE;

        fabMyLocationAnchoredStartVisibility = fabStartVisibility == View.VISIBLE ? View.VISIBLE : View.GONE;
        fabMyLocationAnchoredBottomSheetVisibility = bottomSheetVisibility == View.VISIBLE && fabStartVisibility == View.GONE ? View.VISIBLE : View.GONE;
        fabMyLocationCornerVisibility = bottomSheetVisibility == View.GONE ? View.VISIBLE : View.GONE;


        notifyChange();
    }


    @Override
    protected void updateBottomSheetData(CustomOverlayItem selectedItem) {
        if (raceActive) {
            updateFromActiveState(selectedItem);
        } else {
            updateFromInactiveState(selectedItem);
        }
    }

    /**
     * Called only when raceActive
     *
     * @param selectedItem
     */
    private void updateFromActiveState(CustomOverlayItem selectedItem) {
        if (!raceActive) {
            throw new IllegalStateException("This method is only supposed to be called from a raceActive state");
        }

        if (selectedItem.isPrimary()) {
            Log.d(TAG, "selected the race marker of the ongoing race");
            setTitle(RaceHolder.getInstance().getCurrentRaceTitle());
            setDescription(RaceHolder.getInstance().getCurrentRaceDescription());
        } else {
            Log.d(TAG, "selected checkpoint of same race");
            RaceHolder.getInstance().updateCurrentCheckpoint(selectedItem.getId());
            setTitle(RaceHolder.getInstance().getCurrentCheckpointTitle());
            setDescription(RaceHolder.getInstance().getCurrentCheckpointDescription());
        }
    }

    /**
     * Called only when !raceActive
     *
     * @param selectedItem
     */
    private void updateFromInactiveState(CustomOverlayItem selectedItem) {
        if (raceActive) {
            throw new IllegalStateException("This method is only supposed to be called from a !raceActive state");
        }

        if (RaceHolder.getInstance().getCurrentRaceId().equals(selectedItem.getRaceId())) {
            Log.d(TAG, "selected same race");
        } else {
            Log.d(TAG, "selected different race: " + selectedItem.getRaceId());

            RaceHolder.getInstance().updateCurrentRace(selectedItem.getRaceId());
            setTitle(RaceHolder.getInstance().getCurrentRaceTitle());
            setDescription(RaceHolder.getInstance().getCurrentRaceDescription());

            changeVisibilitiesOnRaceState(); // call this to restore the fab and bottom sheet if no item was previously selected
        }
    }

    protected boolean validateProximityToStart() {
        boolean validStart = false;

        Location lastLocation = raceView.getLastKnownLocation();
        Location raceLocation = RaceHolder.getInstance().getCurrentRaceLocation();

        float distance = lastLocation.distanceTo(raceLocation);

        if (distance > Constants.MINIMUM_DISTANCE_TO_START_RACE) {
            Log.d(TAG, "User was " + distance + "m away from start. Too far by " + (distance - Constants.MINIMUM_DISTANCE_TO_START_RACE) + "m");
            raceView.toast("You are too far to start this race");
        } else {
            Log.d(TAG, "User was " + distance + "m away from start. Inside by " + (Constants.MINIMUM_DISTANCE_TO_START_RACE - distance) + "m");
            validStart = true;
        }

        return validStart;
    }

    protected void startRace() {

        final long timeStart = SystemClock.elapsedRealtime();

        RaceHolder.getInstance().startRace(timeStart);

        raceActive = true;
        changeVisibilitiesOnRaceState();

        raceView.focusOnRace(getOverlayItems());

        RealmCheckpoint targetingCheckpoint = RaceHolder.getInstance().getTargetingCheckpoint();
        triggerNextGeofence(targetingCheckpoint);

        raceView.startChronometer(timeStart);

        raceView.toast("Race started");
    }


    /**
     * Calls the {@link #raceView} to add a geofence for the targeting checkpoint
     */
    protected void triggerNextGeofence(RealmCheckpoint targetingCheckpoint) {
        raceView.addGeoFence(targetingCheckpoint);
    }

    protected void revealNextCheckpoint(RealmCheckpoint targetingCheckpoint) {
        raceView.revealNextCheckpoint(targetingCheckpoint.toCustomOverlayItem());
    }

}
