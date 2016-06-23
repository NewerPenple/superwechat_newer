package newer.project.fulicenter.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private int listSize;
    private PriceReceiver priceReceiver;


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
        registerPriceReceiver();
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
                            boolean more = list.size() > 0;
                            adapter.setMore(more);
                            cartList = list;
                            if (more) {
                                downloadGoodsDetail(action);
                            } else if (action != I.ACTION_PULL_UP) {
                                adapter.initList(cartList);
                            }
                        } else {
                            Utils.showToast(getActivity(), getResources().getString(R.string.failed_to_load_data), Toast.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onError(String error) {
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

    private void setPrice() {
        int totalPrice = 0;
        int totalRankPrice = 0;
        if (cartList == null) {
            return;
        }
        for (CartBean cart : cartList) {
            if (cart.getGoods() == null || !cart.isChecked()) {
                continue;
            }
            int price = Integer.parseInt(cart.getGoods().getCurrencyPrice().substring(1, cart.getGoods().getCurrencyPrice().length()));
            int rankPrice = Integer.parseInt(cart.getGoods().getRankPrice().substring(1, cart.getGoods().getRankPrice().length()));
            totalPrice += price * cart.getCount();
            totalRankPrice += rankPrice * cart.getCount();
        }
        mtvCartPriceTotal.setText("合计：¥" + totalRankPrice);
        mtvCartPriceSave.setText("节省：¥" + (totalPrice - totalRankPrice));
    }

    private class GetGoodsDetailListener implements OkHttpUtils3.OnCompleteListener<GoodDetailsBean> {
        private final int num;
        private int action;

        public GetGoodsDetailListener(int num, int action) {
            this.num = num;
            this.action = action;
        }

        @Override
        public void onSuccess(GoodDetailsBean result) {
            listSize++;
            if (result != null) {
                cartList.get(num).setGoods(result);
            }
            if (listSize == cartList.size()) {
                switch (action) {
                    case I.ACTION_DOWNLOAD:
                        if (adapter.isMore()) {
                            adapter.initList(cartList);
                        }
                        break;
                    case I.ACTION_PULL_UP:
                        if (adapter.isMore()) {
                            adapter.addList(cartList);
                        }
                        break;
                    case I.ACTION_PULL_DOWN:
                        if (adapter.isMore()) {
                            adapter.initList(cartList);
                        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(priceReceiver);
    }

    private class PriceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("update_price")) {
                cartList.clear();
                cartList.addAll((ArrayList<CartBean>) intent.getSerializableExtra("cartList"));
                Log.i("my", TAG + " size = " + cartList.size());
                setPrice();
            }else if (intent.getAction().equals("update_user_cart")) {
//                initData();
            }
        }
    }

    private void registerPriceReceiver() {
        priceReceiver = new PriceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("update_price");
        filter.addAction("update_user_cart");
        getActivity().registerReceiver(priceReceiver, filter);
    }
}
