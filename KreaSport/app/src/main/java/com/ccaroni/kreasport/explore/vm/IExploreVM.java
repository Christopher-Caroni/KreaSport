package com.ccaroni.kreasport.explore.vm;

import android.support.annotation.NonNull;

/**
 * Created by Master on 17/02/2018.
 * Contract that the VM implements, therefore exposing the data to the view as well as exposing listeners for UI events.
 */
public interface IExploreVM {

    /**
     * @return the visible state of the bottom sheet
     */
    int getBottomSheetVisibility();

    /**
     * @return the visible state of the passive bottom sheet header
     */
    int getPassiveInfoVisibility();

    /**
     * @return the visible state of the active bottom sheet header
     */
    int getActiveInfoVisibility();

    /**
     * @return the checkpoint progression
     */
    @NonNull
    String getProgression();

    /**
     * @return the title cf the selected item
     */
    @NonNull
    String getTitle();

    /**
     * @return the description of the selected item
     */
    @NonNull
    String getDescription();

    /**
     * Invoked when the user clicks on the start race button. Triggers an attempt to start a recording for the race corresponding to the selected item.
     */
    void onStartClicked();

    /**
     * Invoked when the user clicks on the stop race button. Triggers an attempt to stop the current recording.
     */
    void onStopClicked();

    /**
     * Invoked when the user clicks on the my location button.
     */
    void onMyLocationClicked();

    /**
     * @param id the id of the race clicked on
     */
    void onRaceSelected(long id);

    /**
     * @param id the id of the checkpoint clicked on
     */
    void onCheckpointSelected(long id);

    void onBackgroundPressed();

}
