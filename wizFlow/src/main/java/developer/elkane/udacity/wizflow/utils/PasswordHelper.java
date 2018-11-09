

package developer.elkane.udacity.wizflow.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import de.greenrobot.event.EventBus;
import developer.elkane.udacity.wizflow.WizFlow;
import developer.elkane.udacity.wizflow.async.bus.PasswordRemovedEvent;
import developer.elkane.udacity.wizflow.db.DbHelper;
import developer.elkane.udacity.wizflow.models.PasswordValidator;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class PasswordHelper {


    public static void requestPassword(final Activity mActivity, final PasswordValidator mPasswordValidator) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        final View v = inflater.inflate(developer.elkane.udacity.wizflow.R.layout.password_request_dialog_layout, null);
        final EditText passwordEditText = (EditText) v.findViewById(developer.elkane.udacity.wizflow.R.id.password_request);

        MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                .autoDismiss(false)
                .title(developer.elkane.udacity.wizflow.R.string.insert_security_password)
                .customView(v, false)
                .positiveText(developer.elkane.udacity.wizflow.R.string.ok)
                .positiveColorRes(developer.elkane.udacity.wizflow.R.color.colorPrimary)
                .onPositive((dialog12, which) -> {
                    String oldPassword = mActivity.getSharedPreferences(Constants.PREFS_NAME, Context
                            .MODE_MULTI_PROCESS)
                            .getString(Constants.PREF_PASSWORD, "");
                    String password = passwordEditText.getText().toString();
                    boolean result = Security.md5(password).equals(oldPassword);

                    if (result) {
                        KeyboardUtils.hideKeyboard(passwordEditText);
                        dialog12.dismiss();
                        mPasswordValidator.onPasswordValidated(true);
                    } else {
                        passwordEditText.setError(mActivity.getString(developer.elkane.udacity.wizflow.R.string.wrong_password));
                    }
                })
                .neutralText(mActivity.getResources().getString(developer.elkane.udacity.wizflow.R.string.password_forgot))
                .onNeutral((dialog13, which) -> {
                    PasswordHelper.resetPassword(mActivity);
                    mPasswordValidator.onPasswordValidated(false);
                    dialog13.dismiss();
                })
                .build();

        dialog.setOnCancelListener(dialog1 -> {
            KeyboardUtils.hideKeyboard(passwordEditText);
            dialog1.dismiss();
            mPasswordValidator.onPasswordValidated(false);
        });

        passwordEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.getActionButton(DialogAction.POSITIVE).callOnClick();
                return true;
            }
            return false;
        });

        dialog.show();

        new Handler().postDelayed(() -> KeyboardUtils.showKeyboard(passwordEditText), 100);
    }


    public static void resetPassword(final Activity mActivity) {
        View layout = mActivity.getLayoutInflater().inflate(developer.elkane.udacity.wizflow.R.layout.password_reset_dialog_layout, null);
        final EditText answerEditText = (EditText) layout.findViewById(developer.elkane.udacity.wizflow.R.id.reset_password_answer);

        MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                .title(WizFlow.getSharedPreferences().getString(Constants.PREF_PASSWORD_QUESTION, ""))
                .customView(layout, false)
                .autoDismiss(false)
                .contentColorRes(developer.elkane.udacity.wizflow.R.color.text_color)
                .positiveText(developer.elkane.udacity.wizflow.R.string.ok)
                .onPositive((dialogElement, which) -> {
                    String oldAnswer = WizFlow.getSharedPreferences().getString(Constants.PREF_PASSWORD_ANSWER, "");
                    String answer1 = answerEditText.getText().toString();
                    boolean result = Security.md5(answer1).equals(oldAnswer);
                    if (result) {
                        dialogElement.dismiss();
                        removePassword();
                    } else {
                        answerEditText.setError(mActivity.getString(developer.elkane.udacity.wizflow.R.string.wrong_answer));
                    }
                }).build();
        dialog.show();

        answerEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.getActionButton(DialogAction.POSITIVE).callOnClick();
                return true;
            }
            return false;
        });

        new Handler().postDelayed(() -> KeyboardUtils.showKeyboard(answerEditText), 100);
    }


    public static void removePassword() {
        Observable
                .from(DbHelper.getInstance().getNotesWithLock(true))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(note -> {
                    note.setLocked(false);
                    DbHelper.getInstance().updateNote(note, false);
                })
                .doOnCompleted(() -> {
                    EventBus.getDefault().post(new PasswordRemovedEvent());
                    WizFlow.getSharedPreferences().edit()
                            .remove(Constants.PREF_PASSWORD)
                            .remove(Constants.PREF_PASSWORD_QUESTION)
                            .remove(Constants.PREF_PASSWORD_ANSWER)
                            .remove("settings_password_access")
                            .apply();
                })
                .subscribe();
    }
}
