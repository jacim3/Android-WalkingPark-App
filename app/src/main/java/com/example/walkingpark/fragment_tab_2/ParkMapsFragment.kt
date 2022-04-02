package com.example.walkingpark.fragment_tab_2

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.walkingpark.MainActivity
import com.example.walkingpark.R
import com.example.walkingpark.databinding.FragmentParkmapsBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/*
*  뷰바인딩 사용 안함
*/
class ParkMapsFragment : Fragment(), OnMapReadyCallback{

    private lateinit var viewModel: ParkMapsViewModel
    private lateinit var googleMap:GoogleMap
    private var binding:FragmentParkmapsBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParkmapsBinding.inflate(layoutInflater, container, false)
        binding!!.mapFragment.onCreate(savedInstanceState)
        binding!!.mapFragment.getMapAsync(this)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ParkMapsViewModel::class.java]
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

    }


    override fun onStart() {
        super.onStart()
        binding!!.mapFragment.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding!!.mapFragment.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding!!.mapFragment.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding!!.mapFragment.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = ParkMapsFragment()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        val seoul = LatLng(37.56, 126.97);
        val markerOptions= MarkerOptions()
        markerOptions.position(seoul);
        markerOptions.title("서울");
        markerOptions.snippet("수도");

        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(seoul))
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(12f))
    }
}