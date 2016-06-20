package newer.project.fulicenter.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.adapter.CategoryDetailAdapter;
import newer.project.fulicenter.bean.CategoryChildBean;
import newer.project.fulicenter.bean.GoodDetailsBean;
import newer.project.fulicenter.data.OkHttpUtils2;
import newer.project.fulicenter.utils.DisplayUtils;
import newer.project.fulicenter.utils.ImageUtils;
import newer.project.fulicenter.utils.Utils;
import newer.project.fulicenter.widget.CatChildFilterButton;
import newer.project.fulicenter.widget.ColorFilterButton;

public class CategoryDetailActivity extends BaseActivity {
    private static final String TAG = BoutiqueDetailActivity.class.getName();
    private CatChildFilterButton mbtnCat;
    private ColorFilterButton mbtnScreen;
    private Button mbtnByPrice;
    private Button mbtnByTime;
    private SwipeRefreshLayout msrLayoutCategoryDetail;
    private RecyclerView mrvCategoryDetail;
    private TextView mtvCategoryDetailRefresh;
    private ArrayList<GoodDetailsBean> goodsList;
    private CategoryDetailAdapter adapter;
    private GridLayoutManager manager;
    private int pageId = 0;
    private android.app.AlertDialog dialog;
    private CategoryChildBean good;
    private String groupName;
    private ArrayList<CategoryChildBean> childList;
    SortStateChangedListener sortStateChangedListener;
    private int sortBy;
    private boolean byPriceAsc;
    private boolean byTimeAsc;

