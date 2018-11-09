

package developer.elkane.udacity.wizflow.async;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import developer.elkane.udacity.wizflow.MainActivity;
import developer.elkane.udacity.wizflow.async.bus.NavigationUpdatedEvent;
import developer.elkane.udacity.wizflow.models.NavigationItem;
import developer.elkane.udacity.wizflow.models.adapters.NavDrawerAdapter;
import developer.elkane.udacity.wizflow.models.misc.DynamicNavigationLookupTable;
import developer.elkane.udacity.wizflow.models.views.NonScrollableListView;
import developer.elkane.udacity.wizflow.utils.Constants;
import developer.elkane.udacity.wizflow.utils.Navigation;


public class MainMenuTask extends AsyncTask<Void, Void, List<NavigationItem>> {

    private final WeakReference<Fragment> mFragmentWeakReference;
    private final MainActivity mainActivity;
    @BindView(developer.elkane.udacity.wizflow.R.id.drawer_nav_list)
    NonScrollableListView mDrawerList;
    @BindView(developer.elkane.udacity.wizflow.R.id.drawer_tag_list)
    NonScrollableListView mDrawerCategoriesList;


    public MainMenuTask(Fragment mFragment) {
        mFragmentWeakReference = new WeakReference<>(mFragment);
        this.mainActivity = (MainActivity) mFragment.getActivity();
        ButterKnife.bind(this, mFragment.getView());
    }


    @Override
    protected List<NavigationItem> doInBackground(Void... params) {
        return buildMainMenu();
    }


    @Override
    protected void onPostExecute(final List<NavigationItem> items) {
        if (isAlive()) {
            mDrawerList.setAdapter(new NavDrawerAdapter(mainActivity, items));
            mDrawerList.setOnItemClickListener((arg0, arg1, position, arg3) -> {
                String navigation = mFragmentWeakReference.get().getResources().getStringArray(developer.elkane.udacity.wizflow.R.array
                        .navigation_list_codes)[items.get(position).getArrayIndex()];
                if (mainActivity.updateNavigation(navigation)) {
                    mDrawerList.setItemChecked(position, true);
                    if (mDrawerCategoriesList != null)
                        mDrawerCategoriesList.setItemChecked(0, false);
                    mainActivity.getIntent().setAction(Intent.ACTION_MAIN);
                    EventBus.getDefault().post(new NavigationUpdatedEvent(mDrawerList.getItemAtPosition(position)));
                }
            });
            mDrawerList.justifyListViewHeightBasedOnChildren();
        }
    }


    private boolean isAlive() {
        return mFragmentWeakReference.get() != null
                && mFragmentWeakReference.get().isAdded()
                && mFragmentWeakReference.get().getActivity() != null
                && !mFragmentWeakReference.get().getActivity().isFinishing();
    }


    private List<NavigationItem> buildMainMenu() {
        if (!isAlive()) {
            return new ArrayList<>();
        }

        String[] mNavigationArray = mainActivity.getResources().getStringArray(developer.elkane.udacity.wizflow.R.array.navigation_list);
        TypedArray mNavigationIconsArray = mainActivity.getResources().obtainTypedArray(developer.elkane.udacity.wizflow.R.array.navigation_list_icons);
        TypedArray mNavigationIconsSelectedArray = mainActivity.getResources().obtainTypedArray(developer.elkane.udacity.wizflow.R.array
                .navigation_list_icons_selected);

        final List<NavigationItem> items = new ArrayList<>();
        for (int i = 0; i < mNavigationArray.length; i++) {
            if (!checkSkippableItem(i)) {
                NavigationItem item = new NavigationItem(i, mNavigationArray[i], mNavigationIconsArray.getResourceId(i,
                        0), mNavigationIconsSelectedArray.getResourceId(i, 0));
                items.add(item);
            }
        }
        return items;
    }


    private boolean checkSkippableItem(int i) {
        boolean skippable = false;
        SharedPreferences prefs = mainActivity.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
        boolean dynamicMenu = prefs.getBoolean(Constants.PREF_DYNAMIC_MENU, true);
        DynamicNavigationLookupTable dynamicNavigationLookupTable = null;
        if (dynamicMenu) {
            dynamicNavigationLookupTable = DynamicNavigationLookupTable.getInstance();
        }
        switch (i) {
            case Navigation.REMINDERS:
                if (dynamicMenu && dynamicNavigationLookupTable.getReminders() == 0)
                    skippable = true;
                break;
            case Navigation.UNCATEGORIZED:
                boolean showUncategorized = prefs.getBoolean(Constants.PREF_SHOW_UNCATEGORIZED, false);
                if (!showUncategorized || (dynamicMenu && dynamicNavigationLookupTable.getUncategorized() == 0))
                    skippable = true;
                break;
            case Navigation.ARCHIVE:
                if (dynamicMenu && dynamicNavigationLookupTable.getArchived() == 0)
                    skippable = true;
                break;
            case Navigation.TRASH:
                if (dynamicMenu && dynamicNavigationLookupTable.getTrashed() == 0)
                    skippable = true;
                break;
            default:
                Log.e(Constants.TAG, "Wrong element choosen: " + i);
        }
        return skippable;
    }

}
