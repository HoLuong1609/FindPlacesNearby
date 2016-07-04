package adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.motthoidecode.findplacesnearby.R;

import java.util.List;

import model.Place;
import utils.Util;

/**
 * Created by Ran on 10/05/2016.
 */
public class PlacesSuggestAdapter extends ArrayAdapter<Place>{

    private Context mContext;
    private int mResourceId;
    private List<Place> mPlaces;

    public PlacesSuggestAdapter(Context context, int resource, List<Place> objects) {
        super(context, resource, objects);
        mContext = context;
        mResourceId = resource;
        mPlaces = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(mContext,mResourceId,null);

        TextView tvName = (TextView)convertView.findViewById(R.id.tvName);
        TextView tvAddress = (TextView)convertView.findViewById(R.id.tvAddress);
        TextView tvDistance = (TextView)convertView.findViewById(R.id.tvDistance);
        ImageView ivCategory = (ImageView)convertView.findViewById(R.id.ivCategory);

        Place place = mPlaces.get(position);
        tvName.setText(place.getName());
        tvAddress.setText(place.getAddress());
        tvDistance.setText(Util.formatDistance(place.getDistance()));
        ivCategory.setImageResource(Util.getSuggestionImageResourceID(place.getCategoryId()));

        return convertView;
    }

}
