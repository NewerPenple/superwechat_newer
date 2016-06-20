package newer.project.fulicenter.utils;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import newer.project.fulicenter.R;

public class DisplayUtils {

    public static void initBack(final Activity activity) {
        activity.findViewById(R.id.tv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.finish();
            }
        });
    }

    public static void initBackWithTitle(final Activity activity, String title) {
        ((TextView) activity.findViewById(R.id.tv_title)).setText(title);
        initBack(activity);
    }
}
