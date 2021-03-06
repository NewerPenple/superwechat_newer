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
package newer.project.superwechat.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.easemob.chat.EMContactManager;
import com.easemob.exceptions.EaseMobException;
import com.easemob.util.EMLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import newer.project.superwechat.Constant;
import newer.project.superwechat.DemoHXSDKHelper;
import newer.project.superwechat.I;
import newer.project.superwechat.R;
import newer.project.superwechat.SuperWeChatApplication;
import newer.project.superwechat.activity.AddContactActivity;
import newer.project.superwechat.activity.ChatActivity;
import newer.project.superwechat.activity.GroupsActivity;
import newer.project.superwechat.activity.MainActivity;
import newer.project.superwechat.activity.NewFriendsMsgActivity;
import newer.project.superwechat.adapter.ContactAdapter;
import newer.project.superwechat.applib.controller.HXSDKHelper;
import newer.project.superwechat.applib.controller.HXSDKHelper.HXSyncListener;
import newer.project.superwechat.bean.Contact;
import newer.project.superwechat.data.OkHttpUtils2;
import newer.project.superwechat.db.EMUserDao;
import newer.project.superwechat.db.InviteMessgeDao;
import newer.project.superwechat.domain.EMUser;
import newer.project.superwechat.utils.UserUtils;
import newer.project.superwechat.widget.Sidebar;

/**
 * 联系人列表页
 * 
 */
public class ContactlistFragment extends Fragment {
	public static final String TAG = "ContactlistFragment";
	private ContactAdapter adapter;
	private List<EMUser> contactList;
	private ListView listView;
	private boolean hidden;
	private Sidebar sidebar;
	private InputMethodManager inputMethodManager;
	private List<String> blackList;
	ImageButton clearSearch;
	EditText query;
	HXContactSyncListener contactSyncListener;
	HXBlackListSyncListener blackListSyncListener;
	HXContactInfoSyncListener contactInfoSyncListener;
	View progressBar;
	Handler handler = new Handler();
    private Contact toBeProcessUser;
    private String toBeProcessUsername;
	ContactlistChangedReceiver receiver;
	private ArrayList<Contact> mContactList;

	class HXContactSyncListener implements HXSDKHelper.HXSyncListener {
		@Override
		public void onSyncSucess(final boolean success) {
			EMLog.d(TAG, "on contact list sync success:" + success);
			ContactlistFragment.this.getActivity().runOnUiThread(new Runnable() {
				public void run() {
				    getActivity().runOnUiThread(new Runnable(){

		                @Override
		                public void run() {
		                    if(success){
		                        progressBar.setVisibility(View.GONE);
                                refresh();
		                    }else{
		                        String s1 = getResources().getString(R.string.get_failed_please_check);
		                        Toast.makeText(getActivity(), s1, Toast.LENGTH_SHORT).show();
		                        progressBar.setVisibility(View.GONE);
		                    }
		                }
		                
		            });
				}
			});
		}
	}
	
	class HXBlackListSyncListener implements HXSyncListener{

