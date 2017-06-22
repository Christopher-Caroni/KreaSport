package com.ccaroni.kreasport.race;

import android.widget.Toast;

import com.ccaroni.kreasport.data.dto.Riddle;
import com.ccaroni.kreasport.data.realm.RealmCheckpoint;
import com.ccaroni.kreasport.map.views.CustomOverlayItem;

import org.osmdroid.util.GeoPoint;

/**
 * Created by Master on 22/05/2017.
 * This interface permits communication to the Model in the MVVM.
 */
public interface RaceCommunication {

    /**
     * Notification to start counting the time from newBase.
     *
     * @param newBase
     */
    void startChronometer(final long newBase);

    /**
     * Notification to stop whatever is tracking the time.
     */
    void stopChronometer();

    /**
     * Display a message to the user, preferably through a {@link Toast}.
     *
     * @param message
     */
    void toast(String message);

    /**
     * @param checkpoint the checkpoint to add a geofence for
     */
    void addGeoFence(RealmCheckpoint checkpoint);

    /**
     * Notifies the {@link android.content.Context} that the next checkpoint should be revealed.
     *
     * @param nextCheckpoint the checkpoint as a {@link CustomOverlayItem}
     */
    void revealNextCheckpoint(CustomOverlayItem nextCheckpoint);

    /**
     * Once the checkpoint has been validated by geofence, the user needs to answer the riddle.
     * This needs to be called from the RaceVM to prevent any tampering.
     *
     * @param riddle
     */
    void askRiddle(Riddle riddle);

    /**
     * Notifies the {@link android.content.Context} that the user is requesting his location to be centered on.
     */
    void onMyLocationClicked();

    void unsetFocusedItem();

    /**
     * The {@link RaceVM} calls this to ask if we need to animate to the start.<br>
     * It's used because if we do, we need to postpone the start while waiting for the animation to end.
     * Otherwise, if this returns false, we can start right away.
     *
     * @param startPoint the point we need to theoretically animate to
     * @return if an animation will be needed
     */
    boolean needToAnimateToStart(GeoPoint startPoint);

    /**
     * Call by the {@link RaceVM} for a user interaction to confirm that the race should be stopped in the middle of its progression.
     */
    void confirmStopRace();
}