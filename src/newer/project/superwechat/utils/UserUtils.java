package newer.project.superwechat.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.squareup.picasso.Picasso;

import newer.project.superwechat.DemoHXSDKHelper;
import newer.project.superwechat.I;
import newer.project.superwechat.R;
import newer.project.superwechat.SuperWeChatApplication;
import newer.project.superwechat.applib.controller.HXSDKHelper;
import newer.project.superwechat.bean.Contact;
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
		Log.i("my", username);
		Contact contact = getContactInfo(username);
		Log.i("my", "contact" + contact);
		if (contact != null && contact.getMContactUserName() != null) {
			String s = getAvatarPath(username);
			Log.i("my", s);
			setUserAvatar(s, niv);
			setUserAvatar(getAvatarPath(username), niv);
		}
	}

	private static void setUserAvatar(String url, NetworkImageView niv) {
		if (url == null || url.isEmpty()) {
			return;
		}
		niv.setDefaultImageResId(R.drawable.default_avatar);
		niv.setImageUrl(url, RequestManager.getImageLoader());
		niv.setErrorImageResId(R.drawable.default_avatar);
	}

	private static String getAvatarPath(String username) {
		if (username == null || username.isEmpty()) {
			return null;
		}
		return I.REQUEST_DOWNLOAD_USER_AVATAR_URL + username;
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
			setUserAvatar(getAvatarPath(user.getMUserName()),niv);
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

	public static void setUserBeanNick(String username,TextView textView){
		Contact contact = getContactInfo(username);
		if(contact != null && contact.getMUserNick() != null){
			textView.setText(contact.getMUserNick());
		}else{
			textView.setText(username);
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
    
}
