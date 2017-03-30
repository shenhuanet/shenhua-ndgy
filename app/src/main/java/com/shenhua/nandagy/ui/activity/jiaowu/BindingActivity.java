package com.shenhua.nandagy.ui.activity.jiaowu;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;

import com.shenhua.commonlibs.annotation.ActivityFragmentInject;
import com.shenhua.commonlibs.base.BaseActivity;
import com.shenhua.commonlibs.callback.TextEnableInputWatcher;
import com.shenhua.nandagy.R;
import com.shenhua.nandagy.service.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 教务信息绑定界面(登录教务系统)
 * Created by Shenhua on 3/30/2017.
 * Email shenhuanet@126.com
 */
@ActivityFragmentInject(
        contentViewId = R.layout.activity_banding,
        toolbarId = R.id.common_toolbar,
        toolbarHomeAsUp = true,
        toolbarTitle = R.string.toolbar_title_binding,
        toolbarTitleId = R.id.toolbar_title
)
public class BindingActivity extends BaseActivity {

    @BindView(R.id.til_zkzh)
    TextInputLayout mNumLayout;
    @BindView(R.id.et_zkzh)
    TextInputEditText mNumEt;
    @BindView(R.id.til_name)
    TextInputLayout mIdLayout;
    @BindView(R.id.et_name)
    TextInputEditText mIdEt;

    @Override
    protected void onCreate(BaseActivity baseActivity, Bundle savedInstanceState) {
        ButterKnife.bind(this);

        mNumEt.addTextChangedListener(new TextEnableInputWatcher(mNumLayout));
        mIdEt.addTextChangedListener(new TextEnableInputWatcher(mIdLayout));
    }

    @OnClick(R.id.btn_query)
    void onClick() {
        String num = mNumEt.getText().toString();
        String id = mIdEt.getText().toString();

        if (TextUtils.isEmpty(num) || num.length() != 10) {
            mNumEt.setError("学号填写不正确");
        } else if (TextUtils.isEmpty(id)) {
            mIdEt.setError("密码填写不正确");
        } else {
            hideKeyboard();

            Intent intent = new Intent(this, ScoreActivity.class);
            intent.putExtra("name_num", num);
            intent.putExtra("name_id", id);
            startActivity(intent);

            this.setResult(Constants.Code.RECULT_CODE_BINDING_SUCCESS);
            this.finish();
        }
    }

}
