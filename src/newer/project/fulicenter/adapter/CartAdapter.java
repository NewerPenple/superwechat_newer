package newer.project.fulicenter.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import newer.project.fulicenter.R;
import newer.project.fulicenter.bean.CartBean;
import newer.project.fulicenter.utils.ImageUtils;

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
        }
    }

    public void addList(ArrayList<CartBean> list) {
        if (cartList != null) {
            this.cartList.addAll(list);
            notifyDataSetChanged();
        }
    }

    @Override
    public CartHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartHolder(layout);
    }

    @Override
    public void onBindViewHolder(final CartHolder holder, final int position) {
        holder.mtvCartName.setText(cartList.get(position).getGoods().getGoodsName());
        holder.mtvCartPrice.setText(cartList.get(position).getGoods().getCurrencyPrice());
        ImageUtils.setGoodsPicture(holder.mnivCart, cartList.get(position).getGoods().getGoodsThumb());
        setNumber(holder.mtvCartNum, cartList.get(position).getCount());
        holder.mcbCart.setChecked(cartList.get(position).isChecked());
        holder.mbtnCartAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cartList.get(position).setCount(cartList.get(position).getCount() + 1);
                setNumber((TextView) view, cartList.get(position).getCount());
                holder.mtvCartPrice.setText("￥" + String.valueOf(Integer.parseInt(cartList.get(position).getGoods().getCurrencyPrice()) * cartList.get(position).getCount()));
            }
        });
        holder.mbtnCartDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cartList.get(position).getCount() > 0) {
                    cartList.get(position).setCount(cartList.get(position).getCount() - 1);
                    setNumber((TextView) view, cartList.get(position).getCount());
                    holder.mtvCartPrice.setText("￥" + String.valueOf(Integer.parseInt(cartList.get(position).getGoods().getCurrencyPrice()) * cartList.get(position).getCount()));
                }
            }
        });
    }

    private void setNumber(TextView tv, int count) {
        tv.setText("(" + count + ")");
    }

    @Override
    public int getItemCount() {
        return cartList == null ? 1 : cartList.size() + 1;
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
