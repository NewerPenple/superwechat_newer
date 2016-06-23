package newer.project.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;

import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.I;
import newer.project.fulicenter.activity.BaseActivity;
import newer.project.fulicenter.bean.CartBean;
import newer.project.fulicenter.bean.GoodDetailsBean;
import newer.project.fulicenter.data.ApiParams;
import newer.project.fulicenter.data.GsonRequest;
import newer.project.fulicenter.data.OkHttpUtils3;
import newer.project.fulicenter.utils.Utils;

public class DownloadCartTask extends BaseActivity {
    private static final String TAG = DownloadCartTask.class.toString();
    private Context context;
    private String username;
    private String path;
    private int pageId = 0;
    private int pageSize;
    private ArrayList<CartBean> cartList;
    private int listSize;

    public DownloadCartTask(Context context,int pageSize) {
        this.context = context;
        this.pageSize = pageSize;
        this.username = FuliCenterApplication.getInstance().getUser().getMUserName();
        initPath();
    }

    public void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Cart.USER_NAME, username)
                    .with(I.PAGE_ID, String.valueOf(pageId))
                    .with(I.PAGE_SIZE, String.valueOf(pageSize))
                    .getRequestUrl(I.REQUEST_FIND_CARTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<CartBean[]>(path, CartBean[].class, responseDownloadCollectCountTask(), errorListener()));
    }

    private Response.Listener<CartBean[]> responseDownloadCollectCountTask() {
        return new Response.Listener<CartBean[]>() {
            @Override
            public void onResponse(CartBean[] cartBeans) {
                if (cartBeans != null && cartBeans.length > 0) {
                    cartList = Utils.array2List(cartBeans);
                    downloadGoodsDetailOfCarts();
                } else {
                    FuliCenterApplication.getInstance().setCartList(null);
                    context.sendStickyBroadcast(new Intent("update_user_cart"));
                }
            }
        };
    }

    private void downloadGoodsDetailOfCarts() {
        listSize = 0;
        for (int i = 0; i < cartList.size(); i++) {
            OkHttpUtils3<GoodDetailsBean> utils = new OkHttpUtils3<GoodDetailsBean>();
            utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                    .addParam(I.KEY_REQUEST,I.REQUEST_FIND_GOOD_DETAILS)
                    .addParam(I.CategoryGood.GOODS_ID, String.valueOf(cartList.get(i).getGoodsId()))
                    .targetClass(GoodDetailsBean.class)
                    .execute(new GoodsDetailListener(i));
        }
    }

    private class GoodsDetailListener implements OkHttpUtils3.OnCompleteListener<GoodDetailsBean> {
        private int num;

        public GoodsDetailListener(int num) {
            this.num = num;
        }

        @Override
        public void onSuccess(GoodDetailsBean result) {
            listSize++;
            if (result != null) {
                cartList.get(num).setGoods(result);
            }
            if (listSize == cartList.size()) {
                FuliCenterApplication.getInstance().setCartList(cartList);
                context.sendStickyBroadcast(new Intent("update_user_cart"));
            }
        }

        @Override
        public void onError(String error) {
            listSize++;
            if (listSize == cartList.size()) {
                FuliCenterApplication.getInstance().setCartList(cartList);
                context.sendStickyBroadcast(new Intent("update_user_cart"));
            }
        }
    }
}