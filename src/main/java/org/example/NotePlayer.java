package org.example;

import javax.sound.sampled.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileOutputStream;

import static java.lang.Math.*;

public class NotePlayer {

    private FileOutputStream audioOutputStream ;

    boolean testFlag = true;

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


    private final Map<Integer, Double> pressDownTimes = new ConcurrentHashMap<>();

    private final Map<Integer, Double> releaseTimes = new ConcurrentHashMap<>();

    private static int millisNowInt(){
        int time = (int) (System.currentTimeMillis() - Main.launchStart);
        return time;
    }


    private static long nanoNowInt(){
        return System.nanoTime() - Main.launchStartNano;
    }

    private final Map<Integer, Boolean> activeNotes = new ConcurrentHashMap<>();
    private SourceDataLine line;
    private double phase = 0.0;

    private double fadingScaleFactor(int timeSinceReleaseMillis){
        double fadeStage = (timeSinceReleaseMillis);
        fadeStage/=FadeTimeMillis;
        return cubicNormalizedFade(fadeStage);
    }


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

    private double cosinoidNormalizedFade(double fadeStage){
        if(fadeStage >= 1){ return 0;}

        return Math.cos(Math.PI*fadeStage)/2+0.5;
    }
    private double exponentialNormalizedFade(double fadeStage){
        if(fadeStage >= 1){ return 0;}

        return pow(2, 7*fadeStage);
    }

    static final int FadeTimeMillis = 750;
    public static final double attackCalibrationVolume = 1;
    public static final double attackCalibrationDuration_s = 0.2;
    private static final double valueAt005 = attackCalibrationVolume* cubicNormalizedFade(0.05*5);

