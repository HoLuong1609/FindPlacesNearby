package adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.motthoidecode.findplacesnearby.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import model.Place;
import utils.Util;

/**
 * Created by Ran on 10/05/2016.
 */
public class MyPlaceAdapter extends ArrayAdapter<Place>{

    private Context mContext;
    private int mResourceId;
    private List<Place> mPlaces;

    public MyPlaceAdapter(Context context, int resource, List<Place> objects) {
        super(context, resource, objects);
        mContext = context;
        mResourceId = resource;
        mPlaces = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(mContext,mResourceId,null);

        TextView tvName = (TextView)convertView.findViewById(R.id.tvMyPlaceName);
        TextView tvAddress = (TextView)convertView.findViewById(R.id.tvMyPlaceAddress);
        TextView tvCategory = (TextView)convertView.findViewById(R.id.tvMyPlaceCategory);
        TextView tvTime = (TextView)convertView.findViewById(R.id.tvMyPlaceTime);
        ImageView ivCategory = (ImageView)convertView.findViewById(R.id.ivMyPlaceCategory);

        Place place = mPlaces.get(position);

        tvName.setText(place.getName());
        tvAddress.setText(place.getAddress());
        tvTime.setText(getCurrentTime());
        ivCategory.setImageResource(Util.getImageResourceID(place.getCategoryId()));
        tvCategory.setText(Util.getCategoryName(place.getCategoryId()));

        return convertView;
    }

    private String getCurrentTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(c.getTime());
    }
}
