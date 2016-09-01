package uiuc.mbr;

import android.location.Location;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback
{

	private GoogleMap map;
	private final LocationRequest locationRequest = new LocationRequest();
	private final GoogleCallbacks googleCallbacks = new GoogleCallbacks();
	private GoogleApiClient googleClient;
	/**Public for testing only*/
	public final LocationHandler locationHandler = new LocationHandler();

	public Marker userLocationMarker;




	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}






	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap)
	{
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		map = googleMap;

		LatLng debugPos = new LatLng(0, 0);
		userLocationMarker = map.addMarker(new MarkerOptions().position(debugPos).title("You are here."));

		CumtdApi api = new CumtdApi("https://developer.cumtd.com/api/v2.2/JSON", "c4d5e4bb2baa48ba85772b857c9839c8");
				PolylineOptions line = new PolylineOptions();
		List<String> list = new ArrayList<String>();
		try {
				list = api.getShapeCoords("22N ILLINI 10");
		} catch (Exception e) {}
		line.width(5);
		for (int i = 0; i < list.size(); i = i + 2) {
				line.add(new LatLng(Double.parseDouble(list.get(i)), Double.parseDouble(list.get(i + 1))));
		}
		map.addPolyline(line);

		map.moveCamera(CameraUpdateFactory.zoomTo(14));
		map.moveCamera(CameraUpdateFactory.newLatLng(debugPos));

		googleClient = new GoogleApiClient.Builder(getApplicationContext())
				.addApi(LocationServices.API)
				.addConnectionCallbacks(googleCallbacks)
				.build();
		googleClient.connect();
	}



	/**Handles connecting to the Google APIs when the activity starts.*/
	private class GoogleCallbacks implements GoogleApiClient.ConnectionCallbacks
	{
		@Override
		public void onConnected(Bundle bundle) throws SecurityException
		{
			//request a location update every 30 seconds
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//GPS, not network-based location
			locationRequest.setInterval(30 * 1000);
			LocationServices.FusedLocationApi.requestLocationUpdates(googleClient, locationRequest, locationHandler);
		}

		@Override
		public void onConnectionSuspended(int i){}
	}


	/**Handles location updates.*/
	public class LocationHandler implements LocationListener
	{
		@Override
		public void onLocationChanged(Location location)
		{
			LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
			userLocationMarker.setPosition(ll);
			map.moveCamera(CameraUpdateFactory.newLatLng(ll));
		}
	}
}