    private double noteFadeAmplitude(int Frequency){
        try {

            Boolean isActive = activeNotes.get(Frequency);
            if (isActive == null) return 0; // Safety check

            boolean wasPressed =true;//activeNotes.containsKey(Frequency);    if isActive!=null then it already contains this key
            boolean wasPressedWasReleased =false;// activeNotes.containsKey(Frequency) && !activeNotes.get(Frequency);
            if (wasPressedWasReleased) {

             //   System.out.print("");
    //           return 0;
            }
            if (!wasPressed) {
                return 0;
            }

            Double pressDownTimes_debug = pressDownTimes.get(Frequency);
            if(pressDownTimes_debug == null){
                return 0;
            }
            Double releaseTimes_debug = releaseTimes.get(Frequency);
            if(releaseTimes_debug == null){
                return 0;
            }


            double sincePressTime = currentTimeDouble - pressDownTimes.get(Frequency);
            boolean justPressed = sincePressTime < 0.2;
            double releaseTime = this.releaseTimes.get(Frequency);
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



            if (isActive) {
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

    private double createWave(double time) {
        double waveSum = 0.0;
        double activeCount = 0;

        for (Map.Entry<Integer, Boolean> entry : activeNotes.entrySet()) {
            boolean active = entry.getValue();
            int frequency = entry.getKey();

            Double releaseDouble = releaseTimes.get(frequency);

            boolean containsRelease = releaseDouble!=null; //releaseTimes.containsKey(frequency);
            boolean releasedRecently = false;
            if(containsRelease){
                releasedRecently = //this.currentTimeDouble- this.releaseTimes.get(frequency)<FadeTimeMillis*1000;
                this.currentTimeDouble- releaseDouble<(float)FadeTimeMillis/1000f;
            }
            if (active || releasedRecently) {
                int semitones= entry.getKey();
                double baseFreq = NoteMapper.frequencies[semitones];

                //double wave =waveFormV1(time, baseFreq);
                //double phonAdjustmentFactor = Math.log(1000f/baseFreq)*INV_LOG_2;
                //phonAdjustmentFactor = Math.pow(1.6, 2*phonAdjustmentFactor);
                double wave = waveFormPianoFourierV5(baseFreq, time, semitones);
                double fadeEffect = noteFadeAmplitude(frequency);

                wave*=fadeEffect;
                waveSum += wave;
                activeCount++;
            }
        }

        if (activeCount > 0) {
            waveSum /= 10;
        }

        return Math.max(-1.0, Math.min(1.0, waveSum));
    }

    double waveFormV1(double baseFreq, double time){
        return  1.0 * sin(2 * Math.PI * baseFreq * time) +
                0.25 * sin(2 * Math.PI * baseFreq * 2 * time)+
                0.125 * sin(2 * Math.PI * baseFreq * 4 * time);
    }

    private double fourierAdditive(double time, double baseFreq, double ratioToRoot, double amplitude){
        return amplitude* Math.sin(2*Math.PI*baseFreq*ratioToRoot*time);
    }
    double waveFormPianoFourierV2_1(double time, double baseFreq){

              return waveFormPianoFourierV2(time, baseFreq/2.7561);
    }
    double waveFormPianoFourierV2(double time, double baseFreq){
        //attempted as a piano due to error in fourier analysis of bass note
        //produces pitch a lot higher than input baseFreq
        //however sounds good for actually high frequencies,
        //reminiscent of vibrating glass or ringing bells in high pitch
        double	ret	=	fourierAdditive(time,	baseFreq,	2.7561,	0.04365);
        ret+=fourierAdditive(time,	baseFreq,	4.87,	0.20893);
        ret+=fourierAdditive(time,	baseFreq,	11.52,	0.000243325);
        ret+=fourierAdditive(time,	baseFreq,	22.036,	1.76273*10E-5);
        ret+=fourierAdditive(time,	baseFreq,	5,1.06215*10E-6);
        ret+=fourierAdditive(time,	baseFreq,	42.48,	5.32336 *10E-9);

    return ret;
    }

    final int ExpectedOctaveCount = 4;
    final int SemitonesPerOctave = 12;
    final int harmonics = 16;
    final int TotalHarmonicsCount = ExpectedOctaveCount*SemitonesPerOctave*harmonics;


    double[] ampTable;   //4 octaves, 12 semitones
    double[] ratioTable;

    public void initHarmonics(double baselineHz){
         ampTable          = new double[TotalHarmonicsCount];
         ratioTable        = new double[TotalHarmonicsCount];
        double currentFreq = InputListener.startingFreq;
        for(int i = 0; i<ExpectedOctaveCount; i++){
            for(int j = 0; j<SemitonesPerOctave; j++){

                    int ptrStride =   (i*SemitonesPerOctave*harmonics)+
                                (j*harmonics);
                    init(currentFreq, ptrStride);

                currentFreq*= Main.semitone;
            }
        }


    }

    void init(double baseFreq, int ptrStride) {


        double B0 = 0.0001;
        double B = B0 * Math.pow(440.0 / baseFreq, 1.7);


        //double x = Math.log(440.0 / baseFreq);
        // B = B0 * (1.0 + 1.5 * x + 0.3 * x * x);


        for (int n = 1; n <= harmonics; n++) {
            double assignAmp = 1.0 / Math.pow(n, 0.9);
            double assignRatio =n * Math.sqrt(1.0 + B * n * n);
            ampTable[ptrStride+n-1] =assignAmp;
            ratioTable[ptrStride + n-1] =assignRatio;
            continue;

        }
    }


     double waveFormPianoFourierV5(double baseFreq, double time, int pitchSemitones){



        double ret = 0.0;
        int semitones = pitchSemitones;


        int indexShift = semitones*harmonics;
        for(int n = 1; n <= harmonics; n++){
            // inharmonicity
            int index = indexShift+n-1;
            //try{


                double amp =ampTable[index];
                double ratio = ratioTable[index];
                double arg = 2 * Math.PI * baseFreq * ratio * time;
                ret += amp * Math.sin(arg);
            //} catch (ArrayIndexOutOfBoundsException e){
               // System.out.println(index);
              //  e.printStackTrace();

            //}
        }

        return ret;
    }


    double waveFormPianoFourierV4(double baseFreq, double time){



        double ret = 0.0;
        int semitones = NoteMapper.hertzToSemitone(baseFreq);
         //unneccesary Division and Log inside intense loop, to fix later
       //int semitones = 12;

        int indexShift = semitones*harmonics;
        for(int n = 1; n <= harmonics; n++){
            // inharmonicity
            int index = indexShift+n-1;
            //try{


                double amp =ampTable[index];
                double ratio = ratioTable[index];
                double arg = 2 * Math.PI * baseFreq * ratio * time;
                ret += amp * Math.sin(arg);
            //} catch (ArrayIndexOutOfBoundsException e){
               // System.out.println(index);
              //  e.printStackTrace();

            //}
        }

        return ret;
    }



    double waveFormPianoFourier(double baseFreq, double time, int semitones){
       // ampTable[0]*=0;
      //  double sus = ampTable[0];

        double ret = fourierAdditive(time, baseFreq, 1, 1);
        ret+= fourierAdditive(time, baseFreq, 2, 0.20893);
        ret+= fourierAdditive(time, baseFreq, 3, 0.01622);
        ret+=fourierAdditive(time, baseFreq, 4, 0.00724);
        ret+=fourierAdditive(time, baseFreq, 5, 0.00155);
//        double finalRatio = 183/baseFreq;
        ret+=fourierAdditive(time, baseFreq, 0.38265, 0.00955);


     //   ret+=fourierAdditive(time, baseFreq, 6, 1.5); //perhaps
       // ret+=fourierAdditive(time, baseFreq, 16, 0.05);
        return ret;
    }


    double waveFormV2(double baseFreq, double time){
        return  1 * sin(2 * Math.PI * baseFreq * time);

    }

    private double currentTimeDouble;
    public void playAllActiveNotes() {
        float sampleRate = 44100;
        int durationMs = 1;
        int numSamples = (int) (sampleRate * durationMs / 1000);
        byte[] buffer = new byte[numSamples * 2]; // 16-bit = 2 bytes

        for (int i = 0; i < numSamples; i++) {
            double wave = createWave(phase);
             double increment =  1.0 / sampleRate;
            phase +=increment;

            short sample = (short) (wave * Short.MAX_VALUE);   // 16-bit
            buffer[2 * i]     = (byte) (sample & 0xFF);        // low byte
            buffer[2 * i + 1] = (byte) ((sample >> 8) & 0xFF); // high byte
            currentTimeDouble+=increment;
        }

        line.write(buffer, 0, buffer.length);
        boolean shouldWrite = !pressDownTimes.isEmpty();
        if(shouldWrite && shouldWriteToFileConfig){
            writeToFile(buffer);
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
       // Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        if(testFlag){


        }
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);// 16-bit PCM

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

    public void updateNoteStatus(int frequency, boolean activity) {
        boolean contains = activeNotes.containsKey(frequency);

        if(!contains) {
            activeNotes.put(frequency, activity);
            pressDownTimes.put(frequency, currentTimeDouble);
            return;
        }
        boolean oldStatus = activeNotes.get(frequency);

        activeNotes.put(frequency, activity);
        
        if(oldStatus == true && activity == true){
            //nothing changed, do nothing
        }
        if(oldStatus == false && activity == false){
            //in theory impossible to enter this
        }
        if(oldStatus == true && activity == false){
            releaseTimes.put(frequency, currentTimeDouble);
        }
        if(oldStatus == false && activity == true){
            pressDownTimes.put(frequency, currentTimeDouble);
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

    private void closeStream() {
        try {
            audioOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}