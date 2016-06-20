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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;

import java.io.File;

import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.bean.Message;
import newer.project.fulicenter.data.OkHttpUtils2;
import newer.project.fulicenter.listener.OnSetAvatarListener;
import newer.project.fulicenter.utils.ImageUtils;
import newer.project.fulicenter.utils.Utils;

/**
 * 注册页
 * 
 */
public class RegisterActivity extends BaseActivity {
	private EditText userNameEditText;
	private EditText nickEditText;
	private EditText passwordEditText;
	private EditText confirmPwdEditText;
	private ImageView updateAvatarImageView;
	private String avatarName;//暂时保存的头像名，用于提交注册请求
	private OnSetAvatarListener mOnSetAvatarListener;
	private final static String TAG = RegisterActivity.class.getName();
	private static String SERVER_ROOT = "http://10.0.2.2:8080/SuperWeChatServer/Server";
	ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		initView();
		setListener();
	}

	/** 实体化布局属性 */
	private void initView() {
		userNameEditText = (EditText) findViewById(R.id.username);
		nickEditText = (EditText) findViewById(R.id.nick);
		passwordEditText = (EditText) findViewById(R.id.password);
		confirmPwdEditText = (EditText) findViewById(R.id.confirm_password);
		updateAvatarImageView = (ImageView) findViewById(R.id.iv_updateAvatar);
	}

	/** 设置监听器 */
	private void setListener() {
//		setOnLoginListener();
		setOnRegisterListener();
		setAvatarListener();
		setBackListener();
	}

	/** 设置返回按钮监听器，返回主界面 */
	private void setBackListener() {
		findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				back();
			}
		});
	}

	/** 点击选取头像布局，给两个选取头像方法的按钮设置选取头像监听器 */
	private void setAvatarListener() {
		findViewById(R.id.layout_updateAvatar).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mOnSetAvatarListener = new OnSetAvatarListener(RegisterActivity.this, R.id.layout_register, getAvatarName(), I.AVATAR_TYPE_USER_PATH);//在OnSetAvatarListener类中自动为两个按钮设置监听
			}
		});
	}

	/** 设置注册按钮监听器 */
	private void setOnRegisterListener() {
		findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				register();
			}
		});
	}

	/** 设置登录按钮监听器，返回登录界面 */
	/*private void setOnLoginListener() {
		findViewById(R.id.btn_lRegister_login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				back();
			}
		});
	}*/

	/** 设置暂存头像名，并返回 */
	public String getAvatarName() {
		avatarName = String.valueOf(System.currentTimeMillis());
		return avatarName;
	}

	/** 接收mOnSetAvatarListener获得头像的信息，将头像显示到updateAvatarImageView上 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			mOnSetAvatarListener.setAvatar(requestCode, data, updateAvatarImageView);
		}
	}

	/** 注册 */
	private void register() {
		final String username = userNameEditText.getText().toString().trim();
		final String nick = nickEditText.getText().toString().trim();
		final String pwd = passwordEditText.getText().toString().trim();
		String confirm_pwd = confirmPwdEditText.getText().toString().trim();
		if (TextUtils.isEmpty(username)) {
			userNameEditText.requestFocus();
			userNameEditText.setError(getResources().getString(R.string.User_name_cannot_be_empty));
			return;
		}else if (!username.matches("[\\w][\\w\\d_]+")) {
			userNameEditText.requestFocus();
			userNameEditText.setError(getResources().getString(R.string.User_name_cannot_be_wd));
		} else if (TextUtils.isEmpty(nick)) {
			nickEditText.requestFocus();
			nickEditText.setError(getResources().getString(R.string.Nick_name_cannot_be_empty));
			return;
		} else if (TextUtils.isEmpty(pwd)) {
			passwordEditText.requestFocus();
			passwordEditText.setError(getResources().getString(R.string.Password_cannot_be_empty));
			return;
		} else if (TextUtils.isEmpty(confirm_pwd)) {
			confirmPwdEditText.requestFocus();
			confirmPwdEditText.setError(getResources().getString(R.string.Confirm_password_cannot_be_empty));
			return;
		} else if (!pwd.equals(confirm_pwd)) {
			confirmPwdEditText.setError(getResources().getString(R.string.Two_input_password));
			return;
		}

		if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
			pd = new ProgressDialog(this);
			pd.setMessage(getResources().getString(R.string.Is_the_registered));
			pd.show();
			registerServer(username, nick, pwd);
		}
	}

	/** 注册到远端服务端 */
	private void registerServer(final String username, final String nick, final String pwd) {
		File file = new File(ImageUtils.getAvatarPath(RegisterActivity.this, I.AVATAR_TYPE_USER_PATH), avatarName + I.AVATAR_SUFFIX_JPG);//用户头像文件夹 + 暂存头像名 + .jpg
		OkHttpUtils2<Message> utils = new OkHttpUtils2<Message>();
		utils.url(SERVER_ROOT)
				.addParam(I.KEY_REQUEST,I.REQUEST_REGISTER)
				.addParam(I.User.USER_NAME, username)
				.addParam(I.User.NICK, nick)
				.addParam(I.User.PASSWORD, pwd)
				.addFile(file)
				.targetClass(Message.class)
				.execute(new OkHttpUtils2.OnCompleteListener<Message>() {
					@Override
					public void onSuccess(Message result) {
						if (result.isResult()) {
							registerEMServer(username,nick,pwd);
						} else {
							Utils.showToast(RegisterActivity.this,Utils.getResourceString(RegisterActivity.this,result.getMsg()),Toast.LENGTH_SHORT);
							pd.dismiss();
							Log.e(TAG, "register fail,error:" + result.getMsg());
						}
					}

					@Override
					public void onError(String error) {
						Utils.showToast(RegisterActivity.this,error,Toast.LENGTH_SHORT);
						pd.dismiss();
						Log.e(TAG, "register fail,error:" + error);
					}
				});

	}

	/** 注册到环信服务端 */
	private void registerEMServer(final String username, final String nick, final String pwd) {
		new Thread(new Runnable() {
			public void run() {
				try {
					// 调用sdk注册方法
					EMChatManager.getInstance().createAccountOnServer(username, pwd);
					runOnUiThread(new Runnable() {
						public void run() {
							if (!RegisterActivity.this.isFinishing())
								pd.dismiss();
							// 保存用户名
							FuliCenterApplication.getInstance().setUserName(username);
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), 0).show();
							finish();
						}
					});
				} catch (final EaseMobException e) {
					unregister(username);//取消远端注册
					runOnUiThread(new Runnable() {
						public void run() {
							if (!RegisterActivity.this.isFinishing())
								pd.dismiss();
							int errorCode=e.getErrorCode();
							if(errorCode== EMError.NONETWORK_ERROR){
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
							}else if(errorCode == EMError.USER_ALREADY_EXISTS){
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
							}else if(errorCode == EMError.UNAUTHORIZED){
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
							}else if(errorCode == EMError.ILLEGAL_USER_NAME){
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name),Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
			}
		}).start();
	}

	/** 环信服务端注册失败时，取消远端服务端的注册 */
	private void unregister(String username) {
		OkHttpUtils2<Message> utils = new OkHttpUtils2<Message>();
		utils.url(SERVER_ROOT)
				.addParam(I.KEY_REQUEST, I.REQUEST_UNREGISTER)
				.addParam(I.User.USER_NAME, username)
				.targetClass(Message.class)
				.execute(new OkHttpUtils2.OnCompleteListener<Message>() {
					@Override
					public void onSuccess(Message result) {
						if (!result.isResult()) {
							Utils.showToast(RegisterActivity.this,Utils.getResourceString(RegisterActivity.this,result.getMsg()),Toast.LENGTH_SHORT);
							Log.e(TAG, "unregister fail,error:" + result.getMsg());
						}
					}

					@Override
					public void onError(String error) {
						Utils.showToast(RegisterActivity.this, error, Toast.LENGTH_SHORT);
						Log.e(TAG, "unregister fail,error:" + error);
					}
				});
	}

	/** 返回登录界面 */
	private void back() {
		finish();
	}

}
