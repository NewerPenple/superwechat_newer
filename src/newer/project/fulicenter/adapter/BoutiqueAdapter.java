package newer.project.fulicenter.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import newer.project.fulicenter.R;
import newer.project.fulicenter.activity.BoutiqueDetailActivity;
import newer.project.fulicenter.bean.BoutiqueBean;
import newer.project.fulicenter.utils.ImageUtils;

public class BoutiqueAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<BoutiqueBean> boutiqueList;
    private String footerText;
    private boolean more;
    private static final int TYPE_BOUTIQUE = 0;
    private static final int TYPE_FOOTER = 1;

    public BoutiqueAdapter(Context context, ArrayList<BoutiqueBean> goodsList) {
        this.context = context;
        this.boutiqueList = new ArrayList<BoutiqueBean>();
        this.boutiqueList.addAll(goodsList);
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

    public void initList(ArrayList<BoutiqueBean> list) {
        if (boutiqueList != null) {
            this.boutiqueList.clear();
            this.boutiqueList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void addList(ArrayList<BoutiqueBean> list) {
        if (boutiqueList != null) {
            this.boutiqueList.addAll(list);
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View layout = null;
        switch (viewType) {
            case TYPE_BOUTIQUE:
                layout = LayoutInflater.from(context).inflate(R.layout.item_boutique, parent, false);
                holder = new BoutiqueHolder(layout);
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
            case TYPE_BOUTIQUE:
                BoutiqueHolder boutiqueHolder = (BoutiqueHolder) holder;
                boutiqueHolder.mtvBoutiqueTitle.setText(boutiqueList.get(position).getTitle());
                boutiqueHolder.mtvBoutiqueName.setText(boutiqueList.get(position).getName());
                boutiqueHolder.mtvBoutiqueDesc.setText(boutiqueList.get(position).getDescription());
                ImageUtils.setBoutiquePicture(boutiqueHolder.mnivBoutique, boutiqueList.get(position).getImageurl());
                boutiqueHolder.mLayoutBoutique.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        context.startActivity(new Intent(context, BoutiqueDetailActivity.class)
                                .putExtra("good", boutiqueList.get(position)));
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
        return boutiqueList == null ? 1 : boutiqueList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        return TYPE_BOUTIQUE;
    }

    class BoutiqueHolder extends RecyclerView.ViewHolder {
        NetworkImageView mnivBoutique;
        TextView mtvBoutiqueTitle,mtvBoutiqueName,mtvBoutiqueDesc;
        RelativeLayout mLayoutBoutique;

        public BoutiqueHolder(View itemView) {
            super(itemView);
            mnivBoutique = (NetworkImageView) itemView.findViewById(R.id.niv_boutique);
            mtvBoutiqueTitle = (TextView) itemView.findViewById(R.id.tv_boutique_title);
            mtvBoutiqueName = (TextView) itemView.findViewById(R.id.tv_boutique_name);
            mtvBoutiqueDesc = (TextView) itemView.findViewById(R.id.tv_boutique_desc);
            mLayoutBoutique = (RelativeLayout) itemView.findViewById(R.id.layout_boutique);
        }
    }
}
