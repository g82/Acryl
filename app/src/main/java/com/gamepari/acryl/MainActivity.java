package com.gamepari.acryl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gamepari.acryl.data.AcrylDatabase;
import com.gamepari.acryl.data.PlaygroundModel;
import com.gamepari.acryl.local.DaumLocalApis;
import com.gamepari.acryl.local.LocalModel;
import com.gamepari.acryl.local.ParserUtil;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity implements
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
        MapView.CurrentLocationEventListener, MapView.MapViewEventListener, MapView.POIItemEventListener, View.OnClickListener {

    private ListView listView;
    private MapView mapView;
    private HashMap<Integer, MapPOIItem> markerHashMap;
    private AcrylDatabase acrylDatabase;

    private PlayGroundAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = new MapView(this);
        mapView.setDaumMapApiKey(getString(R.string.daum_maps_api_key));
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);
        mapView.setHDMapTileEnabled(true);
        mapView.setShowCurrentLocationMarker(true);
        mapView.setCurrentLocationRadius(25);

        markerHashMap = new HashMap<>();

        FrameLayout fl_map = (FrameLayout) findViewById(R.id.fl_map);
        fl_map.addView(mapView);

        listView = (ListView) findViewById(R.id.lv_list);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        findViewById(R.id.btn_view_all).setOnClickListener(this);
        findViewById(R.id.btn_view_done).setOnClickListener(this);
        findViewById(R.id.btn_view_yet).setOnClickListener(this);
        findViewById(R.id.btn_refresh).setOnClickListener(this);

        // check location settings on
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // when gps disabled
            showDialogTurnOnGPS();

        }

        new ReadDatabaseTask().execute(AcrylDatabase.DB_NAME);
    }

    private void turnOnTrackingMode(MapView mapView) {
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        mapView.setCurrentLocationEventListener(this);
    }

    private void showAllPins(List<PlaygroundModel> playgroundModels) {
        for (PlaygroundModel pObject : playgroundModels) {

            LocalModel o = pObject.getLocalModel();

            if (!pObject.isChecked() && o != null && o.getLat() != 0.0) {

                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(o.getTagId() + " / " + o.getTitle());
                marker.setMapPoint(MapPoint.mapPointWithGeoCoord(o.getLat(), o.getLng()));
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomImageResourceId(R.drawable.red_pin_big);
                marker.setCustomImageAutoscale(true);
                marker.setShowDisclosureButtonOnCalloutBalloon(false);
                marker.setUserObject(pObject);
                markerHashMap.put(o.getTagId(), marker);
                mapView.addPOIItem(marker);
            } else continue;

        }
    }


    private void showDialogTurnOnGPS() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.location_disabled)
                .setMessage(R.string.location_disalbed_text)
                .setNegativeButton(R.string.ignore, null)
                .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_refresh:
                turnOnTrackingMode(mapView);
                break;

            case R.id.btn_view_all:
                adapter.setViewMode(PlayGroundAdapter.VIEW_ALL);
                break;

            case R.id.btn_view_done:
                adapter.setViewMode(PlayGroundAdapter.VIEW_ONLY_DONE);
                break;

            case R.id.btn_view_yet:
                adapter.setViewMode(PlayGroundAdapter.VIEW_ONLY_YET);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        PlaygroundModel pObject = adapter.getItem(i);

        if (pObject.isChecked()) return;

        MapPOIItem marker = markerHashMap.get(pObject.getTag_num());
        if (marker == null) {
            new PlaceTask().execute(pObject);
        } else {
            mapView.setMapCenterPoint(marker.getMapPoint(), true);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

        PlaygroundModel pObject = adapter.getItem(i);

        if (pObject.isChecked()) {

            pObject.setChecked(0);

        } else {
            //set complete mark.
            MapPOIItem poiItem = markerHashMap.get(pObject.getTag_num());

            if (poiItem != null) mapView.removePOIItem(poiItem);
            markerHashMap.remove(pObject.getTag_num());

            pObject.setChecked(1);
        }

        new UpdateDataBaseTask().execute(pObject);

        return true;
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        turnOnTrackingMode(mapView);
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    /*

    =============== DAUM MAPVIEW CALLBACK ================

     */

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        mapView.setMapCenterPointAndZoomLevel(mapPoint, 0, true);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        mapView.setCurrentLocationEventListener(null);
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {


    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        PlaygroundModel pObject = (PlaygroundModel) mapPOIItem.getUserObject();
        int index = adapter.getPosition(pObject);
        listView.smoothScrollToPositionFromTop(index, 0);
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    private class PlayGroundAdapter extends BaseAdapter {

        public static final int VIEW_ALL = 0;
        public static final int VIEW_ONLY_DONE = 1;
        public static final int VIEW_ONLY_YET = 2;
        private List<PlaygroundModel> playgroundModelList;
        private int viewMode;

        public PlayGroundAdapter(List<PlaygroundModel> playgroundModels, int viewMode) {
            this.playgroundModelList = playgroundModels;
            this.viewMode = viewMode;
        }

        public int getPosition(PlaygroundModel playgroundModel) {
            int index = playgroundModelList.indexOf(playgroundModel);
            return index;
        }

        public void setViewMode(int viewMode) {
            this.viewMode = viewMode;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return playgroundModelList.size();
        }

        @Override
        public PlaygroundModel getItem(int i) {
            return playgroundModelList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return playgroundModelList.get(i).hashCode();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            ViewHolder holder = null;

            if (view == null) {
                view = View.inflate(MainActivity.this, R.layout.cell_playground, null);
                holder = new ViewHolder();
                holder.llContainer = (LinearLayout) view.findViewById(R.id.ll_container);
                holder.tvTag = (TextView) view.findViewById(R.id.tv_tagid);
                holder.tvShortAddr = (TextView) view.findViewById(R.id.tv_address_short);
                holder.tvFullAddr = (TextView) view.findViewById(R.id.tv_address_full);
                holder.tvInstName = (TextView) view.findViewById(R.id.tv_inst);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            PlaygroundModel pObject = getItem(i);

            switch (viewMode) {
                case VIEW_ALL:
                    if (pObject.isChecked()) {
                        holder.llContainer.setVisibility(View.VISIBLE);
                        holder.llContainer.setBackgroundColor(Color.rgb(228, 93, 79));
                    } else {
                        holder.llContainer.setVisibility(View.VISIBLE);
                        holder.llContainer.setBackgroundColor(Color.TRANSPARENT);
                    }

                    holder.tvTag.setText(String.valueOf(pObject.getTag_num()));
                    holder.tvShortAddr.setText(pObject.getAddress1() + " / " + pObject.getAddress2());
                    holder.tvFullAddr.setText(pObject.getFullAddress());
                    holder.tvInstName.setText(pObject.getInstName());
                    break;

                case VIEW_ONLY_DONE:

                    if (pObject.isChecked()) {
                        holder.llContainer.setVisibility(View.VISIBLE);
                        holder.llContainer.setBackgroundColor(Color.rgb(228, 93, 79));
                        holder.tvTag.setText(String.valueOf(pObject.getTag_num()));
                        holder.tvShortAddr.setText(pObject.getAddress1() + " / " + pObject.getAddress2());
                        holder.tvFullAddr.setText(pObject.getFullAddress());
                        holder.tvInstName.setText(pObject.getInstName());
                    } else {
                        holder.llContainer.setVisibility(View.GONE);
                    }

                    break;

                case VIEW_ONLY_YET:

                    if (pObject.isChecked()) {
                        holder.llContainer.setVisibility(View.GONE);
                    } else {
                        holder.llContainer.setVisibility(View.VISIBLE);
                        holder.llContainer.setBackgroundColor(Color.TRANSPARENT);
                        holder.tvTag.setText(String.valueOf(pObject.getTag_num()));
                        holder.tvShortAddr.setText(pObject.getAddress1() + " / " + pObject.getAddress2());
                        holder.tvFullAddr.setText(pObject.getFullAddress());
                        holder.tvInstName.setText(pObject.getInstName());
                    }

                    break;
            }

            return view;
        }

        private class ViewHolder {
            public TextView tvTag, tvShortAddr, tvFullAddr, tvInstName;
            public LinearLayout llContainer;
        }

    }

    private class PlaceTask extends AsyncTask<PlaygroundModel, Integer, PlaygroundModel> {

        @Override
        protected PlaygroundModel doInBackground(PlaygroundModel... pObjects) {

            try {
                InputStream inputStream = ParserUtil.downloadURL(
                        DaumLocalApis.makeQueryURL(getString(R.string.daum_local_api_key), pObjects[0].getFullAddress()));

                String jsonString = ParserUtil.makeStringFromStream(inputStream);

                JSONObject jsonObject = new JSONObject(jsonString);

                JSONObject channel = jsonObject.getJSONObject("channel");

                int status = channel.getInt("result");

                if (status >= 1) {

                    if (status != 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "부정확할수있음", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    JSONArray item = jsonObject.getJSONObject("channel").getJSONArray("item");
                    JSONObject items = item.getJSONObject(0);

                    double lat = items.getDouble("lat");
                    double lng = items.getDouble("lng");
                    String title = items.getString("title");

                    LocalModel localModel = new LocalModel(lat, lng, title, pObjects[0].getTag_num());

                    pObjects[0].setLocalModel(localModel);
                    return pObjects[0];
                } else return null;

            } catch (IOException e) {
                return null;
            } catch (JSONException e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(PlaygroundModel pObject) {

            super.onPostExecute(pObject);

            if (pObject != null && pObject.getLocalModel() != null) {

                LocalModel o = pObject.getLocalModel();

                mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(o.getLat(), o.getLng()), 0, true);

                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(o.getTagId() + " / " + o.getTitle());
                marker.setMapPoint(MapPoint.mapPointWithGeoCoord(o.getLat(), o.getLng()));
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomImageResourceId(R.drawable.red_pin_big);
                marker.setCustomImageAutoscale(true);
                marker.setShowAnimationType(MapPOIItem.ShowAnimationType.DropFromHeaven);
                marker.setShowDisclosureButtonOnCalloutBalloon(false);
                marker.setUserObject(pObject);
                markerHashMap.put(o.getTagId(), marker);
                mapView.addPOIItem(marker);

                new UpdateDataBaseTask().execute(pObject);

            } else {
                Toast.makeText(MainActivity.this, "실패", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class UpdateDataBaseTask extends AsyncTask<PlaygroundModel, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(PlaygroundModel... params) {
            PlaygroundModel model = params[0];

            boolean isUpdated = acrylDatabase.updatePlayGround(model);

            return isUpdated;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (aBoolean) adapter.notifyDataSetChanged();
        }
    }

    private class ReadDatabaseTask extends AsyncTask<String, Integer, List<PlaygroundModel>> {

        private String TAG = this.getClass().getSimpleName();

        @Override
        protected List<PlaygroundModel> doInBackground(String... strings) {

            List<PlaygroundModel> listResult = null;

            acrylDatabase = new AcrylDatabase(MainActivity.this, strings[0], null, 1);

            listResult = acrylDatabase.getListPlayGround();

            return listResult;
        }

        @Override
        protected void onPostExecute(List<PlaygroundModel> playgroundModels) {
            super.onPostExecute(playgroundModels);

            if (playgroundModels != null) {
                adapter = new PlayGroundAdapter(playgroundModels, PlayGroundAdapter.VIEW_ALL);
                listView.setAdapter(adapter);
                showAllPins(playgroundModels);
            }

        }
    }

    // -- DAUM MAPVIEW CALLBACKS END

}
