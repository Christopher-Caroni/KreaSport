package com.ccaroni.kreasport.race.services;

import android.app.Notification;

import com.ccaroni.kreasport.data.dummy.DummyCheckpoint;

/**
 * Created by Master on 02/02/2018.
 */

public interface IRaceService {

    void startLocationUpdates();

    void stopLocationUpdates();

    void addGeofence(DummyCheckpoint dummyCheckpoint);

    void removeAllGeofences();

    /**
     * @return the updated notification
     */
    Notification updateRelevantNotificationFields();

    /**
     * Creates a new notification and sends it out
     */
    void sendUpdatedNotification();

}
