package org.example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteMapper {


    static final double halftone = 1.05946309436;

    public  ArrayList<Integer> readFromString(String keys){
        ArrayList<Integer> ret = new ArrayList<>();
        for(int i= 0; i<keys.length(); i++){
            char c = keys.charAt(i);
            boolean debug = c == ";".charAt(0);
            if(debug){
                System.out.println("");
            }
            Integer k = keyCodeFromChar(c);
            ret.add(k);
        }
        return ret;
    }

    private static  int keyCodeFromChar(char c){
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);

        return keyCode;

    }

    public static Map<Integer, Integer> chromaticFrequencies(List<Integer> keyCodes, int startingFrequency_hz) {
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
        float frequency = startingFrequency_hz;
        // this was int, and I was getting 192hz instead of 196hz when building scale from 131hz start point

        for (int i = 0; i <keyCodes.size() ; i++) {

            Integer keyCode = keyCodes.get(i);
            boolean debug = keyCode == keyCodeFromChar(";".charAt(0));
            if(debug){
                System.out.print("");
            }
            ret.put(keyCode, (int)frequency);
            //System.out.println(frequency);
            frequency*=halftone;
        }
        return ret;
    }

}
