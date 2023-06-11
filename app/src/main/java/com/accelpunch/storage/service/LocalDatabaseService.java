package com.accelpunch.storage.service;

import android.app.Activity;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.accelpunch.MainActivity;
import com.accelpunch.net.HttpRequest;
import com.accelpunch.storage.room.Bag;
import com.accelpunch.storage.room.Glove;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class LocalDatabaseService {
    static private final Integer _batchSize = 50;
    static private List<Glove> gloveResultSet;
    static private List<Bag> bagRecordResultSet;

    static public void transferAllDataToServer (Activity context) {
        transferGloveDataToServer(context);
        transferBagDataToServer(context);
    }

    static public void transferGloveDataToServer (Activity context) {
        HttpRequest requestGloves = new HttpRequest(context, MainActivity.serverIP, MainActivity.serverPort.toString(), "POST", null);
        final Observer<String> responseObserver = new Observer<String>() {
            @Override
            public void onChanged(final String text) {
                try {
                    if ("".equals(text)) return;
                    JSONObject response = new JSONObject(text);
                    if (response.getInt("status") == 200) {
                        System.out.println("Response: " + text);
                        System.out.println("Connection OK");
                        MainActivity.database.gloveDao().delete(gloveResultSet);
                    }
                } catch (JSONException e) {
                    System.out.println("Error while creating JSON object response for glove dataset transfer");
                }
            }
        };
        requestGloves.getResponse().observe((LifecycleOwner) context, responseObserver);
        ListenableFuture<List<Glove>> futureGloves = MainActivity.database.gloveDao().getAll();
        futureGloves.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    gloveResultSet = futureGloves.get();
                    if (gloveResultSet.size() < _batchSize) return;
                    System.out.println("Glove count on transfer after purge: " + gloveResultSet.size());
                    JSONObject payload = new JSONObject();
                    JSONArray gloves = new JSONArray();
                    for (Glove glove : gloveResultSet) {
                        gloves.put(new JSONObject(
                                "{" +
                                        "\"timestamp\":"+glove.time + "," +
                                        "\"glove\":"+glove.glove + "," +
                                        "\"x\":"+glove.x + "," +
                                        "\"y\":"+glove.y + "," +
                                        "\"z\":"+glove.z +
                                        "}"));
                    }
                    payload.put("gloves", gloves);
                    System.out.println(payload.toString());
                    System.out.println(gloveResultSet.toArray().length);
                    requestGloves.setPayload(payload.toString());
                    requestGloves.execute();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, Executors.newSingleThreadExecutor());
    }

    static public void transferBagDataToServer (Activity context) {
        HttpRequest requestBag = new HttpRequest(context, MainActivity.serverIP, MainActivity.serverPort.toString(), "POST", null);
        final Observer<String> responseObserver = new Observer<String>() {
            @Override
            public void onChanged(final String text) {
                try {
                    if ("".equals(text)) return;
                    JSONObject response = new JSONObject(text);
                    if (response.getInt("status") == 200) {
                        System.out.println("Response: " + text);
                        System.out.println("Connection OK");
                        MainActivity.database.bagDao().delete(bagRecordResultSet);
                    }
                } catch (JSONException e) {
                    System.out.println("Error while creating JSON object response for bag dataset transfer");
                }
            }
        };
        requestBag.getResponse().observe((LifecycleOwner) context, responseObserver);
        ListenableFuture<List<Bag>> futureBag = MainActivity.database.bagDao().getAll();
        futureBag.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    bagRecordResultSet = futureBag.get();
                    if (bagRecordResultSet.size() < _batchSize) return;
                    System.out.println("Bag record count on transfer after purge: " + bagRecordResultSet.size());
                    JSONObject payload = new JSONObject();
                    JSONArray bagRecords = new JSONArray();
                    for (Bag bag : bagRecordResultSet) {
                        bagRecords.put(new JSONObject(
                                "{" +
                                        "\"timestamp\":"+bag.time + "," +
                                        "\"temperature\":"+bag.temp + "," +
                                        "\"x\":"+bag.x + "," +
                                        "\"y\":"+bag.y + "," +
                                        "\"z\":"+bag.z +
                                        "}"));
                    }
                    payload.put("bag", bagRecords);
                    System.out.println(payload.toString());
                    System.out.println(bagRecordResultSet.toArray().length);
                    requestBag.setPayload(payload.toString());
                    requestBag.execute();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, Executors.newSingleThreadExecutor());
    }
}
