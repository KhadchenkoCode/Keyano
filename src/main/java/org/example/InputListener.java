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
        Map<Integer, Integer> map = a.chromaticFrequenciesInt(keys, startingFreq);
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

    private void setAllTimesInit(){




    }

    private void updateOctaveLabel() {
        SwingUtilities.invokeLater(() ->
                octaveLabel.setText("Octave: " + octaveOffset)
        );
    }


    JFrame frame;
    private JLabel octaveLabel;

    private int octaveOffset = 0;
    private void startListening() {
        // Swing GUI to capture key events
        frame = new JFrame("Key Listener");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setFocusable(true);
        frame.setVisible(true);
        this.player = new NotePlayer();
        player.initHarmonics(startingFreq);

        frame.setLayout(new java.awt.BorderLayout());

        octaveLabel = new JLabel("Octave: 0", SwingConstants.CENTER);
        octaveLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));

        frame.add(octaveLabel, java.awt.BorderLayout.CENTER);

        for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
            Integer value = entry.getValue();

               player.updateNoteStatus(value, true);
               player.updateNoteStatus(value, false);
        }

        waveThread();



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

                if(e.getKeyCode() == KeyEvent.VK_SPACE){
                    if(e.isShiftDown()){
                        for (int i = 0; i <4*12 ; i++) {
                            NoteMapper.frequencies[i]*=2;
                        }
                        octaveOffset+=1;
                        player.initHarmonics(NoteMapper.frequencies[0]);
                        updateOctaveLabel();

                    }
                }
                if(e.getKeyCode() == KeyEvent.VK_SPACE){
                    if(e.isControlDown()){
                        for (int i = 0; i <4*12 ; i++) {
                            NoteMapper.frequencies[i]/=2;
                        }

                        player.initHarmonics(NoteMapper.frequencies[0]);
                        octaveOffset-=1;
                        updateOctaveLabel();
                    }
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





























