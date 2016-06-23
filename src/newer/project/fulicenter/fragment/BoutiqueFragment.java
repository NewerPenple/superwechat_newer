package newer.project.fulicenter.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.adapter.BoutiqueAdapter;
import newer.project.fulicenter.bean.BoutiqueBean;
import newer.project.fulicenter.data.OkHttpUtils2;
import newer.project.fulicenter.utils.Utils;

public class BoutiqueFragment extends BaseFragment{
    private static final String TAG = BoutiqueFragment.class.getName();
    private SwipeRefreshLayout msrLayoutBoutique;
    private RecyclerView mrvBoutique;
    private TextView mtvBoutiqueRefresh;
    private ArrayList<BoutiqueBean> BoutiqueList;
    private BoutiqueAdapter adapter;
    private LinearLayoutManager manager;
    private int pageId = 0;
    private AlertDialog dialog;

    public BoutiqueFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_new_goods, container, false);
        msrLayoutBoutique = (SwipeRefreshLayout) layout.findViewById(R.id.srLayout_new_goods);
        mrvBoutique = (RecyclerView) layout.findViewById(R.id.rv_new_goods);
        mtvBoutiqueRefresh = (TextView) layout.findViewById(R.id.tv_new_goods_refresh);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
//        initData();
        setListener();
    }

    private void setListener() {
        msrLayoutBoutique.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageId = 0;
                downloadGoods(I.ACTION_PULL_DOWN);
            }
        });

        mrvBoutique.setOnScrollListener(new RecyclerView.OnScrollListener() {
            private int topPosition, lastPosition;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                lastPosition = manager.findLastVisibleItemPosition();
//                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastPosition >= adapter.getItemCount() - 1 && adapter.isMore()) {
//                    pageId += I.PAGE_SIZE_DEFAULT;
//                    downloadGoods(I.ACTION_PULL_UP);
//                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                topPosition = recyclerView == null || recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop();
                msrLayoutBoutique.setEnabled(topPosition >= 0);
            }
        });
    }

    @Override
    public void initData() {
        downloadGoods(I.ACTION_DOWNLOAD);
    }

    private void downloadGoods(final int action) {
        OkHttpUtils2<BoutiqueBean[]> utils = new OkHttpUtils2<BoutiqueBean[]>();
        utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                .addParam(I.KEY_REQUEST, I.REQUEST_FIND_BOUTIQUES)
                .targetClass(BoutiqueBean[].class)
                .onPreExecute(new Runnable() {
                    @Override
                    public void run() {
                        switch (action) {
                            case I.ACTION_DOWNLOAD:
                                dialog = new AlertDialog.Builder(getActivity())
                                        .setTitle("加载商品信息")
                                        .setMessage("加载中……")
                                        .create();
                                dialog.show();
                                break;
                            case I.ACTION_PULL_UP:
                                adapter.setFooterText("加载中……");
                                break;
                            case I.ACTION_PULL_DOWN:
                                msrLayoutBoutique.setRefreshing(true);
                                mtvBoutiqueRefresh.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                })
                .execute(new OkHttpUtils2.OnCompleteListener<BoutiqueBean[]>() {
                    @Override
                    public void onSuccess(BoutiqueBean[] result) {
                        if (result != null) {
                            ArrayList<BoutiqueBean> list = Utils.array2List(result);
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
                                    msrLayoutBoutique.setRefreshing(false);
                                    mtvBoutiqueRefresh.setVisibility(View.GONE);
                                    break;
                            }
                        } else {
                            dismissDialog();
                            Utils.showToast(getActivity(), getResources().getString(R.string.failed_to_load_data), Toast.LENGTH_SHORT);
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
        msrLayoutBoutique.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        manager = new LinearLayoutManager(getActivity());
        mrvBoutique.setLayoutManager(manager);
        BoutiqueList = new ArrayList<BoutiqueBean>();
        adapter = new BoutiqueAdapter(getActivity(), BoutiqueList);
        mrvBoutique.setAdapter(adapter);
    }
}
