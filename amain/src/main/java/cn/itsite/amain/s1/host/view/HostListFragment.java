package cn.itsite.amain.s1.host.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.itsite.abase.log.ALog;
import cn.itsite.abase.mvp.view.base.BaseFragment;
import cn.itsite.abase.mvp.view.base.Decoration;
import cn.itsite.adialog.dialogfragment.SelectorDialogFragment;
import cn.itsite.amain.R;
import cn.itsite.amain.s1.common.Constants;
import cn.itsite.amain.s1.common.Params;
import cn.itsite.amain.s1.entity.bean.GatewaysBean;
import cn.itsite.amain.s1.host.contract.HostListContract;
import cn.itsite.amain.s1.host.presenter.HostListPresenter;
import cn.itsite.amain.s1.qrcode.ScanQRCodeFragment;
import cn.itsite.amain.yicommunity.widget.PtrHTFrameLayout;
import cn.itsite.statemanager.StateLayout;
import cn.itsite.statemanager.StateManager;
import me.yokeyword.fragmentation.SupportActivity;

/**
 * Created by leguang on 2017/6/22 0022.
 * Email：langmanleguang@qq.com
 */
public class HostListFragment extends BaseFragment<HostListContract.Presenter> implements HostListContract.View {
    public static final String TAG = HostListFragment.class.getSimpleName();
    private TextView toolbarTitle;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private StateLayout stateLayout;
    private PtrHTFrameLayout ptrFrameLayout;
    private HostListRVAdapter adapter;
    private Params params = Params.getInstance();
    private StateManager mStateManager;
    private List<String> addHostTypes;
    private GatewaysBean.DataBean hostBean;

    public static HostListFragment newInstance() {
        return new HostListFragment();
    }

    @NonNull
    @Override
    protected HostListContract.Presenter createPresenter() {
        return new HostListPresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        toolbarTitle = ((TextView) view.findViewById(R.id.toolbar_title));
        toolbar = ((Toolbar) view.findViewById(R.id.toolbar));
        recyclerView = ((RecyclerView) view.findViewById(R.id.recyclerView));
        stateLayout = ((StateLayout) view.findViewById(R.id.stateLayout));
        ptrFrameLayout = ((PtrHTFrameLayout) view.findViewById(R.id.ptrFrameLayout));
        return attachToSwipeBack(view);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToolbar();
        initData();
        initListener();
        initStateManager();
        initPtrFrameLayout(ptrFrameLayout, recyclerView);
    }

    private void initToolbar() {
        initStateBar(toolbar);
        toolbarTitle.setText("管理主机");
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_24dp);
        toolbar.setNavigationOnClickListener(v -> ((SupportActivity) _mActivity).onBackPressedSupport());
    }

    private void initData() {
        recyclerView.setLayoutManager(new LinearLayoutManager(_mActivity));
        recyclerView.addItemDecoration(new Decoration(_mActivity, Decoration.VERTICAL_LIST));
        adapter = new HostListRVAdapter();
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(() -> {
            params.page++;
            mPresenter.requestGateways(params);
        }, recyclerView);
        recyclerView.setAdapter(adapter);
    }

    private void initStateManager() {
        mStateManager = StateManager.builder(_mActivity)
                .setContent(recyclerView)
                .setEmptyView(R.layout.layout_state_empty)
                .setEmptyText("还没有主机哦，赶紧添加吧！")
                .setErrorOnClickListener(v -> ptrFrameLayout.autoRefresh())
                .setEmptyOnClickListener(v -> showAddHostSelecotr())
                .setConvertListener((holder, stateLayout) ->
                        holder.setOnClickListener(R.id.bt_empty_state, v -> showAddHostSelecotr())
                                .setText(R.id.bt_empty_state, "点击添加"))
                .build();
    }

    private void initListener() {
        adapter.setOnItemClickListener((adapter1, view, position)
                -> {
            hostBean = adapter.getData().get(position);
//            startForResult(HostSettingsFragment.newInstance(hostBean), HostSettingsFragment.RESULT_HOST_SETTINGS);
        });
    }

    private void showAddHostSelecotr() {
        if (addHostTypes == null) {
            addHostTypes = new ArrayList<>();
            addHostTypes.add(0, "扫码添加");
            addHostTypes.add(1, "手动输入添加");
        }
        new SelectorDialogFragment()
                .setTitle("请选择添加方式")
                .setItemLayoutId(R.layout.item_rv_simple_selector)
                .setData(addHostTypes)
                .setOnItemConvertListener((holder, position, dialog) ->
                        holder.setText(R.id.tv_item_rv_simple_selector, addHostTypes.get(position)))
                .setOnItemClickListener((view, baseViewHolder, position, dialog) -> {
                    dialog.dismiss();
                    switch (position) {
                        case 0:
                            ((SupportActivity) _mActivity).start(ScanQRCodeFragment.newInstance());
                            break;
                        case 1:
                            ((SupportActivity) _mActivity).start(AddHostFragment.newInstance("", null));
                            break;
                        default:
                            break;
                    }
                })
                .setAnimStyle(R.style.SlideAnimation)
                .setGravity(Gravity.BOTTOM)
                .show(getChildFragmentManager());
    }

    @Override
    public void onRefresh() {
        params.pageSize = 10;
        params.page = 1;
        mPresenter.requestGateways(params);
    }

    @Override
    public void error(String errorMessage) {
        super.error(errorMessage);
        ptrFrameLayout.refreshComplete();
        if (params.page == 1) {
            mStateManager.showError();
        } else if (params.page > 1) {
            adapter.loadMoreFail();
            params.page--;
        }
    }

    @Override
    public void responseGateways(List<GatewaysBean.DataBean> data) {
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
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        super.onFragmentResult(requestCode, resultCode, data);
        ALog.e("requestCode-->" + requestCode);
        ALog.e("resultCode-->" + resultCode);

        if (data != null) {
            hostBean = data.getParcelable(Constants.KEY_HOST);
        }
    }
}