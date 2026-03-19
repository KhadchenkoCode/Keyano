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


    public static void main(String[] args) throws LineUnavailableException {


        String startNote = "E2";
        PrimitiveGuitarTabConverter.lowestNoteCurrentTuning = PrimitiveGuitarTabConverter.pitchFromNote(startNote);

        //58 (A#2), 82, 82*2
        InputListener.startingFreq = (int)(Main.getHzFromNote(startNote)*Math.pow(semitone, 0));
        args = new String[2];
        args[0] = "convert";

        launchStart = System.currentTimeMillis();
        launchStartNano = System.nanoTime();

        //if(args[0] == "play")

        if(args[0] == "convert") {

            //System.out.println(new File("").getAbsolutePath());
            String path = args[1];
            path = "MutterTabs.txt"; // tmp for testing
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
        for(int i = startA0; i<parsedPitch; i++){
            ret*=semitone;
        }
        return ret;

    }

    public static void playFrequency(double frequency, int durationMs) throws LineUnavailableException {
        float sampleRate = 44100;
        byte[] buffer = new byte[(int) (sampleRate * durationMs / 1000)];

        for (int i = 0; i < buffer.length; i++) {
            double angle = 2.0 * Math.PI * frequency * i / sampleRate;
            buffer[i] = (byte) (Math.sin(angle) * 127);
        }

        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
        SourceDataLine line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        line.start();
        line.write(buffer, 0, buffer.length);
        line.drain();
        line.close();
    }

    public static void playFrequencyWithOvertones(double baseFreq, int durationMs) throws LineUnavailableException {
        float sampleRate = 44100;
        int numSamples = (int) (sampleRate * durationMs / 1000);
        byte[] buffer = new byte[numSamples];

        for (int i = 0; i < numSamples; i++) {
            double time = i / sampleRate;

            // Fundamental and overtones (basic additive synthesis)
            double wave =
                    1.0 * Math.sin(2 * Math.PI * baseFreq * time) +                   // Fundamental
                            0.25 * Math.sin(2 * Math.PI * baseFreq * 2 * time) +      // 1st overtone
                            0.125 * Math.sin(2 * Math.PI * baseFreq * 4 * time);     // 1st overtone


            // Scale to byte range
            buffer[i] = (byte) (wave * 127);
        }

        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
        SourceDataLine line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        line.start();
        line.write(buffer, 0, buffer.length);
        line.drain();
        line.close();
    }

}