package cn.itsite.shoppingcart;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.List;

import cn.itsite.abase.common.DialogHelper;
import cn.itsite.abase.mvp.view.base.BaseFragment;
import cn.itsite.abase.network.http.BaseResponse;
import cn.itsite.abase.utils.ScreenUtils;
import cn.itsite.abase.utils.ToastUtils;
import cn.itsite.acommon.GoodsCounterView;
import cn.itsite.acommon.GoodsParams;
import cn.itsite.acommon.OperatorBean;
import cn.itsite.acommon.SpecificationDialog;
import cn.itsite.acommon.model.ProductsBean;
import cn.itsite.adialog.dialogfragment.BaseDialogFragment;
import cn.itsite.shoppingcart.contract.CartContract;
import cn.itsite.shoppingcart.presenter.CartPresenter;

/**
 * Author： Administrator on 2018/1/31 0031.
 * Email： liujia95me@126.com
 */
@Route(path = "/shoppingcart/shoppingcartfragment")
public class ShoppingCartFragment extends BaseFragment<CartContract.Presenter> implements CartContract.View, View.OnClickListener {

    public static final String TAG = ShoppingCartFragment.class.getSimpleName();

    private RelativeLayout mRlToolbar;
    private RecyclerView mRecyclerView;
    private ShoppingCartRVAdapter mAdapter;
    private CheckBox mCbSelectAll;
    private TextView mTvSubmit;
    private TextView mTvTotalSum;
    private TextView mTvEdit;
    private TextView mTvAnchor;//锚，无需在意这个view
    //--------------------------
    private boolean isEditModel;//是编辑模式吗
    //    private GoodsCounterView mCurrentCounterView;//当前计数的view
    List<StoreBean> mDatas = new ArrayList<>();

    StoreBean emptyBean = new StoreBean();//空页面对应的bean

    private String cartUid = "-1";

    private GoodsParams mGoodsParams = new GoodsParams();
    private ImageView mIvArrowLeft;

    private StorePojo.ProductsBean mOperationProduct;
    private int mOptionAmount;
    private SwipeLayout mOperationSwipeLayout;

    public static ShoppingCartFragment newInstance() {
        return new ShoppingCartFragment();
    }

    @NonNull
    @Override
    protected CartContract.Presenter createPresenter() {
        return new CartPresenter(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping_cart, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRlToolbar = view.findViewById(R.id.rl_toolbar);
        mCbSelectAll = view.findViewById(R.id.cb_select_all);
        mTvSubmit = view.findViewById(R.id.tv_submit);
        mTvTotalSum = view.findViewById(R.id.tv_total_sum);
        mTvEdit = view.findViewById(R.id.tv_edit);
        mTvAnchor = view.findViewById(R.id.anchor_1);
        mIvArrowLeft = view.findViewById(R.id.iv_arrow_left);
        return attachToSwipeBack(view);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initStatusBar();
        initData();
        initListener();
    }

    private void initStatusBar() {
        mRlToolbar.setPadding(mRlToolbar.getPaddingLeft(), mRlToolbar.getPaddingTop() + ScreenUtils.getStatusBarHeight(_mActivity), mRlToolbar.getPaddingRight(), mRlToolbar.getPaddingBottom());
    }

    private void initData() {
        emptyBean.setItemType(StoreBean.TYPE_EMPTY);
        emptyBean.setSpanSize(2);

        mRecyclerView.setLayoutManager(new GridLayoutManager(_mActivity, 2));
        mAdapter = new ShoppingCartRVAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setSpanSizeLookup(new BaseQuickAdapter.SpanSizeLookup() {
            @Override
            public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
                return mDatas.get(position).getSpanSize();
            }
        });
        mAdapter.setNewData(mDatas);
        mPresenter.getCarts(cartUid);
    }

