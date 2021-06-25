package com.example.myapplication.cities.alexei;

import android.content.Context;
import android.content.res.AssetManager;

import com.example.myapplication.cities.NearestCity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FileParser {
    private final LinkedList<CityModel> cityModels = new LinkedList<>();
    final String[] toReplace = {
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

    public FileParser(Context context) {
        Reader fileReader = null;
        try {
            //Android way
            AssetManager am = context.getAssets();
            fileReader = new BufferedReader(new InputStreamReader(am.open("cities5000.txt")));
//            fileReader = new FileReader("cities5000.txt");//Pure Java
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (BufferedReader bufferedReader = new BufferedReader(fileReader);) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String temp = deleteEmptyChars(line);
                List<String> strTemp = Arrays.asList(temp.split("  "));
                int size = strTemp.size();
                CityModel model = new CityModel();

                for (int i = 0; i < strTemp.size() - 1; i++) {
                    String element = strTemp.get(i);
                    if ((element.contains(".") && isDouble(element)) && (strTemp.get(i + 1).contains(".") && isDouble(strTemp.get(i + 1)))) {
                        model.setLatitude(Double.parseDouble(element));
                        model.setLongitude(Double.parseDouble(strTemp.get(i + 1)));
                        model.setCityName(strTemp.get(2));
                        model.setCountry(strTemp.get(i + 4));
                        model.setTimezone(strTemp.get(size - 2));
                    }
                }
                cityModels.add(model);
            }
            } catch (IOException e) {
                e.printStackTrace();
              }
    }
    public NearestCity getCity(double targetLatitude, double targetLongitude) {
         double distance = Double.MAX_VALUE - 1;
         CityModel temp = new CityModel();
         for (CityModel model:cityModels) {
                     double latitude = model.getLatitude();
                     double longitude = model.getLongitude();

                     double tmpDistance = distance(targetLatitude, latitude, targetLongitude, longitude, 0, 0);

                     if (tmpDistance < distance) {
                         distance = tmpDistance;
                         temp = model;
                     }
     }
     return converter(temp);
 }
    protected NearestCity converter(CityModel cityModel) {
        return new NearestCity(cityModel.getCityName(), cityModel.getCountry(),
                cityModel.getTimezone(), cityModel.getLatitude(), cityModel.getLongitude());
    }

    protected double distance(double lat1, double lat2, double lon1,
             double lon2, double el1, double el2) {

    	 	 final int R = 6371; // Radius of the earth

    	 	 Double latDistance = Math.toRadians(lat2 - lat1);
    	 	 Double lonDistance = Math.toRadians(lon2 - lon1);
    	 	 Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
    	 			 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
    	 			 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    	 	 Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    	 	 double distance = R * c * 1000; // convert to meters

    	 	 double height = el1 - el2;

    	 	 distance = Math.pow(distance, 2) + Math.pow(height, 2);

    	 	 return Math.sqrt(distance);
}
 	protected boolean isDouble(String strNum) {
 		if (strNum == null) {
 	        return false;
 	    }
 	    try {
 	        double d = Double.parseDouble(strNum);
 	    } catch (NumberFormatException nfe) {
 	        return false;
 	    }
 	    return true;
 	}
    protected String deleteEmptyChars(String line) {
    
        for (String c : toReplace)
            line = line.replace(c, "  ");


        while (line.contains("   "))
            line = line.replace("   ", "  ");
    	return line;
    }
}
