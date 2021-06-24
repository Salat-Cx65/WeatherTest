package com.example.myapplication.cities.sergey;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.myapplication.cities.NearestCity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;

public class CitiesTool {

    private final String[] toReplace = {
            "\t",
            "\u00A0",
            "\u1680",
            "\u180e",
            "\u2000",
            "\u2001",
            "\u2002",
            "\u2003",
            "\u2004",
            "\u2005",
            "\u2006",
            "\u2007",
            "\u2008",
            "\u2009",
            "\u200a",
            "\u202f",
            "\u205f",
            "\u3000"};

    private final Context context;

    public CitiesTool(final Context context) {
        this.context = context;
    }

    private void readFile(StringReadListener stringReadListener) {
        try {
            AssetManager am = context.getAssets();
            BufferedReader in = new BufferedReader(new InputStreamReader(am.open("cities5000.txt")));
            String line;
            while ((line = in.readLine()) != null) {
                for (String c : toReplace)
                    line = line.replace(c, "  ");

                while (line.contains("   "))
                    line = line.replace("   ", "  ");

                line = line.replace("  ", ";").trim();

                stringReadListener.onStringReady(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public NearestCity getNearestCity(double targetLatitude, double targetLongitude) {
        AtomicReference<NearestCity> city = new AtomicReference<>(null);
        try {

            readFile(new StringReadListener() {
                double distance = Double.MAX_VALUE - 1;

                @Override
                public void onStringReady(@NonNull String str) {
                    if (TextUtils.isEmpty(str))
                        return;

                    String[] values = str.split(";");

                    for (int i = 0; i < values.length - 1; i++)
                        try {
                            String candidate1 = values[i];
                            String candidate2 = values[i + 1];
                            if ((candidate1.contains(".") && TextUtils.isDigitsOnly(candidate1.replace(".", ""))) &&
                                    (candidate2.contains(".") && TextUtils.isDigitsOnly(candidate2.replace(".", "")))) {
                                double latitude = Double.parseDouble(candidate1);
                                double longitude = Double.parseDouble(candidate2);

                                double tmpDistance = distance(targetLatitude, latitude, targetLongitude, longitude, 0, 0);

                                if (tmpDistance <= distance) {
                                    distance = tmpDistance;
                                    String cityName = values[2];
                                    String tz = values[values.length - 2];
                                    String country = values[i + 4];
                                    city.set(new NearestCity(cityName, country, tz, latitude, longitude));
                                }
                                break;
                            }


                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return city.get();
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * <p>
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     *
     * @returns Distance in Meters
     */
    private double distance(double lat1, double lat2, double lon1,
                            double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }


}