    private void initListener() {
        mTvSubmit.setOnClickListener(this);
        mTvEdit.setOnClickListener(this);
        mIvArrowLeft.setOnClickListener(this);
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                StoreBean item = mAdapter.getItem(position);
                switch (item.getItemType()) {
                    case StoreBean.TYPE_STORE_TITLE:
                        break;
                    case StoreBean.TYPE_STORE_GOODS:
                        if (view.getId() == R.id.tv_specification) {
                            showSpecificationDialog();
                        } else if (view.getId() == R.id.tv_confirm) {
                            GoodsCounterView goodsCounterView = ((View) view.getParent()).findViewById(R.id.goodsCounterView);
                            mOperationSwipeLayout = (SwipeLayout) view.getParent().getParent();
                            mOperationProduct = item.getProductsBean();
                            mOptionAmount = goodsCounterView.getCounter();
                            ProductsBean productsBean = new ProductsBean();
                            productsBean.setAmount(goodsCounterView.getCounter() + "");
                            productsBean.setSku(item.getProductsBean().getSku());
                            productsBean.setUid(item.getProductsBean().getUid());
                            mPresenter.putProduct(cartUid, productsBean);
                        } else if (view.getId() == R.id.iv_edit) {
                            mOperationSwipeLayout = (SwipeLayout) view.getParent().getParent();
                            mOperationSwipeLayout.open();
                        }
                        break;
                    case StoreBean.TYPE_RECOMMEND_TITLE:
                        break;
                    case StoreBean.TYPE_RECOMMEND_GOODS:
                        Fragment goodsDetailFragment = (Fragment) ARouter.getInstance().build("/goodsdetail/goodsdetailfragment").navigation();
                        start((BaseFragment) goodsDetailFragment);
                        break;
                    case StoreBean.TYPE_EMPTY:
                        pop();
                        break;
                    default:
                }
            }
        });

