package cn.itsite.classify;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.List;

import cn.itsite.abase.BaseApp;
import cn.itsite.abase.mvp.view.base.BaseFragment;
import cn.itsite.abase.utils.DensityUtils;
import cn.itsite.abase.utils.ScreenUtils;
import cn.itsite.acommon.GoodsParams;
import cn.itsite.classify.contract.MenuContract;
import cn.itsite.classify.presenter.MenuPresenter;
import cn.itsite.statemanager.BaseViewHolder;
import cn.itsite.statemanager.StateLayout;
import cn.itsite.statemanager.StateListener;
import cn.itsite.statemanager.StateManager;
import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * Author： Administrator on 2018/1/29 0029.
 * Email： liujia95me@126.com
 */
@Route(path = "/classify/classifyfragment")
public class ClassifyFragment extends BaseFragment<MenuContract.Presenter> implements MenuContract.View, BaseQuickAdapter.OnItemClickListener {

    public static final String TAG = ClassifyFragment.class.getSimpleName();

    private static final int SPAN_COUNT_ONE = 1;
    private static final int SPAN_COUNT_TWO = 2;

    private RecyclerView mRvMenu;
    private RecyclerView mRvContent;
    private RecyclerView mRvSubMenu;
    private LinearLayout mLlToolbar;
    private ImageView mIvSwitchView;
    private ImageView mIvStretchMenu;

    private ClassifyMenuRVAdapter mAdapterMenu;
    private ClassifySubMenuRVAdapter mAdapterSubMenu;
    private ClassifyContentGridRVAdapter mAdapterContentGrid;
    private ClassifyContentLinearRVAdapter mAdapterContentLinear;
    private LinearLayout mLlStretchable;

    private GridLayoutManager mContentLayoutManager;
    private GoodsParams mParams = new GoodsParams();

    private static final int ONE_UNFOLD_LINE_HEIGHT = DensityUtils.dp2px(BaseApp.mContext, 33);
    private static final int ANIMATION_DURATION = 400;
    //    private int maxUnfoldHeight;//展开的最大高度，不能超过(ONE_UNFOLD_LINE_HEIGHT)的4倍高
    private boolean subMenuCanScroll = false;//控制三级菜单能否滚动
    private TextView mTvSearch;
    private ImageView mIvBack;
    private StateManager mStateManager;
    private PtrFrameLayout mPtrFrameLayout;
    private String mMenuUid;

    public static ClassifyFragment newInstance() {
        return new ClassifyFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParams.shopUid = getArguments().getString("shopUid");
        mParams.shoptype = getArguments().getString("shopType");
        mMenuUid = getArguments().getString("menuUid");
    }

