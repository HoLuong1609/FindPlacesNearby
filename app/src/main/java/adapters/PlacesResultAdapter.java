package adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.motthoidecode.findplacesnearby.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import model.Place;
import utils.Util;

/**
 * Created by Ran on 10/05/2016.
 */
public class PlacesResultAdapter extends ArrayAdapter<Place> {

    private Context mContext;
    private int mResourceId;
    private List<Place> mPlaces;

    public PlacesResultAdapter(Context context, int resource, List<Place> objects) {
        super(context, resource, objects);
        mContext = context;
        mResourceId = resource;
        mPlaces = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(mContext, mResourceId, null);

        TextView tvName = (TextView) convertView.findViewById(R.id.tvPlaceName);
        TextView tvAddress = (TextView) convertView.findViewById(R.id.tvPlaceAddress);
        TextView tvCategory = (TextView) convertView.findViewById(R.id.tvPlaceCategory);
        TextView tvDistance = (TextView) convertView.findViewById(R.id.tvPlaceDistance);
        ImageView ivCategory = (ImageView) convertView.findViewById(R.id.ivCategory);
        ImageView ivPlace = (ImageView) convertView.findViewById(R.id.ivPlace);

        Place place = mPlaces.get(position);
        if (position == 0)
            tvName.setTextColor(Color.parseColor("#F7911E")); // orange
        tvName.setText(place.getName());
        tvAddress.setText(place.getAddress());
        tvDistance.setText(Util.formatDistance(place.getDistance()));
        Picasso.with(mContext).load(place.getImageUrl()).into(ivPlace);
        tvCategory.setText(Util.getCategoryName(place.getCategoryId()));
        ivCategory.setImageResource(Util.getImageResourceID(place.getCategoryId()));

        return convertView;
    }

}