//        mAdapter.setOnAddMinusClickListener(new GoodsCounterView.OnAddMinusClickListener() {
//            @Override
//            public void clickAdd(GoodsCounterView view) {
//                mPresenter.putProduct("123", "123");
//                mCurrentCounterView = view;
//            }
//
//            @Override
//            public void clickMinus(GoodsCounterView view) {
//                mPresenter.putProduct("123", "123");
//                mCurrentCounterView = view;
//            }
//        });

        mCbSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                List<StoreBean> data = mAdapter.getData();
                for (int i = 0; i < data.size(); i++) {
                    StoreBean bean = data.get(i);
                    bean.setChecked(isChecked);
                }
                mAdapter.notifyDataSetChanged();
                computePrice();
            }
        });

        mAdapter.setOnCheckedChangedListener(new ShoppingCartRVAdapter.OnCheckedChangedListener() {
            @Override
            public void onStoreCheckedChanged(int position, boolean isChecked) {
                checkStoreGoods(position, isChecked);
                computePrice();
            }

            @Override
            public void onGoodsCheckedChanged(int position, boolean isChecked) {
                mAdapter.getData().get(position).setChecked(isChecked);
                computePrice();
            }
        });
    }

    //计算总额
    private void computePrice() {
        List<StoreBean> data = mAdapter.getData();
        float amountPrice = 0;
        String currency = "";
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getItemType() == StoreBean.TYPE_STORE_GOODS && data.get(i).isChecked()) {
                StorePojo.ProductsBean productsBean = data.get(i).getProductsBean();
                amountPrice += Float.valueOf(productsBean.getPay().getPrice()) * productsBean.getCount();
                if (TextUtils.isEmpty(currency)) {
                    currency = productsBean.getPay().getCurrency();
                }
            }
        }
        mTvTotalSum.setText(currency + amountPrice);
    }

    private void showHintDialog() {
        new BaseDialogFragment()
                .setLayoutId(R.layout.dialog_hint)
                .setConvertListener((holder, dialog) -> {
                    holder.setText(R.id.tv_content, "您确定删除选中的商品？")
                            .setText(R.id.btn_cancel, "取消")
                            .setText(R.id.btn_comfirm, "确定")
                            .setOnClickListener(R.id.btn_cancel, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            })
                            .setOnClickListener(R.id.btn_comfirm, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteProducts();
                                    dialog.dismiss();
                                }
                            });
                })
                .setMargin(40)
                .setDimAmount(0.3f)
                .setGravity(Gravity.CENTER)
                .show(getChildFragmentManager());
    }

    private void deleteProducts() {
        List<StoreBean> data = mAdapter.getData();
        List<OperatorBean> deleteBeans = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getItemType() == StoreBean.TYPE_STORE_GOODS && data.get(i).isChecked()) {
                StorePojo.ProductsBean productsBean = data.get(i).getProductsBean();
                OperatorBean bean = new OperatorBean();
                bean.sku = productsBean.getSku();
                bean.uid = productsBean.getUid();
                deleteBeans.add(bean);
            }
        }
        if (deleteBeans.size() > 0) {
            mPresenter.deleteProduct(cartUid, deleteBeans);
        } else {
            DialogHelper.warningSnackbar(getView(), "请勾选要删除的商品");
        }
    }

    //刷新选中的商城商品
    private void checkStoreGoods(int position, boolean isChecked) {
        StoreBean bean = mAdapter.getData().get(position);
        for (int i = position; i <= bean.getGoodsCount() + position; i++) {
            mDatas.get(i).setChecked(isChecked);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void responseDeleteSuccess(BaseResponse response) {
        DialogHelper.successSnackbar(getView(), response.getMessage());
        mPresenter.getCarts(cartUid);
    }

    @Override
    public void responsePostSuccess(BaseResponse response) {
        DialogHelper.successSnackbar(getView(), response.getMessage());
    }

    @Override
    public void responsePutSuccess(BaseResponse response) {
        DialogHelper.successSnackbar(getView(), response.getMessage());
        if (mOperationSwipeLayout != null) {
            mOperationSwipeLayout.close();
            mOperationProduct.setCount(mOptionAmount);
        }
    }

    @Override
    public void responseGetCartsSuccess(List<StoreBean> data) {
        if (data == null || data.isEmpty()) {
            mDatas.clear();
            mDatas.add(emptyBean);
        } else {
            mDatas = data;
        }
        //查推荐
        mPresenter.getRecommendGoods(mGoodsParams);
    }

    @Override
    public void responseRecommendGoodsSuccess(List<StoreBean> data) {
        mDatas.addAll(data);
        mAdapter.setNewData(mDatas);
    }

    private void switchEditModel() {
        if (isEditModel) {
            mTvEdit.setText("编辑");
            mTvEdit.setTextColor(_mActivity.getResources().getColor(R.color.base_black));
            mTvSubmit.setText("结算");
            mTvSubmit.setBackgroundColor(_mActivity.getResources().getColor(R.color.warn));
            mTvAnchor.setVisibility(View.VISIBLE);
            mTvTotalSum.setVisibility(View.VISIBLE);
        } else {
            mTvEdit.setText("完成");
            mTvEdit.setTextColor(_mActivity.getResources().getColor(R.color.warn));
            mTvSubmit.setText("删除");
            mTvSubmit.setBackgroundColor(_mActivity.getResources().getColor(R.color.error));
            mTvAnchor.setVisibility(View.GONE);
            mTvTotalSum.setVisibility(View.GONE);
        }
        isEditModel = !isEditModel;
    }

    private void clickSubmit() {
        if (isEditModel) {
            //删除
            showHintDialog();
//            mPresenter.deleteProduct("123", "123");
        } else {
            //结算
            ToastUtils.showToast(_mActivity, "结算");
            Fragment fragment = (Fragment) ARouter.getInstance().build("/shoppingcart/shoppingcartfragment").navigation();
            Bundle bundle = new Bundle();
//            bundle.putSerializable();
            fragment.setArguments(bundle);
            start((BaseFragment) fragment);
        }
    }

    private void showSpecificationDialog() {
        SpecificationDialog dialog = new SpecificationDialog();
        dialog.show(getChildFragmentManager());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_edit) {
            switchEditModel();
        } else if (v.getId() == R.id.tv_submit) {
            clickSubmit();
        } else if (v.getId() == R.id.iv_arrow_left) {
            pop();
        }
    }


}
