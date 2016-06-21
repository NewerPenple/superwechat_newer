package newer.project.fulicenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import newer.project.fulicenter.D;
import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.bean.AlbumBean;
import newer.project.fulicenter.bean.GoodDetailsBean;
import newer.project.fulicenter.bean.MessageBean;
import newer.project.fulicenter.bean.User;
import newer.project.fulicenter.data.OkHttpUtils2;
import newer.project.fulicenter.utils.DisplayUtils;
import newer.project.fulicenter.utils.ImageUtils;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_good_detail);
        initView();
        initData();
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
        User user = FuliCenterApplication.getInstance().getUser();
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
                            setCollectIcon(true);
                        } else {
                            setCollectIcon(false);
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

    public void setCollectIcon(boolean isCollect) {
        if (isCollect) {
            mivCollectIcon.setImageResource(R.drawable.bg_collect_out);
        } else {
            mivCollectIcon.setImageResource(R.drawable.bg_collect_in);
        }
    }
}
