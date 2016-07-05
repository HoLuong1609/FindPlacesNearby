package com.motthoidecode.findplacesnearby;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import adapters.MyPlaceAdapter;
import databases.PlaceDbHelper;
import model.Place;

public class MyPlacesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_OPEN_ACTIVITY_ADD_PLACE = 100;
    public static final int RESULT_ADD_PLACE_OK = 200;

    private ListView lvMyPlaces;
    private List<Place> myPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_places);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addPlaceIntent = new Intent(MyPlacesActivity.this,AddPlaceActivity.class);
                startActivityForResult(addPlaceIntent, REQUEST_CODE_OPEN_ACTIVITY_ADD_PLACE);
            }
        });

        lvMyPlaces = (ListView)findViewById(R.id.lvMyPlaces);
        lvMyPlaces.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Place place = myPlaces.get(position);
                AlertDialog.Builder b = new AlertDialog.Builder(MyPlacesActivity.this);
                b.setTitle(R.string.delete).setMessage(getString(R.string.delete_message_my_place) + " - " + place.getName() + " ?")
                        .setIcon(android.R.drawable.ic_delete)
                        .setNegativeButton(R.string.no,null)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PlaceDbHelper myPlaceDbHelper = new PlaceDbHelper(MyPlacesActivity.this,null);
                                if(myPlaceDbHelper.deleteMyPlace(place.getId()) > 0){
                                    Toast.makeText(MyPlacesActivity.this,"Đã xóa địa điểm",Toast.LENGTH_LONG).show();
                                    refreshDataListView();
                                }else
                                    Toast.makeText(MyPlacesActivity.this,"Lỗi",Toast.LENGTH_LONG).show();
                            }
                        });
                b.create().show();

                return true;
            }
        });
        refreshDataListView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_OPEN_ACTIVITY_ADD_PLACE && resultCode == RESULT_ADD_PLACE_OK)
            refreshDataListView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshDataListView() {
        PlaceDbHelper placeDbHelper = new PlaceDbHelper(this,null);
        myPlaces = placeDbHelper.getListMyPlaces();
        MyPlaceAdapter adapter = new MyPlaceAdapter(this,R.layout.my_place_row,myPlaces);
        lvMyPlaces.setAdapter(adapter);
    }

}
