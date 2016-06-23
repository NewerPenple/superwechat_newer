package newer.project.fulicenter.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.bean.CartBean;
import newer.project.fulicenter.bean.MessageBean;
import newer.project.fulicenter.data.OkHttpUtils2;
import newer.project.fulicenter.task.DownloadCartTask;
import newer.project.fulicenter.utils.ImageUtils;
import newer.project.fulicenter.utils.Utils;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartHolder> {
    private static final String TAG = CartAdapter.class.getName();
    private Context context;
    private ArrayList<CartBean> cartList;
    private boolean more;

    public CartAdapter(Context context, ArrayList<CartBean> cartList) {
        this.context = context;
        this.cartList = new ArrayList<CartBean>();
        this.cartList.addAll(cartList);
    }

    public boolean isMore() {
        return more;
    }

    public void setMore(boolean more) {
        this.more = more;
    }

    public void initList(ArrayList<CartBean> list) {
        if (cartList != null) {
            this.cartList.clear();
            this.cartList.addAll(list);
            notifyDataSetChanged();
            context.sendStickyBroadcast(new Intent("update_price").putExtra("cartList", cartList));
        }
    }

    public void addList(ArrayList<CartBean> list) {
        if (cartList != null) {
            this.cartList.addAll(list);
            notifyDataSetChanged();
            context.sendStickyBroadcast(new Intent("update_price").putExtra("cartList", cartList));
        }
    }

    @Override
    public CartHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartHolder(layout);
    }

    @Override
    public void onBindViewHolder(final CartHolder holder, final int position) {
        if (cartList.get(position).getGoods() != null) {
            holder.mtvCartName.setText(cartList.get(position).getGoods().getGoodsName());
            int goodPrice = Integer.parseInt(cartList.get(position).getGoods().getRankPrice().substring(1,cartList.get(position).getGoods().getRankPrice().length()));
            holder.mtvCartPrice.setText("￥" + String.valueOf(goodPrice * cartList.get(position).getCount()));
            ImageUtils.setGoodsPicture(holder.mnivCart, cartList.get(position).getGoods().getGoodsThumb());
            setNumber(holder.mtvCartNum, cartList.get(position).getCount());
            holder.mcbCart.setChecked(cartList.get(position).isChecked());
            holder.mbtnCartAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cartList.get(position).setCount(cartList.get(position).getCount() + 1);
                    setNumber(holder.mtvCartNum, cartList.get(position).getCount());
                    setPrice(holder.mtvCartPrice, cartList.get(position).getGoods().getRankPrice(), cartList.get(position).getCount());
                    updateCart(cartList.get(position), position);
                    context.sendStickyBroadcast(new Intent("update_price").putExtra("cartList", cartList));
                }
            });
            holder.mbtnCartDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cartList.get(position).getCount() > 0) {
                        cartList.get(position).setCount(cartList.get(position).getCount() - 1);
                        setNumber(holder.mtvCartNum, cartList.get(position).getCount());
                        setPrice(holder.mtvCartPrice, cartList.get(position).getGoods().getRankPrice(), cartList.get(position).getCount());
                        updateCart(cartList.get(position), position);
                        context.sendStickyBroadcast(new Intent("update_price").putExtra("cartList", cartList));
                    } else {
                        new AlertDialog.Builder(context)
                                .setTitle("确认删除")
                                .setMessage("确定删除该商品？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        deleteCart(cartList.get(position).getId(), position);
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }).create().show();
                    }
                }
            });
            holder.mcbCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cartList.get(position).setChecked(!cartList.get(position).isChecked());
                    updateCart(cartList.get(position), position);
                    context.sendStickyBroadcast(new Intent("update_price").putExtra("cartList", cartList));
                }
            });
        }
    }

    private void setPrice(TextView tv, String rankPrice, int count) {
        int price = Integer.parseInt(rankPrice.substring(1, rankPrice.length()));
        int totalPrice = price * count;
        tv.setText("￥" + totalPrice);
    }

    private void setNumber(TextView tv, int count) {
        tv.setText("(" + count + ")");
    }

    private void deleteCart(int cartId, final int position) {
        OkHttpUtils2<MessageBean> utils = new OkHttpUtils2<MessageBean>();
        utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                .addParam(I.KEY_REQUEST, I.REQUEST_DELETE_CART)
                .addParam(I.Cart.ID, String.valueOf(cartId))
                .targetClass(MessageBean.class)
                .execute(new OkHttpUtils2.OnCompleteListener<MessageBean>() {
                    @Override
                    public void onSuccess(MessageBean result) {
                        if (result != null && result.isSuccess()) {
                            new DownloadCartTask(context, 1024 * 10).execute();
                            cartList.remove(position);
                            notifyDataSetChanged();
                        } else {
                            Utils.showToast(context, "商品删除失败", Toast.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.i("my", TAG + " " + error);
                    }
                });
    }

    private void updateCart(final CartBean cart, final int position) {
        if (cart != null) {
            OkHttpUtils2<MessageBean> utils = new OkHttpUtils2<MessageBean>();
            utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                    .addParam(I.KEY_REQUEST, I.REQUEST_UPDATE_CART)
                    .addParam(I.Cart.ID, String.valueOf(cart.getId()))
                    .addParam(I.Cart.COUNT, String.valueOf(cart.getCount()))
                    .addParam(I.Cart.IS_CHECKED, String.valueOf(cart.isChecked()))
                    .targetClass(MessageBean.class)
                    .execute(new OkHttpUtils2.OnCompleteListener<MessageBean>() {
                        @Override
                        public void onSuccess(MessageBean result) {
                            if (result != null && result.isSuccess()) {
                                FuliCenterApplication.getInstance().getCartList().get(position).setCount(cart.getCount());
                                FuliCenterApplication.getInstance().getCartList().get(position).setChecked(cart.isChecked());
                                context.sendStickyBroadcast(new Intent("update_user_cart"));
                            } else {
                                Utils.showToast(context, "商品数修改失败", Toast.LENGTH_SHORT);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.i("my", TAG + " " + error);
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return cartList == null ? 0 : cartList.size();
    }

    class CartHolder extends RecyclerView.ViewHolder {
        NetworkImageView mnivCart;
        TextView mtvCartName,mtvCartNum,mtvCartPrice;
        CheckBox mcbCart;
        Button mbtnCartAdd,mbtnCartDelete;

        public CartHolder(View itemView) {
            super(itemView);
            mnivCart = (NetworkImageView) itemView.findViewById(R.id.niv_cart);
            mtvCartName = (TextView) itemView.findViewById(R.id.tv_cart_name);
            mtvCartNum = (TextView) itemView.findViewById(R.id.tv_cart_num);
            mtvCartPrice = (TextView) itemView.findViewById(R.id.tv_cart_price);
            mcbCart = (CheckBox) itemView.findViewById(R.id.cb_cart);
            mbtnCartAdd = (Button) itemView.findViewById(R.id.btn_cart_add);
            mbtnCartDelete = (Button) itemView.findViewById(R.id.btn_cart_delete);
        }
    }
}
