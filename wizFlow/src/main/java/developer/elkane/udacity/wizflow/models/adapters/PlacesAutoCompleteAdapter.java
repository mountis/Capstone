
package developer.elkane.udacity.wizflow.models.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.List;

import developer.elkane.udacity.wizflow.utils.GeocodeHelper;


public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private static final int MIN_CHARS = 7;

    private List<String> resultList;


    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }


    @Override
    public int getCount() {
        return resultList.size();
    }


    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null && constraint.length() > MIN_CHARS) {
                    resultList = GeocodeHelper.autocomplete(constraint.toString());
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }


            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
