package newer.project.fulicenter.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import newer.project.fulicenter.R;

public class FooterHolder extends RecyclerView.ViewHolder{
    TextView mtvFooter;

    public FooterHolder(View itemView) {
        super(itemView);
        mtvFooter = (TextView) itemView.findViewById(R.id.tv_footer);
    }
}
