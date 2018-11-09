

package developer.elkane.udacity.wizflow.async;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.List;

import de.greenrobot.event.EventBus;
import developer.elkane.udacity.wizflow.MainActivity;
import developer.elkane.udacity.wizflow.SettingsActivity;
import developer.elkane.udacity.wizflow.async.bus.NavigationUpdatedEvent;
import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.Category;
import developer.elkane.udacity.wizflow.models.ONStyle;
import developer.elkane.udacity.wizflow.models.adapters.NavDrawerCategoryAdapter;
import developer.elkane.udacity.wizflow.models.views.NonScrollableListView;


public class CategoryMenuTask extends AsyncTask<Void, Void, List<Category>> {

    private final WeakReference<Fragment> mFragmentWeakReference;
    private final MainActivity mainActivity;
    private NonScrollableListView mDrawerCategoriesList;
    private View settingsView;
    private View settingsViewCat;
    private NonScrollableListView mDrawerList;


    public CategoryMenuTask(Fragment mFragment) {
        mFragmentWeakReference = new WeakReference<>(mFragment);
        this.mainActivity = (MainActivity) mFragment.getActivity();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDrawerList = (NonScrollableListView) mainActivity.findViewById(developer.elkane.udacity.wizflow.R.id.drawer_nav_list);
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        settingsView = mainActivity.findViewById(developer.elkane.udacity.wizflow.R.id.settings_view);

        mDrawerCategoriesList = (NonScrollableListView) mainActivity.findViewById(developer.elkane.udacity.wizflow.R.id.drawer_tag_list);
        if (mDrawerCategoriesList.getAdapter() == null && mDrawerCategoriesList.getFooterViewsCount() == 0) {
            settingsViewCat = inflater.inflate(developer.elkane.udacity.wizflow.R.layout.drawer_category_list_footer, null);
            mDrawerCategoriesList.addFooterView(settingsViewCat);
        } else {
            settingsViewCat = mDrawerCategoriesList.getChildAt(mDrawerCategoriesList.getChildCount() - 1);
        }

    }


    @Override
    protected List<Category> doInBackground(Void... params) {
        if (isAlive()) {
            return buildCategoryMenu();
        } else {
            cancel(true);
            return null;
        }
    }


    @Override
    protected void onPostExecute(final List<Category> categories) {
        if (isAlive()) {
            mDrawerCategoriesList.setAdapter(new NavDrawerCategoryAdapter(mainActivity, categories,
                    mainActivity.getNavigationTmp()));
            if (categories.size() == 0) {
                setWidgetVisibility(settingsViewCat, false);
                setWidgetVisibility(settingsView, true);
            } else {
                setWidgetVisibility(settingsViewCat, true);
                setWidgetVisibility(settingsView, false);
            }
            mDrawerCategoriesList.justifyListViewHeightBasedOnChildren();
        }
    }


    private void setWidgetVisibility(View view, boolean visible) {
        if (view != null) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }


    private boolean isAlive() {
        return mFragmentWeakReference.get() != null
                && mFragmentWeakReference.get().isAdded()
                && mFragmentWeakReference.get().getActivity() != null
                && !mFragmentWeakReference.get().getActivity().isFinishing();
    }


    private List<Category> buildCategoryMenu() {
        List<Category> categories = DbHelper.getInstance().getCategories();

        View settings = categories.isEmpty() ? settingsView : settingsViewCat;
        if (settings == null) return categories;
        settings.setOnClickListener(v -> {
            Intent settingsIntent = new Intent(mainActivity, SettingsActivity.class);
            mainActivity.startActivity(settingsIntent);
        });

        mDrawerCategoriesList.setOnItemClickListener((arg0, arg1, position, arg3) -> {

            Object item = mDrawerCategoriesList.getAdapter().getItem(position);
            if (mainActivity.updateNavigation(String.valueOf(((Category) item).getId()))) {
                mDrawerCategoriesList.setItemChecked(position, true);
                if (mDrawerList != null) {
                    mDrawerList.setItemChecked(0, false);
                    EventBus.getDefault().post(new NavigationUpdatedEvent(mDrawerCategoriesList.getItemAtPosition
                            (position)));
                }
            }
        });

        mDrawerCategoriesList.setOnItemLongClickListener((arg0, view, position, arg3) -> {
            if (mDrawerCategoriesList.getAdapter() != null) {
                Object item = mDrawerCategoriesList.getAdapter().getItem(position);
                if (item != null) {
                    mainActivity.editTag((Category) item);
                }
            } else {
                mainActivity.showMessage(developer.elkane.udacity.wizflow.R.string.category_deleted, ONStyle.ALERT);
            }
            return true;
        });

        return categories;
    }

}
