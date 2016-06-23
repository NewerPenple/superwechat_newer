package newer.project.fulicenter.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.chat.EMChat;

import java.util.ArrayList;

import newer.project.fulicenter.FuliCenterApplication;
import newer.project.fulicenter.R;
import newer.project.fulicenter.bean.CartBean;
import newer.project.fulicenter.fragment.BaseFragment;
import newer.project.fulicenter.fragment.BoutiqueFragment;
import newer.project.fulicenter.fragment.CartFragment;
import newer.project.fulicenter.fragment.CategoryFragment;
import newer.project.fulicenter.fragment.NewGoodsFragment;
import newer.project.fulicenter.fragment.PersonFragment;
import newer.project.fulicenter.task.DownloadCartTask;

public class FuliActivity extends BaseActivity {
    private static final String TAG = FuliActivity.class.getName();
    private RadioButton mrbNewGoods,mrbBoutique,mrbCategory,mrbCart,mrbMine;
    private RadioButton[] mrbArr;
    private TextView mtvCartHint;
    private RelativeLayout mLayoutCart;
    private ViewPager mvpContainer;
    private int index,currentIndex;
    private MenuAdapter adapter;

    private NewGoodsFragment newGoodsFragment;
    private BoutiqueFragment boutiqueFragment;
    private CategoryFragment categoryFragment;
    private CartFragment cartFragment;
    private PersonFragment personFragment;
    private BaseFragment[] fragmentArr;
    private CartReceiver cartReceiver;

    private boolean start = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuli);
        initView();
        initFragment();
        setListener();
        start = true;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    private void setListener() {
        mvpContainer.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mrbArr[currentIndex].setChecked(false);
                currentIndex = position;
                mrbArr[position].setChecked(true);
                if (fragmentArr[position] != null) {
                    fragmentArr[position].initData();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initFragment() {
        newGoodsFragment = new NewGoodsFragment();
        boutiqueFragment = new BoutiqueFragment();
        categoryFragment = new CategoryFragment();
        cartFragment = new CartFragment();
        personFragment = new PersonFragment();
        fragmentArr = new BaseFragment[]{newGoodsFragment, boutiqueFragment, categoryFragment, cartFragment, personFragment};
        adapter = new MenuAdapter(getSupportFragmentManager(), fragmentArr);
        mvpContainer.setAdapter(adapter);
        mvpContainer.setCurrentItem(0);
    }

    private void initView() {
        mrbNewGoods = (RadioButton) findViewById(R.id.rb_new_goods);
        mrbBoutique = (RadioButton) findViewById(R.id.rb_boutique);
        mrbCategory = (RadioButton) findViewById(R.id.rb_category);
        mrbCart = (RadioButton) findViewById(R.id.rb_cart);
        mrbMine = (RadioButton) findViewById(R.id.rb_mine);
        mrbArr = new RadioButton[]{mrbNewGoods, mrbBoutique, mrbCategory, mrbCart, mrbMine};
        mtvCartHint = (TextView) findViewById(R.id.tv_cart_hint);
        mLayoutCart = (RelativeLayout) findViewById(R.id.layout_cart);
        mvpContainer = (ViewPager) findViewById(R.id.vp_container);
        registerCartReceiver();
    }

    public void onCheckedChange(View view) {
        switch (view.getId()) {
            case R.id.rb_new_goods:
                index = 0;
                break;
            case R.id.rb_boutique:
                index = 1;
                break;
            case R.id.rb_category:
                index = 2;
                break;
            case R.id.rb_cart:
                index = 3;
                break;
            case R.id.rb_mine:
                index = 4;
                break;
        }
        if (currentIndex != index) {
            if ((index == 3 || index == 4) && !EMChat.getInstance().isLoggedIn()) {
                startActivityForResult(new Intent(this, LoginActivity.class), index);
                mrbArr[index].setChecked(false);
            } else {
                mvpContainer.setCurrentItem(index);
                mrbArr[currentIndex].setChecked(false);
                currentIndex = index;
                mrbArr[index].setChecked(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == LoginActivity.RESULT_CODE_PERSON) {
            mvpContainer.setCurrentItem(requestCode);
            mrbArr[currentIndex].setChecked(false);
            currentIndex = requestCode;
            mrbArr[currentIndex].setChecked(true);
            return;
        }else if (resultCode == LoginActivity.RESULT_CODE_TO_CART) {
            mvpContainer.setCurrentItem(3);
            mrbArr[currentIndex].setChecked(false);
            currentIndex = 3;
            mrbArr[currentIndex].setChecked(true);
        }
    }

    private class MenuAdapter extends FragmentPagerAdapter {
        Fragment[] fragmentArr;

        public MenuAdapter(FragmentManager fm, Fragment[] fragmentArr) {
            super(fm);
            this.fragmentArr = fragmentArr;
        }

        @Override
        public Fragment getItem(int i) {
            return fragmentArr[i];
        }

        @Override
        public int getCount() {
            return fragmentArr.length;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(cartReceiver);
    }

    private class CartReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("update_user_cart")) {
                if (FuliCenterApplication.getInstance().getCartList() != null) {
                    int cartCount = 0;
                    ArrayList<CartBean> list = FuliCenterApplication.getInstance().getCartList();
                    for (CartBean cart : list) {
                        if (cart.getGoods() == null) {
                            cartCount++;
                        } else {
                            cartCount += cart.getCount();
                        }
                    }
                    if (cartCount > 0) {
                        mtvCartHint.setText(String.valueOf(cartCount));
                        mtvCartHint.setVisibility(View.VISIBLE);
                    } else {
                        mtvCartHint.setVisibility(View.GONE);
                    }
                } else {
                    mtvCartHint.setVisibility(View.GONE);
                }
            } else if (intent.getAction().equals("update_cart")) {
                if (EMChat.getInstance().isLoggedIn()) {
                    new DownloadCartTask(FuliActivity.this, 1024 * 10).execute();
                }
            }else if (intent.getAction().equals("log_out")) {
                index = 0;
                mvpContainer.setCurrentItem(index);
                mrbArr[currentIndex].setChecked(false);
                currentIndex = index;
                mrbArr[currentIndex].setChecked(true);
            }
        }
    }

    private void registerCartReceiver() {
        cartReceiver = new CartReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("update_cart");
        filter.addAction("update_user_cart");
        filter.addAction("log_out");
        registerReceiver(cartReceiver, filter);
    }
}
