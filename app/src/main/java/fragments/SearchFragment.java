package fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.motthoidecode.findplacesnearby.R;

/**
 * Created by Administrator on 7/2/2016.
 */
public class SearchFragment extends Fragment{

    private View.OnClickListener mOnCategoryClickListener;

    public SearchFragment(){}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOnCategoryClickListener = (View.OnClickListener) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.search_fragment, container, false);

        rootView.findViewById(R.id.ivRestaurant).setOnClickListener(mOnCategoryClickListener);
        rootView.findViewById(R.id.ivCafe).setOnClickListener(mOnCategoryClickListener);
        rootView.findViewById(R.id.ivATM).setOnClickListener(mOnCategoryClickListener);
        rootView.findViewById(R.id.ivPetrol).setOnClickListener(mOnCategoryClickListener);
        rootView.findViewById(R.id.ivEducation).setOnClickListener(mOnCategoryClickListener);

        return rootView;
    }
}
