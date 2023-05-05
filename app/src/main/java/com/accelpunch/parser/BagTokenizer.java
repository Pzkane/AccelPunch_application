package com.accelpunch.parser;

public class BagTokenizer {
    public static BagToken tokenize(String text) {
        BagToken token = new BagToken();
        String[] tokens = text.split(":");
        token.setDirective(tokens[0]); // BAG
        token.setXAccel(Integer.parseInt(tokens[1])); // left hand acceleration X
        token.setYAccel(Integer.parseInt(tokens[2])); // left hand acceleration Y
        token.setZAccel(Integer.parseInt(tokens[3])); // left hand acceleration Z

        token.setTemp(Integer.parseInt(tokens[4])); // temperature
        return token;
    }
}
