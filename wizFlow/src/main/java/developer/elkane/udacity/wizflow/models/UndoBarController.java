

package developer.elkane.udacity.wizflow.models;

import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.Locale;

import it.feio.android.checklistview.utils.AlphaManager;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;


public class UndoBarController {

    private View mBarView;
    private TextView mMessageView;
    private ViewPropertyAnimator mBarAnimator;

    private UndoListener mUndoListener;

    private Parcelable mUndoToken;
    private CharSequence mUndoMessage;
    private Button mButtonView;
    private boolean isVisible;


    public UndoBarController(View undoBarView, UndoListener undoListener) {
        mBarView = undoBarView;
        mBarAnimator = animate(mBarView);
        mUndoListener = undoListener;

        mMessageView = (TextView) mBarView.findViewById(developer.elkane.udacity.wizflow.R.id.undobar_message);

        mButtonView = (Button) mBarView.findViewById(developer.elkane.udacity.wizflow.R.id.undobar_button);
        mButtonView.setText(mButtonView.getText().toString().toUpperCase(Locale.getDefault()));
        mButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideUndoBar(false);
                mUndoListener.onUndo(mUndoToken);
            }
        });

        hideUndoBar(false);
    }

    public void showUndoBar(boolean immediate, CharSequence message, Parcelable undoToken) {
        mUndoToken = undoToken;
        mUndoMessage = message;
        mMessageView.setText(mUndoMessage);


        mBarView.setVisibility(View.VISIBLE);
        if (immediate) {
            AlphaManager.setAlpha(mBarView, 1);
        } else {
            mBarAnimator.cancel();
            mBarAnimator
                    .alpha(1)
                    .setDuration(
                            mBarView.getResources()
                                    .getInteger(android.R.integer.config_shortAnimTime))
                    .setListener(null);
        }
        isVisible = true;
    }

    public void hideUndoBar(boolean immediate) {
        if (immediate) {
            mBarView.setVisibility(View.GONE);
            AlphaManager.setAlpha(mBarView, 0);
            mUndoMessage = null;
            mUndoToken = null;

        } else {
            mBarAnimator.cancel();
            mBarAnimator
                    .alpha(0)
                    .setDuration(mBarView.getResources()
                            .getInteger(android.R.integer.config_shortAnimTime))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mBarView.setVisibility(View.GONE);
                            mUndoMessage = null;
                            mUndoToken = null;
                        }
                    });
        }
        isVisible = false;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence("undo_message", mUndoMessage);
        outState.putParcelable("undo_token", mUndoToken);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mUndoMessage = savedInstanceState.getCharSequence("undo_message");
            mUndoToken = savedInstanceState.getParcelable("undo_token");

            if (mUndoToken != null || !TextUtils.isEmpty(mUndoMessage)) {
                showUndoBar(true, mUndoMessage, mUndoToken);
            }
        }
    }

    public boolean isVisible() {
        return isVisible;
    }


    public interface UndoListener {

        void onUndo(Parcelable token);
    }

}
