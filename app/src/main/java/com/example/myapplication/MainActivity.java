package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.cities.CityProvider;
import com.example.myapplication.cities.NearestCity;
import com.example.myapplication.countires.Countries;
import com.example.myapplication.weather.DataUpdater;
import com.example.myapplication.weather.Weather;

import java.security.SecureRandom;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private final SecureRandom secureRandom = new SecureRandom();
    @BindView(R.id.location_remote)
    TextView location_remote;
    @BindView(R.id.clock_remote)
    TextClock clock_remote;
    @BindView(R.id.date_remote)
    TextClock date_remote;
    @BindView(R.id.icon_remote)
    ImageView icon_remote;
    @BindView(R.id.temperature_remote)
    TextView temperature_remote;
    @BindView(R.id.precipitation_remote)
    TextView precipitation_remote;
    @BindView(R.id.weather_summary_remote)
    TextView weather_summary_remote;
    private Weather weather;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        loadCityDetails();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadCityDetails() {
        new AsyncTask<Void, Void, NearestCity>() {
            private ProgressDialog pd;
            public double doubleRandom(double max, double min) {
                double r = secureRandom.nextDouble();
                if (r < 0.5) {
                    return ((1 - secureRandom.nextDouble()) * (max - min) + min);
                }
                return (secureRandom.nextDouble() * (max - min) + min);
            }


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pd = new ProgressDialog(MainActivity.this);
                pd.setMessage("Loading...");
                pd.setCancelable(false);
                pd.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
            }

            @Override
            protected NearestCity doInBackground(Void... voids) {
                NearestCity city = null;
                do {
                    double lat = doubleRandom(90.0, -90.0);
                    double lon = doubleRandom(180.0, -180.0);
                    city = CityProvider.getCity(MainActivity.this, lat, lon, CityProvider.Solution.SERGEY);
                }
                while (city == null);

                return city;
            }

            @Override
            protected void onPostExecute(NearestCity city) {
                showWeatherAndDetails(city);
                if (pd.isShowing()) {
                    pd.dismiss();
                }
            }
        }.execute();
    }

    @SuppressLint("SetTextI18n")
    private void showWeatherAndDetails(NearestCity city) {

        String timezone = city.getTimeZone();

        System.out.println("updateRemoteData: " + timezone + ", " + city.getLatitude() + ", " + city.getLongitude());
        if (TextUtils.isEmpty(timezone))
            timezone = TimeZone.getDefault().getID();

        final String tz = timezone;


        String country = city.getCountry();
        if (Countries.INSTANCE.getCOUNTRIES_MAP().containsKey(country.toUpperCase()))
            country = getString(Countries.INSTANCE.getCOUNTRIES_MAP().get(city.getCountry().toUpperCase()));
        else if (country.equalsIgnoreCase("GB"))
            country = getString(Countries.INSTANCE.getCOUNTRIES_MAP().get("UK"));

        location_remote.setText(city.getCity() + ", " + country);

        clock_remote.setTimeZone(tz);
        date_remote.setTimeZone(tz);


        if (clock_remote.is24HourModeEnabled()) {
            clock_remote.setFormat12Hour(null);
            date_remote.setFormat12Hour(null);
            clock_remote.setFormat24Hour(TextClock.DEFAULT_FORMAT_24_HOUR);
            Format dateFormat = DateFormat.getDateFormat(MainActivity.this);
            String pattern = ((SimpleDateFormat) dateFormat).toLocalizedPattern();
            date_remote.setFormat24Hour("EEEE\n" + pattern);

        } else {
            clock_remote.setFormat24Hour(null);
            date_remote.setFormat24Hour(null);
            clock_remote.setFormat12Hour(TextClock.DEFAULT_FORMAT_12_HOUR);
            Format dateFormat = DateFormat.getDateFormat(MainActivity.this);
            String pattern = ((SimpleDateFormat) dateFormat).toLocalizedPattern();
            date_remote.setFormat12Hour("EEEE\n" + pattern);
        }

        final DataUpdater.UpdateListener<Weather.WeatherData> weatherUpdateListener =
                new DataUpdater.UpdateListener<Weather.WeatherData>() {
                    @Override
                    public void onUpdate(Weather.WeatherData data) {
                        if (data != null) {
                            // Populate the remote temperature rounded to a whole number.
                            String temperature = String.format("%d°", Math.round(data.currentTemperature));
                            temperature_remote.setText(temperature);

                            // Populate the 24-hour forecast summary, but strip any period at the end.
                            String summary = stripPeriod(data.daySummary);
                            weather_summary_remote.setText(summary);

                            // Populate the precipitation probability as a percentage rounded to a whole number.
                            String precipitation =
                                    String.format("%d%%", Math.round(100 * data.dayPrecipitationProbability));
                            precipitation_remote.setText(precipitation);

                            // Populate the icon for the remote weather.
                            icon_remote.setImageResource(data.currentIcon);

                            // Show all the views.
                            temperature_remote.setVisibility(View.VISIBLE);
                            weather_summary_remote.setVisibility(View.VISIBLE);
                            precipitation_remote.setVisibility(View.VISIBLE);
                            icon_remote.setVisibility(View.VISIBLE);
                        } else {
                            // Hide everything if there is no data.
                            temperature_remote.setVisibility(View.GONE);
                            weather_summary_remote.setVisibility(View.GONE);
                            precipitation_remote.setVisibility(View.GONE);
                            icon_remote.setVisibility(View.GONE);
                        }
                    }
                };
        final Location location = new Location("");
        location.setLatitude(city.getLatitude());
        location.setLongitude(city.getLongitude());
        if (weather != null)
            weather.stop();

        weather = new Weather(weatherUpdateListener, TimeUnit.SECONDS.toMillis(60), location);
        weather.start();

    }

    /**
     * Removes the period from the end of a sentence, if there is one.
     */
    private String stripPeriod(String sentence) {
        if (sentence == null) {
            return null;
        }
        if ((sentence.length() > 0) && (sentence.charAt(sentence.length() - 1) == '.')) {
            return sentence.substring(0, sentence.length() - 1);
        } else {
            return sentence;
        }
    }
}
