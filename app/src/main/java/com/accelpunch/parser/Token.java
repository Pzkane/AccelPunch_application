package com.accelpunch.parser;

public class Token {
    private String _directive;
    private Integer _x;
    private Integer _y;
    private Integer _z;

    public String get_directive() {
        return _directive;
    }

    public Integer get_x() {
        return _x;
    }

    public Integer get_y() {
        return _y;
    }

    public Integer get_z() {
        return _z;
    }

    public void setDirective(String token) {
        _directive = token;
    }

    public void setXAccel(Integer token) {
        _x = token;
    }

    public void setYAccel(Integer token) {
        _y = token;
    }

    public void setZAccel(Integer token) {
        _z = token;
    }

}
