package com.accelpunch.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.accelpunch.databinding.FragmentDashboardBinding;
import com.accelpunch.parser.BagToken;
import com.accelpunch.parser.GloveToken;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private GraphView accelLGraphView, accelRGraphView, accelRBagGraphView;
    static public LineGraphSeries<DataPoint> seriesL, seriesR, seriesBag;

    static public GloveToken lastAccelLeftToken;
    static public GloveToken lastAccelRightToken;
    static public BagToken lastAccelBagToken;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        accelLGraphView = binding.idGraphViewL;
        accelRGraphView = binding.idGraphViewR;
        accelRBagGraphView = binding.idGraphViewBag;

        // on below line we are adding data to our graph view.
        seriesL = new LineGraphSeries<DataPoint>();
        seriesR = new LineGraphSeries<DataPoint>();
        seriesBag = new LineGraphSeries<DataPoint>();

        // on below line we are setting
        // our title text size.
        accelLGraphView.setTitleTextSize(40);
        accelRGraphView.setTitleTextSize(40);
        accelRBagGraphView.setTitleTextSize(40);

        // on below line we are adding
        // data series to our graph view.
        accelLGraphView.addSeries(seriesL);
        accelRGraphView.addSeries(seriesR);
        accelRBagGraphView.addSeries(seriesBag);

        accelLGraphView.getViewport().setYAxisBoundsManual(true);
        accelRGraphView.getViewport().setYAxisBoundsManual(true);
        accelRBagGraphView.getViewport().setYAxisBoundsManual(true);

        accelLGraphView.getViewport().setMaxX(10);
        accelLGraphView.getViewport().setMaxY(16000);
        accelLGraphView.getViewport().setMinX(0);
        accelLGraphView.getViewport().setMinY(-5000);
        accelLGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        accelRGraphView.getViewport().setMaxX(10);
        accelRGraphView.getViewport().setMaxY(16000);
        accelRGraphView.getViewport().setMinX(0);
        accelRGraphView.getViewport().setMinY(-5000);
        accelRGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        accelRBagGraphView.getViewport().setMaxX(10);
        accelRBagGraphView.getViewport().setMaxY(16000);
        accelRBagGraphView.getViewport().setMinX(0);
        accelRBagGraphView.getViewport().setMinY(-16000);
        accelRBagGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}