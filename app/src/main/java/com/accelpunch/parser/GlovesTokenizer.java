package com.accelpunch.parser;

public class GlovesTokenizer {
    public static GloveToken tokenize(String text) {
        GloveToken token = new GloveToken();
        String[] tokens = text.split(":");
        token.setDirective(tokens[0]); // GLOVE
        token.setXAccelL(Integer.parseInt(tokens[1])); // left hand acceleration X
        token.setYAccelL(Integer.parseInt(tokens[2])); // left hand acceleration Y
        token.setZAccelL(Integer.parseInt(tokens[3])); // left hand acceleration Z

        token.setXAccelR(Integer.parseInt(tokens[4])); // right hand acceleration X
        token.setYAccelR(Integer.parseInt(tokens[5])); // right hand acceleration Y
        token.setZAccelR(Integer.parseInt(tokens[6])); // right hand acceleration Z

        token.setRollL(Float.parseFloat(tokens[7])); // left hand roll angle
        token.setPitchL(Float.parseFloat(tokens[8])); // left hand pitch angle

        token.setRollR(Float.parseFloat(tokens[9])); // right hand roll angle
        token.setPitchR(Float.parseFloat(tokens[10])); // right hand pitch angle

        return token;
    }
}
