// Generated code from Butter Knife. Do not modify!
package developer.elkane.udacity.wizflow.async;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import developer.elkane.udacity.wizflow.R;
import developer.elkane.udacity.wizflow.models.views.NonScrollableListView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MainMenuTask_ViewBinding implements Unbinder {
  private MainMenuTask target;

  @UiThread
  public MainMenuTask_ViewBinding(MainMenuTask target, View source) {
    this.target = target;

    target.mDrawerList = Utils.findRequiredViewAsType(source, R.id.drawer_nav_list, "field 'mDrawerList'", NonScrollableListView.class);
    target.mDrawerCategoriesList = Utils.findRequiredViewAsType(source, R.id.drawer_tag_list, "field 'mDrawerCategoriesList'", NonScrollableListView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    MainMenuTask target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mDrawerList = null;
    target.mDrawerCategoriesList = null;
  }
}
