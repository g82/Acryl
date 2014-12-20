package com.gamepari.acryl;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gamepari.acryl.data.AcrylDatabase;
import com.gamepari.acryl.data.Playground;
import com.gamepari.acryl.local.DaumLocalApis;
import com.gamepari.acryl.local.LocalModel;
import com.gamepari.acryl.local.ParserUtil;

import net.daum.android.map.coord.MapCoordLatLng;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, MapView.CurrentLocationEventListener {

    //modify this path.
    public static final String TSV_FILE_PATH = "we love acryl/test.tsv";
    public static final String DB_NAME = "acryl.db";

    private ListView listView;
    private MapView mapView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = new MapView(this);
        mapView.setDaumMapApiKey(getString(R.string.daum_maps_api_key));
        mapView.setHDMapTileEnabled(true);
        mapView.setCurrentLocationEventListener(this);

        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
        mapView.setShowCurrentLocationMarker(true);
        mapView.setCurrentLocationRadius(20);


        FrameLayout fl_map = (FrameLayout) findViewById(R.id.fl_map);
        fl_map.addView(mapView);

        listView = (ListView) findViewById(R.id.lv_list);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        DatabaseTask databaseTask = new DatabaseTask();
        databaseTask.execute(DB_NAME);

    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Playground pObject = (Playground) adapterView.getAdapter().getItem(i);
        new PlaceTask().execute(pObject.getFullAddress());
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        //long click
        return false;
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        Log.d("mapview", "onupdate.");

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
        Log.d("mapview", "onupdate.");
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
        Log.d("mapview", "onfailed.");

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
        Log.d("mapview", "oncancle.");

    }

    private class PlayGroundAdapter extends BaseAdapter {

        private List<Playground> playgroundList;

        private PlayGroundAdapter(List<Playground> playgrounds) {
            this.playgroundList = playgrounds;
        }

        @Override
        public int getCount() {
            return playgroundList.size();
        }

        @Override
        public Playground getItem(int i) {
            return playgroundList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return playgroundList.get(i).hashCode();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            ViewHolder holder = null;

            if (view == null) {
                view = View.inflate(MainActivity.this, R.layout.cell_playground, null);
                holder = new ViewHolder();
                holder.tvTag = (TextView) view.findViewById(R.id.tv_tagid);
                holder.tvShortAddr = (TextView) view.findViewById(R.id.tv_address_short);
                holder.tvFullAddr = (TextView) view.findViewById(R.id.tv_address_full);
                holder.tvInstName = (TextView) view.findViewById(R.id.tv_inst);
                view.setTag(holder);
            }
            else {
                holder = (ViewHolder) view.getTag();
            }

            Playground pObject = getItem(i);

            holder.tvTag.setText(String.valueOf(pObject.getTag_num()));
            holder.tvShortAddr.setText(pObject.getAddress1() + " / " + pObject.getAddress2());
            holder.tvFullAddr.setText(pObject.getFullAddress());
            holder.tvInstName.setText(pObject.getInstName());

            return view;
        }

        private class ViewHolder {
            public TextView tvTag, tvShortAddr, tvFullAddr, tvInstName;
        }

    }

    private class PlaceTask extends AsyncTask<String, Integer, LocalModel> {

        @Override
        protected LocalModel doInBackground(String... strings) {

            try {
                InputStream inputStream = ParserUtil.downloadURL(
                        DaumLocalApis.makeQueryURL(getString(R.string.daum_local_api_key),strings[0]));

                String jsonString = ParserUtil.makeStringFromStream(inputStream);

                JSONObject jsonObject = new JSONObject(jsonString);

                JSONObject channel = jsonObject.getJSONObject("channel");

                int status = channel.getInt("result");

                if (status == 1) {

                    JSONArray item = jsonObject.getJSONObject("channel").getJSONArray("item");
                    JSONObject items = item.getJSONObject(0);

                    double lat = items.getDouble("lat");
                    double lng = items.getDouble("lng");
                    String title = items.getString("title");

                    LocalModel localModel = new LocalModel(lat,lng,title);
                    return localModel;
                }
                else return null;

            } catch (IOException e) {
                return null;
            } catch (JSONException e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(LocalModel o) {
            super.onPostExecute(o);
            if (o != null) {

                mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(o.getLat(), o.getLng()), 0, true);

                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(o.getTitle());
                marker.setTag(0);
                marker.setMapPoint(MapPoint.mapPointWithGeoCoord(o.getLat(), o.getLng()));
                marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

                mapView.addPOIItem(marker);
            }
        }
    }

    private class DatabaseTask extends AsyncTask<String, Integer, List<Playground>> {

        private String TAG = this.getClass().getSimpleName();

        @Override
        protected List<Playground> doInBackground(String... strings) {

            List<Playground> listResult = null;

            AcrylDatabase acrylDatabase = new AcrylDatabase(MainActivity.this, strings[0], null, 1);

            listResult = acrylDatabase.getListPlayGround();

            return listResult;
        }

        @Override
        protected void onPostExecute(List<Playground> playgrounds) {
            super.onPostExecute(playgrounds);

            if (playgrounds != null) {
                PlayGroundAdapter playGroundAdapter = new PlayGroundAdapter(playgrounds);
                listView.setAdapter(playGroundAdapter);
            }

        }
    }

}
