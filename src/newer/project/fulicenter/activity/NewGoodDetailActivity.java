package newer.project.fulicenter.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.easemob.chat.EMChat;

import java.util.ArrayList;

import newer.project.fulicenter.D;
import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.bean.AlbumBean;
import newer.project.fulicenter.bean.CartBean;
import newer.project.fulicenter.bean.GoodDetailsBean;
import newer.project.fulicenter.bean.MessageBean;
import newer.project.fulicenter.bean.User;
import newer.project.fulicenter.data.OkHttpUtils2;
import newer.project.fulicenter.task.DownloadCartTask;
import newer.project.fulicenter.task.DownloadCollectCountTask;
import newer.project.fulicenter.utils.DisplayUtils;
import newer.project.fulicenter.utils.ImageUtils;
import newer.project.fulicenter.utils.Utils;
import newer.project.fulicenter.widget.FlowIndicator;
import newer.project.fulicenter.widget.SlideAutoLoopView;

public class NewGoodDetailActivity extends BaseActivity {
    private static final String TAG = NewGoodDetailActivity.class.getName();
    private TextView mtvBack,mtvNameEn,mtvNameCh,mtvPrice,mtvCartIconHint;
    private ImageView mivCartIcon,mivCollectIcon,mivShareIcon;
    private RelativeLayout mLayoutCartIcon;
    private SlideAutoLoopView msalvPicture;
    private FlowIndicator mfiCursor;
    private WebView mwvDetail;
    private int goodId;
    private GoodDetailsBean mGoodDetails;
    private LinearLayout mLayoutColor;
    private int currentColor;
    private User user;
    private boolean isCollect;
    private int action;
    private CartCountReceiver cartCountReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_good_detail);
        initView();
        initData();
        setListener();
        setCartCountReceiver();
    }

    private void setListener() {
        mivCollectIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!EMChat.getInstance().isLoggedIn()) {
                    startActivity(new Intent(NewGoodDetailActivity.this, LoginActivity.class));
                } else {
                    switch (action) {
                        case I.ACT_COLLECT_ADD:
                            if (!isCollect) {
                                addCollect();
                            }
                            break;
                        case I.ACT_COLLECT_DELETE:
                            if (isCollect) {
                                deleteCollect();
                            }
                            break;
                    }
                }
            }
        });

        mivCartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<CartBean> list = FuliCenterApplication.getInstance().getCartList();
                for (CartBean cart : list) {
                    if (cart.getGoods().getGoodsId() == goodId) {
                        setResult(LoginActivity.RESULT_CODE_TO_CART);
                        finish();
                        return;
                    }
                }
                addCart();
            }
        });
    }

    private void addCart() {
        OkHttpUtils2<MessageBean> utils = new OkHttpUtils2<MessageBean>();
        utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                .addParam(I.KEY_REQUEST, I.REQUEST_ADD_CART)
                .addParam(I.Cart.USER_NAME, FuliCenterApplication.getInstance().getUser().getMUserName())
                .addParam(I.Cart.GOODS_ID, String.valueOf(goodId))
                .addParam(I.Cart.COUNT, String.valueOf(1))
                .addParam(I.Cart.IS_CHECKED, String.valueOf(true))
                .targetClass(MessageBean.class)
                .execute(new OkHttpUtils2.OnCompleteListener<MessageBean>() {
                    @Override
                    public void onSuccess(MessageBean result) {
                        if (result != null && result.isSuccess()) {
                            new DownloadCartTask(NewGoodDetailActivity.this, 1024 * 10).execute();
                        } else {
                            Utils.showToast(NewGoodDetailActivity.this, "商品添加失败", Toast.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.i("my", TAG + " " + error);
                    }
                });
    }

    private void deleteCollect() {
        OkHttpUtils2<MessageBean> utils = new OkHttpUtils2<MessageBean>();
        utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                .addParam(I.KEY_REQUEST, I.REQUEST_DELETE_COLLECT)
                .addParam(I.Collect.USER_NAME, user.getMUserName())
                .addParam(I.Collect.GOODS_ID, String.valueOf(mGoodDetails.getGoodsId()))
                .targetClass(MessageBean.class)
                .execute(new OkHttpUtils2.OnCompleteListener<MessageBean>() {
                    @Override
                    public void onSuccess(MessageBean result) {
                        if (result != null && result.isSuccess()) {
                            isCollect = false;
                            action = I.ACT_COLLECT_ADD;
                            setCollectIcon();
                            new DownloadCollectCountTask(NewGoodDetailActivity.this).execute();
                        } else {
                            Utils.showToast(NewGoodDetailActivity.this, result.getMsg(), Toast.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.i("my", TAG + " " + error);
                    }
                });
    }

    private void addCollect() {
        OkHttpUtils2<MessageBean> utils = new OkHttpUtils2<MessageBean>();
        utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                .addParam(I.KEY_REQUEST, I.REQUEST_ADD_COLLECT)
                .addParam(I.Collect.USER_NAME, user.getMUserName())
                .addParam(I.Collect.GOODS_ID, String.valueOf(mGoodDetails.getGoodsId()))
                .addParam(I.Collect.GOODS_NAME, mGoodDetails.getGoodsName())
                .addParam(I.Collect.GOODS_ENGLISH_NAME, mGoodDetails.getGoodsEnglishName())
                .addParam(I.Collect.GOODS_THUMB, mGoodDetails.getGoodsThumb())
                .addParam(I.Collect.GOODS_IMG, mGoodDetails.getGoodsImg())
                .addParam(I.Collect.ADD_TIME, String.valueOf(mGoodDetails.getAddTime()))
                .targetClass(MessageBean.class)
                .execute(new OkHttpUtils2.OnCompleteListener<MessageBean>() {
                    @Override
                    public void onSuccess(MessageBean result) {
                        if (result != null && result.isSuccess()) {
                            isCollect = true;
                            action = I.ACT_COLLECT_DELETE;
                            setCollectIcon();
                            new DownloadCollectCountTask(NewGoodDetailActivity.this).execute();
                        } else {
                            Utils.showToast(NewGoodDetailActivity.this, result.getMsg(), Toast.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.i("my", TAG + " " + error);
                    }
                });
    }

    private void initData() {
        DisplayUtils.initBack(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        goodId = intent.getIntExtra(D.NewGood.KEY_GOODS_ID, -1);
        if (goodId != -1) {
            showGood();
        }
    }

    private void downloadIsCollect() {
        user = FuliCenterApplication.getInstance().getUser();
        OkHttpUtils2<MessageBean> utils = new OkHttpUtils2<MessageBean>();
        utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                .addParam(I.KEY_REQUEST,I.REQUEST_IS_COLLECT)
                .addParam(I.Collect.GOODS_ID, String.valueOf(goodId))
                .addParam(I.Collect.USER_NAME,user.getMUserName())
                .targetClass(MessageBean.class)
                .execute(new OkHttpUtils2.OnCompleteListener<MessageBean>() {
                    @Override
                    public void onSuccess(MessageBean result) {
                        if (result != null && result.isSuccess()) {
                            isCollect = true;
                            action = I.ACT_COLLECT_DELETE;
                            setCollectIcon();
                        } else {
                            isCollect = false;
                            action = I.ACT_COLLECT_ADD;
                            setCollectIcon();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.i("my", TAG + " " + error);
                    }
                });
    }

    private void showGood() {
        OkHttpUtils2<GoodDetailsBean> utils = new OkHttpUtils2<GoodDetailsBean>();
        utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                .addParam(I.KEY_REQUEST, I.REQUEST_FIND_GOOD_DETAILS)
                .addParam(D.NewGood.KEY_GOODS_ID, String.valueOf(goodId))
                .targetClass(GoodDetailsBean.class)
                .execute(new OkHttpUtils2.OnCompleteListener<GoodDetailsBean>() {
                    @Override
                    public void onSuccess(GoodDetailsBean result) {
                        if (result != null) {
                            mGoodDetails = result;
                            mtvNameEn.setText(result.getGoodsEnglishName());
                            mtvNameCh.setText(result.getGoodsName());
                            mtvPrice.setText(result.getCurrencyPrice());
                            mwvDetail.loadDataWithBaseURL(null, result.getGoodsBrief().trim(), D.TEXT_HTML, D.UTF_8, null);
                            initColorsBanner();
                            downloadIsCollect();
                        } else {
                            finish();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        finish();
                    }
                });
    }

    private void initColorsBanner() {
        updateColor(0);
        for (int i = 0; i < mGoodDetails.getProperties().length; i++) {
            currentColor = i;
            View layout = View.inflate(NewGoodDetailActivity.this, R.layout.layout_property_color, null);
            NetworkImageView niv = (NetworkImageView) layout.findViewById(R.id.ivColorItem);
            String colorImg = mGoodDetails.getProperties()[i].getColorImg();
            if (colorImg == null || colorImg.isEmpty()) {
                continue;
            }
            ImageUtils.setGoodsDetailThumb(niv, colorImg);
            mLayoutColor.addView(layout);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateColor(currentColor);
                }
            });
        }
    }

    private void updateColor(int i) {
        AlbumBean[] albumArr = mGoodDetails.getProperties()[i].getAlbums();
        String[] albumUrlArr = new String[albumArr.length];
        for (int j = 0; j < albumUrlArr.length; j++) {
            albumUrlArr[j] = albumArr[j].getImgUrl();
        }
        msalvPicture.startPlayLoop(mfiCursor, albumUrlArr, albumUrlArr.length);
    }

    private void initView() {
        mtvBack = (TextView) findViewById(R.id.tv_back);
        mtvNameEn = (TextView) findViewById(R.id.tv_name_en);
        mtvNameCh = (TextView) findViewById(R.id.tv_name_ch);
        mtvPrice = (TextView) findViewById(R.id.tv_price);
        mtvCartIconHint = (TextView) findViewById(R.id.tv_cart_icon_hint);
        mivCartIcon = (ImageView) findViewById(R.id.iv_cart_icon);
        mivCollectIcon = (ImageView) findViewById(R.id.iv_collect_icon);
        mivShareIcon = (ImageView) findViewById(R.id.iv_share_icon);
        mLayoutCartIcon = (RelativeLayout) findViewById(R.id.layout_cart_icon);
        msalvPicture = (SlideAutoLoopView) findViewById(R.id.salv_picture);
        mfiCursor = (FlowIndicator) findViewById(R.id.fi_cursor);
        mLayoutColor = (LinearLayout) findViewById(R.id.layout_Color);
        mwvDetail = (WebView) findViewById(R.id.wv_detail);
        WebSettings settings = mwvDetail.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setBuiltInZoomControls(true);
    }

    public void setCollectIcon() {
        if (isCollect) {
            mivCollectIcon.setImageResource(R.drawable.bg_collect_out);
        } else {
            mivCollectIcon.setImageResource(R.drawable.bg_collect_in);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(cartCountReceiver);
    }

    private class CartCountReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (FuliCenterApplication.getInstance().getCartList() != null) {
                int cartCount = 0;
                ArrayList<CartBean> list = FuliCenterApplication.getInstance().getCartList();
                for (CartBean cart : list) {
                    if (cart.getGoods() == null) {
                        cartCount++;
                    } else {
                        cartCount += cart.getCount();
                    }
                }
                if (cartCount > 0) {
                    mtvCartIconHint.setText(String.valueOf(cartCount));
                    mtvCartIconHint.setVisibility(View.VISIBLE);
                } else {
                    mtvCartIconHint.setVisibility(View.GONE);
                }
            } else {
                mtvCartIconHint.setVisibility(View.GONE);
            }
        }
    }

    private void setCartCountReceiver() {
        cartCountReceiver = new CartCountReceiver();
        IntentFilter filter = new IntentFilter("update_user_cart");
        registerReceiver(cartCountReceiver, filter);
    }
}
