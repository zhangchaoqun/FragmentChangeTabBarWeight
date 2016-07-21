package com.fragmentchangetabbarweight;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.fragmentchangetabbarweight.fragment.CityFragment;
import com.fragmentchangetabbarweight.fragment.HomeFragment;
import com.fragmentchangetabbarweight.fragment.MessageFragment;
import com.fragmentchangetabbarweight.fragment.PersonFragment;
import com.fragmentchangetabbarweight.weight.MainNavigateTabBar;

public class MainActivity extends FragmentActivity {

    MainNavigateTabBar mNavigateTabBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigateTabBar = (MainNavigateTabBar) findViewById(R.id.mNavigate);
        mNavigateTabBar.onRestoreInstanceState(savedInstanceState);
        mNavigateTabBar.addTab(CityFragment.class, new MainNavigateTabBar.TabParam(R.drawable.comui_tab_city, R.drawable.comui_tab_city_selected, "城市"));
        mNavigateTabBar.addTab(HomeFragment.class, new MainNavigateTabBar.TabParam(R.drawable.comui_tab_home, R.drawable.comui_tab_home_selected, "家"));
        mNavigateTabBar.addTab(MessageFragment.class, new MainNavigateTabBar.TabParam(R.drawable.comui_tab_message, R.drawable.comui_tab_message_selected, "消息"));
        mNavigateTabBar.addTab(PersonFragment.class, new MainNavigateTabBar.TabParam(R.drawable.comui_tab_person, R.drawable.comui_tab_person_selected, "个人"));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mNavigateTabBar.onSaveInstanceState(outState);
    }
}
