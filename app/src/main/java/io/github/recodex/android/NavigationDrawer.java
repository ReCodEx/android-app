package io.github.recodex.android;

import android.accounts.Account;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

import javax.inject.Inject;

import io.github.recodex.android.authentication.ReCodExAuthenticator;
import io.github.recodex.android.users.UserWrapper;
import io.github.recodex.android.users.UsersManager;

public class NavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GroupListFragment.OnGroupSelectedListener {

    private final int MANAGE_ACCOUNTS_REQUEST = 666;

    private AlertDialog mAlertDialog;

    @Inject
    UsersManager usersManager;

    /**
     * SyncAdapter status observer. If sync is finished fragment is refreshed... which should cause
     * reloading of data.
     */
    SyncStatusObserver syncObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(final int which) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(getBaseContext().getString(R.string.recodex_log_tag), "Sync status changed");

                    if (usersManager.getCurrentUser() == null) {
                        return;
                    }

                    Account account = usersManager.getCurrentUser().getAccount();
                    if (!ContentResolver.isSyncActive(account, ReCodExAuthenticator.PROVIDER_AUTHORITY) &&
                            !ContentResolver.isSyncPending(account, ReCodExAuthenticator.PROVIDER_AUTHORITY)) {
                        refreshFragment();
                        Log.d(getBaseContext().getString(R.string.recodex_log_tag), "Sync finished, fragment refreshed");
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        // Dagger DI
        ((MyApp) getApplication()).getAppComponent().inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // display group list
        replaceContent(new GroupListFragment());

        // handle current user
        handleAccounts();
    }

    private void fillUserInfo() {
        View header = ((NavigationView) findViewById(R.id.nav_view)).getHeaderView(0);

        UserWrapper user = usersManager.getCurrentUser();

        TextView userName = (TextView) header.findViewById(R.id.userName);
        userName.setText(user.getFullName());

        String avatarUrl = user.getAvatarUrl();
        if (!avatarUrl.isEmpty()) {
            new DownloadImageTask((ImageView) header.findViewById(R.id.userAvatar))
                    .execute(avatarUrl);
        }
    }

    private void handleAccounts() throws SecurityException {
        if (usersManager.getCurrentUser() != null) {
            // we have loaded user, so we do not have to pick one or create one
            fillUserInfo();
            return;
        }

        Account accounts[] = usersManager.getAvailableAccounts();

        if (accounts.length == 0) {
            // we have to login new user
            addNewAccount();
        } else if (accounts.length == 1) {
            // we have only one user, this is it... use him/her
            usersManager.switchCurrentUser(accounts[0]);
            fillUserInfo();
        } else {
            showAccountPicker(accounts);
        }
    }

    private void addNewAccount() {
        usersManager.addAccount(this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    fillUserInfo();
                    refreshFragment();
                } catch (Exception e) {
                    // if not authenticated, close the app
                    Log.d(getBaseContext().getString(R.string.recodex_log_tag), "Add account error: " + e.getMessage());
                    finish();
                }
            }
        });
    }

    private void showAccountPicker(final Account accounts[]) {
        String[] names = new String[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            names[i] = accounts[i].name;
        }

        // Account picker
        mAlertDialog = new AlertDialog.Builder(this).setTitle(R.string.pick_account)
                .setAdapter(new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, names), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserWrapper user = usersManager.switchCurrentUser(accounts[which]);
                showMessage("User '" + user.getFullName() + "' chosen");
                fillUserInfo();
                refreshFragment();
            }
        }).create();
        mAlertDialog.show();
    }

    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void refreshFragment() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_container);
        if (f != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.detach(f).attach(f).commitAllowingStateLoss();
        }
    }

    public void onGroupSelected(String groupId) {
        replaceContent(GroupDetailFragment.newInstance(groupId));
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                RoundedBitmapDrawable dr =
                        RoundedBitmapDrawableFactory.create(getResources(), result);
                dr.setCornerRadius(Math.max(result.getWidth(), result.getHeight()) / 2.0f);
                bmImage.setImageDrawable(dr);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MANAGE_ACCOUNTS_REQUEST:
                handleAccounts();
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_navigation_drawer_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_manage_accounts) {
            Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
            //intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[]{ Constants.accountAuthority });
            startActivityForResult(intent, MANAGE_ACCOUNTS_REQUEST);
        } else if (id == R.id.action_pick_account) {
            showAccountPicker(usersManager.getAvailableAccounts());
        } else if (id == R.id.action_sync_now) {
            if (usersManager.getCurrentUser() != null) {
                usersManager.getCurrentUser().requestSync();
            }
            refreshFragment();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Class<?> activity = null;
        Fragment fragment = null;

        if (id == R.id.action_dashboard) {
            fragment = new GroupListFragment();
        } else if (id == R.id.action_settings) {
            activity = SettingsActivity.class;
        } else if (id == R.id.action_about) {
            activity = AboutActivity.class;
        }

        if (activity != null) {
            Intent intent = new Intent(this, activity);
            startActivity(intent);
        }

        if (fragment != null) {
            replaceContent(fragment);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void replaceContent(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_container, fragment).addToBackStack(null).commit();
    }

    Object handleSyncObserver;
    @Override
    protected void onResume() {
        super.onResume();
        handleSyncObserver = ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE |
                ContentResolver.SYNC_OBSERVER_TYPE_PENDING, syncObserver);
    }

    @Override
    protected void onPause() {
        if (handleSyncObserver != null)
            ContentResolver.removeStatusChangeListener(handleSyncObserver);
        super.onStop();
    }
}
