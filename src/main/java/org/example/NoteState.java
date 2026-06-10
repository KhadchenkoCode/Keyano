package org.example;

public class NoteState {

    public int semitones;
    public double pressTime;
    public double releaseTime;
    public boolean pressed;
    public double currentVolume;

    NoteState(int semitones, double pressTime, double releaseTime) {
        this.semitones = semitones;
        this.pressTime = pressTime;
        this.releaseTime = releaseTime;

    }

    NoteState(int semitones, double pressTime) {
        this.semitones = semitones;
        this.pressTime = pressTime;
        currentVolume = 0;

    }

}
