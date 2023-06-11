package com.accelpunch.ui.dashboard;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.accelpunch.R;
import com.accelpunch.databinding.FragmentDashboardBinding;
import com.accelpunch.parser.BagToken;
import com.accelpunch.parser.GloveToken;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding _binding;
    private GraphView _accelLGraphView;
    private static GraphView _accelRGraphView;
    private GraphView _accelRBagGraphView;
    private static LinearLayout _layoutGloveLeft, _layoutGloveRight, _layoutPunchBag;
    private static TextView _txtGloveLeft, _txtGloveRight, _txtPunchBag;

    private static MediaPlayer _mp_whoosh1, _mp_whoosh2, _mp_whoosh3, _mp_kick, _mp_critical;

    static public LineGraphSeries<DataPoint> seriesL, seriesR, seriesBag;

    static public GloveToken lastAccelLeftToken;
    static public GloveToken lastAccelRightToken;
    static public BagToken lastAccelBagToken;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        _binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = _binding.getRoot();

        _accelLGraphView = _binding.idGraphViewL;
        _accelRGraphView = _binding.idGraphViewR;
        _accelRBagGraphView = _binding.idGraphViewBag;
        _layoutGloveLeft = _binding.layoutGloveLeft;
        _layoutGloveRight = _binding.layoutGloveRight;
        _layoutPunchBag = _binding.layoutPunchBag;
        _txtGloveLeft = _binding.txtGloveLeft;
        _txtGloveRight = _binding.txtGloveLeft;
        _txtPunchBag = _binding.txtPunchBag;

        // on below line we are adding data to our graph view.
        seriesL = new LineGraphSeries<DataPoint>();
        seriesR = new LineGraphSeries<DataPoint>();
        seriesBag = new LineGraphSeries<DataPoint>();

        // on below line we are setting
        // our title text size.
        _accelLGraphView.setTitleTextSize(40);
        _accelRGraphView.setTitleTextSize(40);
        _accelRBagGraphView.setTitleTextSize(40);

        // on below line we are adding
        // data series to our graph view.
        _accelLGraphView.addSeries(seriesL);
        _accelRGraphView.addSeries(seriesR);
        _accelRBagGraphView.addSeries(seriesBag);

        _accelLGraphView.getViewport().setYAxisBoundsManual(true);
        _accelRGraphView.getViewport().setYAxisBoundsManual(true);
        _accelRBagGraphView.getViewport().setYAxisBoundsManual(true);

        _accelLGraphView.getViewport().setMaxX(10);
        _accelLGraphView.getViewport().setMaxY(30000);
        _accelLGraphView.getViewport().setMinX(0);
        _accelLGraphView.getViewport().setMinY(-5000);
        _accelLGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        _accelRGraphView.getViewport().setMaxX(10);
        _accelRGraphView.getViewport().setMaxY(30000);
        _accelRGraphView.getViewport().setMinX(0);
        _accelRGraphView.getViewport().setMinY(-5000);
        _accelRGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        _accelRBagGraphView.getViewport().setMaxX(10);
        _accelRBagGraphView.getViewport().setMaxY(16000);
        _accelRBagGraphView.getViewport().setMinX(0);
        _accelRBagGraphView.getViewport().setMinY(-16000);
        _accelRBagGraphView.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        _binding = null;
    }

    static public void setLGloveText(String text) {
        _txtGloveLeft.setText(text);
    }

    static public void setRGloveText(String text) {
        _txtGloveRight.setText(text);
    }

    static public void setBagText(String text) {
        _txtPunchBag.setText(text);
    }

    static private void playSound(int resource) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Grab context from random element
                    MediaPlayer mp = MediaPlayer.create(_accelRGraphView.getContext(), resource);
                    mp.start();
                } catch (NullPointerException e) {
                    // Fragment is not loaded yet...
                }
            }
        });
        t.start();
    }

    static public void flashLGlove(Activity context, @ColorInt int color) {
        ObjectAnimator anim = ObjectAnimator.ofInt(_layoutGloveLeft, "backgroundColor",
                color, context.getResources().getColor(R.color.bgL, context.getTheme()));
        anim.setDuration(250);
        anim.setEvaluator(new ArgbEvaluator());
        playSound(R.raw.whoosh_1);
        anim.start();
    }

    static public void flashRGlove(Activity context, @ColorInt int color) {
        ObjectAnimator anim = ObjectAnimator.ofInt(_layoutGloveRight, "backgroundColor",
                color, context.getResources().getColor(R.color.bgR, context.getTheme()));
        anim.setDuration(250);
        anim.setEvaluator(new ArgbEvaluator());
        playSound(R.raw.whoosh_1);
        anim.start();
    }

    static public void flashBag(Activity context, @ColorInt int color, boolean critical) {
        ObjectAnimator anim = ObjectAnimator.ofInt(_layoutPunchBag, "backgroundColor",
                color, context.getResources().getColor(R.color.bag, context.getTheme()));
        anim.setDuration(250);
        anim.setEvaluator(new ArgbEvaluator());
        if (!critical) {
            playSound(R.raw.kick);
        } else {
            playSound(R.raw.critical);
        }
        anim.start();
    }
}