package com.fragmentchangetabbarweight.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fragmentchangetabbarweight.R;


/**
 * User:Shine
 * Date:2015-10-20
 * Description:
 */
public class PersonFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_person, container, false);
    }
}
