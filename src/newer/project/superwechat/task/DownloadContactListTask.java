package newer.project.superwechat.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import newer.project.superwechat.I;
import newer.project.superwechat.SuperWeChatApplication;
import newer.project.superwechat.activity.BaseActivity;
import newer.project.superwechat.bean.Contact;
import newer.project.superwechat.data.ApiParams;
import newer.project.superwechat.data.GsonRequest;
import newer.project.superwechat.utils.Utils;

public class DownloadContactListTask extends BaseActivity{
    private static final String TAG = DownloadContactListTask.class.toString();
    private Context context;
    private String username;
    private String path;

    public DownloadContactListTask(Context context, String username) {
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
                    ArrayList<Contact> contactList = SuperWeChatApplication.getInstance().getContactList();
                    ArrayList<Contact> list = Utils.array2List(contacts);
                    contactList.clear();
                    contactList.addAll(list);
                    HashMap<String, Contact> userList = SuperWeChatApplication.getInstance().getUserList();
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
