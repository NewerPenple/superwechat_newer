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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;

import newer.project.superwechat.I;
import newer.project.superwechat.R;
import newer.project.superwechat.bean.Contact;
import newer.project.superwechat.listener.OnSetAvatarListener;

public class NewGroupActivity extends BaseActivity {
	private EditText groupNameEditText;
	private ProgressDialog progressDialog;
	private EditText introductionEditText;
	private CheckBox checkBox;
	private CheckBox memberCheckbox;
	private LinearLayout openInviteContainer;
	private RelativeLayout mLayoutUploadGroupAvatar;
	private ImageView mivUploadGroupAvatar;
	private TextView mtvUploadGroupAvatar;
	private Button mbtnSaveGroup;
	private OnSetAvatarListener mOnSetAvatarListener;
	private String avatarName;
	private final static int CREATE_GROUP = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_group);
		initView();
		setListener();
	}

	private void setListener() {
		mbtnSaveGroup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String str6 = getResources().getString(R.string.Group_name_cannot_be_empty);
				String name = groupNameEditText.getText().toString();
				if (TextUtils.isEmpty(name)) {
					Intent intent = new Intent(NewGroupActivity.this, AlertDialog.class);
					intent.putExtra("msg", str6);
					startActivity(intent);
				} else {
					// 进通讯录选人
					startActivityForResult(new Intent(NewGroupActivity.this, GroupPickContactsActivity.class).putExtra("groupName", name), CREATE_GROUP);
				}
			}
		});

		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					openInviteContainer.setVisibility(View.INVISIBLE);
				} else {
					openInviteContainer.setVisibility(View.VISIBLE);
				}
			}
		});

		mLayoutUploadGroupAvatar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mOnSetAvatarListener = new OnSetAvatarListener(NewGroupActivity.this, R.id.layout_new_group, getAvatarName(), I.AVATAR_TYPE_GROUP_PATH);
			}
		});
	}

	public String getAvatarName() {
		avatarName = String.valueOf(System.currentTimeMillis());
		return avatarName;
	}

	private void initView() {
		groupNameEditText = (EditText) findViewById(R.id.edit_group_name);
		introductionEditText = (EditText) findViewById(R.id.edit_group_introduction);
		checkBox = (CheckBox) findViewById(R.id.cb_public);
		memberCheckbox = (CheckBox) findViewById(R.id.cb_member_inviter);
		openInviteContainer = (LinearLayout) findViewById(R.id.ll_open_invite);
		mLayoutUploadGroupAvatar = (RelativeLayout) findViewById(R.id.layout_uploadGroupAvatar);
		mivUploadGroupAvatar = (ImageView) findViewById(R.id.iv_uploadGroupAvatar);
		mtvUploadGroupAvatar = (TextView) findViewById(R.id.tv_uploadGroupAvatar);
		mbtnSaveGroup = (Button) findViewById(R.id.btn_save_group);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		if (requestCode == CREATE_GROUP) {
			setProgressDialog();
			//新建群组

			createNewGroup(data);
		} else {
			mOnSetAvatarListener.setAvatar(requestCode, data, mivUploadGroupAvatar);
		}
	}

	private void createNewGroup(final Intent data) {
		final String st2 = getResources().getString(R.string.Failed_to_create_groups);
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 调用sdk创建群组方法
				String groupName = groupNameEditText.getText().toString().trim();
				String desc = introductionEditText.getText().toString();
				Contact[] contacts = (Contact[])data.getSerializableExtra("newmembers");
				String[] members = null;
				String[] memberIds = null;
				if (contacts != null) {
					members = new String[contacts.length];
					memberIds = new String[contacts.length];
					for (int i = 0; i < contacts.length; i++) {
						members[i] = contacts[i].getMContactCname() + ",";
						memberIds[i] = contacts[i].getMContactId() + ",";
					}
				}
				EMGroup emGroup;
				try {
					if(checkBox.isChecked()){
						//创建公开群，此种方式创建的群，可以自由加入
						//创建公开群，此种方式创建的群，用户需要申请，等群主同意后才能加入此群
						emGroup = EMGroupManager.getInstance().createPublicGroup(groupName, desc, members, true,200);
					}else{
						//创建不公开群
						emGroup = EMGroupManager.getInstance().createPrivateGroup(groupName, desc, members, memberCheckbox.isChecked(),200);
					}
					String hxid = emGroup.getGroupId();
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							setResult(RESULT_OK);
							finish();
						}
					});
				} catch (final EaseMobException e) {
					runOnUiThread(new Runnable() {
						public void run() {
							progressDialog.dismiss();
							Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
						}
					});
				}

			}
		}).start();
	}

	public void setProgressDialog() {
		String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(st1);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
	}

	public void back(View view) {
		finish();
	}
}
