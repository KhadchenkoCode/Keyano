package org.example;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InputListener {
    private final Set<Integer> selectedKeys = new HashSet<>();
    private boolean running = true;

    public Map<Integer, Integer> freqMap;
    public static int startingFreq = 82;

    public InputListener() {
        // Define the keys you want to listen to

        NoteMapper a = new NoteMapper();
        String chromatic3 = "\\qazwsxedcrfvtgbyhnujmik,ol.p;/['";
        ArrayList<Integer> keys = a.readFromString(chromatic3);
        Map<Integer, Integer> map = a.chromaticFrequencies(keys, startingFreq);
        freqMap = map;

        // Create a thread to handle key listening
        Thread listenerThread = new Thread(this::startListening);
        listenerThread.start();


    }
    NotePlayer player;


    private void waveThread(){
        Thread thread = new Thread(() ->player.noteThreadLoop());
        thread.start();
    }
    static final int msAfterRelease = 0;


    boolean testFlag = true;
    private void startListening() {
        // Swing GUI to capture key events
        JFrame frame = new JFrame("Key Listener");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setFocusable(true);
        frame.setVisible(true);
        this.player = new NotePlayer();
        waveThread();

        if(testFlag&&false){
            synchronized (freqMap) {
                testFlag = false;
                for (Integer k : freqMap.keySet()) {
                    Integer originalValue = freqMap.get(k);
                    freqMap.put(originalValue, originalValue / 2);
                }
            }
        }

        this.player = new NotePlayer();
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {

                if (freqMap.containsKey(e.getKeyCode())) {

                    int freq = freqMap.get(e.getKeyCode());
                 //   System.out.println("Selected key pressed: " + KeyEvent.getKeyText(e.getKeyCode()));
                   // System.out.println("frequency = "+freq);
                    player.updateNoteStatus(freq, true);
                }


            }


            @Override
            public void keyReleased(KeyEvent e){
                if (freqMap.containsKey(e.getKeyCode())) {
                  //  System.out.println("Selected key released: " + KeyEvent.getKeyText(e.getKeyCode()));
                    int freq = freqMap.get(e.getKeyCode());
                    try {
                        Thread.sleep(msAfterRelease);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    player.updateNoteStatus(freq, false);
                }

            }

        });

        // Keep the thread alive
        while (running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        frame.dispose();
    }

    public void stop() {
        running = false;
    }

    public static void main(String[] args) {
        new InputListener();
    }
}





























