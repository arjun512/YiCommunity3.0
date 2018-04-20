package cn.itsite.amain.yicommunity.main.door.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import cn.itsite.abase.common.DialogHelper;
import cn.itsite.abase.mvp.view.base.BaseFragment;
import cn.itsite.amain.R;
import cn.itsite.amain.yicommunity.common.Constants;
import cn.itsite.amain.yicommunity.common.Params;
import cn.itsite.abase.common.UserHelper;
import cn.itsite.amain.yicommunity.entity.bean.OpenDoorRecordBean;
import cn.itsite.amain.yicommunity.event.EventCommunity;
import cn.itsite.amain.yicommunity.main.door.contract.OpenDoorRecordContract;
import cn.itsite.amain.yicommunity.main.door.presenter.OpenDoorRecordPresenter;
import cn.itsite.statemanager.StateManager;
import in.srain.cube.views.ptr.PtrFrameLayout;
import me.yokeyword.fragmentation.SupportActivity;

/**
 * Author: LiuJia on 2017/4/21 10:31.
 * Email: liujia95me@126.com
 * [开门记录]的View层
 * 打开方式：StartApp-->管家-->智慧门禁[开门记录]
 */
public class OpenDoorRecordFragment extends BaseFragment<OpenDoorRecordContract.Presenter> implements OpenDoorRecordContract.View {
    private static final String TAG = OpenDoorRecordFragment.class.getSimpleName();
    private TextView toolbarTitle;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private PtrFrameLayout ptrFrameLayout;
    private OpenDoorRecordRVAdapter adapter;
    private Params params = Params.getInstance();
    private StateManager mStateManager;

    public static OpenDoorRecordFragment newInstance() {
        return new OpenDoorRecordFragment();
    }

    @NonNull
    @Override
    protected OpenDoorRecordContract.Presenter createPresenter() {
        return new OpenDoorRecordPresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opendoor_record, container, false);
        toolbarTitle = ((TextView) view.findViewById(R.id.toolbar_title));
        toolbar = ((Toolbar) view.findViewById(R.id.toolbar));
        recyclerView = ((RecyclerView) view.findViewById(R.id.recyclerView));
        ptrFrameLayout = ((PtrFrameLayout) view.findViewById(R.id.ptrFrameLayout));
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToolbar();
        initData();
        initStateManager();
        initPtrFrameLayout(ptrFrameLayout, recyclerView);
    }

    private void initToolbar() {
        initStateBar(toolbar);
        toolbarTitle.setText("开门记录");
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_24dp);
        toolbar.setNavigationOnClickListener(v -> ((SupportActivity) _mActivity).onBackPressedSupport());
        toolbarTitle.setOnClickListener(v -> recyclerView.scrollToPosition(0));
    }

    private void initData() {
        recyclerView.setLayoutManager(new LinearLayoutManager(_mActivity));
        adapter = new OpenDoorRecordRVAdapter();
        adapter.setEnableLoadMore(true);
        //设置Item动画
        adapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT);
        adapter.isFirstOnly(true);
        //设置允许加载更多
        adapter.setOnLoadMoreListener(() -> {
            params.page++;
            mPresenter.requestRecord(params);//请求开门记录列表
        }, recyclerView);
        recyclerView.setAdapter(adapter);
    }

    private void initStateManager() {
        mStateManager = StateManager.builder(_mActivity)
                .setContent(recyclerView)
                .setEmptyView(R.layout.state_empty)
                .setEmptyImage(R.drawable.ic_open_record_empty_state_gray_200px)
                .setEmptyText("暂无开门记录！")
                .setErrorOnClickListener(v -> ptrFrameLayout.autoRefresh())
                .setEmptyOnClickListener(v -> ptrFrameLayout.autoRefresh())
                .build();
    }

    @Override
    public void onRefresh() {
        params.page = 1;
        params.pageSize = Constants.PAGE_SIZE;
        params.cmnt_c = UserHelper.communityCode;
        mPresenter.requestRecord(params);//请求开门记录列表
    }

    /**
     * 请求响应开门记录列表。
     *
     * @param data
     */
    @Override
    public void responseRecord(List<OpenDoorRecordBean.DataBean> data) {
        ptrFrameLayout.refreshComplete();
        if (data == null || data.isEmpty()) {
            if (params.page == 1) {
                mStateManager.showEmpty();
            }
            adapter.loadMoreEnd();
            return;
        }

        if (params.page == 1) {
            mStateManager.showContent();
            adapter.setNewData(data);
            adapter.disableLoadMoreIfNotFullPage(recyclerView);
        } else {
            adapter.addData(data);
            adapter.setEnableLoadMore(true);
            adapter.loadMoreComplete();
        }
    }

    @Override
    public void error(String errorMessage) {
        super.error(errorMessage);
        ptrFrameLayout.refreshComplete();
        DialogHelper.warningSnackbar(getView(), errorMessage);
        if (params.page == 1) {
            mStateManager.showError();
        } else if (params.page > 1) {
            adapter.loadMoreFail();
            params.page--;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 选择完社区后自动刷新
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventCommunity event) {
        ptrFrameLayout.autoRefresh();
    }
}
