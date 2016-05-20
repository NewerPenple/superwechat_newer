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
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;

import newer.project.superwechat.I;
import newer.project.superwechat.R;
import newer.project.superwechat.SuperWeChatApplication;
import newer.project.superwechat.listener.OnSetAvatarListener;

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
	private String avatarName;
	private OnSetAvatarListener mOnSetAvatarListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		userNameEditText = (EditText) findViewById(R.id.username);
		nickEditText = (EditText) findViewById(R.id.nick);
		passwordEditText = (EditText) findViewById(R.id.password);
		confirmPwdEditText = (EditText) findViewById(R.id.confirm_password);
		updateAvatarImageView = (ImageView) findViewById(R.id.iv_updateAvatar);
		setListener();
	}

	private void setListener() {
		setOnLoginListener();
		setOnRegisterListener();
		setAvatarListener();
	}

	private void setAvatarListener() {
		findViewById(R.id.layout_updateAvatar).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mOnSetAvatarListener = new OnSetAvatarListener(RegisterActivity.this, R.id.layout_register, getAvatarName(), I.AVATAR_TYPE_USER_PATH);
			}
		});
	}

	private void setOnRegisterListener() {
		findViewById(R.id.btn_lRegister_register).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				register();
			}
		});
	}

	private void setOnLoginListener() {
		findViewById(R.id.btn_lRegister_login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				back();
			}
		});
	}

	public String getAvatarName() {
		return avatarName;
	}

	public void setAvatarName(String avatarName) {
		this.avatarName = avatarName;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RESULT_OK) {
			mOnSetAvatarListener.setAvatar(requestCode,data,updateAvatarImageView);
		}
	}

	/**
	 * 注册
	 */
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
			final ProgressDialog pd = new ProgressDialog(this);
			pd.setMessage(getResources().getString(R.string.Is_the_registered));
			pd.show();

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
								SuperWeChatApplication.getInstance().setUserName(username);
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), 0).show();
								finish();
							}
						});
					} catch (final EaseMobException e) {
						runOnUiThread(new Runnable() {
							public void run() {
								if (!RegisterActivity.this.isFinishing())
									pd.dismiss();
								int errorCode=e.getErrorCode();
								if(errorCode==EMError.NONETWORK_ERROR){
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
	}

	private void back() {
		finish();
	}

}