    @NonNull
    @Override
    protected MenuContract.Presenter createPresenter() {
        return new MenuPresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_classify, container, false);
        mLlToolbar = view.findViewById(R.id.ll_toolbar);
        mRvMenu = view.findViewById(R.id.rv_menu);
        mRvContent = view.findViewById(R.id.recyclerView);
        mIvSwitchView = view.findViewById(R.id.iv_switch);
        mRvSubMenu = view.findViewById(R.id.rv_sub_menu);
        mLlStretchable = view.findViewById(R.id.ll_stretchable);
        mIvStretchMenu = view.findViewById(R.id.iv_stretch);
        mTvSearch = view.findViewById(R.id.tv_input);
        mIvBack = view.findViewById(R.id.iv_back);
        mPtrFrameLayout = view.findViewById(R.id.ptrFrameLayout);
        return attachToSwipeBack(view);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initStatusBar();
        initStateManager();
        initData();
        initListener();
        initPtrFrameLayout(mPtrFrameLayout, mRvContent);
    }

    private void initStatusBar() {
        mLlToolbar.setPadding(mLlToolbar.getPaddingLeft(), mLlToolbar.getPaddingTop() + ScreenUtils.getStatusBarHeight(_mActivity), mLlToolbar.getPaddingRight(), mLlToolbar.getPaddingBottom());
    }

    private void initStateManager() {
        mStateManager = StateManager.builder(_mActivity)
                .setContent(mRvContent)
                .setEmptyView(R.layout.state_empty_layout)
                .setEmptyImage(R.drawable.ic_prompt_shangpin_01)
                .setConvertListener(new StateListener.ConvertListener() {
                    @Override
                    public void convert(BaseViewHolder holder, StateLayout stateLayout) {
                        holder.setVisible(R.id.bt_empty_state, false);
                    }
                })
                .setEmptyText("抱歉，该分类暂无商品~")
                .build();
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mParams.page = 1;
        //为了防止首次进来就立即刷新，商品是要获取category参数之后才刷新的
        if (mParams.category != null)
            mPresenter.getProducts(mParams);
    }

    private void initData() {
        mContentLayoutManager = new GridLayoutManager(_mActivity, SPAN_COUNT_TWO);
        mRvMenu.setLayoutManager(new LinearLayoutManager(_mActivity));
        mAdapterMenu = new ClassifyMenuRVAdapter();
        mRvMenu.setAdapter(mAdapterMenu);

        mRvContent.setLayoutManager(mContentLayoutManager);
        mAdapterContentGrid = new ClassifyContentGridRVAdapter();
        mAdapterContentGrid.setEnableLoadMore(true);
        mAdapterContentGrid.setOnLoadMoreListener(() -> {
            mParams.page++;
            mPresenter.getProducts(mParams);
        }, mRvContent);
        mAdapterContentLinear = new ClassifyContentLinearRVAdapter();
        mAdapterContentLinear.setEnableLoadMore(true);
        mAdapterContentLinear.setOnLoadMoreListener(() -> {
            mParams.page++;
            mPresenter.getProducts(mParams);
        }, mRvContent);
        mRvContent.setAdapter(mAdapterContentGrid);

        mRvSubMenu.setLayoutManager(new GridLayoutManager(_mActivity, 3) {
            @Override
            public boolean canScrollVertically() {
                return subMenuCanScroll;
            }
        });
        mAdapterSubMenu = new ClassifySubMenuRVAdapter();
        mRvSubMenu.setAdapter(mAdapterSubMenu);

//        mParams.category = "123";
        mPresenter.getGategories(mParams);
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop();
            }
        });
        mIvSwitchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContentLayoutManager.getSpanCount() == SPAN_COUNT_ONE) {
                    mContentLayoutManager.setSpanCount(SPAN_COUNT_TWO);
                    mRvContent.setAdapter(mAdapterContentGrid);
                    mIvSwitchView.setImageResource(R.drawable.ic_item_switch0_gray_24dp);
                } else {
                    mContentLayoutManager.setSpanCount(SPAN_COUNT_ONE);
                    mRvContent.setAdapter(mAdapterContentLinear);
                    mIvSwitchView.setImageResource(R.drawable.ic_item_switch1_gray_24dp);
                }
            }
        });

        mIvStretchMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int dataCount = mAdapterSubMenu.getData().size();
                int maxUnfoldHeight = (int) (Math.min(Math.ceil(dataCount / 3.0), 4) * ONE_UNFOLD_LINE_HEIGHT);
                if (mIvStretchMenu.isSelected()) {
                    closeSubMenu();
                } else {
                    openSubMenu(maxUnfoldHeight);
                }
                mIvStretchMenu.setSelected(!mIvStretchMenu.isSelected());
            }
        });

        mRvContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                closeSubMenu();
                return false;
            }
        });

        mAdapterContentGrid.setOnItemClickListener(this);
        mAdapterContentLinear.setOnItemClickListener(this);

        mAdapterMenu.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter1, View view, int position) {
                clickFirstMenu(position);
            }
        });

        mAdapterSubMenu.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter1, View view, int position) {
                MenuBean.ChildrenBean bean = mAdapterSubMenu.getData().get(position);
                mAdapterSubMenu.setSelectedPosition(position);
                mParams.category = bean.getUid();
                onRefresh();

            }
        });

        mTvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = (Fragment) ARouter.getInstance().build("/goodssearch/searchgoodsfragment").navigation();
                start((BaseFragment) fragment);
            }
        });
    }

    //点击一级菜单
    private void clickFirstMenu(int position) {
        MenuBean menuBean = mAdapterMenu.getData().get(position);
        mAdapterMenu.setSelectedPosition(position);
        mRvMenu.scrollToPosition(position);
        //判断是否隐藏二级菜单右侧展开按钮
        if (menuBean.getChildren().size() > 2) {
            mIvStretchMenu.setVisibility(View.VISIBLE);
        } else {
            mIvStretchMenu.setVisibility(View.INVISIBLE);
        }
        //增加一项：全部
        MenuBean.ChildrenBean allBean = new MenuBean.ChildrenBean();
        allBean.setCategory("全部");
        allBean.setUid(menuBean.getUid());

        mAdapterSubMenu.setNewData(null);
        mAdapterSubMenu.addData(allBean);
        mAdapterSubMenu.addData(menuBean.getChildren());
        mAdapterSubMenu.setSelectedPosition(0);
        //逻辑按点击“全部”一致，请求网络
        mParams.category = menuBean.getUid();
        onRefresh();

        stretch(ONE_UNFOLD_LINE_HEIGHT);
    }

    //把三级菜单伸缩至指定的高度
    private void stretch(int end) {
        final ViewGroup.LayoutParams layoutParams = mLlStretchable.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(layoutParams.height, end);
        animator.setDuration(ANIMATION_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                layoutParams.height = value;
                mLlStretchable.setLayoutParams(layoutParams);
            }
        });
        animator.start();
    }

    private void closeSubMenu() {
        stretch(ONE_UNFOLD_LINE_HEIGHT);
        mRvSubMenu.scrollToPosition(0);//回到顶部
        subMenuCanScroll = false;
    }

    private void openSubMenu(int maxUnfoldHeight) {
        stretch(maxUnfoldHeight);
        subMenuCanScroll = true;
    }

    @Override
    public void start(Object response) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void responseGetGategories(List<MenuBean> data) {
        mAdapterMenu.setNewData(data);
        int selectedMenuPos = 0;
        if (mMenuUid != null) {
            for (int i = 0; i < data.size(); i++) {
                if (mMenuUid.equals(data.get(i).getUid())) {
                    selectedMenuPos = i;
                    break;
                }
            }
        }
        if (data.size() > 0) {
            //选中第一项
            clickFirstMenu(selectedMenuPos);
        }
    }

    @Override
    public void responseGetProducts(List<ProductBean> datas) {
        mPtrFrameLayout.refreshComplete();
        if (datas == null || datas.isEmpty()) {
            if (mParams.page == 1) {
                mStateManager.showEmpty();
            }
            mAdapterContentGrid.loadMoreEnd();
            mAdapterContentLinear.loadMoreEnd();
            return;
        }

        if (mParams.page == 1) {
            mStateManager.showContent();
            mAdapterContentGrid.setNewData(datas);
            mAdapterContentGrid.disableLoadMoreIfNotFullPage(mRvContent);
            mAdapterContentLinear.setNewData(datas);
            mAdapterContentLinear.disableLoadMoreIfNotFullPage(mRvContent);
        } else {
            mAdapterContentGrid.addData(datas);
            mAdapterContentGrid.setEnableLoadMore(true);
            mAdapterContentGrid.loadMoreComplete();
            mAdapterContentLinear.addData(datas);
            mAdapterContentLinear.setEnableLoadMore(true);
            mAdapterContentLinear.loadMoreComplete();
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Fragment goodsDetailFragment = (Fragment) ARouter.getInstance().build("/goodsdetail/goodsdetailfragment").navigation();
        Bundle bundle = new Bundle();
        bundle.putString("uid", mAdapterContentLinear.getData().get(position).getUid());
        goodsDetailFragment.setArguments(bundle);
        start((BaseFragment) goodsDetailFragment);
    }
}
