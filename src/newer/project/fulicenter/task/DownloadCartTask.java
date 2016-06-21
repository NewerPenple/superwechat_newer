package newer.project.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.I;
import newer.project.fulicenter.activity.BaseActivity;
import newer.project.fulicenter.bean.CartBean;
import newer.project.fulicenter.data.ApiParams;
import newer.project.fulicenter.data.GsonRequest;

public class DownloadCartTask extends BaseActivity {
    private static final String TAG = DownloadCartTask.class.toString();
    private Context context;
    private String username;
    private String path;
    private int pageId = 0;
    private int pageSize = 1024 * 100;

    public DownloadCartTask(Context context) {
        this.context = context;
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
                    int count = cartBeans.length;
                    FuliCenterApplication.getInstance().setCartCount(count);
                    context.sendStickyBroadcast(new Intent("update_cart_count"));
                }
            }
        };
    }
}