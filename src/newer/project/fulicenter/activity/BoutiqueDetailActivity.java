package newer.project.fulicenter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.adapter.BoutiqueDetailAdapter;
import newer.project.fulicenter.bean.BoutiqueBean;
import newer.project.fulicenter.bean.NewGoodBean;
import newer.project.fulicenter.data.OkHttpUtils2;
import newer.project.fulicenter.utils.DisplayUtils;
import newer.project.fulicenter.utils.Utils;

public class BoutiqueDetailActivity extends BaseActivity {
    private static final String TAG = BoutiqueDetailActivity.class.getName();
    private SwipeRefreshLayout msrLayoutBoutiqueDetail;
    private RecyclerView mrvBoutiqueDetail;
    private TextView mtvBoutiqueDetailRefresh;
    private ArrayList<NewGoodBean> goodsList;
    private BoutiqueDetailAdapter adapter;
    private GridLayoutManager manager;
    private int pageId = 0;
    private android.app.AlertDialog dialog;
    private BoutiqueBean good;

    public BoutiqueDetailActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boutique_detail);
        good = (BoutiqueBean) getIntent().getSerializableExtra("good");
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        msrLayoutBoutiqueDetail.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageId = 0;
                downloadGoods(I.ACTION_PULL_DOWN);
            }
        });

        mrvBoutiqueDetail.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
                msrLayoutBoutiqueDetail.setEnabled(topPosition >= 0);
            }
        });
    }

    public void initData() {
        DisplayUtils.initBackWithTitle(this, good.getName());
        downloadGoods(I.ACTION_DOWNLOAD);
    }

    private void downloadGoods(final int action) {
        OkHttpUtils2<NewGoodBean[]> utils = new OkHttpUtils2<NewGoodBean[]>();
        utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                .addParam(I.KEY_REQUEST,I.REQUEST_FIND_NEW_BOUTIQUE_GOODS)
                .addParam(I.NewAndBoutiqueGood.CAT_ID, String.valueOf(good.getId()))
                .addParam(I.PAGE_ID, String.valueOf(pageId))
                .addParam(I.PAGE_SIZE, String.valueOf(I.PAGE_SIZE_DEFAULT))
                .targetClass(NewGoodBean[].class)
                .onPreExecute(new Runnable() {
                    @Override
                    public void run() {
                        switch (action) {
                            case I.ACTION_DOWNLOAD:
                                dialog = new android.app.AlertDialog.Builder(BoutiqueDetailActivity.this)
                                        .setTitle("加载商品信息")
                                        .setMessage("加载中……")
                                        .create();
                                dialog.show();
                                break;
                            case I.ACTION_PULL_UP:
                                adapter.setFooterText("加载中……");
                                break;
                            case I.ACTION_PULL_DOWN:
                                msrLayoutBoutiqueDetail.setRefreshing(true);
                                mtvBoutiqueDetailRefresh.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                })
                .execute(new OkHttpUtils2.OnCompleteListener<NewGoodBean[]>() {
                    @Override
                    public void onSuccess(NewGoodBean[] result) {
                        if (result != null) {
                            ArrayList<NewGoodBean> list = Utils.array2List(result);
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
                                    msrLayoutBoutiqueDetail.setRefreshing(false);
                                    mtvBoutiqueDetailRefresh.setVisibility(View.GONE);
                                    break;
                            }
                        } else {
                            dismissDialog();
                            Utils.showToast(BoutiqueDetailActivity.this, getResources().getString(R.string.failed_to_load_data), Toast.LENGTH_SHORT);
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
        msrLayoutBoutiqueDetail = (SwipeRefreshLayout) findViewById(R.id.srLayout_boutique_detail);
        mtvBoutiqueDetailRefresh = (TextView) findViewById(R.id.tv_boutique_detail_refresh);
        mrvBoutiqueDetail = (RecyclerView) findViewById(R.id.rv_boutique_detail);
        msrLayoutBoutiqueDetail.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        manager = new GridLayoutManager(this, 2);
        mrvBoutiqueDetail.setLayoutManager(manager);
        goodsList = new ArrayList<NewGoodBean>();
        adapter = new BoutiqueDetailAdapter(this, goodsList, I.SORT_BY_PRICE_DESC);
        mrvBoutiqueDetail.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == LoginActivity.RESULT_CODE_TO_CART) {
            setResult(resultCode);
            finish();
        }
    }
}
