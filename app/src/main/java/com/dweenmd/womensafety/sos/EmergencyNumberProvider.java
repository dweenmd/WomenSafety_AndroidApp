package com.dweenmd.womensafety.sos;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmergencyNumberProvider {

    public static class EmergencyNumbers {
        public String general;
        public String police;
        public String ambulance;

        public EmergencyNumbers(String general, String police, String ambulance) {
            this.general = general;
            this.police = police;
            this.ambulance = ambulance;
        }
    }

    private static final Map<String, EmergencyNumbers> numbersMap = new HashMap<>();

    static {
        // North America
        numbersMap.put("US", new EmergencyNumbers("911", "911", "911"));
        numbersMap.put("CA", new EmergencyNumbers("911", "911", "911"));
        
        // Europe
        String[] euCountries = {"GB", "FR", "DE", "IT", "ES", "NL", "BE", "SE", "PT", "FI", "IE"}; // UK also has 999 but 112 works
        for (String cc : euCountries) {
            numbersMap.put(cc, new EmergencyNumbers("112", "112", "112"));
        }
        numbersMap.put("GB", new EmergencyNumbers("999", "999", "999")); // Override for UK specifically for preference

        // Asia
        numbersMap.put("IN", new EmergencyNumbers("112", "100", "102"));
        numbersMap.put("BD", new EmergencyNumbers("999", "999", "999"));
        numbersMap.put("PK", new EmergencyNumbers("112", "15", "115"));
        numbersMap.put("LK", new EmergencyNumbers("119", "119", "1990"));

        // Oceania
        numbersMap.put("AU", new EmergencyNumbers("000", "000", "000"));
        numbersMap.put("NZ", new EmergencyNumbers("111", "111", "111"));
    }

    public static EmergencyNumbers getEmergencyNumber(Context context) {
        String countryCode = getCountryIso(context);
        
        if (countryCode != null && numbersMap.containsKey(countryCode)) {
            return numbersMap.get(countryCode);
        }
        
        // Fallback to 112 globally as it works on most GSM networks
        return new EmergencyNumbers("112", "112", "112");
    }

    private static String getCountryIso(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            String networkCountry = tm.getNetworkCountryIso();
            if (networkCountry != null && !networkCountry.isEmpty()) {
                return networkCountry.toUpperCase(Locale.US);
            }
            
            String simCountry = tm.getSimCountryIso();
            if (simCountry != null && !simCountry.isEmpty()) {
                return simCountry.toUpperCase(Locale.US);
            }
        }
        
        return Locale.getDefault().getCountry().toUpperCase(Locale.US);
    }
}
