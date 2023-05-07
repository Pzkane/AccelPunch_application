package com.accelpunch.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.accelpunch.MainActivity;
import com.accelpunch.R;
import com.accelpunch.databinding.FragmentHomeBinding;
import com.accelpunch.net.HttpRequest;
import com.accelpunch.storage.service.LocalDatabaseService;


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
                @Override
                public void onChanged(@Nullable final String text) {
                    _outputBox.setText(request.getResponse().getValue().toString());
                    LocalDatabaseService.transferAllDataToServer(getActivity());
                }
            };
            request.getResponse().observe(getActivity(), responseObserver);
            request.execute();
        }
    };
}