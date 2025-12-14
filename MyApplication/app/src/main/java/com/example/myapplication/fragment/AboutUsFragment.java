package com.example.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

public class AboutUsFragment extends Fragment {
    ImageView btnBack;
    Fragment previousFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, container, false);
        super.onCreate(savedInstanceState);
        btnBack = view.findViewById(R.id.btnBack);
        setupAction();
        return view;
    }

    public void setPreviousFragment(Fragment previousFragment) {
        this.previousFragment = previousFragment;
    }
    private void setupAction(){
        btnBack.setOnClickListener(v -> {
            ((MainActivity) getActivity()).switchFragment(previousFragment);
        });
    }
}

