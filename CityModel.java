package com.example.myapplication.cities.aleksei;

public class CityModel {
        private String cityName;
        private double latitude;
        private double longitude;
        private String timezone;
        private String country;

        public CityModel(){

        }

        public CityModel(String cityName, double latitude, double longitude, String timezone, String country){
            this.cityName = cityName;
            this.latitude = latitude;
            this.longitude = longitude;
            this.timezone = timezone;
            this.country = country;
        }

        public void setCountry (String country){
            this.country = country;
        }

        public String getCountry(){
            return country;
        }

        public void setCityName(String cityName){
            this.cityName = cityName;
        }

        public String getCityName(){
            return cityName;
        }

        public void setLatitude(double latitude){
            this.latitude = latitude;
        }

        public double getLatitude(){
            return latitude;
        }

        public void setLongitude(double longitude){
            this.longitude = longitude;
        }

        public double getLongitude(){
            return longitude;
        }

        public void setTimezone(String timezone){
            this.timezone = timezone;
        }

        public String getTimezone(){
            return timezone;
        }

        @Override
        public String toString(){
            return String.format("City name: %s \n"
                    + "City latitude: %s \n"
                    + "City longitude: %s \n"
                    + "City timezone: %s \n"
                    + "Country: %s \n", cityName, latitude, longitude, timezone, country);
        }
}
