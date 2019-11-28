package com.certoclav.app.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.certoclav.app.R;
import com.certoclav.app.activities.CertoclavSuperActivity;
import com.certoclav.app.listener.NavigationbarListener;
import com.certoclav.app.model.Autoclave;
import com.certoclav.app.model.AutoclaveState;
import com.certoclav.app.model.CertoclavNavigationbarClean;
import com.certoclav.app.util.LockoutManager;
import es.dmoral.toasty.Toasty;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link } representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link }.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class SettingsActivity extends CertoclavSuperActivity implements ItemListFragment.Callbacks, NavigationbarListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean isAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isAdmin = getIntent().getBooleanExtra("isAdmin", false)
                || (Autoclave.getInstance().getState() != AutoclaveState.LOCKED && Autoclave.getInstance().getUser().isAdmin());

        if (!isAdmin && LockoutManager.getInstance().isLockedAll()) {
            Toasty.warning(this,
                    getString(R.string.these_settings_are_locked_by_the_admin),
                    Toast.LENGTH_SHORT,
                    true).show();
            finish();
            return;
        }

        setContentView(R.layout.settings_activity); //ItemListFragment, sont nix
        CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.showButtonBack();
        navigationbar.setHeadText(getString(R.string.settings));


        //((ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list)).setActivateOnItemClick(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);
    }

    /**
     * Callback method from {@link ItemListFragment.Callbacks} indicating that
     * the item with the given ID was selected.
     */
    private Fragment currentFragment;

    @Override
    public void onItemSelected(long id, Fragment fragment) { //ItemListFragment ruft diese Funktion auf, falls ein Listenelement angeklickt wurde und �bergibt die id des Listenelements.
        currentFragment = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, fragment).commit();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    public void onClickNavigationbarButton(int buttonId) {

    }
}
