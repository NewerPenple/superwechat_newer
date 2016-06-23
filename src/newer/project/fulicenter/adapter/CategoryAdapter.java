package newer.project.fulicenter.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.activity.CategoryDetailActivity;
import newer.project.fulicenter.bean.CategoryChildBean;
import newer.project.fulicenter.bean.CategoryGroupBean;
import newer.project.fulicenter.utils.ImageUtils;

public class CategoryAdapter extends BaseExpandableListAdapter{
    private Context context;
    private ArrayList<CategoryGroupBean> groupList;
    private ArrayList<ArrayList<CategoryChildBean>> childList;

    public CategoryAdapter(Context context, ArrayList<CategoryGroupBean> groupList, ArrayList<ArrayList<CategoryChildBean>> childList) {
        this.context = context;
        this.groupList = new ArrayList<CategoryGroupBean>();
        this.groupList.addAll(groupList);
        this.childList = new ArrayList<ArrayList<CategoryChildBean>>();
        this.childList.addAll(childList);
    }

    @Override
    public int getGroupCount() {
        return groupList == null ? 0 : groupList.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return childList == null || childList.get(i) == null ? 0 : childList.get(i).size();
    }

    @Override
    public CategoryGroupBean getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public CategoryChildBean getChild(int groupPosition, int childPosition) {
        return childList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewGroupHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_category_group, null);
            holder = new ViewGroupHolder();
            holder.mnivCategoryGroup = (NetworkImageView) convertView.findViewById(R.id.niv_category_group);
            holder.mtvCategoryGroup = (TextView) convertView.findViewById(R.id.tv_category_group);
            holder.mivCategoryGroup = (ImageView) convertView.findViewById(R.id.iv_catetory_group);
            convertView.setTag(holder);
        } else {
            holder = (ViewGroupHolder) convertView.getTag();
        }
        CategoryGroupBean group = getGroup(groupPosition);
        holder.mtvCategoryGroup.setText(group.getName());
        String url = I.DOWNLOAD_DOWNLOAD_CATEGORY_GROUP_IMAGE_URL + group.getImageUrl();
        ImageUtils.setThumb(holder.mnivCategoryGroup, url);
        if (isExpanded) {
            holder.mivCategoryGroup.setImageResource(R.drawable.expand_off);
        } else {
            holder.mivCategoryGroup.setImageResource(R.drawable.expand_on);
        }
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewChildHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_catetory_child, null);
            holder = new ViewChildHolder();
            holder.mnivCategoryChild = (NetworkImageView) convertView.findViewById(R.id.niv_category_child);
            holder.mtvCategoryChild = (TextView) convertView.findViewById(R.id.tv_category_child);
            holder.mLayoutCategoryChild = (RelativeLayout) convertView.findViewById(R.id.layout_category_child);
            convertView.setTag(holder);
        } else {
            holder = (ViewChildHolder) convertView.getTag();
        }
        final CategoryChildBean child = getChild(groupPosition, childPosition);
        holder.mtvCategoryChild.setText(child.getName());
        String url = I.DOWNLOAD_DOWNLOAD_CATEGORY_CHILD_IMAGE_URL + child.getImageUrl();
        ImageUtils.setThumb(holder.mnivCategoryChild, url);
        holder.mLayoutCategoryChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity) context).startActivityForResult(new Intent(context, CategoryDetailActivity.class)
                        .putExtra(I.CategoryChild.CAT_ID, childList.get(groupPosition).get(childPosition).getId())
                        .putExtra(I.CategoryGroup.NAME, groupList.get(groupPosition).getName())
                        .putExtra("childList", childList.get(groupPosition)), 0);
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void initList(ArrayList<CategoryGroupBean> groupList, ArrayList<ArrayList<CategoryChildBean>> childList) {
        this.groupList = new ArrayList<CategoryGroupBean>();
        this.groupList.addAll(groupList);
        this.childList = new ArrayList<ArrayList<CategoryChildBean>>();
        this.childList.addAll(childList);
        notifyDataSetChanged();
    }

    private class ViewGroupHolder {
        NetworkImageView mnivCategoryGroup;
        TextView mtvCategoryGroup;
        ImageView mivCategoryGroup;
    }

    private class ViewChildHolder {
        NetworkImageView mnivCategoryChild;
        TextView mtvCategoryChild;
        RelativeLayout mLayoutCategoryChild;
    }
}
