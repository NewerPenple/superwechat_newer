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
package newer.project.fulicenter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.android.volley.toolbox.NetworkImageView;
import com.easemob.util.EMLog;
import com.easemob.util.PathUtil;

import java.io.File;

import newer.project.fulicenter.I;
import newer.project.fulicenter.R;
import newer.project.fulicenter.data.RequestManager;

public class ImageUtils {
//	public static String getThumbnailImagePath(String imagePath) {
//		String path = imagePath.substring(0, imagePath.lastIndexOf("/") + 1);
//		path += "th" + imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.length());
//		EMLog.d("msg", "original image path:" + imagePath);
//		EMLog.d("msg", "thum image path:" + path);
//		return path;
//	}
	
	public static String getImagePath(String remoteUrl)
	{
		String imageName= remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1, remoteUrl.length());
		String path =PathUtil.getInstance().getImagePath()+"/"+ imageName;
        EMLog.d("msg", "image path:" + path);
        return path;
		
	}
	
	
	public static String getThumbnailImagePath(String thumbRemoteUrl) {
		String thumbImageName= thumbRemoteUrl.substring(thumbRemoteUrl.lastIndexOf("/") + 1, thumbRemoteUrl.length());
		String path =PathUtil.getInstance().getImagePath()+"/"+ "th"+thumbImageName;
        EMLog.d("msg", "thum image path:" + path);
        return path;
    }

	public static String getAvatarPath(Context context, String path) {
		File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File avatar = new File(dir, path);
		if (!avatar.exists()) {
			avatar.mkdir();
		}
		return avatar.getAbsolutePath();
	}

	public static void setGoodsPicture(NetworkImageView niv, String goodsUrl) {
		niv.setDefaultImageResId(R.drawable.default_image);
		niv.setImageUrl(I.REQUEST_DOWNLOAD_BOUTIQUE_IMG_URL + goodsUrl, RequestManager.getImageLoader());
		niv.setErrorImageResId(R.drawable.default_image);
	}

	public static void setGoodsDetailThumb(NetworkImageView niv, String goodsImg) {
		niv.setDefaultImageResId(R.drawable.nopic);
		niv.setImageUrl(I.REQUEST_DOWNLOAD_COLOR_IMG_URL + goodsImg, RequestManager.getImageLoader());
		niv.setErrorImageResId(R.drawable.nopic);
	}

	public static void setBoutiquePicture(NetworkImageView niv, String boutiqueUrl) {
		niv.setDefaultImageResId(R.drawable.default_image);
		niv.setImageUrl(I.REQUEST_DOWNLOAD_BOUTIQUE_IMG_URL + boutiqueUrl, RequestManager.getImageLoader());
		niv.setErrorImageResId(R.drawable.default_image);
	}

	public static void setThumb(NetworkImageView niv, String url) {
		niv.setDefaultImageResId(R.drawable.nopic);
		niv.setImageUrl(url, RequestManager.getImageLoader());
		niv.setErrorImageResId(R.drawable.nopic);
	}

	public static int getDrawableWidth(Context context,int resId){
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
		return bitmap.getWidth();
	}
	public static int getDrawableHeight(Context context,int resId){
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
		return bitmap.getHeight();
	}

}
