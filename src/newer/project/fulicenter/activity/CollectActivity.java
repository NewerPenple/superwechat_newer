package newer.project.fulicenter.activity;

import android.app.Activity;
import android.app.AlertDialog;
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
import newer.project.fulicenter.adapter.CollectAdapter;
import newer.project.fulicenter.bean.CollectBean;
import newer.project.fulicenter.data.OkHttpUtils2;
import newer.project.fulicenter.utils.DisplayUtils;
import newer.project.fulicenter.utils.Utils;

public class CollectActivity extends Activity {
    private static final String TAG = CollectActivity.class.getName();
    private TextView mtvBack;
    private SwipeRefreshLayout msrLayoutCollect;
    private RecyclerView mrvCollect;
    private TextView mtvCollectRefresh;
    private ArrayList<CollectBean> collectList;
    private CollectAdapter adapter;
    private GridLayoutManager manager;
    private int pageId = 0;
    private AlertDialog dialog;

    public CollectActivity() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        msrLayoutCollect.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageId = 0;
                downloadGoods(I.ACTION_PULL_DOWN);
            }
        });

        mrvCollect.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
                msrLayoutCollect.setEnabled(topPosition >= 0);
            }
        });
    }

    public void initData() {
        DisplayUtils.initBack(this);
        downloadGoods(I.ACTION_DOWNLOAD);
    }

    private void downloadGoods(final int action) {
        OkHttpUtils2<CollectBean[]> utils = new OkHttpUtils2<CollectBean[]>();
        utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                .addParam(I.KEY_REQUEST,I.REQUEST_FIND_COLLECTS)
                .addParam(I.Collect.USER_NAME, FuliCenterApplication.getInstance().getUser().getMUserName())
                .addParam(I.PAGE_ID, String.valueOf(pageId))
                .addParam(I.PAGE_SIZE, String.valueOf(I.PAGE_SIZE_DEFAULT))
                .targetClass(CollectBean[].class)
                .onPreExecute(new Runnable() {
                    @Override
                    public void run() {
                        switch (action) {
                            case I.ACTION_DOWNLOAD:
                                dialog = new AlertDialog.Builder(CollectActivity.this)
                                        .setTitle("加载商品信息")
                                        .setMessage("加载中……")
                                        .create();
                                dialog.show();
                                break;
                            case I.ACTION_PULL_UP:
                                adapter.setFooterText("加载中……");
                                break;
                            case I.ACTION_PULL_DOWN:
                                msrLayoutCollect.setRefreshing(true);
                                mtvCollectRefresh.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                })
                .execute(new OkHttpUtils2.OnCompleteListener<CollectBean[]>() {
                    @Override
                    public void onSuccess(CollectBean[] result) {
                        if (result != null) {
                            ArrayList<CollectBean> list = Utils.array2List(result);
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
                                    msrLayoutCollect.setRefreshing(false);
                                    mtvCollectRefresh.setVisibility(View.GONE);
                                    break;
                            }
                        } else {
                            dismissDialog();
                            Utils.showToast(CollectActivity.this, getResources().getString(R.string.failed_to_load_data), Toast.LENGTH_SHORT);
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
        mtvBack = (TextView) findViewById(R.id.tv_back);
        msrLayoutCollect = (SwipeRefreshLayout) findViewById(R.id.srLayout_collect);
        mtvCollectRefresh = (TextView) findViewById(R.id.tv_collect_refresh);
        mrvCollect = (RecyclerView) findViewById(R.id.rv_collect);
        msrLayoutCollect.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        manager = new GridLayoutManager(this, 2);
        mrvCollect.setLayoutManager(manager);
        collectList = new ArrayList<CollectBean>();
        adapter = new CollectAdapter(this, collectList);
        mrvCollect.setAdapter(adapter);
    }
}
