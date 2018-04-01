package cn.itsite.order;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import cn.itsite.acommon.StorePojo;

/**
 * Author： Administrator on 2018/2/1 0001.
 * Email： liujia95me@126.com
 */

public class SubmitOrderBean implements MultiItemEntity {
    private static final String TAG = SubmitOrderBean.class.getSimpleName();

    public static final int TYPE_STORE_TITLE = 1;
    public static final int TYPE_STORE_GOODS = 2;
    public static final int TYPE_ORDER_INFO = 3;

    private int itemType;

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    private StorePojo.ShopBean shopBean;
    private StorePojo.ProductsBean productsBean;

    public StorePojo.ShopBean getShopBean() {
        return shopBean;
    }

    public void setShopBean(StorePojo.ShopBean shopBean) {
        this.shopBean = shopBean;
    }

    public StorePojo.ProductsBean getProductsBean() {
        return productsBean;
    }

    public void setProductsBean(StorePojo.ProductsBean productsBean) {
        this.productsBean = productsBean;
    }
}
