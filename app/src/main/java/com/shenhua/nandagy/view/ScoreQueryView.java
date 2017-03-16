package com.shenhua.nandagy.view;

import com.shenhua.commonlibs.mvp.BaseView;
import com.shenhua.nandagy.bean.scorebean.ScoreData;

/**
 * 查询成绩视图
 * Created by shenhua on 9/8/2016.
 */
public interface ScoreQueryView extends BaseView {

    void showToast(String msg);

    void onGetQueryResult(ScoreData data);

}
