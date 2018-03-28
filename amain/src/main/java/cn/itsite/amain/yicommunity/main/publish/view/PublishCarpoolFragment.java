package cn.itsite.amain.yicommunity.main.publish.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bigkoo.pickerview.TimePickerView;
import com.bilibili.boxing.Boxing;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing_impl.ui.BoxingActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.itsite.abase.common.DialogHelper;
import cn.itsite.abase.log.ALog;
import cn.itsite.abase.mvp.view.base.BaseFragment;
import cn.itsite.abase.utils.KeyBoardUtils;
import cn.itsite.abase.utils.ToastUtils;
import cn.itsite.amain.R;
import cn.itsite.amain.yicommunity.common.ApiService;
import cn.itsite.amain.yicommunity.common.Constants;
import cn.itsite.amain.yicommunity.common.Params;
import cn.itsite.acommon.UserHelper;
import cn.itsite.acommon.BaseBean;
import cn.itsite.amain.yicommunity.event.EventCommunity;
import cn.itsite.amain.yicommunity.event.EventPublish;
import cn.itsite.amain.yicommunity.main.picker.PickerActivity;
import cn.itsite.amain.yicommunity.main.picker.view.CityPickerFragment;
import cn.itsite.amain.yicommunity.main.publish.contract.PublishContract;
import cn.itsite.amain.yicommunity.main.publish.presenter.PublishCarpoolPresenter;
import cn.itsite.amain.yicommunity.web.WebActivity;

/**
 * Author: LiuJia on 2017/5/11 0011 21:32.
 * Email: liujia95me@126.com
 */

public class PublishCarpoolFragment extends BaseFragment<PublishContract.Presenter> implements PublishContract.View, View.OnClickListener {
    public final String TAG = PublishCarpoolFragment.class.getSimpleName();
    private TextView toolbarTitle;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private TextView tvSelectStartPoint;
    private TextView tvSelectEndPoint;
    private TextView tvSelectGoTime;
    private EditText etInputContent;
    private TextView tvCommunityAddress;
    private TextView toolbarMenu;
    private CheckBox cbAgreement;
    private EditText etStartPointDetailAddress;
    private EditText etEndPointDetailAddress;
    private PublishImageRVAdapter adapter;
    private Params params = Params.getInstance();
    private int REQUEST_START_POINT_CODE = 100;//选择起始时间的rqeeustCode
    private int REQUEST_END_POINT_CODE = 101;//选择结束时间的rqeeustCode
    BaseMedia addMedia = new BaseMedia() {
        @Override
        public TYPE getType() {
            return TYPE.IMAGE;
        }
    };
    private ArrayList<BaseMedia> selectedMedia = new ArrayList<>();
    private Date goTime;
    private Button btSubmit;
    private LinearLayout llLocation;
    private RadioButton rbHasCar;
    private RadioButton rbNoCar;
    private TextView tvAgreement;

    public static PublishCarpoolFragment newInstance() {
        return new PublishCarpoolFragment();
    }

