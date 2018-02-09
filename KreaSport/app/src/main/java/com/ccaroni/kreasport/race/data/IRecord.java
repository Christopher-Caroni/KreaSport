package com.ccaroni.kreasport.race.data;

/**
 * Created by Master on 09/02/2018.
 */

public interface IRecord {
    String getId();

    String getRaceId();

    String getUserId();

    long getTimeExpired();

    String getDateTime();
}