    public CategoryDetailActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_detail);
        good = (CategoryChildBean) getIntent().getSerializableExtra("goodDetail");
        groupName = getIntent().getStringExtra("groupName");
        childList = (ArrayList<CategoryChildBean>) getIntent().getSerializableExtra("childList");
        Log.i("my", TAG + String.valueOf(childList.size()));
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        msrLayoutCategoryDetail.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageId = 0;
                downloadGoods(I.ACTION_PULL_DOWN);
            }
        });

        mrvCategoryDetail.setOnScrollListener(new RecyclerView.OnScrollListener() {
            private int topPosition, lastPosition;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                lastPosition = manager.findLastVisibleItemPosition();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastPosition >= adapter.getItemCount() - 1 && adapter.isMore()) {
                    pageId += I.PAGE_SIZE_DEFAULT;
                    downloadGoods(I.ACTION_PULL_UP);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                topPosition = recyclerView == null || recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop();
                msrLayoutCategoryDetail.setEnabled(topPosition >= 0);
            }
        });

        mbtnCat.setOnCatFilterClickListener(groupName, childList);
        mbtnByPrice.setOnClickListener(sortStateChangedListener);
        mbtnByTime.setOnClickListener(sortStateChangedListener);
    }

    public void initData() {
        DisplayUtils.initBack(this);
        downloadGoods(I.ACTION_DOWNLOAD);
    }

    private void downloadGoods(final int action) {
        Log.i("my", TAG + " " + good.getId());
        OkHttpUtils2<GoodDetailsBean[]> utils = new OkHttpUtils2<GoodDetailsBean[]>();
        utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                .addParam(I.KEY_REQUEST, I.REQUEST_FIND_NEW_BOUTIQUE_GOODS)
                .addParam(I.CategoryGood.CAT_ID, String.valueOf(good.getId()))
                .addParam(I.PAGE_ID, String.valueOf(I.PAGE_ID_DEFAULT))
                .addParam(I.PAGE_SIZE, String.valueOf(I.PAGE_SIZE_DEFAULT))
                .targetClass(GoodDetailsBean[].class)
                .onPreExecute(new Runnable() {
                    @Override
                    public void run() {
                        switch (action) {
                            case I.ACTION_DOWNLOAD:
                                dialog = new android.app.AlertDialog.Builder(CategoryDetailActivity.this)
                                        .setTitle("加载商品信息")
                                        .setMessage("加载中……")
                                        .create();
                                dialog.show();
                                break;
                            case I.ACTION_PULL_UP:
                                adapter.setFooterText("加载中……");
                                break;
                            case I.ACTION_PULL_DOWN:
                                msrLayoutCategoryDetail.setRefreshing(true);
                                mtvCategoryDetailRefresh.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                })
                .execute(new OkHttpUtils2.OnCompleteListener<GoodDetailsBean[]>() {
                    @Override
                    public void onSuccess(GoodDetailsBean[] result) {
                        if (result != null) {
                            ArrayList<GoodDetailsBean> list = Utils.array2List(result);
                            boolean more = list != null && list.size() > 0;
                            adapter.setMore(more);
                            switch (action) {
                                case I.ACTION_DOWNLOAD:
                                    if (more) {
                                        adapter.initList(list);
                                    }
                                    dismissDialog();
                                    break;
                                case I.ACTION_PULL_UP:
                                    if (more) {
                                        adapter.addList(list);
                                        adapter.setFooterText(getResources().getString(R.string.load_more));
                                    } else {
                                        adapter.setFooterText(getResources().getString(R.string.no_more));
                                    }
                                    break;
                                case I.ACTION_PULL_DOWN:
                                    if (more) {
                                        adapter.initList(list);
                                    }
                                    msrLayoutCategoryDetail.setRefreshing(false);
                                    mtvCategoryDetailRefresh.setVisibility(View.GONE);
                                    break;
                            }
                        } else {
                            dismissDialog();
                            Utils.showToast(CategoryDetailActivity.this, getResources().getString(R.string.failed_to_load_data), Toast.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        dismissDialog();
                        Log.i("my", TAG + " " + error);
                    }
                });
    }

    private void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void initView() {
        mbtnCat = (CatChildFilterButton) findViewById(R.id.btn_category_detail_cat);
        mbtnScreen = (ColorFilterButton) findViewById(R.id.btn_category_detail_screen);
        mbtnByPrice = (Button) findViewById(R.id.btn_category_detail_by_price);
        mbtnByTime = (Button) findViewById(R.id.btn_category_detail_by_time);
        msrLayoutCategoryDetail = (SwipeRefreshLayout) findViewById(R.id.srLayout_category_detail);
        mtvCategoryDetailRefresh = (TextView) findViewById(R.id.tv_category_detail_refresh);
        mrvCategoryDetail = (RecyclerView) findViewById(R.id.rv_category_detail);
        msrLayoutCategoryDetail.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        manager = new GridLayoutManager(this, 2);
        mrvCategoryDetail.setLayoutManager(manager);
        goodsList = new ArrayList<GoodDetailsBean>();
        adapter = new CategoryDetailAdapter(this, goodsList, I.SORT_BY_PRICE_DESC);
        mrvCategoryDetail.setAdapter(adapter);
        sortStateChangedListener = new SortStateChangedListener();
        sortBy = I.SORT_BY_PRICE_DESC;
    }

    private class SortStateChangedListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Drawable right = null;
            int resId;
            switch (view.getId()) {
                case R.id.btn_category_detail_by_price:
                    if (byPriceAsc) {
                        sortBy = I.SORT_BY_PRICE_ASC;
                        right = CategoryDetailActivity.this.getResources().getDrawable(R.drawable.arrow_order_up);
                        resId = R.drawable.arrow_order_up;
                    } else {
                        sortBy = I.SORT_BY_PRICE_DESC;
                        right = CategoryDetailActivity.this.getResources().getDrawable(R.drawable.arrow_order_down);
                        resId = R.drawable.arrow_order_down;
                    }
                    byPriceAsc = !byPriceAsc;
                    right.setBounds(0, 0, ImageUtils.getDrawableWidth(CategoryDetailActivity.this, resId), ImageUtils.getDrawableHeight(CategoryDetailActivity.this, resId));
                    mbtnByPrice.setCompoundDrawables(null, null, right, null);
                    break;
                case R.id.btn_category_detail_by_time:
                    if (byTimeAsc) {
                        sortBy = I.SORT_BY_ADDTIME_ASC;
                        right = CategoryDetailActivity.this.getResources().getDrawable(R.drawable.arrow_order_up);
                        resId = R.drawable.arrow_order_up;
                    } else {
                        sortBy = I.SORT_BY_ADDTIME_DESC;
                        right = CategoryDetailActivity.this.getResources().getDrawable(R.drawable.arrow_order_down);
                        resId = R.drawable.arrow_order_down;
                    }
                    byTimeAsc = !byTimeAsc;
                    right.setBounds(0, 0, ImageUtils.getDrawableWidth(CategoryDetailActivity.this, resId), ImageUtils.getDrawableHeight(CategoryDetailActivity.this, resId));
                    mbtnByTime.setCompoundDrawables(null, null, right, null);
                    break;
            }
            adapter.setSortBy(sortBy);
        }
    }
}
