package com.ror.engine;

public class RealmsOfRiftborne {
    public static void main(String[] args) {
        System.out.println("Debug Build Prototype v1.4.0");
        
        javax.swing.SwingUtilities.invokeLater(() -> {
                new GameFrame();
        });
    }
}