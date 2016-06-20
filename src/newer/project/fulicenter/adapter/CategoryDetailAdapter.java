package newer.project.fulicenter.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import newer.project.fulicenter.D;
import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.activity.NewGoodDetailActivity;
import newer.project.fulicenter.bean.GoodDetailsBean;
import newer.project.fulicenter.utils.ImageUtils;

public class CategoryDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<GoodDetailsBean> goodsList;
    private String footerText;
    private boolean more;
    private static final int TYPE_GOODS = 0;
    private static final int TYPE_FOOTER = 1;
    private int sortBy;

    public CategoryDetailAdapter(Context context, ArrayList<GoodDetailsBean> goodsList,int sortBy) {
        this.context = context;
        this.goodsList = new ArrayList<GoodDetailsBean>();
        this.goodsList.addAll(goodsList);
        this.sortBy = sortBy;
    }

    public void setFooterText(String footerText) {
        this.footerText = footerText;
        notifyDataSetChanged();
    }

    public boolean isMore() {
        return more;
    }

    public void setMore(boolean more) {
        this.more = more;
    }

    public void setSortBy(int sortBy) {
        this.sortBy = sortBy;
        sort(sortBy);
        notifyDataSetChanged();
    }

    public void initList(ArrayList<GoodDetailsBean> list) {
        if (goodsList != null) {
            this.goodsList.clear();
            this.goodsList.addAll(list);
            sort(sortBy);
            notifyDataSetChanged();
        }
    }

    public void addList(ArrayList<GoodDetailsBean> list) {
        if (goodsList != null) {
            this.goodsList.addAll(list);
            sort(sortBy);
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View layout = null;
        switch (viewType) {
            case TYPE_GOODS:
                layout = LayoutInflater.from(context).inflate(R.layout.item_category_detail, parent, false);
                holder = new CategoryDetailHolder(layout);
                break;
            case TYPE_FOOTER:
                layout = LayoutInflater.from(context).inflate(R.layout.item_footer, parent, false);
                holder = new FooterHolder(layout);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case TYPE_GOODS:
                CategoryDetailHolder boutiqueDetailHolder = (CategoryDetailHolder) holder;
                boutiqueDetailHolder.mtvCategoryDetailName.setText(goodsList.get(position).getGoodsName());
                boutiqueDetailHolder.mtvCategoryDetailPrice.setText(goodsList.get(position).getCurrencyPrice());
                ImageUtils.setGoodsPicture(boutiqueDetailHolder.mnivCategoryDetail, goodsList.get(position).getGoodsThumb());
                boutiqueDetailHolder.mLayoutCategoryDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, NewGoodDetailActivity.class);
                        intent.putExtra(D.NewGood.KEY_GOODS_ID, goodsList.get(position).getGoodsId());
                        context.startActivity(intent);
                    }
                });
                break;
            case TYPE_FOOTER:
                FooterHolder footerHolder = (FooterHolder) holder;
                footerHolder.mtvFooter.setText(footerText);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return goodsList == null ? 1 : goodsList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        return TYPE_GOODS;
    }

    private void sort(final int sortBy) {
        Collections.sort(goodsList, new Comparator<GoodDetailsBean>() {
            @Override
            public int compare(GoodDetailsBean g1, GoodDetailsBean g2) {
                int result = 0;
                switch (sortBy) {
                    case I.SORT_BY_ADDTIME_ASC:
                        result = (int) (g1.getAddTime() - g2.getAddTime());
                        break;
                    case I.SORT_BY_ADDTIME_DESC:
                        result = (int) (g2.getAddTime() - g1.getAddTime());
                        break;
                    case I.SORT_BY_PRICE_ASC: {
                        int p1 = converPrice(g1.getCurrencyPrice());
                        int p2 = converPrice(g2.getCurrencyPrice());
                        result = p1 - p2;
                    }
                    break;
                    case I.SORT_BY_PRICE_DESC: {
                        int p1 = converPrice(g1.getCurrencyPrice());
                        int p2 = converPrice(g2.getCurrencyPrice());
                        result = p2 - p1;
                    }
                    break;
                }
                return result;
            }

            private int converPrice(String price) {
                price = price.substring(price.indexOf("￥") + 1);
                int p1 = Integer.parseInt(price);
                return p1;
            }
        });
    }

    class CategoryDetailHolder extends RecyclerView.ViewHolder {
        NetworkImageView mnivCategoryDetail;
        TextView mtvCategoryDetailName,mtvCategoryDetailPrice;
        LinearLayout mLayoutCategoryDetail;

        public CategoryDetailHolder(View itemView) {
            super(itemView);
            mnivCategoryDetail = (NetworkImageView) itemView.findViewById(R.id.niv_category_detail);
            mtvCategoryDetailName = (TextView) itemView.findViewById(R.id.tv_category_detail_name);
            mtvCategoryDetailPrice = (TextView) itemView.findViewById(R.id.tv_category_detail_price);
            mLayoutCategoryDetail = (LinearLayout) itemView.findViewById(R.id.layout_category_detail);
        }
    }
}
