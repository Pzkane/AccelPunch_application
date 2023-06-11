package com.accelpunch.ui.home;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.accelpunch.MainActivity;
import com.accelpunch.R;
import com.accelpunch.databinding.FragmentHomeBinding;
import com.accelpunch.net.HttpRequest;
import com.accelpunch.storage.service.LocalDatabaseService;

import org.json.JSONException;
import org.json.JSONObject;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding _binding;
    private Button _pingBtn, _updateServerIp;
    private TextView _outputBox;
    private HomeViewModel _homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        _homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        _binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = _binding.getRoot();

        final TextView textViewGloves = _binding.textGloves, textViewBag = _binding.textBag;
        _homeViewModel.getGlovesText().observe(getViewLifecycleOwner(), textViewGloves::setText);
        _homeViewModel.getBagText().observe(getViewLifecycleOwner(), textViewBag::setText);

        _pingBtn = root.findViewById(R.id.ipBtn);
        _updateServerIp = root.findViewById(R.id.serverIpBtn);
        _outputBox = root.findViewById(R.id.ipOutput);
        _outputBox.setMovementMethod(new ScrollingMovementMethod());

        _pingBtn.setOnClickListener(getToServer);
        _updateServerIp.setOnClickListener(refreshAPs);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    private View.OnClickListener refreshAPs = new View.OnClickListener() {
        @Override
        public void onClick(View v){
            ((MainActivity)getActivity()).showStartingDialog();
        }
    };

    private View.OnClickListener getToServer = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if ("".equals(MainActivity.serverIP) || MainActivity.serverIP == null) {
                ((MainActivity)getActivity()).showStartingDialog();
                return;
            }
            HttpRequest request = new HttpRequest(getActivity(), MainActivity.serverIP, MainActivity.serverPort.toString(), "GET", null);
            final Observer<String> responseObserver = new Observer<String>() {
                private StringBuilder parseStats(String title, JSONObject stats, JSONObject shadowStats) throws JSONException {
                    final String RED_COLOR_OPEN = "<font color=\"red\">";
                    final String GREEN_COLOR_OPEN = "<font color=\"green\">";
                    final String BLUE_COLOR_OPEN = "<font color=\"blue\">";
                    final String COLOR_CLOSE = "</font>";

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("<h5>" + title + ":<h5");
                    stringBuilder.append("<ul>");

                    // Strongest hands
                    if (!"0".equals(stats.getJSONObject("strongest_left").getString("x"))) {
                        System.out.println(stats.getJSONObject("strongest_left").getString("x"));
                        stringBuilder.append("<li>strongest Left punch was on ");
                        stringBuilder.append(BLUE_COLOR_OPEN);
                        stringBuilder.append(stats.getJSONObject("strongest_left").getString("x"));
                        stringBuilder.append(COLOR_CLOSE);
                        stringBuilder.append(" with force of ");
                        stringBuilder.append(GREEN_COLOR_OPEN);
                        stringBuilder.append(Double.toString(stats.getJSONObject("strongest_left").getDouble("y") / 1000) + "g</li>");
                        stringBuilder.append(COLOR_CLOSE);
                    }

                    if (!"0".equals(stats.getJSONObject("strongest_right").getString("x"))) {
                        stringBuilder.append("<li>strongest Right punch was on ");
                        stringBuilder.append(BLUE_COLOR_OPEN);
                        stringBuilder.append(stats.getJSONObject("strongest_right").getString("x"));
                        stringBuilder.append(COLOR_CLOSE);
                        stringBuilder.append(" with force of ");
                        stringBuilder.append(GREEN_COLOR_OPEN);
                        stringBuilder.append(Double.toString(stats.getJSONObject("strongest_right").getDouble("y") / 1000) + "g</li>");
                        stringBuilder.append(COLOR_CLOSE);
                    }

                    // Fastest hands
                    if (!"0".equals(stats.getJSONObject("fastest_left").getJSONObject("punch").getString("x"))) {
                        stringBuilder.append("<li>fastest Left punch was on ");
                        stringBuilder.append(BLUE_COLOR_OPEN);
                        stringBuilder.append(stats.getJSONObject("fastest_left").getJSONObject("punch").getString("x"));
                        stringBuilder.append(COLOR_CLOSE);
                        stringBuilder.append(" with speed of ");
                        stringBuilder.append(RED_COLOR_OPEN);
                        stringBuilder.append(Double.toString(Math.round(stats.getJSONObject("fastest_left").getDouble("speed_mps") * 100.0) / 100.0) + "m/s</li>");
                        stringBuilder.append(COLOR_CLOSE);
                    }

                    if (!"0".equals(stats.getJSONObject("fastest_right").getJSONObject("punch").getString("x"))) {
                        stringBuilder.append("<li>fastest Right punch was on ");
                        stringBuilder.append(BLUE_COLOR_OPEN);
                        stringBuilder.append(stats.getJSONObject("fastest_right").getJSONObject("punch").getString("x"));
                        stringBuilder.append(COLOR_CLOSE);
                        stringBuilder.append(" with speed of ");
                        stringBuilder.append(RED_COLOR_OPEN);
                        stringBuilder.append(Double.toString(Math.round(stats.getJSONObject("fastest_right").getDouble("speed_mps") * 100.0) / 100.0) + "m/s</li>");
                        stringBuilder.append(COLOR_CLOSE);
                    }

                    // Strongest hit on a bag
                    if (!"0".equals(stats.getJSONObject("bag").getString("x"))) {
                        stringBuilder.append("<li>strongest force applied to the Bag was on ");
                        stringBuilder.append(BLUE_COLOR_OPEN);
                        stringBuilder.append(stats.getJSONObject("bag").getString("x"));
                        stringBuilder.append(COLOR_CLOSE);
                        stringBuilder.append(" with force of ");
                        stringBuilder.append(GREEN_COLOR_OPEN);
                        stringBuilder.append(Double.toString(stats.getJSONObject("bag").getDouble("y") / 1000) + "g</li>");
                        stringBuilder.append(COLOR_CLOSE);
                    }

                    // Shadowboxing
                    stringBuilder.append("<li><h6>Shadowboxing:</h6><ul>\n");
                    // Strongest hands
                    if (!"0".equals(shadowStats.getJSONObject("strongest_left").getString("x"))) {
                        stringBuilder.append("<li>strongest Left punch was on ");
                        stringBuilder.append(BLUE_COLOR_OPEN);
                        stringBuilder.append(shadowStats.getJSONObject("strongest_left").getString("x"));
                        stringBuilder.append(COLOR_CLOSE);
                        stringBuilder.append(" with force of ");
                        stringBuilder.append(GREEN_COLOR_OPEN);
                        stringBuilder.append(Double.toString(shadowStats.getJSONObject("strongest_left").getDouble("y") / 1000) + "g</li>");
                        stringBuilder.append(COLOR_CLOSE);
                    }

                    if (!"0".equals(shadowStats.getJSONObject("strongest_right").getString("x"))) {
                        stringBuilder.append("<li>strongest Right punch was on ");
                        stringBuilder.append(BLUE_COLOR_OPEN);
                        stringBuilder.append(shadowStats.getJSONObject("strongest_right").getString("x"));
                        stringBuilder.append(COLOR_CLOSE);
                        stringBuilder.append(" with force of ");
                        stringBuilder.append(GREEN_COLOR_OPEN);
                        stringBuilder.append(Double.toString(shadowStats.getJSONObject("strongest_right").getDouble("y") / 1000) + "g</li>");
                        stringBuilder.append(COLOR_CLOSE);
                    }

                    // Fastest hands
                    if (!"0".equals(shadowStats.getJSONObject("fastest_left").getJSONObject("punch").getString("x"))) {
                        stringBuilder.append("<li>fastest Left punch was on ");
                        stringBuilder.append(BLUE_COLOR_OPEN);
                        stringBuilder.append(shadowStats.getJSONObject("fastest_left").getJSONObject("punch").getString("x"));
                        stringBuilder.append(COLOR_CLOSE);
                        stringBuilder.append(" with speed of ");
                        stringBuilder.append(RED_COLOR_OPEN);
                        stringBuilder.append(Double.toString(Math.round(shadowStats.getJSONObject("fastest_left").getDouble("speed_mps") * 100.0) / 100.0) + "m/s</li>");
                        stringBuilder.append(COLOR_CLOSE);
                    }

                    if (!"0".equals(shadowStats.getJSONObject("fastest_right").getJSONObject("punch").getString("x"))) {
                        stringBuilder.append("<li>fastest Right punch was on ");
                        stringBuilder.append(BLUE_COLOR_OPEN);
                        stringBuilder.append(shadowStats.getJSONObject("fastest_right").getJSONObject("punch").getString("x"));
                        stringBuilder.append(COLOR_CLOSE);
                        stringBuilder.append(" with speed of ");
                        stringBuilder.append(RED_COLOR_OPEN);
                        stringBuilder.append(Double.toString(Math.round(shadowStats.getJSONObject("fastest_right").getDouble("speed_mps") * 100.0) / 100.0) + "m/s</li>");
                        stringBuilder.append(COLOR_CLOSE);
                    }

                    stringBuilder.append("</ul></li></ul>");
                    return stringBuilder;
                }
                @Override
                public void onChanged(final String text) {
                    try {
                        if ("".equals(text)) return;
                        JSONObject response = new JSONObject(text);
                        if (response.getInt("status") == 200) {
                            JSONObject stats = response.getJSONObject("data");
                            JSONObject hourStats = stats.getJSONObject("hour");
                            JSONObject hourStatsShadow = hourStats.getJSONObject("shadow");
                            JSONObject overallStats = stats.getJSONObject("overall");
                            JSONObject overallStatsShadow = overallStats.getJSONObject("shadow");
                            System.out.println(stats.toString(4));
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("<h4>Your Stats:<h4>");
                            StringBuilder recentStats = parseStats("Last hour", hourStats, hourStatsShadow);
                            StringBuilder generalStats = parseStats("Overall", overallStats, overallStatsShadow);

                            if (recentStats.toString().contains("with")) {
                                stringBuilder.append(recentStats);
                            } else {
                                stringBuilder.append("<h5>No recent stats. Train harder!<h5>");
                            }

                            if (generalStats.toString().contains("with")) {
                                stringBuilder.append(generalStats);
                            } else {
                                stringBuilder.append("<h5>No overall stats. Train harder!<h5>");
                            }

                            System.out.println(stringBuilder);
                            _outputBox.setText(Html.fromHtml(stringBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
                            LocalDatabaseService.transferAllDataToServer(getActivity());
                        }
                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
                        System.out.println("Error while creating JSON object response for ping request");
                    }
                }
            };
            request.getResponse().observe(getActivity(), responseObserver);
            request.execute();
        }
    };
}