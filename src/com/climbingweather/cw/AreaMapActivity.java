package com.climbingweather.cw;

//import com.google.maps;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class AreaMapActivity extends MapActivity {
	
	private MapController mapController;
	private MapView mapView;
	private GeoPoint areaPoint;
	//private LocationManager locationManager;

	public void onCreate(Bundle bundle) {
		
		super.onCreate(bundle);
		setContentView(R.layout.area_map); // bind the layout to the activity

		// create a map view
		//RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.mainlayout);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.setStreetView(true);
		mapController = mapView.getController();
		mapController.setZoom(14); // Zoon 1 is world view
		
		Integer latitude = 38000000; // new Integer(38.060000 * 1000000);
		Integer longitude = -81120000; // -81.120000 * 1000000;
		areaPoint = new GeoPoint(latitude, longitude);
		mapController.setCenter(areaPoint);
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.climbing);
		AreaMapItemizedOverlay itemizedoverlay = new AreaMapItemizedOverlay(drawable, this);
		
		OverlayItem marker = new OverlayItem(areaPoint, "My Point", "Test");
		marker.getTitle();
		itemizedoverlay.addOverlay(marker);
		mapOverlays.add(itemizedoverlay);
		
		/*
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.dunno);
		OverlayItem marker = new OverlayItem(areaPoint, "My Point", "Test");
		mapOverlays.add(marker);
		*/
		
		
		//locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
		//		0, new GeoUpdateHandler());
	}
	
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
}
