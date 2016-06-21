package newer.project.fulicenter.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.I;
import newer.project.fulicenter.activity.BaseActivity;
import newer.project.fulicenter.bean.Contact;
import newer.project.fulicenter.data.ApiParams;
import newer.project.fulicenter.data.GsonRequest;
import newer.project.fulicenter.utils.Utils;

public class DownloadContactListTask extends BaseActivity{
    private static final String TAG = DownloadContactListTask.class.toString();
    private Context context;
    private String username;
    private String path;

    public DownloadContactListTask(Context context, final String username) {
        this.context = context;
        this.username = username;
        initPath();
    }

    public void initPath() {
        try {
            path = new ApiParams()
                    .with(I.Contact.USER_NAME, username)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_CONTACT_ALL_LIST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Contact[]>(path, Contact[].class, responseDownloadContactListTask(), errorListener()));
    }

    private Response.Listener<Contact[]> responseDownloadContactListTask() {
        return new Response.Listener<Contact[]>() {
            @Override
            public void onResponse(Contact[] contacts) {
                if (contacts != null) {
                    ArrayList<Contact> contactList = FuliCenterApplication.getInstance().getContactList();
                    ArrayList<Contact> list = Utils.array2List(contacts);
                    contactList.clear();
                    contactList.addAll(list);
                    HashMap<String, Contact> userList = FuliCenterApplication.getInstance().getUserList();
                    userList.clear();
                    for (Contact c : contacts) {
                        userList.put(c.getMContactCname(), c);
                    }
                    context.sendStickyBroadcast(new Intent("update_contact_list"));
                }
            }
        };
    }
}
