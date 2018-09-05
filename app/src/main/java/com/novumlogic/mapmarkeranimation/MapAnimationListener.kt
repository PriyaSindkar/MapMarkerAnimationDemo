package com.novumlogic.mapmarkeranimation

import com.google.android.gms.maps.model.LatLng

interface MapAnimationListener {
    fun onPauseClicked(pausePosition: LatLng?)

    fun onUpdateMapRoute(updatePosition: LatLng?, animationProgress: Int)

    fun onResumeClicked()

    fun showHideAnimationControls(isShow: Boolean)
}