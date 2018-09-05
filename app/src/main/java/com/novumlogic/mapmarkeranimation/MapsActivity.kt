package com.novumlogic.mapmarkeranimation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, MapAnimationListener {
    private var mGoogleMap: GoogleMap? = null
    private val POINT_A = LatLng(22.3064492, 73.1794404)
    private val POINT_B = LatLng(22.3039422, 73.1827233)
    private var vehicleRoute: ArrayList<LatLng> = ArrayList()
    private var thisMarker: Marker? = null
    private var isPlay = true
    private var isResume = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        showHideAnimationControls(false)
        MapRouteAnimator.instance.mapAnimationListener = this

        fabPlay.setOnClickListener {
            when {
                isPlay -> {
                    showHideAnimationControls(true)
                    isPlay = false
                    fabPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause))
                    startAnim()
                }
                isResume -> {
                    isResume = false
                    fabPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause))
                    MapRouteAnimator.instance.resumeAnimation()
                }
                else -> {
                    fabPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow))
                    isResume = true
                    MapRouteAnimator.instance.pauseAimation()
                }
            }
        }

        animateProgressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MapRouteAnimator.instance.onAnimationProgressChanged(progress)
                }
                if (progress == 99) {
                    onMapRouteAnimCompleted()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onMapReady(aGoogleMap: GoogleMap) {
        mGoogleMap = aGoogleMap
        initRouteCoordinates()
        mGoogleMap!!.setOnMapLoadedCallback {
            initMapCamera()
        }
    }

    private fun initMapCamera() {
        val builder = LatLngBounds.Builder()
        builder.include(POINT_A)
        builder.include(POINT_B)
        val bounds = builder.build()
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 200)

        mGoogleMap!!.moveCamera(cu)
        mGoogleMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f), 2000, null)
        MapRouteAnimator.instance.drawInitialPath(mGoogleMap!!, vehicleRoute, ContextCompat.getColor(this, R.color.colorAccent))
    }

    private fun startAnim() {
        if (mGoogleMap != null) {
            if (thisMarker != null) {
                thisMarker?.remove()
                thisMarker = null
            }
            MapRouteAnimator.instance.animateRoute(0)
        } else {
            Toast.makeText(applicationContext, "Something went wrong with map.", Toast.LENGTH_LONG).show()
        }
    }

    private fun initRouteCoordinates() {
        vehicleRoute.add(LatLng(22.3064492, 73.1794404))
        vehicleRoute.add(LatLng(22.306369, 73.179021))
        vehicleRoute.add(LatLng(22.305654, 73.179504))
        vehicleRoute.add(LatLng(22.304870, 73.180008))
        vehicleRoute.add(LatLng(22.303570, 73.180040))
        vehicleRoute.add(LatLng(22.303754, 73.181016))
        vehicleRoute.add(LatLng(22.303555, 73.182046))
        vehicleRoute.add(LatLng(22.303088, 73.183784))
        vehicleRoute.add(LatLng(22.304974, 73.183548))
        vehicleRoute.add(LatLng(22.306587, 73.183951))
        vehicleRoute.add(LatLng(22.305535, 73.187925))
        vehicleRoute.add(LatLng(22.305307, 73.189234))
        vehicleRoute.add(LatLng(22.306587, 73.189084))
        vehicleRoute.add(LatLng(22.3065919, 73.1868953))
        vehicleRoute.add(LatLng(22.307771, 73.189670))
        vehicleRoute.add(LatLng(22.3074808, 73.1895576))
    }

    override fun onPauseClicked(pausePosition: LatLng?) {
        thisMarker?.showInfoWindow()
        mGoogleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(thisMarker?.position).zoom(18f).build()))
    }

    override fun onUpdateMapRoute(updatePosition: LatLng?, animationProgress: Int) {
        if (thisMarker == null) {
            val markerOptions = MarkerOptions().position(updatePosition!!)
            markerOptions.title("${updatePosition.latitude}, ${updatePosition.longitude}")
            markerOptions?.icon(getMarkerIconFromDrawable(ContextCompat.getDrawable(this, R.drawable.circle_marker)!!))
            thisMarker = mGoogleMap?.addMarker(markerOptions)
        } else {
            thisMarker?.position = updatePosition
            thisMarker?.title = "${updatePosition!!.latitude}, ${updatePosition.longitude}"
            thisMarker?.setIcon(getMarkerIconFromDrawable(ContextCompat.getDrawable(this, R.drawable.circle_marker)!!))
        }
        animateProgressSeekBar.progress = animationProgress
//        mGoogleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(updatePosition).zoom(20f).build()))
    }

    override fun onResumeClicked() {
        thisMarker?.hideInfoWindow()
        mGoogleMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f), 2000, null)
    }

    private fun onMapRouteAnimCompleted() {
        animateProgressSeekBar.progress = 0
        isPlay = true
        fabPlay.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow))
        showHideAnimationControls(false)
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun showHideAnimationControls(isShow: Boolean) {
        if (isShow) {
            fabPlay.show()
            animateProgressSeekBar.visibility = View.VISIBLE
        } else {
            animateProgressSeekBar.visibility = View.GONE
        }
    }
}
