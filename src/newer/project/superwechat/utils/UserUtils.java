package newer.project.superwechat.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.easemob.util.HanziToPinyin;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import newer.project.superwechat.Constant;
import newer.project.superwechat.DemoHXSDKHelper;
import newer.project.superwechat.I;
import newer.project.superwechat.R;
import newer.project.superwechat.SuperWeChatApplication;
import newer.project.superwechat.applib.controller.HXSDKHelper;
import newer.project.superwechat.bean.Contact;
import newer.project.superwechat.bean.Group;
import newer.project.superwechat.bean.Member;
import newer.project.superwechat.bean.User;
import newer.project.superwechat.data.RequestManager;
import newer.project.superwechat.domain.EMUser;

public class UserUtils {
    /**
     * 根据username获取相应user，由于demo没有真实的用户数据，这里给的模拟的数据；
     * @param username
     * @return
     */
    public static EMUser getUserInfo(String username){
        EMUser user = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getContactList().get(username);
        if(user == null){
            user = new EMUser(username);
        }
            
        if(user != null){
            //demo没有这些数据，临时填充
        	if(TextUtils.isEmpty(user.getNick()))
        		user.setNick(username);
        }
        return user;
    }

	public static Contact getContactInfo(String username) {
		return SuperWeChatApplication.getInstance().getUserList().get(username);
	}

    /**
     * 设置用户头像
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView){
    	EMUser user = getUserInfo(username);
        if(user != null && user.getAvatar() != null){
            Picasso.with(context).load(user.getAvatar()).placeholder(R.drawable.default_avatar).into(imageView);
        }else{
            Picasso.with(context).load(R.drawable.default_avatar).into(imageView);
        }
    }

	public static void setContactAvatar(String username, NetworkImageView niv) {
		Contact contact = getContactInfo(username);
		if (contact != null && contact.getMContactUserName() != null) {
			setUserAvatar(getAvatarPath(username), niv);
		}
	}

	public static void setUserAvatar(String url, NetworkImageView niv) {
		if (url == null || url.isEmpty()) {
			return;
		}
//		RequestManager.getRequestQueue().getCache().remove(url);
		niv.setDefaultImageResId(R.drawable.default_avatar);
		niv.setImageUrl(url, RequestManager.getImageLoader());
		niv.setErrorImageResId(R.drawable.default_avatar);
	}

	public static String getAvatarPath(String username) {
		if (username == null || username.isEmpty()) {
			return null;
		}
		return I.REQUEST_DOWNLOAD_USER_AVATAR_URL + username;
	}

	public static void setGroupAvatar(String hxid, NetworkImageView niv) {
		niv.setDefaultImageResId(R.drawable.group_icon);
		niv.setImageUrl(I.REQUEST_DOWNLOAD_GROUP_AVATAR_URL + hxid, RequestManager.getImageLoader());
		niv.setErrorImageResId(R.drawable.group_icon);
	}

	/**
     * 设置当前用户头像
     */
	public static void setCurrentUserAvatar(Context context, ImageView imageView) {
		EMUser user = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().getCurrentUserInfo();
		if (user != null && user.getAvatar() != null) {
			Picasso.with(context).load(user.getAvatar()).placeholder(R.drawable.default_avatar).into(imageView);
		} else {
			Picasso.with(context).load(R.drawable.default_avatar).into(imageView);
		}
	}

	public static void setCurrentUserAvatar(NetworkImageView niv) {
		User user = SuperWeChatApplication.getInstance().getUser();
		if (user != null) {
			setUserAvatar(getAvatarPath(user.getMUserName()), niv);
		}
	}

    /**
     * 设置用户昵称
     */
    public static void setUserNick(String username,TextView textView){
    	EMUser user = getUserInfo(username);
    	if(user != null){
    		textView.setText(user.getNick());
    	}else{
    		textView.setText(username);
    	}
    }

	public static void setUserBeanNick(String username,TextView textView) {
		Contact contact = getContactInfo(username);
		if (contact != null && contact.getMUserNick() != null) {
			textView.setText(contact.getMUserNick());
		} else {
			textView.setText(username);
		}
	}

	public static void setNewUserNick(User user, TextView textView) {
		if (user != null && user.getMUserNick() != null) {
			textView.setText(user.getMUserNick());
		} else {
			textView.setText("？？？");
		}
	}

    /**
     * 设置当前用户昵称
     */
    public static void setCurrentUserNick(TextView textView){
    	EMUser user = ((DemoHXSDKHelper)HXSDKHelper.getInstance()).getUserProfileManager().getCurrentUserInfo();
    	if(textView != null){
    		textView.setText(user.getNick());
    	}
    }

	public static void setCurrentUserBeanNick(TextView textView) {
		User user = SuperWeChatApplication.getInstance().getUser();
		if (user != null && user.getMUserNick() != null && textView != null) {
			textView.setText(user.getMUserNick());
		}
	}

    /**
     * 保存或更新某个用户
     * @param newUser
     */
	public static void saveUserInfo(EMUser newUser) {
		if (newUser == null || newUser.getUsername() == null) {
			return;
		}
		((DemoHXSDKHelper) HXSDKHelper.getInstance()).saveContact(newUser);
	}

	public static void setUserHeader(String username, Contact user) {
		String headerName = null;
		if (!TextUtils.isEmpty(user.getMUserNick())) {
			headerName = user.getMUserNick();
		} else {
			headerName = user.getMContactCname();
		}
		if (username.equals(Constant.NEW_FRIENDS_USERNAME) || username.equals(Constant.GROUP_USERNAME)) {
			user.setHeader("");
		} else if (Character.isDigit(headerName.charAt(0))) {
			user.setHeader("#");
		} else {
			user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1)).get(0).target.substring(0, 1)
					.toUpperCase());
			char header = user.getHeader().toLowerCase().charAt(0);
			if (header < 'a' || header > 'z') {
				user.setHeader("#");
			}
		}
	}

	public static String hanziToPinyin(String hanzi) {
		String pinyin = "";
		for (int i = 0; i < hanzi.length(); i++) {
			String s = hanzi.substring(i, i + 1);
			pinyin = pinyin + HanziToPinyin.getInstance().get(s).get(0).target.toLowerCase();
		}
		return pinyin;
	}

	public static Group getGroupByHXID(String hxid) {
		if (hxid != null && !hxid.isEmpty()) {
			ArrayList<Group> groupList = SuperWeChatApplication.getInstance().getGroupList();
			for (Group g : groupList) {
				if (g.getMGroupHxid().equals(hxid)) {
					return g;
				}
			}
		}
		return null;
	}

	private static Member getGroupMember(String hxid, String userName) {
		ArrayList<Member> members = SuperWeChatApplication.getInstance().getGroupMembers().get(hxid);
		if (members != null) {
			for (Member member : members) {
				if (member.getMMemberUserName().equals(userName)) {
					return member;
				}
			}
		}
		return null;
	}

	public static void setGroupMemberNick(String hxid, String userName, TextView textView) {
		Member member = getGroupMember(hxid, userName);
		if (member != null) {
			setNewUserNick(member, textView);
		}
	}
}
