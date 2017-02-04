package com.coolweather.android.Binding;


import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.coolweather.android.R;

/**
 * Created by 令子 on 2017/2/4.
 */

public class Handers {

    public void onHomeClick(View view){
        DrawerLayout drawerLayout = (DrawerLayout)view.getRootView().findViewById(R.id.drawer_layout);
        drawerLayout.openDrawer(GravityCompat.START);
    }

}
