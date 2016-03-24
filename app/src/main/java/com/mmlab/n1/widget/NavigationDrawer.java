package com.mmlab.n1.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mmlab.n1.FriendActivity;
import com.mmlab.n1.MainActivity;
import com.mmlab.n1.MyApplication;
import com.mmlab.n1.MyFavoriteActivity;
import com.mmlab.n1.R;
import com.mmlab.n1.SignInActivity;
import com.mmlab.n1.model.DEHUser;
import com.mmlab.n1.model.Friend;
import com.mmlab.n1.model.MyFavorite;
import com.mmlab.n1.model.User;

import io.realm.Realm;

/**
 * Created by waynewei on 2015/11/23.
 */
public class NavigationDrawer {

    private final int LOG_OUT = 0;
    private final Activity activity;
    private final Toolbar toolbar;
    private Drawer drawer;
    private AccountHeader header;
    private MyApplication globalVariable;
    private Realm realm;
    private ProfileDrawerItem profile;

    public NavigationDrawer(Activity activity, Toolbar toolbar) {
        globalVariable = (MyApplication) activity.getApplicationContext();
        realm = Realm.getInstance(activity);
        this.activity = activity;
        this.toolbar = toolbar;
    }

    public void createDefaultDrawer() {

        drawer = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_home).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_sign_in).withIcon(FontAwesome.Icon.faw_user).withIdentifier(1).withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_settings).withIcon(FontAwesome.Icon.faw_cog).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_help).withIcon(FontAwesome.Icon.faw_question).withEnabled(false).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(FontAwesome.Icon.faw_bullhorn).withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        Intent intent = null;

                        if (drawerItem != null) {
                            if (drawerItem.getIdentifier() == 1) {
                                if (globalVariable.checkInternet())
                                    intent = new Intent(activity, SignInActivity.class);
                                else
                                    globalVariable.noticeInternet(activity, view);
                            }
                            if (intent != null) {
                                activity.startActivity(intent);
                            }

                        }

                        return false;
                    }
                })
                .addStickyDrawerItems(
                        new SecondaryDrawerItem().withName(R.string.drawer_item_settings).withIcon(FontAwesome.Icon.faw_cog).withIdentifier(10)
                )
                .withSelectedItem(-1)
                .build();

        globalVariable.setDrawer(drawer);

    }

    public void createUserDrawer(String name, String email, String photo, String cover) {


        if (photo != null) {
            profile = new ProfileDrawerItem().withName(name).withEmail(email).withIcon(photo);
        } else {
            profile = new ProfileDrawerItem().withName(name).withEmail(email).withIcon(activity.getResources().getDrawable(R.drawable.profile));
        }

        header = new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        profile,
                        new ProfileSettingDrawerItem().withName(activity.getString(R.string.logout_title)).withIcon(FontAwesome.Icon.faw_sign_out).withIdentifier(LOG_OUT)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                                                 @Override
                                                 public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {

                                                     if (profile instanceof IDrawerItem && profile.getIdentifier() == LOG_OUT) {

                                                         new MaterialDialog.Builder(activity)
                                                                 .iconRes(R.mipmap.ic_launcher)
                                                                 .limitIconToDefaultSize() // limits the displayed icon size to 48dp
                                                                 .title(R.string.logout_title)
                                                                 .content(R.string.logout_detail)
                                                                 .positiveText(R.string.confirm)
                                                                 .negativeText(R.string.cancel)
                                                                 .callback(new MaterialDialog.ButtonCallback() {
                                                                     @Override
                                                                     public void onPositive(MaterialDialog dialog) {
                                                                         realm.beginTransaction();
                                                                         realm.clear(User.class);
                                                                         realm.clear(DEHUser.class);
                                                                         realm.clear(Friend.class);
                                                                         realm.clear(MyFavorite.class);
                                                                         realm.commitTransaction();
                                                                         activity.finish();
                                                                         if (activity.getClass() == MainActivity.class)
                                                                             activity.startActivity(activity.getIntent());
                                                                         else {
                                                                             Intent intent = new Intent(activity, MainActivity.class);
                                                                             activity.startActivity(intent);
                                                                         }

                                                                     }
                                                                 })
                                                                 .show();
                                                         LoginManager.getInstance().logOut();
                                                     }
                                                     return false;
                                                 }
                                             }
                )
                .build();

        drawer = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withAccountHeader(header)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_home)
                                .withIcon(FontAwesome.Icon.faw_home)
                                .withIdentifier(0),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_friends)
                                .withIcon(FontAwesome.Icon.faw_users)
                                .withIdentifier(1),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_favorite)
                                .withIcon(FontAwesome.Icon.faw_star)
                                .withIdentifier(2),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_settings)
                                .withIcon(FontAwesome.Icon.faw_cog),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_help)
                                .withIcon(FontAwesome.Icon.faw_question),
                        new SecondaryDrawerItem()
                                .withName(R.string.drawer_item_contact)
                                .withIcon(FontAwesome.Icon.faw_bullhorn)
                )
                .withOnDrawerItemClickListener(
                        new Drawer.OnDrawerItemClickListener() {

                            public boolean onItemClick(View view, int position, final IDrawerItem drawerItem) {

                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        if (drawerItem != null) {
                                            Intent intent = null;
                                            if (drawerItem.getIdentifier() == 0 && activity.getClass() != MainActivity.class) {
                                                intent = new Intent(activity, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            }
                                            if (drawerItem.getIdentifier() == 1 && activity.getClass() != FriendActivity.class) {

                                                intent = new Intent(activity, FriendActivity.class);
                                            }
                                            if (drawerItem.getIdentifier() == 2 && activity.getClass() != MyFavoriteActivity.class) {
                                                intent = new Intent(activity, MyFavoriteActivity.class);
                                            }
                                            if (intent != null) {
                                                activity.startActivity(intent);
                                            }
                                            if (intent != null && activity.getClass() != MainActivity.class) {
                                                activity.finish();
                                            }
                                        }
                                    }
                                }, 500);

                                return false;
                            }
                        }

                )
                .withSelectedItem(-1)
                .build();

        if (cover != null)
            withNewHeaderBackground(cover);


        globalVariable.setDrawer(drawer);
        globalVariable.setHeader(header);
    }


    public void withNewHeaderBackground(String url) {
        Glide.with(activity).load(url).into(header.getHeaderBackgroundView());
    }
}
