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
package newer.project.superwechat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import newer.project.superwechat.I;
import newer.project.superwechat.R;
import newer.project.superwechat.bean.Group;
import newer.project.superwechat.data.RequestManager;

public class GroupAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private String newGroup;
	private String addPublicGroup;
	private ArrayList<Group> groups;

	public GroupAdapter(Context context, int res, ArrayList<Group> groups) {
		this.inflater = LayoutInflater.from(context);
		newGroup = context.getResources().getString(R.string.The_new_group_chat);
		addPublicGroup = context.getResources().getString(R.string.add_public_group_chat);
		this.groups = new ArrayList<Group>();
		this.groups.addAll(groups);
	}

	public void initList(ArrayList<Group> list) {
		groups.clear();
		groups.addAll(list);
		notifyDataSetChanged();
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return 0;
		} else if (position == 1) {
			return 1;
		} else if (position == 2) {
			return 2;
		} else {
			return 3;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (getItemViewType(position) == 0) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.search_bar_with_padding, null);
			}
			final EditText query = (EditText) convertView.findViewById(R.id.query);
			final ImageButton clearSearch = (ImageButton) convertView.findViewById(R.id.search_clear);
			/*query.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					getFilter().filter(s);
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
				}
			});*/
		} else if (getItemViewType(position) == 1) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_add_group, null);
			}
			((ImageView) convertView.findViewById(R.id.avatar)).setImageResource(R.drawable.create_group);
			((TextView) convertView.findViewById(R.id.name)).setText(newGroup);
		} else if (getItemViewType(position) == 2) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_add_group, null);
			}
			((ImageView) convertView.findViewById(R.id.avatar)).setImageResource(R.drawable.add_public_group);
			((TextView) convertView.findViewById(R.id.name)).setText(addPublicGroup);
			((TextView) convertView.findViewById(R.id.header)).setVisibility(View.VISIBLE);

		} else {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.row_group, null);
			}
			((TextView) convertView.findViewById(R.id.name)).setText(getItem(position - 3).getMGroupName());
			NetworkImageView miv = ((NetworkImageView) convertView.findViewById(R.id.avatar));
			String hxid = groups.get(position - 3).getMGroupHxid();
			miv.setDefaultImageResId(R.drawable.group_icon);
			miv.setImageUrl(I.REQUEST_DOWNLOAD_GROUP_AVATAR_URL + hxid, RequestManager.getImageLoader());
			miv.setErrorImageResId(R.drawable.group_icon);
		}

		return convertView;
	}

	@Override
	public int getCount() {
		return groups == null? 3:groups.size() + 3;
	}

	@Override
	public Group getItem(int i) {
		return groups.get(i);
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

}