package org.example.ui;

import lombok.Getter;

import javax.swing.*;


public class ProgressBar extends JFrame{
    @Getter
    private JProgressBar jb;

    public ProgressBar(int maxVal){
        jb=new JProgressBar(0,maxVal);
        jb.setBounds(40,40,160,30);
        jb.setValue(0);
        jb.setStringPainted(true);
        add(jb);
        setSize(250,150);
        setLayout(null);
        setLocationRelativeTo(null);
    }
}
