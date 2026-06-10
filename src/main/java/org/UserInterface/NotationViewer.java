package org.UserInterface;

import java.awt.*;

public class NotationViewer {
    String text;
    int position;
    int pace;
    boolean active;
    public TextArea notation;

    public void doSomething(){
       notation.setBounds(0, 0, 200, 200);
    }

    public void setText(String text){
        //String newText = transponeString(text);
        notation.setText(text);
    }

    public NotationViewer(){

      //  Font f1 = notation.getFont();
        notation = new TextArea();
       Font f = new Font("Monospaced", Font.PLAIN, 13);
        notation.setFont(f);

    }

    private String transponeString(String str){
        StringBuilder builder = new StringBuilder();
        String[] lines = str.split("\n");
        int maxLength = 1;
        for(int i = 0; i < lines.length; i++){
            int length = lines[i].length();
            if(length > maxLength){
                maxLength = length;
            }
        }



        for(int i = 0; i < maxLength; i++){
            StringBuilder addedLine = new StringBuilder();
            for(int j = 0; j < lines.length; j++){
                int length = lines[j].length();
                if(i<length) {
                    addedLine.append(lines[j].charAt(i));
                }
            }
            String strAdd = addedLine.toString()+"\n";
            if(strAdd.trim().isEmpty()) continue;
            builder.append(strAdd);
        }

        return builder.toString();
    }


}
