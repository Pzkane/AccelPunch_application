package com.accelpunch.parser;

public class GloveToken extends Token {
    private Integer _xR = 0;
    private Integer _yR = 0;
    private Integer _zR = 0;
    private Float _rollL = 0f;
    private Float _rollR = 0f;
    private Float _pitchL = 0f;
    private Float _pitchR = 0f;


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

    public Float getRollL() { return _rollL; }

    public Float getPitchL() { return _pitchL; }

    public Float getRollR() { return _rollR; }

    public Float getPitchR() { return _pitchR; }

    public void setXAccelL(Integer token) {
        setXAccel(token);
    }

    public void setYAccelL(Integer token) {
        setYAccel(token);
    }

    public void setZAccelL(Integer token) {
        setZAccel(token);
    }

    public void setXAccelR(Integer token) { _xR = token; }

    public void setYAccelR(Integer token) {
        _yR = token;
    }

    public void setZAccelR(Integer token) { _zR = token; }

    public void setRollL(Float token) { _rollL = token; }

    public void setRollR(Float token) { _rollR = token; }

    public void setPitchL(Float token) {
        _pitchL = token;
    }

    public void setPitchR(Float token) {
        _pitchR = token;
    }
}
