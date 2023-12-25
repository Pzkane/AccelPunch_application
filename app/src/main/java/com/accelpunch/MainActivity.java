package com.accelpunch;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import com.accelpunch.ble.MWBoardMAC;
import com.accelpunch.databinding.ActivityMainBinding;
import com.accelpunch.net.Client;
import com.accelpunch.net.HttpRequest;
import com.accelpunch.parser.BagToken;
import com.accelpunch.parser.BagTokenizer;
import com.accelpunch.parser.GloveToken;
import com.accelpunch.parser.GlovesTokenizer;
import com.accelpunch.storage.room.Bag;
import com.accelpunch.storage.room.Glove;
import com.accelpunch.storage.room.LocalDatabase;
import com.accelpunch.storage.room.RoomEntity;
import com.accelpunch.storage.room.T3Vertebrae;
import com.accelpunch.storage.service.LocalDatabaseService;
import com.accelpunch.ui.dashboard.DashboardFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.data.Quaternion;
import com.mbientlab.metawear.module.SensorFusionBosch;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;

import bolts.CancellationTokenSource;
import bolts.Continuation;
import bolts.Task;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    private ActivityMainBinding _binding;
    private AlertDialog.Builder _alertBuilder;
    private MWBoardMAC _sensor_T3;
    private BtleService.LocalBinder _serviceBinder;
    static public Client clientGloves = null, clientBag = null;

    // Static node IPs
    static public String glovesIp = "192.168.43.2", bagIp = "192.168.43.3";
    static public Integer glovesPort = 8266, bagPort = 8267;
    static public String serverIP;
    static public Integer serverPort = 3000;
    static public boolean serverInitialized = false;
    static public LocalDatabase database;
    static public boolean databaseConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(_binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        getApplicationContext().bindService(new Intent(this, BtleService.class),
                this, Context.BIND_AUTO_CREATE);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(_binding.navView, navController);
        initLocalDatabase();
        showStartingDialog();
        initConnectedNodes();
    }

    private void initLocalDatabase() {
        databaseConnected = true;
        database = Room
                .databaseBuilder(getApplicationContext(), LocalDatabase.class, "punch-database-local")
                .build();
    }

    private Integer _hitCountL = 0, _hitCountR = 0, _hitCountBag = 0;
    private Long _timeframeL = null, _timeframeR = null, _timeframeBag = null;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        _serviceBinder = (BtleService.LocalBinder) service;
        _sensor_T3 = new MWBoardMAC(this, "F1:4C:58:5C:33:2D", _serviceBinder);
        _sensor_T3.retrieveBoard().continueWith(new Continuation<Void, Void>()  {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    System.out.println("Failed to connect to MW board");
                    return null;
                }
                System.out.println("Connected to MW board");
                System.out.println("Retrieving MW board");
                MetaWearBoard board = null;
                try {
                    board = _sensor_T3.getBoard();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                System.out.println("MAC of a connected board: " + board.getMacAddress());
                System.out.println("Board model: " + board.getModelString());
                final SensorFusionBosch sensorFusion = board.getModule(SensorFusionBosch.class);
                sensorFusion.configure()
                        .mode(SensorFusionBosch.Mode.NDOF)
                        .accRange(SensorFusionBosch.AccRange.AR_16G)
                        .gyroRange(SensorFusionBosch.GyroRange.GR_2000DPS)
                        .commit();

                sensorFusion.quaternion().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                _sensor_T3.setQuaternion(data.value(Quaternion.class));
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        sensorFusion.quaternion().start();
                        sensorFusion.linearAcceleration().addRouteAsync(new RouteBuilder() {
                            @Override
                            public void configure(RouteComponent source) {
                                source.stream(new Subscriber() {
                                    @Override
                                    public void apply(Data data, Object... env) {
                                        _sensor_T3.setAcceleration(data.value(Acceleration.class));
                                    }
                                });
                            }
                        }).continueWith(new Continuation<Route, Void>() {
                            @Override
                            public Void then(Task<Route> task) throws Exception {
                                sensorFusion.linearAcceleration().start();
                                return null;
                            }
                        });
                        sensorFusion.start();
                        return null;
                    }
                });
                return null;
            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) { }

    private enum EntityType { Glove, Bag, T3 };

    private void insertAndTransfer(RoomEntity nodeData, EntityType type) {
        switch (type) {
            case Glove:
                database.gloveDao().insert((Glove) nodeData);
                break;
            case Bag:
                database.bagDao().insert((Bag) nodeData);
                break;
            case T3:
                database.t3VertebraeDao().insert((T3Vertebrae) nodeData);
                break;
            default:
                break;
        }
        LocalDatabaseService.transferAllDataToServer(MainActivity.this);
    }

    private void initConnectedNodes() {
        final Observer<String> clientGlovesDataObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String text) {
                GloveToken token = GlovesTokenizer.tokenize(text);
                TextView homeText = findViewById(R.id.textGloves);
                GraphView dashGraphL = findViewById(R.id.idGraphViewL);
                GraphView dashGraphR = findViewById(R.id.idGraphViewR);

                final Integer delay = 300; // delay in ms between punches
                final Integer punchBaseline = 4000; // Tweak this for database access
                final Integer accelLeft = Math.abs(token.get_xL() + token.get_yL() + token.get_zL());
                final Integer accelRight = Math.abs(token.get_xR() + token.get_yR() + token.get_zR());
                Integer accelLeftPrev = DashboardFragment.lastAccelLeftToken != null
                        ? Math.abs(DashboardFragment.lastAccelLeftToken.get_xL()
                            + DashboardFragment.lastAccelLeftToken.get_yL()
                            + DashboardFragment.lastAccelLeftToken.get_zL())
                        : 0;
                Integer accelRightPrev = DashboardFragment.lastAccelRightToken != null
                        ? Math.abs(DashboardFragment.lastAccelRightToken.get_xR()
                            + DashboardFragment.lastAccelRightToken.get_yR()
                            + DashboardFragment.lastAccelRightToken.get_zR())
                        : 0;
                Long timestamp = System.currentTimeMillis();

                if (accelLeft >= punchBaseline && accelLeft > accelLeftPrev) {
                    DashboardFragment.lastAccelLeftToken = token;
                } else if (accelLeft < accelLeftPrev && accelLeftPrev >= punchBaseline) {
                    if (_timeframeL == null || System.currentTimeMillis() - _timeframeL >= delay) {
                        Glove gl = new Glove();
                        gl.time = System.currentTimeMillis();
                        gl.glove = 'L';
                        gl.x = DashboardFragment.lastAccelLeftToken.get_xL();
                        gl.y = DashboardFragment.lastAccelLeftToken.get_yL();
                        gl.z = DashboardFragment.lastAccelLeftToken.get_zL();
                        gl.roll = DashboardFragment.lastAccelLeftToken.getRollL();
                        gl.pitch = DashboardFragment.lastAccelLeftToken.getPitchL();
                        registerAndInsertT3(gl.time);
                        insertAndTransfer(gl, EntityType.Glove);
                        _timeframeL = timestamp;
                        System.out.println("Hit Left! " + _hitCountL++);
                        DashboardFragment.flashLGlove(MainActivity.this, getResources().getColor(R.color.flashL, getTheme()));
                    }
                    DashboardFragment.lastAccelLeftToken = token;
                }

                if (accelRight >= punchBaseline && accelRight > accelRightPrev) {
                    DashboardFragment.lastAccelRightToken = token;
                } else if (accelRight < accelRightPrev && accelRightPrev >= punchBaseline) {
                    if (_timeframeR == null || System.currentTimeMillis() - _timeframeR >= delay) {
                        Glove gl = new Glove();
                        gl.time = System.currentTimeMillis();
                        gl.glove = 'R';
                        gl.x = DashboardFragment.lastAccelRightToken.get_xR();
                        gl.y = DashboardFragment.lastAccelRightToken.get_yR();
                        gl.z = DashboardFragment.lastAccelRightToken.get_zR();
                        gl.roll = DashboardFragment.lastAccelRightToken.getRollR();
                        gl.pitch = DashboardFragment.lastAccelRightToken.getPitchR();
                        registerAndInsertT3(gl.time);
                        insertAndTransfer(gl, EntityType.Glove);
                        _timeframeR = timestamp;
                        System.out.println("Hit Right!" + _hitCountR++);
                        DashboardFragment.flashRGlove(MainActivity.this, getResources().getColor(R.color.flashR, getTheme()));
                    }
                    DashboardFragment.lastAccelRightToken = token;
                }

                if (DashboardFragment.seriesL != null)
                    DashboardFragment.seriesL.appendData(new DataPoint(new Date(),accelLeft), true, 60);
                if (DashboardFragment.seriesR != null)
                    DashboardFragment.seriesR.appendData(new DataPoint(new Date(),accelRight), true, 60);

                if (homeText != null)
                    homeText.setText(text);
                if (dashGraphL != null) {
                    dashGraphL.onDataChanged(false, true);
                    dashGraphR.onDataChanged(false, true);
                }
            }

            private void registerAndInsertT3(long time) {
                T3Vertebrae t3 = new T3Vertebrae();
                t3.time = time;
                t3.w = _sensor_T3.getQuaternion().w();
                t3.x = _sensor_T3.getQuaternion().x();
                t3.y = _sensor_T3.getQuaternion().y();
                t3.z = _sensor_T3.getQuaternion().z();
                t3.xg = _sensor_T3.getAcceleration().x();
                t3.yg = _sensor_T3.getAcceleration().y();
                t3.zg = _sensor_T3.getAcceleration().z();
                // This will not transfer as T3 records are exported only with glove records
                insertAndTransfer(t3, EntityType.T3);
            }
        };
        final Observer<String> clientBagDataObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String text) {
                BagToken token = BagTokenizer.tokenize(text);
                TextView homeText = findViewById(R.id.textBag);
                GraphView dashGraphBag = findViewById(R.id.idGraphViewBag);

                final Integer delay = 400; // delay in ms between hits on a bag
                final Integer hitBaseline = 1400; // Tweak this for database access
                final Integer accelBag = Math.abs(token.get_x() + token.get_y());
                Integer accelBagPrev = DashboardFragment.lastAccelBagToken != null
                        ? Math.abs(DashboardFragment.lastAccelBagToken.get_x()
                        + DashboardFragment.lastAccelBagToken.get_y()
                        )
                        : 0;
                final Long timestamp = System.currentTimeMillis();

                if (accelBag >= hitBaseline && accelBag > accelBagPrev) {
                    DashboardFragment.lastAccelBagToken = token;
                } else if (accelBag < accelBagPrev && accelBagPrev >= hitBaseline) {
                    if (_timeframeBag == null || System.currentTimeMillis() - _timeframeBag >= delay) {
                        Bag bag = new Bag();
                        bag.time = System.currentTimeMillis();
                        bag.x = DashboardFragment.lastAccelBagToken.get_x();
                        bag.y = DashboardFragment.lastAccelBagToken.get_y();
                        bag.z = DashboardFragment.lastAccelBagToken.get_z();
                        bag.temp = DashboardFragment.lastAccelBagToken.get_temp();
                        insertAndTransfer(bag, EntityType.Bag);
                        _timeframeBag = timestamp;
                        System.out.println("Hit Bag! " + _hitCountBag++);
                        DashboardFragment.flashBag(MainActivity.this, getResources().getColor(R.color.flashBag, getTheme()), false);
                    }
                    DashboardFragment.lastAccelBagToken = token;
                }

                if (homeText != null)
                    homeText.setText(text);
                if (dashGraphBag != null) {
                    DashboardFragment.seriesBag.appendData(new DataPoint(new Date(),Math.abs(token.get_x() + token.get_y())), true, 60);
                    dashGraphBag.onDataChanged(false, true);
                }
            }
        };

        clientGloves = new Client(glovesIp,glovesPort);
        clientBag = new Client(bagIp,bagPort);

        clientGloves.getData().observe(MainActivity.this, clientGlovesDataObserver);
        clientBag.getData().observe(MainActivity.this, clientBagDataObserver);
    }

    public void showStartingDialog() {
        serverIP = "";
        // Create dialog to specify server IP address
        EditText serverIPInput = new EditText(this);
        _alertBuilder = new AlertDialog.Builder(this);
        _alertBuilder
                .setTitle("Server IP address")
                .setView(serverIPInput)
                .setPositiveButton("Ping", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Stub listener to prevent dialog from closing on button press
                    }
                })
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = _alertBuilder.create();
        dialog.show();

        HttpRequest request = new HttpRequest(MainActivity.this, null, serverPort.toString(), "GET", null);;
        final Observer<String> responseObserver = new Observer<String>() {
            @Override
            public void onChanged(final String text) {
                if (text.isEmpty()) return;
                try {
                    if (request.connected == true && request.connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        MainActivity.serverIP = serverIPInput.getText().toString();
                        Toast.makeText(MainActivity.this, "Connection established", Toast.LENGTH_LONG).show();
                        serverInitialized = true;
                        LocalDatabaseService.transferAllDataToServer(MainActivity.this);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, request.getResponse().getValue().toString(), Toast.LENGTH_LONG).show();
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        request.getResponse().observe(MainActivity.this, responseObserver);

        // Finally specify dialog's positive button listener
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("".equals(serverIPInput.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Provide valid server IP address", Toast.LENGTH_LONG).show();
                } else {
                    request.setUrl(serverIPInput.getText().toString().trim());
                    request.execute();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
        _sensor_T3.destroy();
    }
}