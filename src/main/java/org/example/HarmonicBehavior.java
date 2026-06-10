package org.example;

public class HarmonicBehavior {
    double[] ampTable;
    double[] ratioTable;
    HarmonicBehavior() {
        int totalHarmonics = 4 * 12* harmonics;
        ampTable = new double[totalHarmonics];
        ratioTable = new double[totalHarmonics];
    }
    public static final int ExpectedOctaveCount =  4;
    public static final int SemitonesPerOctave  = 12;
    final int TotalHarmonicsCount = ExpectedOctaveCount * SemitonesPerOctave * harmonics;
    public void initHarmonics(double baselineHz){
        ampTable          = new double[TotalHarmonicsCount];
        ratioTable        = new double[TotalHarmonicsCount];
        double currentFreq =  baselineHz;
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
        double baseFreqOriginal = baseFreq;
        baseFreq*=95.8920010653;
        double B = B0 * Math.pow(440/ baseFreq, 1.7);
        for (int n = 1; n <= harmonics; n++) {
            double assignAmp = 1.0 / Math.pow(n, 0.9);
            double assignRatio =n * Math.sqrt(1.0 + B * n * n);
            double calibrationFactor = Math.log(baseFreqOriginal/110)*0.02;
            double highDiminishFactor = calibrationFactor * Math.pow(baseFreqOriginal, 0.3);
            double diminish = Math.exp(-highDiminishFactor * (n-1));
            if(diminish >= 1.5){
                diminish = 1.5;
            }
            assignAmp*=diminish;
            ampTable[ptrStride+n-1] =assignAmp;
            ratioTable[ptrStride + n-1] =assignRatio;
        }

    }
    public static final int harmonics = 16;
    public static void initiatePipe(double baseFreq, int ptrStride, double[] ampTable_arg, double[] ratioTable_arg){
            double B0 = 0.0;
            double baseFreqOriginal = baseFreq;
            baseFreq*=95.8920010653;
            double B = B0 * Math.pow(440/ baseFreq, 1.7);
            for (int n = 1; n <= harmonics; n+=2) {
                double assignAmp = 1.0 / Math.pow(n, 0.9);
                double assignRatio =n * Math.sqrt(1.0 + B * n * n);
                double calibrationFactor = Math.log(baseFreqOriginal/110)*0.02;
                double highDiminishFactor = calibrationFactor * Math.pow(baseFreqOriginal, 0.3);
                double diminish = Math.exp(-highDiminishFactor * (n-1));
                if(diminish >= 1.2){
                    diminish = 1.2;
                }
                assignAmp*=diminish;
                ampTable_arg[ptrStride+n-1] =assignAmp;
                ratioTable_arg[ptrStride + n-1] =assignRatio;
            }
    }
    public double getSample(double baseFreq, double time, int pitchSemitones){
        double ret = 0.0;
        int semitones = pitchSemitones;
        int indexShift = semitones*harmonics;
        for(int n = 1; n <= harmonics; n++){
            int index = indexShift+n-1;
            double amp =ampTable[index];
            double ratio = ratioTable[index];
            double arg = 2 * Math.PI * baseFreq * ratio * time;
            ret += amp * Math.sin(arg);
        }
        return ret;
    }
}
