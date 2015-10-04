package fi.ptm.locationexample;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by PTM on 04/10/15.
 */
public class MainActivity extends Activity implements
    ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private String mLastUpdateTime;
    private AddressResultReceiver mResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Connected to Google Play services!

        // Request the last known location
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            TextView textView2 = (TextView) findViewById(R.id.textView2);
            TextView textView3 = (TextView) findViewById(R.id.textView3);
            textView2.setText("Latitude: " + String.valueOf(mLastLocation.getLatitude()));
            textView3.setText("Longitude: "+ String.valueOf(mLastLocation.getLongitude()));
        }

        // geocoding address receiver
        mResultReceiver = new AddressResultReceiver(new Handler());

        // Request location updates
        createLocationRequest();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection has been interrupted.
        Toast.makeText(getApplicationContext(),"Google Play Services connection suspended!",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        Toast.makeText(getApplicationContext(),"Google Play Services connection failed!",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
        stopLocationUpdates();
        super.onStop();
    }

    /** Request Location Updates below this comment **/

    protected void createLocationRequest() {
        // create a location request object
        mLocationRequest = new LocationRequest();
        // update interval in milliseconds
        mLocationRequest.setInterval(10000);
        // fastest update interval in this app (5 sec)
        mLocationRequest.setFastestInterval(5000);
        // priority, the most precise location possible
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        TextView textView5 = (TextView) findViewById(R.id.textView5);
        TextView textView6 = (TextView) findViewById(R.id.textView6);
        TextView textView7 = (TextView) findViewById(R.id.textView7);
        textView5.setText("Time: " + mLastUpdateTime);
        textView6.setText("Latitude: " + String.valueOf(mLastLocation.getLatitude()));
        textView7.setText("Longitude: " + String.valueOf(mLastLocation.getLongitude()));
        // start intent service to fetch location address
        startIntentService();
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    protected void startIntentService() {
        TextView textView9 = (TextView) findViewById(R.id.textView9);
        textView9.setText("Fetching address...");
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    // Receiver for data sent from FetchAddressIntentService
    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        // receives data sent from FetchAddressIntentService
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            TextView textView9 = (TextView) findViewById(R.id.textView9);
            textView9.setText(resultData.getString(Constants.RESULT_DATA_KEY));
        }
    }
}
