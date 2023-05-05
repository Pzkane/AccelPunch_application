package com.accelpunch.storage.service;

import android.app.Activity;

import androidx.annotation.Nullable;
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
    static public void transferAllDataToServer (Activity context) {
        final Observer<String> responseObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String text) {
                // Stub
            }
        };
        HttpRequest requestGloves = new HttpRequest(context, MainActivity.serverIP, MainActivity.serverPort.toString(), "POST", null);
        requestGloves.getResponse().observe((LifecycleOwner) context, responseObserver);
        ListenableFuture<List<Glove>> futureGloves = MainActivity.database.gloveDao().getAll();
        futureGloves.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Glove> result = futureGloves.get();
//                  if (result.size() > 0)
//                    MainActivity.database.gloveDao().purge();
                    JSONObject payload = new JSONObject();
                    JSONArray gloves = new JSONArray();
                    for (Glove glove : result) {
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
                    System.out.println(result.toArray().length);
                    requestGloves.setPayload(payload.toString());
                    requestGloves.execute();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, Executors.newSingleThreadExecutor());

        HttpRequest requestBag = new HttpRequest(context, MainActivity.serverIP, MainActivity.serverPort.toString(), "POST", null);
        requestBag.getResponse().observe((LifecycleOwner) context, responseObserver);
        ListenableFuture<List<Bag>> futureBag = MainActivity.database.bagDao().getAll();
        futureBag.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Bag> result = futureBag.get();
//                  if (result.size() > 0)
//                    MainActivity.database.bagDao().purge();
                    JSONObject payload = new JSONObject();
                    JSONArray bagRecords = new JSONArray();
                    for (Bag bag : result) {
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
                    System.out.println(result.toArray().length);
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
