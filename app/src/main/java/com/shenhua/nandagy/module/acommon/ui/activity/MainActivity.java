package com.shenhua.nandagy.module.acommon.ui.activity;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.shenhua.commonlibs.annotation.ActivityFragmentInject;
import com.shenhua.commonlibs.base.BaseActivity;
import com.shenhua.commonlibs.utils.BusBooleanEvent;
import com.shenhua.nandagy.R;
import com.shenhua.nandagy.module.home.ui.fragement.HomeFragment;
import com.shenhua.nandagy.module.jiaowu.ui.fragment.JiaoWuFragment;
import com.shenhua.nandagy.module.me.ui.fragment.UserFragment;
import com.shenhua.nandagy.module.more.common.MoreFragment;
import com.shenhua.nandagy.module.xuegong.ui.fragment.XueGongFragment;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.update.BmobUpdateAgent;

/**
 * MainActivity
 * Created by Shenhua on 8/28/2016.
 */
@ActivityFragmentInject(
        contentViewId = R.layout.activity_main,
        toolbarId = R.id.common_toolbar,
        toolbarTitle = R.string.home,
        toolbarTitleId = R.id.toolbar_title,
        useBusEvent = true
)
public class MainActivity extends BaseActivity implements TabHost.OnTabChangeListener {

    private static final String TAG = "MainActivity";
    @BindView(R.id.tab_host)
    FragmentTabHost tabHost;
    @BindView(R.id.toolbar_pro)
    ProgressBar progressBar;
    private static long firstTime;

    @Override
    protected void onCreate(BaseActivity baseActivity, Bundle savedInstanceState) {
        BmobUpdateAgent.update(this);
//        BmobUpdateAgent.setUpdateOnlyWifi(false);
//        BmobPush.startWork(this);

        ButterKnife.bind(this);
        setupTabHost();
    }

    private void setupTabHost() {
        final Class[] fragments = {HomeFragment.class, XueGongFragment.class,
                JiaoWuFragment.class, MoreFragment.class, UserFragment.class};
        final String titles[] = getResources().getStringArray(R.array.main_tabs_titles);
        int[] icons = new int[titles.length];
        TypedArray ar = getResources().obtainTypedArray(R.array.main_tabs_images);
        for (int i = 0; i < titles.length; i++) {
            icons[i] = ar.getResourceId(i, 0);
        }
        ar.recycle();
        tabHost.setup(this, getSupportFragmentManager(), R.id.content_frame);
        tabHost.setOnTabChangedListener(this);
        tabHost.getTabWidget().setDividerDrawable(null);
        for (int i = 0; i < titles.length; i++) {
            View tabIndicatorView = LayoutInflater.from(this).inflate(R.layout.item_tabhost, null, false);
            TextView titleTv = (TextView) tabIndicatorView.findViewById(R.id.tv_main_tab);
            titleTv.setText(titles[i]);
            titleTv.setCompoundDrawablesWithIntrinsicBounds(0, icons[i], 0, 0);
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(titles[i]).setIndicator(tabIndicatorView);
            tabHost.addTab(tabSpec, fragments[i], null);
        }
        tabHost.setCurrentTab(0);
    }

    @Override
    public void onTabChanged(String tabId) {
        setupToolbarTitle(tabId);
    }

    @Subscribe
    public void onProgressBarEvent(BusBooleanEvent event) {
        if (event.isBoolean()) progressBar.setVisibility(View.VISIBLE);
        else progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (firstTime + 2000 > System.currentTimeMillis()) {
            super.onKeyDown(keyCode, event);
        } else {
            toast("再按一次退出程序");
        }
        firstTime = System.currentTimeMillis();
        return true;
    }

}