    @NonNull
    @Override
    protected PublishContract.Presenter createPresenter() {
        return new PublishCarpoolPresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publish_carpool, container, false);
        toolbarTitle = ((TextView) view.findViewById(R.id.toolbar_title));
        toolbar = ((Toolbar) view.findViewById(R.id.toolbar));
        recyclerView = ((RecyclerView) view.findViewById(R.id.recyclerView));
        tvSelectStartPoint = ((TextView) view.findViewById(R.id.tv_select_start_point));
        tvSelectEndPoint = ((TextView) view.findViewById(R.id.tv_select_end_point));
        tvSelectGoTime = ((TextView) view.findViewById(R.id.tv_select_go_time));
        etInputContent = ((EditText) view.findViewById(R.id.et_input_content));
        tvCommunityAddress = ((TextView) view.findViewById(R.id.tv_community_address));
        toolbarMenu = ((TextView) view.findViewById(R.id.toolbar_menu));
        cbAgreement = ((CheckBox) view.findViewById(R.id.cb_agreement_publish_carpool_fragment));
        etStartPointDetailAddress = ((EditText) view.findViewById(R.id.et_start_point_detail_address));
        etEndPointDetailAddress = ((EditText) view.findViewById(R.id.et_end_point_detail_address));
        btSubmit = ((Button) view.findViewById(R.id.btn_submit_publish_carpool_fragment));
        llLocation = ((LinearLayout) view.findViewById(R.id.ll_location));
        rbHasCar = ((RadioButton) view.findViewById(R.id.rb_carpool_has_car));
        rbNoCar = ((RadioButton) view.findViewById(R.id.rb_carpool_hasnot_car));
        tvAgreement = ((TextView) view.findViewById(R.id.tv_agreement_publish_carpool_fragment));
        EventBus.getDefault().register(this);
        return attachToSwipeBack(view);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToolbar();
        initData();
        initListener();
    }

    private void initToolbar() {
        initStateBar(toolbar);
        toolbarTitle.setText("发布");
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_24dp);
        toolbar.setNavigationOnClickListener(v -> onBackPressedSupport());
    }

    private void initData() {
        //判断之前是否有


        //因为params是单例，所以要将上次选择的清除
        params.files = new ArrayList<>();
        tvCommunityAddress.setText(UserHelper.communityName);
        recyclerView.setLayoutManager(new GridLayoutManager(_mActivity, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        List<BaseMedia> datas = new ArrayList<>();
        addMedia.setPath("android.resource://" + _mActivity.getPackageName() + "/" + R.drawable.ic_image_add_tian_80px);
        datas.add(addMedia);
        adapter = new PublishImageRVAdapter(datas);
        recyclerView.setAdapter(adapter);
        cbAgreement.setChecked(UserHelper.isCarpoolAgree);
    }

    private void initListener() {
        btSubmit.setOnClickListener(this);
        llLocation.setOnClickListener(this);
        tvSelectStartPoint.setOnClickListener(this);
        tvSelectEndPoint.setOnClickListener(this);
        tvSelectGoTime.setOnClickListener(this);
        rbHasCar.setOnClickListener(this);
        rbNoCar.setOnClickListener(this);
        tvAgreement.setOnClickListener(this);

        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            selectPhoto();
        });
        cbAgreement.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UserHelper.setCarpoolAgree(isChecked);
        });
    }

    private void selectPhoto() {
        BoxingConfig config = new BoxingConfig(BoxingConfig.Mode.MULTI_IMG); // Mode：Mode.SINGLE_IMG, Mode.MULTI_IMG, Mode.VIDEO
        config.needCamera(R.drawable.ic_boxing_camera_white).needGif().withMaxCount(3) // 支持gif，相机，设置最大选图数
                .withMediaPlaceHolderRes(R.drawable.ic_boxing_default_image); // 设置默认图片占位图，默认无
        Boxing.of(config).withIntent(_mActivity, BoxingActivity.class, selectedMedia).start(this, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ALog.d(TAG, "onActivityResult:" + requestCode + " --- :" + resultCode);
        if (resultCode == RESULT_OK && requestCode == 100) {
            ArrayList<BaseMedia> medias = new ArrayList<>(Boxing.getResult(data));
            selectedMedia = Boxing.getResult(data);
            params.files.clear();
            for (int i = 0; i < medias.size(); i++) {
                params.files.add(new File(medias.get(i).getPath()));
            }
            if (params.files.size() > 0) {
                params.type = 1;
            }
            medias.add(addMedia);
            adapter.setNewData(medias);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        KeyBoardUtils.hideKeybord(getView(), _mActivity);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void error(String errorMessage) {
        super.error(errorMessage);
        dismissLoading();
        DialogHelper.errorSnackbar(getView(), errorMessage);
    }

    /**
     * 响应请求发布拼车服务成功
     *
     * @param bean
     */
    @Override
    public void responseSuccess(BaseBean bean) {
        dismissLoading();
        DialogHelper.successSnackbar(getView(), "提交成功!");
        EventBus.getDefault().post(new EventPublish());
        pop();
    }

    private void selectTogoTime() {
        TimePickerView pvTime = new TimePickerView.Builder(_mActivity, (date, v) -> {
            if (System.currentTimeMillis() > date.getTime()) {
                ToastUtils.showToast(_mActivity, "出发时间不能小于当前时间");
                return;
            }
            goTime = date;
            params.outTime = getTime(date);
            tvSelectGoTime.setText(params.outTime);
        }).setType(TimePickerView.Type.YEAR_MONTH_DAY_HOUR_MIN).build();
        pvTime.setDate(Calendar.getInstance());//注：根据需求来决定是否使用该方法（一般是精确到秒的情况），此项可以在弹出选择器的时候重新设置当前时间，避免在初始化之后由于时间已经设定，导致选中时间与当前时间不匹配的问题。
        pvTime.show();

    }

    private String getTime(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        super.onFragmentResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_END_POINT_CODE) {
                ALog.d(TAG, "onFragmentResult end code:" + data.getString("city"));
                params.endPlace = data.getString(Constants.KEY_CITY);
                tvSelectEndPoint.setText(data.getString(Constants.KEY_CITY));
            } else if (requestCode == REQUEST_START_POINT_CODE) {
                ALog.d(TAG, "onFragmentResult start code:" + data.getString("city"));
                params.startPlace = data.getString(Constants.KEY_CITY);
                tvSelectStartPoint.setText(data.getString(Constants.KEY_CITY));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventCommunity event) {
        ALog.e(TAG, "onEvent:::" + event.bean.getName());
        tvCommunityAddress.setText(event.bean.getName());
        params.cmnt_c = event.bean.getCode();
    }

    private void submit() {
        String startPointDetailAddress = etStartPointDetailAddress.getText().toString().trim();
        String endPointDetailAddress = etEndPointDetailAddress.getText().toString().trim();

        if (TextUtils.isEmpty(params.startPlace)) {
            DialogHelper.errorSnackbar(getView(), "请选择起点城市!");
            return;
        }
        if (TextUtils.isEmpty(startPointDetailAddress)) {
            DialogHelper.errorSnackbar(getView(), "请选择起点的详细地址!");
            return;
        }
        if (TextUtils.isEmpty(params.endPlace)) {
            DialogHelper.errorSnackbar(getView(), "请选择终点城市!");
            return;
        }
        if (TextUtils.isEmpty(endPointDetailAddress)) {
            DialogHelper.errorSnackbar(getView(), "请选择终点的详细地址!");
            return;
        }
        if (TextUtils.isEmpty(params.outTime)) {
            DialogHelper.errorSnackbar(getView(), "请选择出发时间!");
            return;
        }
        if (System.currentTimeMillis() > goTime.getTime()) {
            ToastUtils.showToast(_mActivity, "出发时间不能小于当前时间");
            return;
        }
        if (params.carpoolType == 0) {
            DialogHelper.errorSnackbar(getView(), "请选择拼车类型!");
            return;
        }
        params.content = etInputContent.getText().toString().trim();
        if (TextUtils.isEmpty(params.content)) {
            DialogHelper.errorSnackbar(getView(), "请输入留言!");
            return;
        }
        if (!cbAgreement.isChecked()) {
            new AlertDialog.Builder(_mActivity).setTitle("提示")
                    .setMessage("是否同意我们的协议？")
                    .setPositiveButton("同意", (dialog, which) -> cbAgreement.setChecked(true))
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }
        //将地址合并
        params.startPlace += startPointDetailAddress;
        params.endPlace += endPointDetailAddress;

        params.cmnt_c = UserHelper.communityCode;
        params.currentPositionLat = UserHelper.latitude;
        params.currentPositionLng = UserHelper.longitude;
        params.positionAddress = UserHelper.locationAddress;
        params.positionType = 2;
        showLoading();
        mPresenter.requestSubmit(params);//请求提交拼车服务
    }

    @Override
    public boolean onBackPressedSupport() {
        if (!TextUtils.isEmpty(etInputContent.getText().toString())
                || !selectedMedia.isEmpty()) {
            new AlertDialog.Builder(_mActivity)
                    .setTitle("提示")
                    .setMessage("如果退出，当前填写信息将会丢失，是否退出？")
                    .setPositiveButton("退出", (dialog, which) -> pop())
                    .show();
            return true;
        } else {
            pop();
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_submit_publish_carpool_fragment) {
            submit();

        } else if (i == R.id.ll_location) {
            _mActivity.startActivity(new Intent(_mActivity, PickerActivity.class));

        } else if (i == R.id.tv_select_start_point) {
            startForResult(CityPickerFragment.newInstance(), REQUEST_START_POINT_CODE);

        } else if (i == R.id.tv_select_end_point) {
            startForResult(CityPickerFragment.newInstance(), REQUEST_END_POINT_CODE);

        } else if (i == R.id.tv_select_go_time) {
            selectTogoTime();

        } else if (i == R.id.rb_carpool_has_car) {
            params.carpoolType = 2;

        } else if (i == R.id.rb_carpool_hasnot_car) {
            params.carpoolType = 1;

        } else if (i == R.id.tv_agreement_publish_carpool_fragment) {
            Intent introductionIntent = new Intent(_mActivity, WebActivity.class);
            introductionIntent.putExtra(Constants.KEY_TITLE, "亿社区拼车用户协议");
            introductionIntent.putExtra(Constants.KEY_LINK, ApiService.AGREEMENT_CARPOOL);
            startActivity(introductionIntent);

        }
    }
}
