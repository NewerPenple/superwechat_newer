package newer.project.fulicenter.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import newer.project.fulicenter.D;
import newer.project.fulicenter.R;
import newer.project.fulicenter.activity.NewGoodDetailActivity;
import newer.project.fulicenter.bean.CollectBean;
import newer.project.fulicenter.utils.ImageUtils;

public class CollectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<CollectBean> collectList;
    private String footerText;
    private boolean more;
    private static final int TYPE_COLLECT = 0;
    private static final int TYPE_FOOTER = 1;

    public CollectAdapter(Context context, ArrayList<CollectBean> collectList) {
        this.context = context;
        this.collectList = new ArrayList<CollectBean>();
        this.collectList.addAll(collectList);
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

    public void initList(ArrayList<CollectBean> list) {
        if (collectList != null) {
            this.collectList.clear();
            this.collectList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void addList(ArrayList<CollectBean> list) {
        if (collectList != null) {
            this.collectList.addAll(list);
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View layout = null;
        switch (viewType) {
            case TYPE_COLLECT:
                layout = LayoutInflater.from(context).inflate(R.layout.item_collect, parent, false);
                holder = new CollectHolder(layout);
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
            case TYPE_COLLECT:
                CollectHolder collectHolder = (CollectHolder) holder;
                collectHolder.mtvCollectName.setText(collectList.get(position).getGoodsName());
                ImageUtils.setGoodsPicture(collectHolder.mnivCollect, collectList.get(position).getGoodsThumb());
                collectHolder.mLayoutCollect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, NewGoodDetailActivity.class);
                        intent.putExtra(D.NewGood.KEY_GOODS_ID, collectList.get(position).getGoodsId());
                        context.startActivity(intent);
                    }
                });
                collectHolder.mivCollectDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

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
        return collectList == null ? 1 : collectList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        return TYPE_COLLECT;
    }

    class CollectHolder extends RecyclerView.ViewHolder {
        NetworkImageView mnivCollect;
        TextView mtvCollectName;
        RelativeLayout mLayoutCollect;
        ImageView mivCollectDelete;

        public CollectHolder(View itemView) {
            super(itemView);
            mnivCollect = (NetworkImageView) itemView.findViewById(R.id.niv_collect);
            mtvCollectName = (TextView) itemView.findViewById(R.id.tv_collect_name);
            mLayoutCollect = (RelativeLayout) itemView.findViewById(R.id.layout_collect);
            mivCollectDelete = (ImageView) itemView.findViewById(R.id.iv_collect_delete);
        }
    }
}
