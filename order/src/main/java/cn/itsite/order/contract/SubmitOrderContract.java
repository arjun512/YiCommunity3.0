package cn.itsite.order.contract;

import java.util.List;

import cn.itsite.abase.mvp.contract.base.BaseContract;
import cn.itsite.abase.network.http.BaseOldResponse;
import cn.itsite.abase.network.http.BaseRequest;
import cn.itsite.abase.network.http.BaseResponse;
import cn.itsite.acommon.data.bean.DeliveryBean;
import cn.itsite.acommon.data.bean.OperateBean;
import rx.Observable;

/**
 * @author liujia
 * @version v0.0.0
 * @E-mail liujia95me@126.com
 * @time 2018/4/2 0002 15:22
 */

public interface SubmitOrderContract {

    interface View extends BaseContract.View {
        void responseGetAddress(List<DeliveryBean> data);

        void responsePostOrdersSuccess(BaseResponse<List<OperateBean>> response);

        void responseCheckOrderStatus(int status);

        void responseWalletPay(BaseOldResponse response);
    }

    interface Presenter extends BaseContract.Presenter {
        void getAddress();

        void postOrders(List<OperateBean> data);

        void checkOrderStatus(String orderUid);

        void requestWalletPay(BaseRequest request);
    }

    interface Model extends BaseContract.Model {
        Observable<BaseResponse<List<DeliveryBean>>> getAddress();

        Observable<BaseResponse<List<OperateBean>>> postOrders(List<OperateBean> data);

        Observable<BaseResponse<OperateBean>> checkOrderStatus(String orderUid);

        Observable<BaseOldResponse> requestWalletPay(BaseRequest request);
    }

}
