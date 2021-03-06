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
package newer.project.superwechat.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import newer.project.superwechat.Constant;
import newer.project.superwechat.DemoHXSDKHelper;
import newer.project.superwechat.I;
import newer.project.superwechat.R;
import newer.project.superwechat.SuperWeChatApplication;
import newer.project.superwechat.applib.controller.HXSDKHelper;
import newer.project.superwechat.bean.User;
import newer.project.superwechat.data.ApiParams;
import newer.project.superwechat.data.GsonRequest;
import newer.project.superwechat.data.OkHttpUtils2;
import newer.project.superwechat.db.EMUserDao;
import newer.project.superwechat.db.UserDao;
import newer.project.superwechat.domain.EMUser;
import newer.project.superwechat.listener.OnSetAvatarListener;
import newer.project.superwechat.task.DownloadAllGroupTask;
import newer.project.superwechat.task.DownloadContactListTask;
import newer.project.superwechat.task.DownloadPublicGroupTask;
import newer.project.superwechat.utils.CommonUtils;
import newer.project.superwechat.utils.MD5;
import newer.project.superwechat.utils.Utils;

/**
 * 登陆页面
 * 
 */
public class LoginActivity extends BaseActivity {
	private static final String TAG = "LoginActivity";
	public static final int REQUEST_CODE_SETNICK = 1;
	private EditText usernameEditText;
	private EditText passwordEditText;

	private boolean progressShow;
	private boolean autoLogin = false;

	private String currentUsername;
	private String currentPassword;

	ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 如果用户名密码都有，直接进入主页面
		if (DemoHXSDKHelper.getInstance().isLogined()) {
			autoLogin = true;
			startActivity(new Intent(LoginActivity.this, MainActivity.class));

			return;
		}
		setContentView(R.layout.activity_login);

		usernameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);

		setListener();
	}

	/** 设置监听器 */
	private void setListener() {
		setOnLoginListener();
		setOnRegisterListener();
		setOnUserNameChangedListener();
	}

	/** 设置账号文本框监听器，如果用户名改变，清空密码 */
	private void setOnUserNameChangedListener() {
		usernameEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				passwordEditText.setText(null);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		if (SuperWeChatApplication.getInstance().getUserName() != null) {
			usernameEditText.setText(SuperWeChatApplication.getInstance().getUserName());
		}
	}

	/** 设置注册按钮监听器 */
	private void setOnRegisterListener() {
		findViewById(R.id.btn_to_register).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				register();
			}
		});
	}

	/** 设置登录按钮监听器 */
	private void setOnLoginListener() {
		findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				login();
			}
		});
	}

	/** 登录 */
	private void login() {
		if (!CommonUtils.isNetWorkConnected(this)) {
			Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
			return;
		}
		currentUsername = usernameEditText.getText().toString().trim();
		currentPassword = passwordEditText.getText().toString().trim();

		if (TextUtils.isEmpty(currentUsername)) {
			Toast.makeText(this, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(currentPassword)) {
			Toast.makeText(this, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
			return;
		}

		showProgressDialog();

		final long start = System.currentTimeMillis();
		// 调用sdk登陆方法登陆聊天服务器
		EMChatManager.getInstance().login(currentUsername, currentPassword, new EMCallBack() {

			@Override
			public void onSuccess() {
				if (!progressShow) {
					return;
				}
				Log.i(TAG, "onSuccess");
				loginClientServer();
			}

			@Override
			public void onProgress(int progress, String status) {
			}

			@Override
			public void onError(final int code, final String message) {
				if (!progressShow) {
					return;
				}
				runOnUiThread(new Runnable() {
					public void run() {
						pd.dismiss();
						Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message,
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	/** 登录到客户端 */
	private void loginClientServer() {
		UserDao userDao = new UserDao(this);
		User user = userDao.findUserByUserName(currentUsername);
		Log.i(TAG, String.valueOf(user == null));
		if (user != null) { //本地是否保存该账号
			if (user.getMUserPassword().equals(MD5.getData(currentPassword))) {
				Log.i(TAG, "loginClient");
				loginSuccess();
			} else {
				Log.i(TAG, "user == null");
				Toast.makeText(getApplicationContext(), R.string.login_failure_failed, Toast.LENGTH_SHORT).show();
				pd.dismiss();
			}
		} else { //在远程登录
			try {
				String path = new ApiParams()
                        .with(I.User.USER_NAME, currentUsername)
                        .with(I.User.PASSWORD, currentPassword)
                        .getRequestUrl(I.REQUEST_LOGIN);
				executeRequest(new GsonRequest<User>(path, User.class, responseListener(), errorListener()));
			} catch (Exception e) {
				Log.i(TAG, "login fail"+e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/** 设置登录成功监听 */
	private Response.Listener<User> responseListener() {
		return new Response.Listener<User>() {
			@Override
			public void onResponse(User user) {
				Log.i(TAG, "onResponse");
				if (user.isResult()) {
					saveUser(user);
					loginSuccess();
				} else {
					Utils.showToast(LoginActivity.this, Utils.getResourceString(LoginActivity.this, user.getMsg()), Toast.LENGTH_SHORT);
					pd.dismiss();
				}
			}
		};
	}

	/** 保存用户信息 */
	private void saveUser(User user) {
		SuperWeChatApplication intance = SuperWeChatApplication.getInstance();
		intance.setUser(user);
		intance.setUserName(currentUsername);
		intance.setPassword(currentPassword);
		SuperWeChatApplication.currentUserNick = user.getMUserNick();
	}

	/** 显示进度对话框 */
	private void showProgressDialog() {
		progressShow = true;
		pd = new ProgressDialog(LoginActivity.this);
		pd.setCanceledOnTouchOutside(false);
		pd.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				progressShow = false;
			}
		});
		pd.setMessage(getString(R.string.Is_landing));
		pd.show();
	}

	/** 登录成功后操作 */
	private void loginSuccess() {
		try {
			// ** 第一次登录或者之前logout后再登录，加载所有本地群和回话
			// ** manually load all local groups and
			EMGroupManager.getInstance().loadAllGroups();
			EMChatManager.getInstance().loadAllConversations();
			//下载用户头像
			final OkHttpUtils2<Message> utils = new OkHttpUtils2<Message>();
			utils.url(SuperWeChatApplication.SERVER_ROOT)
					.addParam(I.KEY_REQUEST,I.REQUEST_DOWNLOAD_AVATAR)
					.addParam(I.AVATAR_TYPE,currentUsername)
					.doInBackground(new Callback() {
						@Override
						public void onFailure(Request request, IOException e) {
							Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onResponse(com.squareup.okhttp.Response response) throws IOException {
							String path = I.AVATAR_TYPE_USER_PATH + I.BACKSLASH + currentUsername + I.AVATAR_SUFFIX_JPG;
							File file = OnSetAvatarListener.getAvatarFile(LoginActivity.this, path);
							utils.downloadFile(response, file, false);
						}
					})
					.execute(null);
			// 处理好友和群组
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new DownloadContactListTask(LoginActivity.this, currentUsername).execute();
					new DownloadAllGroupTask(LoginActivity.this, currentUsername).execute();
					new DownloadPublicGroupTask(LoginActivity.this, currentUsername, I.PAGE_ID_DEFAULT, I.PAGE_SIZE_DEFAULT).execute();
				}
			});
			initializeContacts();
		} catch (Exception e) {
			e.printStackTrace();
			// 取好友或者群聊失败，不让进入主页面
			runOnUiThread(new Runnable() {
				public void run() {
					pd.dismiss();
					DemoHXSDKHelper.getInstance().logout(true, null);
					Toast.makeText(getApplicationContext(), R.string.login_failure_failed, Toast.LENGTH_SHORT).show();
				}
			});
			return;
		}
		// 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
		boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
				SuperWeChatApplication.currentUserNick.trim());
		if (!updatenick) {
			Log.e("LoginActivity", "update current user nick fail");
		}
		if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
			pd.dismiss();
		}
		// 进入主页面
		Intent intent = new Intent(LoginActivity.this,
				MainActivity.class);
		startActivity(intent);

		finish();
	}

	private void initializeContacts() {
		Map<String, EMUser> userlist = new HashMap<String, EMUser>();
		// 添加user"申请与通知"
		EMUser newFriends = new EMUser();
		newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
		String strChat = getResources().getString(
				R.string.Application_and_notify);
		newFriends.setNick(strChat);

		userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);
		// 添加"群聊"
		EMUser groupUser = new EMUser();
		String strGroup = getResources().getString(R.string.group_chat);
		groupUser.setUsername(Constant.GROUP_USERNAME);
		groupUser.setNick(strGroup);
		groupUser.setHeader("");
		userlist.put(Constant.GROUP_USERNAME, groupUser);
		
		// 添加"Robot"
		/*EMUser robotUser = new EMUser();
		String strRobot = getResources().getString(R.string.robot_chat);
		robotUser.setUsername(Constant.CHAT_ROBOT);
		robotUser.setNick(strRobot);
		robotUser.setHeader("");
		userlist.put(Constant.CHAT_ROBOT, robotUser);*/
		
		// 存入内存
		((DemoHXSDKHelper)HXSDKHelper.getInstance()).setContactList(userlist);
		// 存入db
		EMUserDao dao = new EMUserDao(LoginActivity.this);
		List<EMUser> users = new ArrayList<EMUser>(userlist.values());
		dao.saveContactList(users);
	}
	
	/** 注册 */
	private void register() {
		startActivityForResult(new Intent(this, RegisterActivity.class), 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (autoLogin) {
			return;
		}
	}
}
