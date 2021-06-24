package com.example.myapplication.cities;

import android.content.Context;

import com.example.myapplication.cities.sergey.CitiesTool;

public class CityProvider {
    public static NearestCity getCity(Context context, double latitude, double longitude, Solution solution) {
        switch (solution) {
            case SERGEY:
                return new CitiesTool(context).getNearestCity(latitude, longitude);
            default:
                throw new IllegalStateException("Not implemented");
        }
    }

    public enum Solution {
        SERGEY,
        ALEXEY
    }
}
