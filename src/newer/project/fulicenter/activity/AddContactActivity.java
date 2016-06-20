/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package newer.project.fulicenter.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.easemob.chat.EMContactManager;

import java.util.HashMap;

import newer.project.fulicenter.DemoHXSDKHelper;
import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.applib.controller.HXSDKHelper;
import newer.project.fulicenter.bean.Contact;
import newer.project.fulicenter.bean.User;
import newer.project.fulicenter.data.OkHttpUtils2;
import newer.project.fulicenter.utils.UserUtils;

public class AddContactActivity extends BaseActivity{
	private final static String TAG = "AddContactActivity";
	private EditText editText;
	private LinearLayout searchedUserLayout;
	private TextView nameText,mTextView;
	private Button searchBtn;
	private NetworkImageView avatar;
	private InputMethodManager inputMethodManager;
	private String toAddUsername;
	private ProgressDialog progressDialog;

	private String userNick;
	private String avatarPath;
	boolean exist = false;
	private TextView mtvNoUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);
		mTextView = (TextView) findViewById(R.id.add_list_friends);
		
		editText = (EditText) findViewById(R.id.edit_note);
		String strAdd = getResources().getString(R.string.add_friend);
		mTextView.setText(strAdd);
		String strUserName = getResources().getString(R.string.user_name);
		editText.setHint(strUserName);
		searchedUserLayout = (LinearLayout) findViewById(R.id.ll_user);
		nameText = (TextView) findViewById(R.id.name);
		searchBtn = (Button) findViewById(R.id.search);
		avatar = (NetworkImageView) findViewById(R.id.avatar);
		inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		mtvNoUser = (TextView) findViewById(R.id.tvNoUser);
	}
	
	
	/**
	 * 查找contact
	 * @param v
	 */
	public void searchContact(View v) {
		final String name = editText.getText().toString();
		String saveText = searchBtn.getText().toString();
		
		if (getString(R.string.button_search).equals(saveText)) {
			toAddUsername = name;
			if(TextUtils.isEmpty(name)) {
				String st = getResources().getString(R.string.Please_enter_a_username);
				startActivity(new Intent(this, AlertDialog.class).putExtra("msg", st));
				return;
			}
			if (FuliCenterApplication.getInstance().getUserName().equals(toAddUsername)) {
				String str = getString(R.string.not_add_myself);
				startActivity(new Intent(this, AlertDialog.class).putExtra("msg", str));
				return;
			}
			// TODO 从服务器获取此contact,如果不存在提示不存在此用户
			
			//服务器存在此用户，显示此用户和添加按钮
			existUser(toAddUsername);
		}
	}

	private void existUser(final String userName) {
		OkHttpUtils2<User> utils = new OkHttpUtils2<User>();
		utils.url(FuliCenterApplication.SERVER_ROOT)
				.addParam(I.KEY_REQUEST,I.REQUEST_FIND_USER)
				.addParam(I.User.USER_NAME,userName)
				.targetClass(User.class)
				.execute(new OkHttpUtils2.OnCompleteListener<User>() {
					@Override
					public void onSuccess(User result) {
						if (result != null) {
							if (result.getMUserNick() != null) {
								userNick = result.getMUserNick();
							} else {
								userNick = result.getMUserName();
							}
							exist = true;
							showUser();
						}
					}

					@Override
					public void onError(String error) {
						exist = false;
						showUser();
					}
				});
		Log.i("my", String.valueOf(exist));
	}

	private void showUser() {
		if (exist) {
			mtvNoUser.setVisibility(View.GONE);
			HashMap<String,Contact> map = FuliCenterApplication.getInstance().getUserList();
			if (map.containsKey(toAddUsername)) {
				searchedUserLayout.setVisibility(View.GONE);
				startActivity(new Intent(this, UserProfileActivity.class).putExtra("username", toAddUsername));
			} else {
				searchedUserLayout.setVisibility(View.VISIBLE);
				nameText.setText(userNick);
				UserUtils.setUserAvatar(UserUtils.getAvatarPath(toAddUsername), avatar);
			}
		} else {
			searchedUserLayout.setVisibility(View.GONE);
			mtvNoUser.setVisibility(View.VISIBLE);
		}
	}

	/**
	 *  添加contact
	 * @param view
	 */
	public void addContact(View view){
		if(FuliCenterApplication.getInstance().getUserName().equals(nameText.getText().toString())){
			String str = getString(R.string.not_add_myself);
			startActivity(new Intent(this, AlertDialog.class).putExtra("msg", str));
			return;
		}
		
		if(((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().containsKey(nameText.getText().toString())){
		    //提示已在好友列表中，无需添加
		    if(EMContactManager.getInstance().getBlackListUsernames().contains(nameText.getText().toString())){
		        startActivity(new Intent(this, AlertDialog.class).putExtra("msg", "此用户已是你好友(被拉黑状态)，从黑名单列表中移出即可"));
		        return;
		    }
			String strin = getString(R.string.This_user_is_already_your_friend);
			startActivity(new Intent(this, AlertDialog.class).putExtra("msg", strin));
			return;
		}
		
		progressDialog = new ProgressDialog(this);
		String stri = getResources().getString(R.string.Is_sending_a_request);
		progressDialog.setMessage(stri);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
		
		new Thread(new Runnable() {
			public void run() {
				
				try {
					//demo写死了个reason，实际应该让用户手动填入
					String s = getResources().getString(R.string.Add_a_friend);
					EMContactManager.getInstance().addContact(toAddUsername, s);
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							String s1 = getResources().getString(R.string.send_successful);
							Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							String s2 = getResources().getString(R.string.Request_add_buddy_failure);
							Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}
	
	public void back(View v) {
		finish();
	}
}