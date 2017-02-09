package com.shenhua.nandagy.ui.activity.me;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.shenhua.nandagy.R;
import com.shenhua.nandagy.base.BaseActivity;
import com.shenhua.nandagy.bean.bmobbean.MyUser;
import com.shenhua.nandagy.bean.bmobbean.UserZone;
import com.shenhua.nandagy.utils.bmobutils.UserUtils;
import com.shenhua.nandagy.widget.BaseShareView;
import com.shenhua.nandagy.widget.CircleImageView;
import com.shenhua.nandagy.widget.LoadingAlertDialog;
import com.shenhua.photopicker.Crop;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * 用户主页或个人主页界面
 * Created by Shenhua on 9/3/2016.
 */
public class UserZoneActivity extends BaseActivity {

    @Bind(R.id.iv_zone_photo)
    CircleImageView mZonePhotoIv;
    @Bind(R.id.iv_zone_gender)
    ImageView mZoneGenderIv;
    @Bind(R.id.tv_zone_id)
    TextView mZoneIdTv;
    @Bind(R.id.tv_zone_exper)
    TextView mZoneExperTv;
    @Bind(R.id.tv_zone_mi)
    TextView mZoneMiTv;
    private boolean accessFromMe;
    @Bind(R.id.tv_zone_dynamic_str)
    TextView mDynamicStrTv;
    @Bind(R.id.tv_zone_name)
    TextView mNameTv;
    @Bind(R.id.tv_zone_sign)
    TextView mSignTv;
    @Bind(R.id.tv_zone_birth)
    TextView mBirthTv;
    @Bind(R.id.tv_zone_locate)
    TextView mLoctateTv;
    @Bind(R.id.tv_zone_love)
    TextView mLoveTv;
    @Bind(R.id.tv_zone_depart)
    TextView mDepartTv;
    @Bind(R.id.tv_zone_qual)
    TextView mQuelTv;
    @Bind(R.id.tv_zone_highSchool)
    TextView mHighSchoolTv;
    @Bind(R.id.bpv)
    BaseShareView mBpv;
    private UserZone userZoneBean;
    private String userObjectId;
    private String finalPhotoPath;
    private String cacheDir = "Ndgy/temp";
    private String cacheHou = ".nui";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_zone);
        ButterKnife.bind(this);
        setupActionBar("主页详情", true);
        accessFromMe = getIntent().getBooleanExtra("isMySelf", false);
        if (accessFromMe) {
            setPhotoView(getIntent().getStringExtra("photo"), getIntent().getBooleanExtra("sex", false));
        }
        initSelectPhotoView();
    }

    private void initSelectPhotoView() {
        mBpv.setInterpolator(new BounceInterpolator());
        View content = mBpv.getContentView();
        TextView take = (TextView) content.findViewById(R.id.tv_take_photo);
        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBpv.hide();
                finalPhotoPath = Crop.takePhoto(UserZoneActivity.this, cacheDir, userObjectId + cacheHou);
            }
        });
        TextView pick = (TextView) content.findViewById(R.id.tv_pick_photo);
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBpv.hide();
                Crop.pickImage(UserZoneActivity.this);
            }
        });
        TextView cancel = (TextView) content.findViewById(R.id.tv_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBpv.hide();
            }
        });
    }

    @OnClick(R.id.iv_zone_photo)
    void photo() {
        if (accessFromMe && !mBpv.getIsShowing()) {
            mBpv.show();
        } else {
//            showPhotoDetail();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpTopView();
    }

    private void setUpTopView() {
        userObjectId = getIntent().getStringExtra("userObjectId");
        String zoneObjectId = getIntent().getStringExtra("zoneObjectId");
        BmobQuery<UserZone> bmobQuery = new BmobQuery<>();
        bmobQuery.getObject(zoneObjectId, new QueryListener<UserZone>() {
            @Override
            public void done(UserZone userZone, BmobException e) {
                if (e == null) {
                    userZoneBean = userZone;
                    if (!accessFromMe) setPhotoView(userZone.getPhotoUrl(), userZone.getSex());
                    mZoneIdTv.setText("ID:" + userZone.getObjectId());
                    mZoneExperTv.setText("经验值:" + userZone.getExper());
                    mZoneMiTv.setText("米粒数:" + userZone.getMi());
                    resetText(mDynamicStrTv, userZone.getDynamicStr());
                    resetText(mNameTv, userZone.getName());
                    resetText(mSignTv, userZone.getSign());
                    resetText(mBirthTv, userZone.getBirth());
                    resetText(mLoctateTv, userZone.getLocate());
                    resetText(mLoveTv, userZone.getLove());
                    resetText(mDepartTv, userZone.getDepart());
                    resetText(mQuelTv, userZone.getQual());
                    resetText(mHighSchoolTv, userZone.getHighSchool());
                } else {
                    toast("用户主页资料获取失败：" + e.getMessage());
                }
            }
        });
    }

    private void resetText(TextView textView, String str) {
        if (!TextUtils.isEmpty(str)) textView.setText(str);
    }

    private void setPhotoView(String imgUrl, boolean sex) {
        if (TextUtils.isEmpty(imgUrl)) {
            if (sex) {
                Glide.with(this).load(R.drawable.img_photo_woman).centerCrop().into(mZonePhotoIv);
            } else {
                Glide.with(this).load(R.drawable.img_photo_man).centerCrop().into(mZonePhotoIv);
            }
        } else {
            Glide.with(this).load(imgUrl).centerCrop().into(mZonePhotoIv);
        }
        mZoneGenderIv.setImageResource(sex ? R.drawable.ic_user_gender_female : R.drawable.ic_user_gender_male);
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
            Intent intent = new Intent(this, UserZoneEditActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("userZoneInfo", userZoneBean);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mBpv.getIsShowing()) {
            mBpv.hide();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Crop.REQUEST_PICK) {
                new Crop(data.getData()).fileOutDir(cacheDir).fileOutName(userObjectId + cacheHou)
                        .setCropType(true).start(this);
            }
            if (requestCode == Crop.REQUEST_TAKE) {
                new Crop(Uri.fromFile(new File(finalPhotoPath)))
                        .fileOutDir(cacheDir).fileOutName(userObjectId + cacheHou)
                        .setCropType(true).start(this);
            }
            if (requestCode == Crop.REQUEST_CROP) {
                String phopath = Crop.getOutput(data).getPath();
                upLoadPhoto(phopath);
                Glide.with(this).load(phopath).centerCrop().skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(mZonePhotoIv);
            }
            if (requestCode == Crop.RESULT_ERROR) {
                toast("裁剪照片发生错误！" + Crop.getError(data).getMessage());
            }
        }
    }

    private void upLoadPhoto(String filePath) {
        LoadingAlertDialog.showLoadDialog(this, "头像更新中...",true);
        final BmobFile bmobFile = new BmobFile(new File(filePath));
        bmobFile.uploadblock(new UploadFileListener() {

            @Override
            public void done(BmobException e) {
                if (e == null) {
                    final String result = bmobFile.getFileUrl();
                    MyUser user = UserUtils.getInstance().getUserInfo(UserZoneActivity.this);
                    user.setUrl_photo(result);
                    user.update(user.getUserId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e != null) {
                                LoadingAlertDialog.dissmissLoadDialog();
                                toast("头像更新失败：" + e.getMessage());
                            } else {
                                UserUtils.getInstance().updateUserInfo(UserZoneActivity.this, "url_photo", result);
                                LoadingAlertDialog.dissmissLoadDialog();
                                toast("头像更新成功！");
                            }
                        }
                    });
                } else {
                    LoadingAlertDialog.dissmissLoadDialog();
                    toast("头像更新失败：" + e.getMessage());
                }
            }
        });
    }
}