package com.shenhua.nandagy.ui.activity.me;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.shenhua.commonlibs.annotation.ActivityFragmentInject;
import com.shenhua.commonlibs.base.BaseActivity;
import com.shenhua.commonlibs.widget.BaseShareView;
import com.shenhua.lib.boxing.impl.Boxing;
import com.shenhua.lib.boxing.impl.BoxingCrop;
import com.shenhua.lib.boxing.impl.BoxingUcrop;
import com.shenhua.lib.boxing.loader.BoxingGlideLoader;
import com.shenhua.lib.boxing.loader.BoxingMediaLoader;
import com.shenhua.lib.boxing.model.config.BoxingConfig;
import com.shenhua.lib.boxing.model.config.BoxingCropOption;
import com.shenhua.lib.boxing.model.entity.BaseMedia;
import com.shenhua.lib.boxing.ui.BoxingActivity;
import com.shenhua.nandagy.R;
import com.shenhua.nandagy.bean.bmobbean.MyUser;
import com.shenhua.nandagy.bean.bmobbean.UserZone;
import com.shenhua.nandagy.databinding.ActivityUserZoneBinding;
import com.shenhua.nandagy.utils.bmobutils.UserUtils;
import com.shenhua.nandagy.utils.bmobutils.UserZoneUtils;
import com.shenhua.nandagy.widget.LoadingAlertDialog;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * 用户主页或个人主页界面
 * Created by Shenhua on 9/3/2016.
 */
