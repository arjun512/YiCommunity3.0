package cn.itsite.order.model;

import java.util.List;

import cn.itsite.abase.mvp.model.base.BaseModel;
import cn.itsite.abase.network.http.BaseResponse;
import cn.itsite.abase.network.http.HttpHelper;
import cn.itsite.acommon.GoodsParams;
import cn.itsite.order.contract.OrderService;
import cn.itsite.order.contract.MineOrderContract;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * @author liujia
 * @version v0.0.0
 * @E-mail liujia95me@126.com
 * @time 2018/3/19 0019 15:51
 */

public class MineOrderModel extends BaseModel implements MineOrderContract.Model{

    @Override
    public Observable<BaseResponse<List<CategoryBean>>> getCategories(GoodsParams goodsParams) {
        return HttpHelper.getService(OrderService.class)
                .getCategories(goodsParams.toString())
                .subscribeOn(Schedulers.io());
    }

}
