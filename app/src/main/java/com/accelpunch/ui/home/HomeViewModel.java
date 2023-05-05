package com.accelpunch.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> _textGloves, _textBag;

    public HomeViewModel() {
        _textGloves = new MutableLiveData<>();
        _textBag = new MutableLiveData<>();

        _textGloves.setValue("Glove values go here...");
        _textBag.setValue("Bag values go here...");
    }

    public LiveData<String> getGlovesText() {
        return _textGloves;
    }

    public LiveData<String> getBagText() {
        return _textBag;
    }
}