@ActivityFragmentInject(
        toolbarId = R.id.common_toolbar,
        toolbarHomeAsUp = true,
        toolbarTitle = R.string.toolbar_title_user_zone,
        toolbarTitleId = R.id.toolbar_title
)
public class UserZoneActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    @BindView(R.id.appbar)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;
    @BindView(R.id.tv_zone_id)
    TextView mZoneIdTv;
    @BindView(R.id.tv_zone_exper)
    TextView mZoneExperTv;
    @BindView(R.id.tv_zone_mi)
    TextView mZoneMiTv;
    @BindView(R.id.bpv)
    BaseShareView mBpv;
    private static final String TAG = "UserZoneActivity";
    public static final int REQUEST_EDIT = 1;
    private static final int REQUEST_TAKE = 12;
    private static final int REQUEST_SELECT = 13;
    private boolean accessFromMe;
    private String zoneObjectId;
    private String finalPhotoPath;
    private String cacheHou = ".nui";
    private ActivityUserZoneBinding binding;
    private Serializable userZoneBean;

    @Override
    protected void onCreate(BaseActivity baseActivity, Bundle savedInstanceState) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_zone);
        initToolbar();
        ButterKnife.bind(this);
        accessFromMe = getIntent().getBooleanExtra("isMyself", false);
        zoneObjectId = getIntent().getStringExtra("zoneObjectId");
        mZoneIdTv.setText(String.format(getString(R.string.userzone_text_objid), zoneObjectId));
        mAppBarLayout.addOnOffsetChangedListener(this);
        updataViews(false);
        initSelectPhotoView();
    }

    private void updataViews(boolean upgrade) {
        if (upgrade) {
            BmobQuery<UserZone> query = new BmobQuery<>();
            query.getObject(zoneObjectId, new QueryListener<UserZone>() {
                @Override
                public void done(UserZone userZone, BmobException e) {
                    if (e == null) {
                        binding.setUserZone(userZone);
                        if (accessFromMe) {
                            userZoneBean = userZone;
                            UserZoneUtils.getInstance().saveUserZone(UserZoneActivity.this, userZone);
                        }
                    } else {
                        toast("用户主页资料获取失败：" + e.getMessage());
                    }
                }
            });
        } else {// 自己访问，不要从网络请求
            UserZone uz = (UserZone) getIntent().getExtras().getSerializable("userzone");
            if (uz == null) return;
            userZoneBean = uz;
            binding.setUserZone(uz);
        }
    }

    /**
     * 初始化头像点击事件
     */
    private void initSelectPhotoView() {
        if (!accessFromMe) return;

        BoxingMediaLoader.getInstance().init(new BoxingGlideLoader());
        BoxingCrop.getInstance().init(new BoxingUcrop());

        mBpv.setInterpolator(new BounceInterpolator());
        View content = mBpv.getContentView();
        TextView take = (TextView) content.findViewById(R.id.tv_take_photo);


        take.setOnClickListener(v -> {
            mBpv.hide();
            // finalPhotoPath = Crop.takePhoto(UserZoneActivity.this, getCacheDir().getPath(), zoneObjectId + cacheHou);
        });
        TextView pick = (TextView) content.findViewById(R.id.tv_pick_photo);
        pick.setOnClickListener(v -> {
            mBpv.hide();
            String cachePath = Environment.getExternalStorageDirectory() + File.separator + "Pictures";
            if (TextUtils.isEmpty(cachePath)) {
                Toast.makeText(getApplicationContext(), "设备存储读取出错或暂不可用，请稍候重试", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri destUri = new Uri.Builder()
                    .scheme("file")
                    .appendPath(cachePath)
                    .appendPath("123456.jpg")
                    .build();
            BoxingConfig singleCropImgConfig = new BoxingConfig(BoxingConfig.Mode.SINGLE_IMG)
                    .withCropOption(new BoxingCropOption(destUri)
                            //设置最大尺寸
                            .withMaxResultSize(400, 400)
                            //设置比例为1:1
                            .aspectRatio(1f, 1f));
            Boxing.of(singleCropImgConfig).withIntent(this, BoxingActivity.class).start(this, REQUEST_SELECT);
        });
        TextView cancel = (TextView) content.findViewById(R.id.tv_cancel);
        cancel.setOnClickListener(v -> mBpv.hide());
    }

    @OnClick(R.id.iv_zone_photo)
    void photo() {
        if (accessFromMe && !mBpv.getIsShowing()) {
            mBpv.show();
        } else {
            // TODO: 3/30/2017  showPhotoDetail();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_edit).setVisible(accessFromMe);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.zone_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            if (userZoneBean == null) {
                toast("当前无法编辑个人信息，请稍候重试");
                return true;
            }
            Intent intent = new Intent(this, UserZoneEditActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("userZoneInfo", userZoneBean);
            intent.putExtra("zoneObjId", zoneObjectId);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_EDIT);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mBpv.getIsShowing()) {
                mBpv.hide();
            } else {
                return super.onKeyDown(keyCode, event);
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT && resultCode == UserZoneEditActivity.RESULT_EDIT) {
            // 刷新界面
            Log.d(TAG, "onActivityResult: 编辑了");
            updataViews(true);
        }
        if (requestCode == REQUEST_SELECT && resultCode == RESULT_OK) {
            ArrayList<BaseMedia> medias = Boxing.getResult(data);
            if (medias == null) {
                Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "--" + medias.size(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onActivityResult: " + medias.get(0).getPath());// /data/data/com.shenhua.pictruechoice/cache/boxing/123.jpg
        }
    }

    /**
     * 上传头像
     *
     * @param filePath filePath
     */
    private void upLoadPhoto(String filePath) {
        LoadingAlertDialog.getInstance(this).showLoadDialog("头像更新中...", true);
        final BmobFile bmobFile = new BmobFile(new File(filePath));
        bmobFile.uploadblock(new UploadFileListener() {

            @Override
            public void done(BmobException e) {
                LoadingAlertDialog.getInstance(UserZoneActivity.this).dissmissLoadDialog();
                if (e == null) {
                    final String result = bmobFile.getFileUrl();
                    MyUser user = BmobUser.getCurrentUser(MyUser.class);
                    user.setUrl_photo(result);
                    user.update(user.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e != null) {
                                toast("头像更新失败：" + e.getMessage());
                            } else {
                                UserUtils.getInstance().updateUserInfo(UserZoneActivity.this, "url_photo", result);
                                toast("头像更新成功！");
                            }
                        }
                    });
                } else {
                    toast("头像更新失败：" + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        mToolbarTitle.setAlpha((float) Math.abs(verticalOffset) / (float) appBarLayout.getTotalScrollRange());
    }
}
