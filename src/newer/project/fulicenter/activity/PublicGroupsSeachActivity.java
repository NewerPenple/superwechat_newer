package newer.project.fulicenter.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;

import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.SuperWeChatApplication;
import newer.project.fulicenter.bean.Group;
import newer.project.fulicenter.data.OkHttpUtils2;
import newer.project.fulicenter.utils.UserUtils;
import newer.project.fulicenter.utils.Utils;

public class PublicGroupsSeachActivity extends BaseActivity{
    private RelativeLayout containerLayout;
    private EditText idET;
    private TextView nameText;
    private NetworkImageView nivAvatar;
    public static Group searchedGroup;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_public_groups_search);
        
        containerLayout = (RelativeLayout) findViewById(R.id.rl_searched_group);
        idET = (EditText) findViewById(R.id.et_search_id);
        nameText = (TextView) findViewById(R.id.name);
        nivAvatar = (NetworkImageView) findViewById(R.id.avatar);
        searchedGroup = null;
    }
    
    /**
     * 搜索
     * @param v
     */
    public void searchGroup(View v){
        if(TextUtils.isEmpty(idET.getText())){
            return;
        }
        
        pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.searching));
        pd.setCancelable(false);
        pd.show();

        OkHttpUtils2<Group> utils = new OkHttpUtils2<Group>();
        utils.url(SuperWeChatApplication.SERVER_ROOT)
                .addParam(I.KEY_REQUEST,I.REQUEST_FIND_PUBLIC_GROUP_BY_HXID)
                .addParam(I.Group.HX_ID,idET.getText().toString().trim())
                .targetClass(Group.class)
                .execute(new OkHttpUtils2.OnCompleteListener<Group>() {
                    @Override
                    public void onSuccess(Group result) {
                        if (result != null) {
                            searchedGroup = result;
                            containerLayout.setVisibility(View.VISIBLE);
                            nameText.setText(result.getMGroupName());
                            UserUtils.setGroupAvatar(result.getMGroupHxid(), nivAvatar);
                        } else {
                            containerLayout.setVisibility(View.GONE);
                            Utils.showToast(PublicGroupsSeachActivity.this, getResources().getString(R.string.group_not_existed), Toast.LENGTH_SHORT);
                        }
                        pd.dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        containerLayout.setVisibility(View.GONE);
                        Utils.showToast(PublicGroupsSeachActivity.this, getResources().getString(R.string.group_search_failed), Toast.LENGTH_SHORT);
                        pd.dismiss();
                    }
                });

        /*new Thread(new Runnable() {

            public void run() {
                try {
                    searchedGroup = EMGroupManager.getInstance().getGroupFromServer(idET.getText().toString());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            containerLayout.setVisibility(View.VISIBLE);
                            nameText.setText(searchedGroup.getGroupName());
                        }
                    });
                    
                } catch (final EaseMobException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pd.dismiss();
                            searchedGroup = null;
                            containerLayout.setVisibility(View.GONE);
                            if(e.getErrorCode() == EMError.GROUP_NOT_EXIST){
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.group_not_existed), 0).show();
                            }else{
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.group_search_failed) + " : " + getString(R.string.connect_failuer_toast), 0).show();
                            }
                        }
                    });
                }
            }
        }).start();*/
        
    }
    
    
    /**
     * 点击搜索到的群组进入群组信息页面
     * @param view
     */
    public void enterToDetails(View view){
        startActivity(new Intent(this, GroupSimpleDetailActivity.class));
    }
}
