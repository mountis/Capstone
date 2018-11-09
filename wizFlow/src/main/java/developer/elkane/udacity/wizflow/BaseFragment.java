

package developer.elkane.udacity.wizflow;

import android.support.v4.app.Fragment;

import com.squareup.leakcanary.RefWatcher;


public class BaseFragment extends Fragment {


    @Override
    public void onStart() {
        super.onStart();
        ((WizFlow) getActivity().getApplication()).getAnalyticsHelper().trackScreenView(getClass().getName());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = WizFlow.getRefWatcher();
        refWatcher.watch(this);
    }

}
