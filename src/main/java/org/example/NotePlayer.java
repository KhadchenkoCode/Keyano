package org.example;

import javax.sound.sampled.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileOutputStream;

import static java.lang.Math.*;

public class NotePlayer {

    private FileOutputStream audioOutputStream ;


    private HarmonicBehavior harmonicBehavior;

    NotePlayer(){


        try {
            if(shouldWriteToFileConfig || true) {
                audioOutputStream = new FileOutputStream("output.pcm", true); // true = append;
            }
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        if (audioOutputStream != null) {
                            audioOutputStream.close();
                            System.out.println("Audio output stream closed.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

            this.currentTimeDouble = 0;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void setHarmonicBehavior(HarmonicBehavior harmonicBehavior) {
        this.harmonicBehavior = harmonicBehavior;
    }





    private SourceDataLine line;
    private double phase = 0.0;


    private double fadingScaleFactor(double nanoTimeSinceRelease_ms){
        double fadeStage = (nanoTimeSinceRelease_ms);
        fadeStage*=1000;
        fadeStage/=FadeTimeMillis;
        return cubicNormalizedFade(fadeStage);
    }

    private static double cubicNormalizedFade(double fadeStage){
        if(fadeStage >= 1){ return 0;}
        return 1- fadeStage*fadeStage*(3-2*fadeStage);
    }


    static final int FadeTimeMillis = 750;
    public static final double attackCalibrationVolume = 1;
    public static final double attackCalibrationDuration_s = 0.2;
    public static final double valueAt005 = attackCalibrationVolume* cubicNormalizedFade(0.05*5);

    private double noteFadeAmplitude(NoteState state){
        try {

            if(state==null){
                return 0.0;
            }





            boolean wasPressed =true;
            boolean wasPressedWasReleased =false;
            if (wasPressedWasReleased) {

            }
            if (!wasPressed) {
                return 0;
            }



            double sincePressTime = currentTimeDouble - state.pressTime; //pressDownTimes.get(Frequency);
            boolean justPressed = sincePressTime < 0.2;
            double releaseTime = state.releaseTime; //this.releaseTimes.get(Frequency);
            double sinceRelease = currentTimeDouble - releaseTime;
            if (justPressed) {
                boolean attackStage = sincePressTime<0.05;
                if(attackStage){
                    if(sinceRelease>(double)FadeTimeMillis/1000) {
                        return (20*sincePressTime)*(1+valueAt005);
                    } else{
                        return Math.max( (20*sincePressTime)*(1+valueAt005), attackCalibrationVolume* fadingScaleFactor(sinceRelease));
                    }

                }


                return 1 + attackCalibrationVolume* cubicNormalizedFade(sincePressTime/attackCalibrationDuration_s);
            }
            if(!justPressed && state.pressed){
                return 1;
            }

            double factor = fadingScaleFactor(sinceRelease);
            return factor;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static final double INV_LOG_2 = 1.0 / Math.log(2.0);

    public void updateHarmonics(double baselineHz){
        harmonicBehavior.initHarmonics(baselineHz);
    }

    private double createWave(double time) {
        double waveSum = 0.0;
        double activeCount = 0;

        for (Map.Entry<Integer, NoteState> entry : NoteStatusMap.entrySet()) {

            NoteState state = entry.getValue();
            if(state==null){
                continue;
            }

            Double releaseDouble = state.releaseTime;


            boolean containsRelease = releaseDouble!=null;
            boolean releasedRecently = false;
            boolean active = state.pressed;

            if(containsRelease){
                releasedRecently =
                this.currentTimeDouble- releaseDouble<(float)FadeTimeMillis/1000f;
            }


            if (active || releasedRecently) {
                int semitones= entry.getKey();
                double baseFreq = NoteMapper.frequencies[semitones];
                  double wave;

                double waveRefactored = harmonicBehavior.getSample(baseFreq, time, semitones);
               wave = waveRefactored;

                double fadeEffect=1;

                    fadeEffect = noteFadeAmplitude(state);


                state.currentVolume=fadeEffect;

                wave*=fadeEffect;
                waveSum += wave;
                activeCount++;
            } else {

            }
        }

        if (activeCount > 0) {
            waveSum /= 12;
        }

        return Math.max(-1.0, Math.min(1.0, waveSum));
    }



    private double fourierAdditive(double time, double baseFreq, double ratioToRoot, double amplitude){
        return amplitude* Math.sin(2*Math.PI*baseFreq*ratioToRoot*time);
    }



    final int ExpectedOctaveCount = 4;
    final int SemitonesPerOctave = 12;
    final int harmonics = 16;
    final int TotalHarmonicsCount = ExpectedOctaveCount*SemitonesPerOctave*harmonics;


    double[] ampTable;   //4 octaves, 12 semitones
    double[] ratioTable;
















    private double currentTimeDouble;

    float sampleRate = 44100;
    int durationMs = 10;
    int numSamples = (int) (sampleRate * durationMs / 1000);
    private byte[] reuseBuffer;
    private void initialiseReuseBuffer(){
        reuseBuffer = new byte[numSamples * 2]; // 16-bit = 2 bytes
    }
    public void playAllActiveNotes() {
        for (int i = 0; i < numSamples; i++) {
            double wave = createWave(phase);
            double increment =  1.0 / sampleRate;
            phase +=increment;

            short sample = (short) (wave * Short.MAX_VALUE);   // 16-bit
            reuseBuffer[2 * i]     = (byte) (sample & 0xFF);        // low byte
            reuseBuffer[2 * i + 1] = (byte) ((sample >> 8) & 0xFF); // high byte
            currentTimeDouble+=increment;
        }

        line.write(reuseBuffer, 0, reuseBuffer.length);
        if(shouldWriteToFileConfig){
            writeToFile(reuseBuffer);
        }
    }
   static boolean shouldWriteToFileConfig = false;
    //config parameter set on launch by reading cfg file
    //implement later

    public void noteThreadLoop() {
        float sampleRate = 44100;
        int frameSize = 2; //2 bytes
        int bufferMs = 10;
        int bufferSize = (int) (sampleRate * bufferMs / 1000) * frameSize;
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        initialiseReuseBuffer();

        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);// 16-bit PC

        try {
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, bufferSize);
            line.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException("Failed to initialize audio line", e);
        }

        while (true) {
            playAllActiveNotes();
        }
    }

    public Map<Integer, NoteState> NoteStatusMap = new HashMap<Integer, NoteState>();
    public void updateNoteStatus(int frequency, boolean activity) {
        NoteState state = NoteStatusMap.get(frequency);

        boolean containsFixed = state!=null;
        boolean oldPath = false;

        if(!containsFixed && !oldPath) {
            state = new NoteState(frequency, currentTimeDouble);
            NoteStatusMap.put(frequency, state);
            return;
        }


        if (state.pressed != activity) {
            state.pressed = activity;
                //if state changed
            if (activity) {

                state.pressTime = currentTimeDouble;
            } else {
                state.releaseTime = currentTimeDouble;
            }
        }

        return;
    }




    private void writeToFile(byte[] buffer) {
        try {
            if (audioOutputStream != null) {
                audioOutputStream.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}