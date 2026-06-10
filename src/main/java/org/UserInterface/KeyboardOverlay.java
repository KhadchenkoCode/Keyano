package org.UserInterface;


import org.PrimitiveTabConverter.PrimitiveGuitarTabConverter;
import org.example.Main;
import org.example.NoteMapper;
import org.example.NoteState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;


public class KeyboardOverlay extends JPanel {

    //PerFrame each of the playable buttons gets brightness set to volume of the note that corresponds to it



    public KeyboardOverlay(Map<Integer, NoteState> parentMap) {
        this.stateMap = parentMap;
    }

    public Map<Integer, NoteState> stateMap;

    public Map<Integer, KeyVisualiser> keyVisualiserMap;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Map.Entry<Integer, NoteState> entry : stateMap.entrySet()){

            int semitoneIndex = entry.getKey();
            int keyboardCode = NoteMapper.keyCodeFromChar(Main.chromatic3.charAt(semitoneIndex));
            double volume = entry.getValue().currentVolume;
            KeyVisualiser keyVisualiser = keyVisualiserMap.get(keyboardCode);
            boolean using_existing = false;
            if(keyVisualiser == null){
                System.out.println("KeyVisualiser is null in KeyboardOverlay paintComponent");

            } else {
                using_existing = true;
            }


            if(keyVisualiser.bounds==null){
                //should never happen
            }

            keyVisualiser.brightness = (float)volume;
            try {
                keyVisualiser.draw(g);
            }catch(Exception e){
                e.printStackTrace();
            }

        }

    }

    private static KeyVisualiser createKeyVisualiser(int keyboardCode){
        KeyVisualiser keyVisualiser = new KeyVisualiser();
        Point2D position = KeyPositioner.positionFromKeyCode(keyboardCode);
        char c = (char)(keyboardCode);
        keyVisualiser.bounds = KeyVisualiser.rectangleFromPoint(position);
        keyVisualiser.text = "" + c;
        return keyVisualiser;

    }



    public void initiateElements(){
        keyVisualiserMap = new HashMap<Integer, KeyVisualiser>();
        for (Map.Entry<Integer, NoteState> entry : stateMap.entrySet()){
            Integer semitoneIndex = entry.getKey();
            char letter = Main.chromatic3.charAt(semitoneIndex);
            int keyboardCode = KeyEvent.getExtendedKeyCodeForChar(letter);
        if(keyboardCode==153){
            System.out.println("debug");
        }
        addKeyVisualiser(keyboardCode);

        }

    }



    private void addKeyVisualiser(Integer keyCode){
        if(keyCode == 0){
            System.out.println("Keycode = 0 in method addKeyVisualiser");
        }

        KeyVisualiser kv = createKeyVisualiser(keyCode);
     //   System.out.println("KeyboardOverlay.addKeyVisualiser ");
        System.out.printf("");
        keyVisualiserMap.put(keyCode, kv);
    }





}
