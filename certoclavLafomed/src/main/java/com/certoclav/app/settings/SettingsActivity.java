package com.certoclav.app.settings;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.certoclav.app.R;
import com.certoclav.app.model.CertoclavNavigationbarClean;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class SettingsActivity extends FragmentActivity implements ItemListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity); //ItemListFragment, sont nix
        CertoclavNavigationbarClean navigationbar = new CertoclavNavigationbarClean(this);
        navigationbar.setHeadText(getString(R.string.settings));


        //((ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list)).setActivateOnItemClick(true);


        // TODO: If exposing deep links into your app, handle intents here.
    }


    /**
     * Callback method from {@link ItemListFragment.Callbacks} indicating that
     * the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(long id) { //ItemListFragment ruft diese Funktion auf, falls ein Listenelement angeklickt wurde und �bergibt die id des Listenelements.

        // In two-pane mode, show the detail view in this activity by
        // adding or replacing the detail fragment using a
        // fragment transaction.
        Log.e("String id:", Integer.toString((int) id));

        if (id == 0) {
            getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, new UserEditFragment()).commit();
        } else if (id == 1) {
            getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, new SettingsNetworkFragment()).commit();
        } else if (id == 2) {
            getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, new SettingsSterilisationFragment()).commit();
        } else if (id == 3) {
            getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, new SettingsDeviceFragment()).commit();
        } else if (id == 4) {
            getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, new SettingsLanguageFragment()).commit();
        } else if (id == 5) {
            getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, new SettingsConditionFragment()).commit();
        } else if (id == 6) {
            getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, new CalibrateFragment()).commit();
        } else if (id == 7) {
            getSupportFragmentManager().beginTransaction().replace(R.id.item_detail_container, new SettingsServiceFragment()).commit();
        }


    }
}
