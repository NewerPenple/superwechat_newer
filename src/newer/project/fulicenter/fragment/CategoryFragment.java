package newer.project.fulicenter.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;

import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.adapter.CategoryAdapter;
import newer.project.fulicenter.bean.CategoryChildBean;
import newer.project.fulicenter.bean.CategoryGroupBean;
import newer.project.fulicenter.data.OkHttpUtils2;
import newer.project.fulicenter.utils.Utils;

public class CategoryFragment extends BaseFragment {
    private ExpandableListView melvCategory;
    private ArrayList<CategoryGroupBean> groupList;
    private ArrayList<ArrayList<CategoryChildBean>> childList;
    private CategoryAdapter adapter;
    private boolean init = false;
    private static final int PAGE_ID = 0;
    private static final int PAGE_SIZE = 50;

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_category, container, false);
        melvCategory = (ExpandableListView) layout.findViewById(R.id.elv_category);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    @Override
    public void initData() {
        if (init) {
            return;
        }
        init = true;
        OkHttpUtils2<CategoryGroupBean[]> utils = new OkHttpUtils2<CategoryGroupBean[]>();
        utils.url(FuliCenterApplication.FULI_SERVER_ROOT)
                .addParam(I.KEY_REQUEST,I.REQUEST_FIND_CATEGORY_GROUP)
                .targetClass(CategoryGroupBean[].class)
                .execute(new OkHttpUtils2.OnCompleteListener<CategoryGroupBean[]>() {
                    @Override
                    public void onSuccess(CategoryGroupBean[] result) {
                        if (result != null) {
                            groupList = Utils.array2List(result);
                            for (int i = 0; i < groupList.size(); i++) {
                                childList.add(null);
                            }
                            initChildData();
                        } else {
                            Utils.showToast(getActivity(), getResources().getString(R.string.get_failed_please_check), Toast.LENGTH_SHORT);
                            init = false;
                        }
                    }

                    @Override
                    public void onError(String error) {
                        init = false;
                    }
                });
    }

    private void initChildData() {
        for (int i = 0; i < groupList.size(); i++) {
            final int num = i;
            OkHttpUtils2<CategoryChildBean[]> utils2 = new OkHttpUtils2<CategoryChildBean[]>();
            utils2.url(FuliCenterApplication.FULI_SERVER_ROOT)
                    .addParam(I.KEY_REQUEST, I.REQUEST_FIND_CATEGORY_CHILDREN)
                    .addParam(I.CategoryChild.PARENT_ID, String.valueOf(groupList.get(i).getId()))
                    .addParam(I.PAGE_ID, String.valueOf(PAGE_ID))
                    .addParam(I.PAGE_SIZE, String.valueOf(PAGE_SIZE))
                    .targetClass(CategoryChildBean[].class)
                    .execute(new OkHttpUtils2.OnCompleteListener<CategoryChildBean[]>() {
                        @Override
                        public void onSuccess(CategoryChildBean[] result) {
                            if (result != null) {
                                ArrayList<CategoryChildBean> list = Utils.array2List(result);
                                for (int j = 0; j < groupList.size(); j++) {
                                    if (list.get(0).getParentId() == groupList.get(j).getId()) {
                                        childList.set(j, list);
                                    }
                                }
                            }
                            if (num == groupList.size() - 1) {
                                adapter.initList(groupList, childList);
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (num == groupList.size() - 1) {
                                adapter.initList(groupList, childList);
                            }
                        }
                    });
        }
    }

    private void initView() {
        groupList = new ArrayList<CategoryGroupBean>();
        childList = new ArrayList<ArrayList<CategoryChildBean>>();
        adapter = new CategoryAdapter(getActivity(),groupList,childList);
        melvCategory.setAdapter(adapter);
    }
}
