package newer.project.fulicenter.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.R;
import newer.project.fulicenter.activity.CollectActivity;
import newer.project.fulicenter.activity.SettingsActivity;
import newer.project.fulicenter.task.DownloadCollectCountTask;
import newer.project.fulicenter.utils.UserUtils;

public class PersonFragment extends BaseFragment implements View.OnClickListener{
    private static final String TAG = PersonFragment.class.getName();
    private TextView mtvSettingIcon,mtvUserName;
    private ImageView mivMsgIcon,mivQrcodeIcon;
    private NetworkImageView mnivUserAvatar;
    private RelativeLayout mLayoutCollectCount;
    private TextView mtvCollectCount;
    private RelativeLayout mLayoutOrders;
    private ImageView mivOrder1,mivOrder2,mivOrder3,mivOrder4,mivOrder5;
    private RelativeLayout mLayoutCardList,mLayoutCard1,mLayoutCard2,mLayoutCard3, mLayoutPrivilege;
    private int collectCount;
    PersonReceiver mPersonReceiver;

    public PersonFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_person, container, false);
        initView(layout);
        setListener();
        RegisterReceiver();
        return layout;
    }

    private void setListener() {
        mtvSettingIcon.setOnClickListener(this);
        mLayoutCollectCount.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_setting_icon:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
            case R.id.layout_collect_count:
                startActivityForResult(new Intent(getActivity(), CollectActivity.class), 0);
                break;
        }
    }

    private void initView(View layout) {
        mtvSettingIcon = (TextView) layout.findViewById(R.id.tv_setting_icon);
        mtvUserName = (TextView) layout.findViewById(R.id.tv_user_name);
        mivMsgIcon = (ImageView) layout.findViewById(R.id.iv_msg_icon);
        mivQrcodeIcon = (ImageView) layout.findViewById(R.id.iv_qrcode_icon);
        mnivUserAvatar = (NetworkImageView) layout.findViewById(R.id.niv_user_avatar);
        mLayoutCollectCount = (RelativeLayout) layout.findViewById(R.id.layout_collect_count);
        mtvCollectCount = (TextView) layout.findViewById(R.id.tv_collect_count);
        mLayoutOrders = (RelativeLayout) layout.findViewById(R.id.layout_orders);
        mivOrder1 = (ImageView) layout.findViewById(R.id.iv_order1);
        mivOrder2 = (ImageView) layout.findViewById(R.id.iv_order2);
        mivOrder3 = (ImageView) layout.findViewById(R.id.iv_order3);
        mivOrder4 = (ImageView) layout.findViewById(R.id.iv_order4);
        mivOrder5 = (ImageView) layout.findViewById(R.id.iv_order5);
        mLayoutCardList = (RelativeLayout) layout.findViewById(R.id.layout_card_list);
        mLayoutCard1 = (RelativeLayout) layout.findViewById(R.id.layout_card1);
        mLayoutCard2 = (RelativeLayout) layout.findViewById(R.id.layout_card2);
        mLayoutCard3 = (RelativeLayout) layout.findViewById(R.id.layout_card3);
        mLayoutPrivilege = (RelativeLayout) layout.findViewById(R.id.layout_privilege);
    }

    @Override
    public void initData() {
        collectCount = FuliCenterApplication.getInstance().getCollectCount();
        mtvCollectCount.setText(String.valueOf(collectCount));
        UserUtils.setCurrentUserBeanNick(mtvUserName);
        UserUtils.setCurrentUserAvatar(mnivUserAvatar);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mPersonReceiver);
    }

    private class PersonReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("update_collect_count")) {
                initData();
            }else if (action.equals("update_user")) {
                new DownloadCollectCountTask(getActivity()).execute();
            }
        }
    }

    private void RegisterReceiver() {
        mPersonReceiver = new PersonReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("update_collect_count");
        filter.addAction("update_user");
        getActivity().registerReceiver(mPersonReceiver, filter);
    }
}
