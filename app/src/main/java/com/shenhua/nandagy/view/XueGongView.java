package com.shenhua.nandagy.view;

import com.shenhua.nandagy.manager.HttpManager;

import java.util.ArrayList;

/**
 * 学工视图
 * Created by shenhua on 8/31/2016.
 */
public interface XueGongView extends BaseView {

    void updateList(ArrayList[] lists, @HttpManager.DataLoadType.DataLoadTypeChecker int type);
}