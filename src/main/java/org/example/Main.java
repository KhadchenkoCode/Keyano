package org.example;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.PrimitiveTabConverter.PrimitiveGuitarTabConverter;

import java.io.File;

public class Main {

    public static long launchStart;
    public static long launchStartNano;
    public static final double semitone = 1.05946309436;

    public static final String chromatic3 = "\\qazwsxedcrfvtgbyhnujmik,ol.p;/['";

    public static String currentFile = "--" +
            "Tabs/Lindemann_PA_Synth.txt";

    public static void main(String[] args) throws LineUnavailableException {



        String        startNote = "E2";




        PrimitiveGuitarTabConverter.lowestNoteCurrentTuning = PrimitiveGuitarTabConverter.pitchFromNote(startNote);

        byte E7Byte = PrimitiveGuitarTabConverter.pitchFromNote("E7");
        byte E0Byte = PrimitiveGuitarTabConverter.pitchFromNote("E0");


        double E7Hz = Main.getHzFromNote("E7");
        double E6Hz = Main.getHzFromNote("E6");

        double E5Hz = Main.getHzFromNote("E5");
        double E4Hz = Main.getHzFromNote("E4");
        double E3Hz = Main.getHzFromNote("E3");
        double E2Hz = Main.getHzFromNote("E2");
        double E1Hz = Main.getHzFromNote("E1");
        double E0Hz = Main.getHzFromNote("E0");

        //58 (A#2), 82, 82*2
        InputListener.startingFreq = (Main.getHzFromNote(startNote)*Math.pow(semitone, 0));


        args = new String[2];
      //  args[0] = "convert";

        launchStart = System.currentTimeMillis();
        launchStartNano = System.nanoTime();

        //if(args[0] == "play")

        if(args[0] == "convert") {

            //System.out.println(new File("").getAbsolutePath());
            String path = args[1];
            path = "Tabs/EngelTab.txt"; // tmp for testing
            String output = PrimitiveGuitarTabConverter.parseFile(path);
            output = output.replace('\r', '\n');
            output = output.replace('-', ' ');
            System.out.println(output);
        }
        new InputListener();
    }

    public static final float A0_Hz = 27.5f;
    public static float getHzFromNote(String note){
        float semitone = 1.059463094f;
        byte parsedPitch = PrimitiveGuitarTabConverter.pitchFromNote(note);
        byte startA0 = PrimitiveGuitarTabConverter.pitchFromNote("A0");

        float ret = A0_Hz;
        if(startA0<parsedPitch)
        for(int i = startA0; i<parsedPitch; i++){
            ret*=semitone;
        }
        if(startA0>parsedPitch){
            for (int i = startA0; i >parsedPitch; i--) {
                ret/=semitone;
            }
        }

        return ret;

    }



}