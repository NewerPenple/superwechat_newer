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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.adapter.CartAdapter;
import newer.project.fulicenter.bean.CartBean;
import newer.project.fulicenter.bean.GoodDetailsBean;
import newer.project.fulicenter.data.OkHttpUtils3;
import newer.project.fulicenter.utils.Utils;

public class CartFragment extends BaseFragment {
    private static final String TAG = CartFragment.class.getName();
    TextView mtvCartPriceTotal,mtvCartPriceSave;
    Button mbtnCartBuy;
    SwipeRefreshLayout msrLayoutCart;
    TextView mtvCartRefresh;
    RecyclerView mrvCart;
    private ArrayList<CartBean> cartList;
    private CartAdapter adapter;
    private LinearLayoutManager manager;
    private int pageId = 0;
    private AlertDialog dialog;
    private int listSize;


    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_cart, container, false);
        mtvCartPriceTotal = (TextView) layout.findViewById(R.id.tv_cart_price_total);
        mtvCartPriceSave = (TextView) layout.findViewById(R.id.tv_cart_price_save);
        mbtnCartBuy = (Button) layout.findViewById(R.id.btn_cart_buy);
        msrLayoutCart = (SwipeRefreshLayout) layout.findViewById(R.id.srLayout_cart);
        mtvCartRefresh = (TextView) layout.findViewById(R.id.tv_cart_refresh);
        mrvCart = (RecyclerView) layout.findViewById(R.id.rv_cart);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        setListener();
    }

    private void setListener() {
        msrLayoutCart.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageId = 0;
                downloadGoods(I.ACTION_PULL_DOWN);
            }
        });

        mrvCart.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
                msrLayoutCart.setEnabled(topPosition >= 0);
            }
        });
    }

    @Override
    public void initData() {
        downloadGoods(I.ACTION_DOWNLOAD);
    }

    private void downloadGoods(final int action) {
        OkHttpUtils3<CartBean[]> utils = new OkHttpUtils3<CartBean[]>();
        utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                .addParam(I.KEY_REQUEST, I.REQUEST_FIND_CARTS)
                .addParam(I.Cart.USER_NAME, FuliCenterApplication.getInstance().getUser().getMUserName())
                .addParam(I.PAGE_ID, String.valueOf(pageId))
                .addParam(I.PAGE_SIZE, String.valueOf(I.PAGE_SIZE_DEFAULT))
                .targetClass(CartBean[].class)
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
                                break;
                            case I.ACTION_PULL_DOWN:
                                msrLayoutCart.setRefreshing(true);
                                mtvCartRefresh.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                })
                .execute(new OkHttpUtils3.OnCompleteListener<CartBean[]>() {
                    @Override
                    public void onSuccess(CartBean[] result) {
                        if (result != null) {
                            ArrayList<CartBean> list = Utils.array2List(result);
                            boolean more = list != null && list.size() > 0;
                            adapter.setMore(more);
                            cartList = list;
                            downloadGoodsDetail(action);
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

    private void downloadGoodsDetail(final int action) {
        listSize = 0;
        for (int i = 0; i < cartList.size(); i++) {
            OkHttpUtils3<GoodDetailsBean> utils = new OkHttpUtils3<GoodDetailsBean>();
            utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                    .addParam(I.KEY_REQUEST, I.REQUEST_FIND_GOOD_DETAILS)
                    .addParam(I.CategoryGood.GOODS_ID, String.valueOf(cartList.get(i).getGoodsId()))
                    .targetClass(GoodDetailsBean.class)
                    .execute(new GetGoodsDetailListener(i, action));
        }
    }

    private void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void initView() {
        msrLayoutCart.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        manager = new LinearLayoutManager(getActivity());
        mrvCart.setLayoutManager(manager);
        cartList = new ArrayList<CartBean>();
        adapter = new CartAdapter(getActivity(), cartList);
        mrvCart.setAdapter(adapter);
    }

    private class GetGoodsDetailListener implements OkHttpUtils3.OnCompleteListener<GoodDetailsBean> {
        private final int num;
        private int action;

        public GetGoodsDetailListener(int num, int action) {
            Log.i("my", TAG + " num = " + num);
            this.num = num;
            this.action = action;
        }

        @Override
        public void onSuccess(GoodDetailsBean result) {
            listSize++;
            if (result != null) {
                Log.i("my", TAG + " final num = " + num);
                cartList.get(num).setGoods(result);
            }
            if (listSize == cartList.size()) {
                switch (action) {
                    case I.ACTION_DOWNLOAD:
                        adapter.initList(cartList);
                        dismissDialog();
                        break;
                    case I.ACTION_PULL_UP:
                        adapter.addList(cartList);
                        break;
                    case I.ACTION_PULL_DOWN:
                        adapter.initList(cartList);
                        msrLayoutCart.setRefreshing(false);
                        mtvCartRefresh.setVisibility(View.GONE);
                        break;
                }
            }
        }

        @Override
        public void onError(String error) {
            listSize++;
            if (listSize == cartList.size()) {
                switch (action) {
                    case I.ACTION_DOWNLOAD:
                        adapter.initList(cartList);
                        dismissDialog();
                        break;
                    case I.ACTION_PULL_UP:
                        adapter.addList(cartList);
                        break;
                    case I.ACTION_PULL_DOWN:
                        adapter.initList(cartList);
                        msrLayoutCart.setRefreshing(false);
                        mtvCartRefresh.setVisibility(View.GONE);
                        break;
                }
            }
        }
    }
}
