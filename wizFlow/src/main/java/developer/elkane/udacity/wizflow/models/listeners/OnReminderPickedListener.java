

package developer.elkane.udacity.wizflow.models.listeners;


public interface OnReminderPickedListener {

    void onReminderPicked(long reminder);

    void onRecurrenceReminderPicked(String recurrenceRule);
}
