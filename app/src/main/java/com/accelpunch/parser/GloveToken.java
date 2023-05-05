package com.accelpunch.parser;

public class GloveToken extends Token {
    private Integer _xR = 0;
    private Integer _yR = 0;
    private Integer _zR = 0;

    public Integer get_xL() {
        return get_x();
    }

    public Integer get_yL() {
        return get_y();
    }

    public Integer get_zL() {
        return get_z();
    }

    public Integer get_xR() {
        return _xR;
    }

    public Integer get_yR() {
        return _yR;
    }

    public Integer get_zR() {
        return _zR;
    }

    public void setXAccelL(Integer token) {
        setXAccel(token);
    }

    public void setYAccelL(Integer token) {
        setYAccel(token);
    }

    public void setZAccelL(Integer token) {
        setZAccel(token);
    }

    public void setXAccelR(Integer token) {
        _xR = token;
    }

    public void setYAccelR(Integer token) {
        _yR = token;
    }

    public void setZAccelR(Integer token) {
        _zR = token;
    }
}
