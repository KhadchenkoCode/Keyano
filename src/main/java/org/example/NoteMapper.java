package org.example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.NotePlayer.INV_LOG_2;

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
    public static int hertzToSemitone(double hz){

        int semitones = (int) Math.round(
                12 * Math.log(hz / InputListener.startingFreq) * INV_LOG_2


        );
        return semitones;
    }
    public static final int FourOctaves=4*12;
    public static double[] frequencies;



   public static Map<Integer, Integer> chromaticFrequenciesInt(List<Integer> keyCodes, int startingFrequency_hz) {
        Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
        int frequency = hertzToSemitone(startingFrequency_hz);
        frequencies=new double[FourOctaves];
        float frequencyHz = startingFrequency_hz;

        for (int i = 0; i <keyCodes.size() ; i++) {

            Integer keyCode = keyCodes.get(i);
            boolean debug = keyCode == keyCodeFromChar(";".charAt(0));
            if(debug){
                System.out.print("");
            }

            ret.put(keyCode, (int)frequency);
            //System.out.println(frequency);
            frequencies[frequency] = frequencyHz;
            frequency++;
            frequencyHz*=Main.semitone;
        }
        return ret;
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