        @Override
        public void onSyncSucess(boolean success) {
            getActivity().runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    blackList = EMContactManager.getInstance().getBlackListUsernames();
                    refresh();
                }
                
            });
        }
	    
	};
	
	class HXContactInfoSyncListener implements HXSDKHelper.HXSyncListener{

		@Override
		public void onSyncSucess(final boolean success) {
			EMLog.d(TAG, "on contactinfo list sync success:" + success);
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					progressBar.setVisibility(View.GONE);
					if(success){
						refresh();
					}
				}
			});
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_contact_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//防止被T后，没点确定按钮然后按了home键，长期在后台又进app导致的crash
		if(savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false))
		    return;
		inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		listView = (ListView) getView().findViewById(R.id.list);
		sidebar = (Sidebar) getView().findViewById(R.id.sidebar);
		sidebar.setListView(listView);
        
		//黑名单列表
		blackList = EMContactManager.getInstance().getBlackListUsernames();
		contactList = new ArrayList<EMUser>();
		mContactList = new ArrayList<Contact>();
		// 获取设置contactlist
		getContactList();
		
		//搜索框
		setQuery();
		
		// 设置adapter
		setAdapter();

		setListViewListener();

		ImageView addContactView = (ImageView) getView().findViewById(R.id.iv_new_contact);
		// 进入添加好友页
		addContactView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), AddContactActivity.class));
			}
		});
		registerForContextMenu(listView);
		
		progressBar = (View) getView().findViewById(R.id.progress_bar);

		contactSyncListener = new HXContactSyncListener();
		HXSDKHelper.getInstance().addSyncContactListener(contactSyncListener);
		
		blackListSyncListener = new HXBlackListSyncListener();
		HXSDKHelper.getInstance().addSyncBlackListListener(blackListSyncListener);
		
		contactInfoSyncListener = new HXContactInfoSyncListener();
		((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().addSyncContactInfoListener(contactInfoSyncListener);
		
		if (!HXSDKHelper.getInstance().isContactsSyncedWithServer()) {
			progressBar.setVisibility(View.VISIBLE);
		} else {
			progressBar.setVisibility(View.GONE);
		}
		RegisterContactlistChangedReceiver();
	}

	private void setQuery() {
		query = (EditText) getView().findViewById(R.id.query);
		query.setHint(R.string.search);
		clearSearch = (ImageButton) getView().findViewById(R.id.search_clear);
		query.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				adapter.getFilter().filter(s);
				if (s.length() > 0) {
					clearSearch.setVisibility(View.VISIBLE);
				} else {
					clearSearch.setVisibility(View.INVISIBLE);

				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		clearSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				query.getText().clear();
				hideSoftKeyboard();
			}
		});
	}

	private void setAdapter() {
		adapter = new ContactAdapter(getActivity(), R.layout.row_contact, mContactList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String username = adapter.getItem(position).getMContactCname();
				if (Constant.NEW_FRIENDS_USERNAME.equals(username)) {
					// 进入申请与通知页面
					EMUser user = ((DemoHXSDKHelper) HXSDKHelper.getInstance()).getContactList().get(Constant.NEW_FRIENDS_USERNAME);
					user.setUnreadMsgCount(0);
					startActivity(new Intent(getActivity(), NewFriendsMsgActivity.class));
				} else if (Constant.GROUP_USERNAME.equals(username)) {
					// 进入群聊列表页面
					startActivity(new Intent(getActivity(), GroupsActivity.class));
//				} else if(Constant.CHAT_ROOM.equals(username)){
//					//进入聊天室列表页面
//				    startActivity(new Intent(getActivity(), PublicChatRoomsActivity.class));
//				}else if(Constant.CHAT_ROBOT.equals(username)){
//					//进入Robot列表页面
//					startActivity(new Intent(getActivity(), RobotsActivity.class));
				} else {
					// demo中直接进入聊天页面，实际一般是进入用户详情页
					startActivity(new Intent(getActivity(), ChatActivity.class).putExtra("userId", adapter.getItem(position).getMContactCname()));
				}
			}
		});
	}

	private void setListViewListener() {
		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 隐藏软键盘
				if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
					if (getActivity().getCurrentFocus() != null)
						inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				}
				return false;
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (((AdapterContextMenuInfo) menuInfo).position > 1) {
		    toBeProcessUser = adapter.getItem(((AdapterContextMenuInfo) menuInfo).position);
		    toBeProcessUsername = toBeProcessUser.getMContactCname();
			getActivity().getMenuInflater().inflate(R.menu.context_contact_list, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete_contact) {
			try {
                // 删除此联系人
                deleteContact(toBeProcessUser);
                // 删除相关的邀请消息
                InviteMessgeDao dao = new InviteMessgeDao(getActivity());
                dao.deleteMessage(toBeProcessUser.getMContactCname());
            } catch (Exception e) {
                e.printStackTrace();
            }
			return true;
		}else if(item.getItemId() == R.id.add_to_blacklist){
			moveToBlacklist(toBeProcessUsername);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if (!hidden) {
			refresh();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!hidden) {
			refresh();
		}
	}

	/**
	 * 删除联系人
	 * 
	 * @param tobeDeleteUser
	 */
	public void deleteContact(final Contact tobeDeleteUser) {
		String st1 = getResources().getString(R.string.deleting);
		final String st2 = getResources().getString(R.string.Delete_failed);
		final ProgressDialog pd = new ProgressDialog(getActivity());
		pd.setMessage(st1);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		OkHttpUtils2<Boolean> utils = new OkHttpUtils2<Boolean>();
		utils.url(SuperWeChatApplication.SERVER_ROOT)
				.addParam(I.KEY_REQUEST,I.REQUEST_DELETE_CONTACT)
				.addParam(I.Contact.USER_NAME, SuperWeChatApplication.getInstance().getUserName())
				.addParam(I.Contact.CU_NAME,tobeDeleteUser.getMContactCname())
				.targetClass(Boolean.class)
				.execute(new OkHttpUtils2.OnCompleteListener<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if (result != null && result) {
							SuperWeChatApplication.getInstance().getContactList().remove(tobeDeleteUser);
							SuperWeChatApplication.getInstance().getUserList().remove(tobeDeleteUser.getMContactCname());
							getActivity().sendStickyBroadcast(new Intent("update_contact_list"));
						}
					}

					@Override
					public void onError(String error) {

					}
				});
		new Thread(new Runnable() {
			public void run() {
				try {
					EMContactManager.getInstance().deleteContact(tobeDeleteUser.getMContactCname());
					// 删除db和内存中此用户的数据
					EMUserDao dao = new EMUserDao(getActivity());
					dao.deleteContact(tobeDeleteUser.getMContactCname());
					((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().remove(tobeDeleteUser.getMContactCname());
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							adapter.remove(tobeDeleteUser);
							adapter.notifyDataSetChanged();

						}
					});
				} catch (final Exception e) {
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(getActivity(), st2 + e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					});

				}

			}
		}).start();

	}

	/**
	 * 把user移入到黑名单
	 */
	private void moveToBlacklist(final String username){
		final ProgressDialog pd = new ProgressDialog(getActivity());
		String st1 = getResources().getString(R.string.Is_moved_into_blacklist);
		final String st2 = getResources().getString(R.string.Move_into_blacklist_success);
		final String st3 = getResources().getString(R.string.Move_into_blacklist_failure);
		pd.setMessage(st1);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					//加入到黑名单
					EMContactManager.getInstance().addUserToBlackList(username,false);
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(getActivity(), st2, Toast.LENGTH_SHORT).show();
							refresh();
						}
					});
				} catch (EaseMobException e) {
					e.printStackTrace();
					getActivity().runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(getActivity(), st3, Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();
		
	}
	
	// 刷新ui
	public void refresh() {
		try {
			// 可能会在子线程中调到这方法
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					getContactList();
					adapter.notifyDataSetChanged();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		if (contactSyncListener != null) {
			HXSDKHelper.getInstance().removeSyncContactListener(contactSyncListener);
			contactSyncListener = null;
		}
		
		if(blackListSyncListener != null){
		    HXSDKHelper.getInstance().removeSyncBlackListListener(blackListSyncListener);
		}
		
		if(contactInfoSyncListener != null){
			((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().removeSyncContactInfoListener(contactInfoSyncListener);
		}
		super.onDestroy();
		getActivity().unregisterReceiver(receiver);
	}
	
	public void showProgressBar(boolean show) {
		if (progressBar != null) {
			if (show) {
				progressBar.setVisibility(View.VISIBLE);
			} else {
				progressBar.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 获取联系人列表，并过滤掉黑名单和排序
	 */
	private void getContactList() {
//		contactList.clear();
		mContactList.clear();
		//获取本地好友列表
		ArrayList<Contact> contacts = SuperWeChatApplication.getInstance().getContactList();
		mContactList.addAll(contacts);

		/*Map<String, EMUser> users = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList();
		Iterator<Entry<String, EMUser>> iterator = users.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, EMUser> entry = iterator.next();
			if (!entry.getKey().equals(Constant.NEW_FRIENDS_USERNAME)
			        && !entry.getKey().equals(Constant.GROUP_USERNAME)
			        && !entry.getKey().equals(Constant.CHAT_ROOM)
					&& !entry.getKey().equals(Constant.CHAT_ROBOT)
					&& !blackList.contains(entry.getKey()))
				contactList.add(entry.getValue());
		}*/

		// 添加"群聊"
		Contact groupUser = new Contact();
		groupUser.setMContactId(-2);
		String strGroup = getActivity().getString(R.string.group_chat);
		groupUser.setMContactCname(Constant.GROUP_USERNAME);
		groupUser.setMUserName(Constant.NEW_FRIENDS_USERNAME);
		groupUser.setMUserNick(strGroup);
		groupUser.setHeader("");
		if (mContactList.indexOf(groupUser) == -1) {
			mContactList.add(0, groupUser);
		}

		// 添加user"申请与通知"
		Contact newFriends = new Contact();
		newFriends.setMContactId(-1);
		String strChat = getActivity().getString(R.string.Application_and_notify);
		newFriends.setMContactCname(Constant.NEW_FRIENDS_USERNAME);
		newFriends.setMUserName(Constant.NEW_FRIENDS_USERNAME);
		newFriends.setMUserNick(strChat);
		newFriends.setHeader("");
		if (mContactList.indexOf(newFriends) == -1) {
			mContactList.add(0, newFriends);
		}

		// 排序
		for (Contact c : mContactList) {
			UserUtils.setUserHeader(c.getMContactCname(), c);
		}
		Collections.sort(mContactList, new Comparator<Contact>() {

			@Override
			public int compare(Contact lhs, Contact rhs) {
				return lhs.getHeader().compareTo(rhs.getHeader());
			}
		});

		/*if(users.get(Constant.CHAT_ROBOT)!=null){
			contactList.add(0, users.get(Constant.CHAT_ROBOT));
		}*/
		// 加入"群聊"和"聊天室"
        /*if(users.get(Constant.CHAT_ROOM) != null)
            contactList.add(0, users.get(Constant.CHAT_ROOM));*/
//        if(users.get(Constant.GROUP_USERNAME) != null)
//            contactList.add(0, users.get(Constant.GROUP_USERNAME));
//
//		// 把"申请与通知"添加到首位
//		if(users.get(Constant.NEW_FRIENDS_USERNAME) != null)
//		    contactList.add(0, users.get(Constant.NEW_FRIENDS_USERNAME));
		
	}
	
	void hideSoftKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	    if(((MainActivity)getActivity()).isConflict){
	    	outState.putBoolean("isConflict", true);
	    }else if(((MainActivity)getActivity()).getCurrentAccountRemoved()){
	    	outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
	    }
	}

	public class ContactlistChangedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			refresh();
		}
	}

	private void RegisterContactlistChangedReceiver() {
		receiver = new ContactlistChangedReceiver();
		IntentFilter filter = new IntentFilter("update_contact_list");
		getActivity().registerReceiver(receiver, filter);
	}
}
