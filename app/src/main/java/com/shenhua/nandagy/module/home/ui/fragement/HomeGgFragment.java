package com.shenhua.nandagy.module.home.ui.fragement;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shenhua.commonlibs.annotation.ActivityFragmentInject;
import com.shenhua.nandagy.R;
import com.shenhua.nandagy.module.home.presenter.HomePresenter;
import com.shenhua.nandagy.service.Constants;

import butterknife.ButterKnife;

/**
 * 公告公示
 * Created by shenhua on 8/29/2016.
 */
@ActivityFragmentInject(contentViewId = R.layout.frag_home_content)
public class HomeGgFragment extends HomeContentBaseFragment {

    public static HomeGgFragment newInstance() {
        return new HomeGgFragment();
    }

    @Override
    public void onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState, View rootView) {
        ButterKnife.bind(this, rootView);
        presenter.execute(1);
    }

    @Override
    public HomePresenter createPresenter() {
        presenter = new HomePresenter(this, Constants.HOME_URL_GGGS);
        return presenter;
    }

    @Override
    public void onReload() {
        super.onReload();
        presenter.execute(1);
    }

}
