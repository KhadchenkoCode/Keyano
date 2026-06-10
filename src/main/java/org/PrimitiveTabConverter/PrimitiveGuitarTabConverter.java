package org.PrimitiveTabConverter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class PrimitiveGuitarTabConverter {


    private static StringBuilder addSeparators(StringBuilder stringBuilder, String highestString) {
        String LineBreak = "\n----------------------------------------------------------------------------------------------------------------------------------------------\n";
        final int lengthBreak = LineBreak.length();
        final int lengthHigh = highestString.length();
        if(true) {
            int index = stringBuilder.indexOf(highestString);
            while (index != -1) {
                try {
                    stringBuilder.insert(index, LineBreak);
                    index = stringBuilder.indexOf(highestString, index+lengthBreak+lengthHigh);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder;

    }

    public static String getFullText(String filepath){
        StringBuilder fullText = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                fullText.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fullText.toString();
    }

    public static String parseFile(String filepath) {
        //tuningMap=new HashMap<>();
       String text = getFullText(filepath);
        return parse(text);

    }


    private static String parse(String input){
        return convertTabsection(input);

    }






    public static final String chromatic3 = "\\qazwsxedcrfvtgbyHNujmik<oL>p;?['";;



    static boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }



    static final String[] notes = {
           "C",
            "C#",
            "D",
            "D#",
            "E",
            "F",
            "F#",
            "G",
            "G#",
            "A",
            "A#",
            "B",
    };

    public static final byte C0_byte = 32-9;

    public static byte lowestNoteCurrentTuning;


    public static byte pitchFromNote(String stringBaseNote){
        String octave = stringBaseNote.charAt(stringBaseNote.length()-1)+"";
        String note = stringBaseNote.substring(0,stringBaseNote.length()-1);

        int octave_int = Integer.parseInt(octave);

        int index = -1;
        for (int i = 0; i < notes.length; i++) {
            if (notes[i].equals(note)) {
                index = i;
                break;
            }
        }
        return (byte)(octave_int*12+index+C0_byte);
    }

    public static byte pitchFromFret(String stringBaseNote, int fret) {
        byte basePitch = pitchFromNote(stringBaseNote);
        byte ret = (byte)(basePitch+fret);
        return ret;
    }


    public static String convertTabsection(String input) {
        if(input == null){
            return null;
        }
        String ret = input;
        String[] lines = ret.split("\n");
        StringBuilder fullText = new StringBuilder();

        for(int i = 0; i < lines.length; i++){
            if(lines[i].length() <4){
                fullText.append("\n");
                continue;
            }
            String baseString = lines[i].substring(0, 2);

            String[] beats = lines[i].split("\\|");
            StringBuilder linebuilder = new StringBuilder();
            for (int j = 0; j < beats.length; j++) {
                String beat = beats[j];
                StringBuilder beatBuilder = new StringBuilder(beat);

                String localNumber = "";
                for (int k = 0; k < beat.length(); k++) {
                    char c = beat.charAt(k);
                    if(c == '-' || c == '|'){
                        if(k>1){
                            if(localNumber.length()==0){
                                continue;
                            }


                            byte pitchByte = pitchFromFret(baseString, Byte.parseByte(localNumber));
                            byte button = (byte)(pitchByte-lowestNoteCurrentTuning);
                            char newCharReworked;
                            if(button<0){
                                newCharReworked = '@';
                            } else if(button>=chromatic3.length()){
                                newCharReworked = '#';
                            } else {
                            newCharReworked = chromatic3.charAt(button);
                            }

                            int length = localNumber.length();

                           beatBuilder.replace(k-length, k, insteadOfNumber(newCharReworked, length));

                            localNumber = "";
                        }
                    } else {
                        if(isDigit(c)){
                            localNumber += c;
                        }
                    }
                }





                linebuilder.append(beatBuilder);
                if(j<beats.length-1){ linebuilder.append("|"); }

            }
           fullText.append(linebuilder);

        }

        fullText=addSeparators(fullText, "E4");
        return  fullText.toString();
    }

    private static String insteadOfNumber(char c, int length){
        String ret = ""+c;
        for (int i = 1; i < length; i++) {
            ret+="-";
        }
        return ret;
    }

}
