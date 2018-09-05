package com.novumlogic.mapmarkeranimation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.animation.LinearInterpolator
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions


class MapRouteAnimator {
    private var foregroundPolyline: Polyline? = null
    private var backgroundPolyline: Polyline? = null
    private var routeAnimationSet: AnimatorSet? = null
    private var lastPosition: LatLng? = null

    lateinit var firstRunRouteAnimator: ObjectAnimator
    lateinit var mapAnimationListener: MapAnimationListener

    private object Holder {
        val INSTANCE = MapRouteAnimator()
    }

    companion object {
        val instance: MapRouteAnimator by lazy { Holder.INSTANCE }
    }

    fun animateRoute(progressStartValue: Int) {
        routeAnimationSet = if (routeAnimationSet == null) {
            AnimatorSet()
        } else {
            routeAnimationSet?.removeAllListeners()
            routeAnimationSet?.end()
            routeAnimationSet?.cancel()
            AnimatorSet()
        }

        // To re-trace the route when the vehicle is stopped
        val rerunRouteAnimator = ValueAnimator.ofInt(progressStartValue, 100)
        rerunRouteAnimator.duration = 9000
        rerunRouteAnimator.interpolator = LinearInterpolator()


        // To Update the marker header for the route being traced
        rerunRouteAnimator.addUpdateListener { animation ->
            val foregroundPoints = foregroundPolyline?.points
            val percentageValue = animation.animatedValue as Int
            val pointcount = foregroundPoints?.size
            val countTobeRemoved = (pointcount!! * (percentageValue / 100f)).toInt()
            val subListTobeRemoved = foregroundPoints.subList(0, countTobeRemoved)
            subListTobeRemoved.clear()

            backgroundPolyline?.points = foregroundPoints
            if (foregroundPoints.isNotEmpty()) {
                lastPosition = foregroundPoints[0]
                mapAnimationListener.onUpdateMapRoute(foregroundPoints[0], percentageValue)
            }
        }

        /**
         *
         * Listeners to handle pause and resume of the map route trace animation
         */
        routeAnimationSet?.addPauseListener(object : Animator.AnimatorPauseListener {
            override fun onAnimationPause(animator: Animator) {
                mapAnimationListener.onPauseClicked(lastPosition)
            }

            override fun onAnimationResume(animator: Animator) {
                mapAnimationListener.onResumeClicked()
            }
        })

        firstRunRouteAnimator.duration = 2000
        routeAnimationSet?.play(rerunRouteAnimator)
        routeAnimationSet?.start()
    }

    fun drawInitialPath(googleMap: GoogleMap, vehicleRoute: List<LatLng>, routeColor: Int) {
        val optionsFirstRun = PolylineOptions().add(vehicleRoute[0]).color(routeColor).width(8f)
        foregroundPolyline = googleMap.addPolyline(optionsFirstRun)

        val optionsRerun = PolylineOptions().add(vehicleRoute[0]).color(Color.GRAY).width(8f)
        backgroundPolyline = googleMap.addPolyline(optionsRerun)

        firstRunRouteAnimator = ObjectAnimator.ofObject(this, "routeIncreaseForward", RouteEvaluator(), *vehicleRoute.toTypedArray())
        firstRunRouteAnimator.interpolator = LinearInterpolator()
        firstRunRouteAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                foregroundPolyline?.points = backgroundPolyline?.points
                mapAnimationListener.showHideAnimationControls(true)
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        firstRunRouteAnimator.duration = 5000
        firstRunRouteAnimator.start()
    }

    fun pauseAimation() {
        routeAnimationSet?.pause()
    }

    fun resumeAnimation() {
        routeAnimationSet?.resume()
    }

    fun onAnimationProgressChanged(progress: Int) {
        animateRoute(progress)
    }

    /**
     * This will be invoked by the ObjectAnimator multiple times. Mostly every 16ms.
     */
    fun setRouteIncreaseForward(endLatLng: LatLng) {
        val foregroundPoints = backgroundPolyline?.points
        foregroundPoints?.add(endLatLng)
        backgroundPolyline?.points = foregroundPoints
    }
}