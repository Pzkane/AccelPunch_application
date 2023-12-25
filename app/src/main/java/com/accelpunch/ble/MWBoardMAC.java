package com.accelpunch.ble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.IBinder;

import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.data.Quaternion;

import bolts.Continuation;
import bolts.Task;

public class MWBoardMAC {
    private BtleService.LocalBinder _serviceBinder;
    private String MW_MAC_ADDRESS;
    private MetaWearBoard _board;
    private Activity _context;
    private Quaternion _q;
    private Acceleration _acc;

    public MWBoardMAC(Activity context, String mac_addr, IBinder serviceBinder) {
        MW_MAC_ADDRESS = mac_addr;
        _context = context;
        _serviceBinder = (BtleService.LocalBinder) serviceBinder;
    }

    public Task<Void> retrieveBoard() {
        final BluetoothManager btManager=
                (BluetoothManager) _context.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        // Create a MetaWear board object for the Bluetooth Device
        _board = _serviceBinder.getMetaWearBoard(remoteDevice);
        return _board.connectAsync();
    }

    public MetaWearBoard getBoard() throws Exception {
        if (_board != null) return _board;
        throw new Exception("No board connected!");
    }

    public void destroy() {
        if (_board == null) return;
        _board.disconnectAsync().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                System.out.println("Disconnected from MW board");
                return null;
            }
        });
    }

    public void setQuaternion(Quaternion value) {
        _q = value;
    }

    public void setAcceleration(Acceleration value) {
        _acc = value;
    }

    public Quaternion getQuaternion() {
        return _q;
    }

    public Acceleration getAcceleration() {
        return _acc;
    }
}
