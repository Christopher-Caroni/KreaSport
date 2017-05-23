package com.ccaroni.kreasport.map.viewmodels;

import android.widget.Toast;

import com.ccaroni.kreasport.data.realm.RealmCheckpoint;
import com.ccaroni.kreasport.map.views.CustomOverlayItem;

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
     *
     * @param checkpoint the checkpoint to add a geofence for
     */
    void addGeoFence(RealmCheckpoint checkpoint);

    /**
     * Notifies the {@link android.content.Context} that the next checkpoint should be revealed.
     * @param nextCheckpoint the checkpoint as a {@link CustomOverlayItem}
     */
    void revealNextCheckpoint(CustomOverlayItem nextCheckpoint);
}