package org.example;

import org.PrimitiveTabConverter.PrimitiveGuitarTabConverter;
import org.UserInterface.KeyboardOverlay;
import org.UserInterface.NotationViewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
//import java.awt.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

public class InputListener {
    private final Set<Integer> selectedKeys = new HashSet<>();
    private boolean running = true;

    public Map<Integer, Integer> freqMap;
    public static double startingFreq = 82;
    NotationViewer notationViewer;



    public InputListener() {
        // Define the keys you want to listen to

        NoteMapper a = new NoteMapper();
        String chromatic3 = Main.chromatic3;
        ArrayList<Integer> keys = a.readFromString(chromatic3);
        Map<Integer, Integer> map = a.chromaticFrequenciesInt(keys, startingFreq);
        freqMap = map;
        harmonicBehaviors=new ArrayList<>();


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
        String str = "<html>Octave:"+octaveOffset +"<br/>"
                + "updating <br> on jumps: "+updateTabs +"</html>";
        SwingUtilities.invokeLater(() ->
                octaveLabel.setText(str)
        );
    }


    JFrame frame;
    private JLabel octaveLabel;

    private int octaveOffset = 0;

    KeyboardOverlay keyboardOverlay;

    ArrayList<HarmonicBehavior> harmonicBehaviors;

    String currentTabs;
    boolean updateTabs = false;
    private void startListening() {
        // Swing GUI to capture key events



        frame = new JFrame("Key Listener");
        frame.setSize(1000, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setFocusable(true);
        frame.setVisible(true);
        this.player = new NotePlayer();

        HarmonicBehavior piano = new HarmonicBehavior(){};
        HarmonicBehavior pipe = new HarmonicBehavior(){
            @Override
            void init(double baseFreq, int ptrStride){
                initiatePipe(baseFreq, ptrStride, this.ampTable, this.ratioTable);
            }

        };
        harmonicBehaviors.add(piano);
        harmonicBehaviors.add(pipe);

        this.player.setHarmonicBehavior(piano);
        pipe.initHarmonics(NoteMapper.frequencies[0]);
        piano.initHarmonics(NoteMapper.frequencies[0]);



        frame.setLayout(new java.awt.BorderLayout());

        octaveLabel = new JLabel("Octave: 0", SwingConstants.CENTER);
        octaveLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));



        for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
            Integer value = entry.getValue();

