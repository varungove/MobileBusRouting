package uiuc.mbr.events;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class LocationLookup
{
	private LocationLookup(){throw new UnsupportedOperationException();}


    /**Uses Google's Geocoding API to look up a user-entered location string.
	 * Returns the best result Google could find, or null if the location wasn't understood (or null).*/
    @Nullable public static LatLng lookupLocation(String location, Context c) {
		if(location == null)
			return null;

        Geocoder geocoder = new Geocoder(c, Locale.getDefault());
        List<Address> addresses = new ArrayList<Address>() {};
        try {
            addresses = geocoder.getFromLocationName(location, 1, 39.47, -88.95, 40.49, -87.43); //Champaign area coords
        } catch (IOException e) {throw new RuntimeException(e);}

		if(addresses == null || addresses.isEmpty())
			return null;

        Address address = addresses.get(0);
        return new LatLng(address.getLatitude(), address.getLongitude());
    }
}
