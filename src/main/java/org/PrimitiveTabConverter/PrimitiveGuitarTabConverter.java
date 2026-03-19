package org.PrimitiveTabConverter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class PrimitiveGuitarTabConverter {

    /*


INPUT

E4|--------------------7------------
B3|-------8------8-7------8--7--------8---10
G3|----9----9-------------------9----
D3|--9-----------------------------9-
A2|7--------------------------------
E2|---------------------------------

OUTPUT
E4|--------------------{-------------------
B3|------->------>-L------>--L-------->---:
G3|----<----<-------------------<----------
D3|--U-----------------------------U-------
A2|V---------------------------------------
E2|----------------------------------------


     */

    public static String parseFile(String filepath) {
        tuningMap=new HashMap<>();
        StringBuilder fullText = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                fullText.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return parse( fullText.toString());

    }


    private static String parse(String input){
        int indexHeaderStart = input.indexOf("Header_Start");
        int indexHeaderEnd = input.indexOf("Header_End");

        final int HeaderStartLength = "Header_Start".length();





        //String substringHeader = input.substring(indexHeaderStart+HeaderStartLength+1, indexHeaderEnd-1);
        //parseHeader(substringHeader);


        return convertTabsection(input);

    }



    static HashMap<String, Integer> tuningMap;
    //establishes button corresponding to this string with open fret
    // from there 3chromatic scale is assumed
    // int index is a key to stringKeyboardLayout index

    private static void parseHeader(String input){
        /*

Header_Start
E4|0 1 2 3 4 5 6 7 8 --|
E4|< o l > p : ? { " --|

B3|0 1 2 3 4 5 6 7 8 9 10 11 12 13  --|
B3|u j m i k < o l > p :  ?  {  "   --|


G3|0 1 2 3 4 5 6 7 8 9 10 11 12|
G3|b y h n u j m i k < o  l  > |

D3|0 1 2 3 4 5 6 7 8 9 10 11 12|
D3|r f v t g b y h n u j  m  i |


A2|0 1 2 3 4 5 6 7 8 9 10-------|
A2|s x e d c r f v t g b--------|

E2|0 1 2 3 4 5 6 7 8 9 10-------|
E2|\ q a z w s x e d c r--------|

Header_End

         */

        String[] lines = input.split("\n");
        for(int i = 0; i < lines.length; i++){
            if(lines[i].length() <4){ continue;}

            String key = lines[i].substring(0, 2);//first 2 chars
            char fourthChar = lines[i].charAt(3);
            int indexOfChar = chromatic3.indexOf(fourthChar);
       //     char debugChar = chromatic3.charAt(0);
            if(indexOfChar>=0)
            tuningMap.put(key, indexOfChar);
            else {
                System.out.println("char not found in chromatic 3" + fourthChar);
            }
        }
    }

    static final String chromatic3 = "\\qazwsxedcrfvtgbyHNujmik<oL>p;?['";;

    private static char getButton(int fret, String string){
        int index = tuningMap.get(string);
        if(index+fret>=chromatic3.length()){
            return '1';
        }
        return chromatic3.charAt(index+fret);

    }

    static boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

    public static byte pitch(String stringBaseNote, int fret){
        return 0;
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

    public static final byte C0_byte = 32-9; // range 32-126

    public static byte lowestNoteCurrentTuning = (byte)pitchFromNote("E2");


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

    private static String convertTabsection(String input) {
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

//                            char newChar = getButton(Integer.parseInt(localNumber), baseString);

                            byte pitchByte = pitchFromFret(baseString, Byte.parseByte(localNumber));
                            byte button = (byte)(pitchByte-lowestNoteCurrentTuning);
                            char newCharReworked = chromatic3.charAt(button); // exact same operation but through a different method
                            int length = localNumber.length();
if(localNumber.equals("29")){
    System.out.println("debug");
}
                           beatBuilder.replace(k-length, k, insteadOfNumber(newCharReworked, length));

                            localNumber = "";
                        }
                    } else {
                        if(isDigit(c)){
                            localNumber += c;
                        }
                    }
                }

                //at this point we have a beatBuilder with converted beat, we should put it into newString somewhere
                linebuilder.append(beatBuilder);
                if(j<beats.length-1){ linebuilder.append("|"); }

            }
           fullText.append(linebuilder);
            if(baseString.equals("E2")&&false) {
                System.out.println("");
                linebuilder.insert(0, "|");
                        fullText.append("\n");
            }
        }
        return fullText.toString();
    }

    private static String insteadOfNumber(char c, int length){
        String ret = ""+c;
        for (int i = 1; i < length; i++) {
            ret+="-";
        }
        return ret;
    }



    public static String convertToNotes(String originalTabs){

    return "";

    }
}