               player.updateNoteStatus(value, true);
               player.updateNoteStatus(value, false);
        }
        this.keyboardOverlay = new KeyboardOverlay(player.NoteStatusMap);
        keyboardOverlay.initiateElements();

      //  frame.add(keyboardOverlay.grid);
        keyboardOverlay.setBorder(new EmptyBorder(0, 0, 200, 0));
        frame.add(keyboardOverlay, BorderLayout.SOUTH);
      //  frame.repaint();

        int fps = 60;
        int delay = 1000 / fps; // приблизно 16 мс

        String text;
        try{
            currentTabs = PrimitiveGuitarTabConverter.getFullText(Main.currentFile);
            text = PrimitiveGuitarTabConverter.convertTabsection(currentTabs);
            text = text.replace("\r\n", "\n")
                    .replace("\r", "\n")
                    .replace("-", " ");
        }catch (Exception exc){
            text = "";
        }
        NotationViewer viewer = new NotationViewer();

        viewer.setText(text);
        TextArea  notationTextArea=  viewer.notation;
        this.notationViewer= viewer;
   //     notationTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        notationTextArea.setBounds(200, 200, 500, 500);
         notationTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // move focus away from the TextArea
                    //t.getParent().requestFocus();
                    notationTextArea.transferFocus();;
                }
            }
        });




        keyboardOverlay.setPreferredSize(new Dimension(750, 180));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        bottomPanel.add(keyboardOverlay);
        bottomPanel.add(Box.createHorizontalStrut(10));
        bottomPanel.add(octaveLabel);

        frame.add(notationTextArea, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);




        Timer redrawTimer = new Timer(delay, e -> {

            keyboardOverlay.repaint();
             notationTextArea.repaint();
             bottomPanel.repaint();
        });

        redrawTimer.start();

        setDragDrop();

        waveThread();





        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {

                String KeyEventStr = "key event, key char \t" + e.getKeyChar()+"\t" + e.getExtendedKeyCode() +"\t" +e.getKeyCode() +"\t" + e.getKeyLocation();
                //System.out.println(KeyEventStr);
                if(e.getKeyCode() == KeyEvent.VK_BACK_QUOTE){
                    updateTabs= updateTabs ^= true;
                    updateOctaveLabel();
                    updateTabs_();
                }

                if(e.getKeyCode()== KeyEvent.VK_1){
                    if(e.isAltDown()){
                        player.setHarmonicBehavior(harmonicBehaviors.get(0));
                    }
                }
                if(e.getKeyCode()== KeyEvent.VK_2){
                    if(e.isAltDown()){
                        player.setHarmonicBehavior(harmonicBehaviors.get(1));
                    }
                }

                if (freqMap.containsKey(e.getKeyCode())) {

                    int freq = freqMap.get(e.getKeyCode());

                    player.updateNoteStatus(freq, true);
                }

                if(e.getKeyCode() == KeyEvent.VK_SPACE){
                    if(e.isShiftDown()){
                        for (int i = 0; i <4*12 ; i++) {
                            NoteMapper.frequencies[i]*=2;
                        }

                        PrimitiveGuitarTabConverter.lowestNoteCurrentTuning+=12;

                        octaveOffset+=1;
                        //player.initHarmonics(NoteMapper.frequencies[0]);
                        updateTabs_();
                        player.updateHarmonics(NoteMapper.frequencies[0]);
                        updateOctaveLabel();

                    }
                }
                if(e.getKeyCode() == KeyEvent.VK_SPACE){
                    if(e.isControlDown()){
                        for (int i = 0; i <4*12 ; i++) {
                            NoteMapper.frequencies[i]/=2;
                        }

                        System.out.println("notemapper freq 0 = " +NoteMapper.frequencies[0]);
                        player.updateHarmonics(NoteMapper.frequencies[0]);
                        PrimitiveGuitarTabConverter.lowestNoteCurrentTuning-=12;

                        if(updateTabs) {
                            String text = PrimitiveGuitarTabConverter.convertTabsection(currentTabs);
                            text = text.replace("\r\n", "\n")
                                    .replace("\r", "\n")
                                    .replace("-", " ");
                            viewer.notation.setText(text);
                        }
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
    private  void updateTabs_(){
        if(updateTabs) {
            String text = PrimitiveGuitarTabConverter.convertTabsection(currentTabs);
            text = text.replace("\r\n", "\n")
                    .replace("\r", "\n")
                    .replace("-", " ");
            this.notationViewer.setText(text);
            Font f = notationViewer.notation.getFont();
            System.out.printf("");
        }
    }

    private void loadFile(File file) {
        try {
            String content = new String(
                    java.nio.file.Files.readAllBytes(file.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8
            );

            // your parsing pipeline
            this.currentTabs = content;

            String text = PrimitiveGuitarTabConverter.convertTabsection(currentTabs);
            text = text.replace("\r\n", "\n")
                    .replace("\r", "\n")
                    .replace("-", " ");

            notationViewer.setText(text);

        } catch (Exception e) {
            System.err.println("failed to parse notation " + file.getName());
            e.printStackTrace();
        }
    }

    private void setDragDrop(){
        frame.setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    List<File> files = (List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    for (File file : files) {
                        System.out.println("Dropped file: " + file.getAbsolutePath());

                        // example: load your file
                        loadFile(file);
                    }

                    return true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

    }

    public void stop() {
        running = false;
    }

    public static void main(String[] args) {
        new InputListener();
    }
}





























