package cn.itsite.amain.yicommunity.main.picker;

import android.os.Bundle;

import cn.itsite.abase.mvp.view.base.BaseActivity;
import cn.itsite.amain.R;
import cn.itsite.amain.yicommunity.common.Constants;
import cn.itsite.amain.yicommunity.main.picker.view.CommunityPickerFragment;
import cn.itsite.amain.yicommunity.main.picker.view.ParkPickerFragment;


/**
 * Created by Administrator on 2017/4/29 0029.
 * 选择器的父容器
 */
public class PickerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int intFromTo = getIntent().getIntExtra(Constants.KEY_FROM_TO, 0);
        if (savedInstanceState == null) {
            if (intFromTo == 100) {
                loadRootFragment(R.id.fl_main_activity, ParkPickerFragment.newInstance());
            } else {
                loadRootFragment(R.id.fl_main_activity, CommunityPickerFragment.newInstance());
            }
        }
    }
}
