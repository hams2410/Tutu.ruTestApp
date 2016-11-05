package ru.pro2410.tutu;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public class SearchStationActivity extends AppCompatActivity {
    private String direction;
    private String strJson;
    private static final int POPULATE_COUNTRY = 1;
    private static final int POPULATE_CITY = 2;
    private static final int PARSE_COUNTRY = 4;
    private static final int PARSE_CITY = 5;
    private static final int PARSE_STATION = 6;
    private static final String EMPTY_ITEM = "";
    private Spinner spCountry;
    private Spinner spCity;
    private AdapterStation adapterStation;
    private ArrayList<StationModel> arlStation;
    private ArrayList<StationModel> arlStationDisplayed;
    private ListView lViewStation;
    private JSONArray arrayCity;
    private String strCountry = "";
    private String strCity = "";
    private SearchView edSearchStations;

    private View vLayoutStationParameters;

    private static final int DIALOG_PARAMETERS = 7;
    private static final int DIALOG_ABOUT_STATION = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_for_search_station);

        // отправление или прибытие
        Bundle extras = getIntent().getExtras();
        if (extras != null) direction = extras.getString(getString(R.string.fromOrTo));
        // парсим json
        new ParseTask().execute();

        vLayoutStationParameters = getLayoutInflater().inflate(R.layout.dialog_layout_select_station_parameters,null);
        spCountry = (Spinner) vLayoutStationParameters.findViewById(R.id.spCountry);
        spCity = (Spinner) vLayoutStationParameters.findViewById(R.id.spCity);
        lViewStation = (ListView) findViewById(R.id.idlViewStation);
        View viewAction = getLayoutInflater().inflate(R.layout.actionbar_for_search_station,null);

        edSearchStations = (SearchView) viewAction.findViewById(R.id.idSearch);

        edSearchStations.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //фильтурем станции
                adapterStation.getFilter().filter(newText);
                return true;
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(viewAction);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent resultIntent = new Intent();
                setResult(RESULT_CANCELED, resultIntent);
                finish();
                return true;
            case R.id.action_name:
                createDialog(DIALOG_PARAMETERS,getResources().getInteger(R.integer.zero));
        }
        return super.onOptionsItemSelected(item);
    }
    private void createDialog(int id, final int position){
        //диалоги
        AlertDialog.Builder dialog = new AlertDialog.Builder(SearchStationActivity.this);
        switch (id){
            case DIALOG_PARAMETERS:
                if (vLayoutStationParameters.getParent()!=null) ((ViewGroup) vLayoutStationParameters.getParent()).removeView(vLayoutStationParameters);
                dialog.setView(vLayoutStationParameters);
                dialog.setTitle(R.string.titleDialogParametersStation);
                dialog.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;
            case DIALOG_ABOUT_STATION:

                View viewDialogAbout = getLayoutInflater().inflate(R.layout.dialog_about_station,null);
                dialog.setView(viewDialogAbout);
                dialog.setTitle(getString(R.string.aboutStationTitle));
                dialog.setPositiveButton("Выбрать",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("station", arlStationDisplayed.get(position).station);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeButton("Отмена",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                TextView nStation = (TextView) viewDialogAbout.findViewById(R.id.idStationName);
                TextView nCountry = (TextView) viewDialogAbout.findViewById(R.id.idCountryName);
                TextView nCity = (TextView) viewDialogAbout.findViewById(R.id.idCityName);
                TextView nRegion = (TextView) viewDialogAbout.findViewById(R.id.idRegionName);

                StationModel stationModel = arlStationDisplayed.get(position);
                nStation.setText(stationModel.station);
                nCountry.setText(stationModel.country);
                nCity.setText(stationModel.city);
                nRegion.setText(stationModel.region);
                dialog.show();

                break;
        }
    }

    private void populateSpinner(int id){
        //заполняем списки стран и городов(только при выборе страны)
        ArrayList<String> list ;
        ArrayAdapter<String> adapter;
        switch (id) {
            case POPULATE_COUNTRY:
                //парсим страны
                list = parseName(PARSE_COUNTRY);
                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, list);

                spCountry.setAdapter(adapter);
                spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //красим выбранный пункт
                        setColorSelectedItem(parent);
                        //сохраняем текст выбранной странны
                        strCountry = spCountry.getSelectedItem().toString();
                        //обновляем станции при выборе страны
                        populateStation();
                        //фильтруем если в поиске присутствует текст
                        adapterStation.getFilter().filter(edSearchStations.getQuery());
                        if (position>0) {
                            //если страна выбрана - заполняем список городов
                            spCity.setClickable(true);
                            populateSpinner(POPULATE_CITY);
                        }else{
                            //в противном случае обнуляем список городов
                            spCity.setClickable(false);
                            spCity.setAdapter(null);
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
                break;
            case POPULATE_CITY:
                //парсим города
                list = parseName(PARSE_CITY);
                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, list);

                spCity.setAdapter(adapter);
                spCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //красим выбранный пункт
                        setColorSelectedItem(parent);
                        //сохр текст города
                        strCity = spCity.getSelectedItem().toString();
                        //обновляем станции
                        populateStation();
                        //фильтруем
                        adapterStation.getFilter().filter(edSearchStations.getQuery());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
                break;
        }
    }
    private void setColorSelectedItem(AdapterView<?> parent){
        TextView selectedText = (TextView) parent.getChildAt(getResources().getInteger(R.integer.zero));
        if (selectedText != null) {
            selectedText.setTextColor(Color.BLACK);
        }
    }
    private void populateStation(){
        //заполняем общий список станций в зависимости от города и страны
        arlStation = parseName(PARSE_STATION);
        // отдаем ссылку переменной список которой будет отображаться
        arlStationDisplayed = arlStation;
        adapterStation = new AdapterStation();
        lViewStation.setAdapter(adapterStation);
    }

    private void initialJson(){
        //получаем массив городов
        try {
            JSONObject dataJsonObj = new JSONObject(strJson);
            arrayCity = dataJsonObj.getJSONArray(direction);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ArrayList parseName(int id){
        // получаем названия стран, городов, а также обьекты станций с данными и сортируем
        ArrayList arrayList = new ArrayList<>();

        switch (id){
            case PARSE_COUNTRY:
                //парсим и добавляем уникальные страны
                HashSet<String> hashset = new HashSet<>();
                try {
                    for (int i = 0; i < arrayCity.length(); i++) {
                        JSONObject city = arrayCity.getJSONObject(i);
                        hashset.add(city.getString(getString(R.string.countryTitle)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                arrayList.addAll(hashset);
                break;
            case PARSE_CITY:
                //добавляем город только если соответствует выбранной стране
                try {
                    for (int i = 0; i < arrayCity.length(); i++) {
                        JSONObject city = arrayCity.getJSONObject(i);
                        if (city.getString(getString(R.string.countryTitle)).equals(strCountry)){
                            arrayList.add(city.getString(getString(R.string.cityTitle)));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case PARSE_STATION:
                // добавляем лиюо все станции, либо только по стране, либо по городу и стране
                try {
                    for (int i = 0; i < arrayCity.length(); i++) {
                        JSONObject city = arrayCity.getJSONObject(i);
                        if(strCountry.equals(EMPTY_ITEM)){
                            addStationToArray(arrayList,city);

                        }else if (strCity.equals(EMPTY_ITEM)&&city.getString(getString(R.string.countryTitle)).equals(strCountry)){
                            addStationToArray(arrayList,city);

                        }else if(city.getString(getString(R.string.cityTitle)).equals(strCity)){
                            addStationToArray(arrayList,city);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }
        //сортируем список
        sortArray(arrayList,id);
        return arrayList;
    }
    private void sortArray(ArrayList arrayList, int id){
        //сортируем список
        switch (id){
            case PARSE_STATION:
                Collections.sort(arrayList, new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        String str1= ((StationModel)o1).station.toLowerCase();
                        String str2= ((StationModel)o2).station.toLowerCase();
                        return str1.compareTo(str2);
                    }
                });
                break;
            default:
                Collections.sort(arrayList);
                arrayList.add(0,"");
        }
    }
    private void addStationToArray(ArrayList<StationModel> arrayList, JSONObject city){
        //получаем массив станций города, добавляем данные каждой станции в список
        try {
            JSONArray stations = city.getJSONArray(getString(R.string.stations));
            for (int j = 0; j < stations.length(); j++) {
                JSONObject station = stations.getJSONObject(j);

                StationModel stationModel = new StationModel(
                        station.getString(getString(R.string.stationTitle)),
                        station.getString(getString(R.string.countryTitle)),
                        station.getString(getString(R.string.cityTitle)),
                        station.getString(getString(R.string.regionTitle)));

                arrayList.add(stationModel);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private class ParseTask extends AsyncTask<Void, Void, Void> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected Void doInBackground(Void... params) {
            // получаем json строку с внешнего ресурса
            try {
                URL url = new URL(getString(R.string.jsonAddress));

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            strJson = resultJson;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //получаем массив городов
            initialJson();
            //заполняем список станциями
            populateStation();
            //заполняем список стран
            populateSpinner(POPULATE_COUNTRY);
        }
    }
    private class AdapterStation extends BaseAdapter implements Filterable{
        View viewStation;

        @Override
        public int getCount() {return arlStationDisplayed.size();}

        @Override
        public Object getItem(int position) {
            return arlStationDisplayed.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                viewStation = inflater.inflate(R.layout.drawer_item, null);
            }
            else {
                viewStation = convertView;
            }
            TextView textView = (TextView) viewStation.findViewById(R.id.idTitleItem);
            textView.setText(arlStationDisplayed.get(position).station);
            textView.setTextColor(Color.BLACK);
            // открываем диалог с детальной информацией о станции
            viewStation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   createDialog(DIALOG_ABOUT_STATION,position);
                }
            });
            return viewStation;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();       // содержит результаты работы фильтрации
                    ArrayList<StationModel> filteredArray = new ArrayList<>();

                    if (constraint == null || constraint.length() == 0) { // если строка поиска пустая - не фильтруем
                        results.count = arlStation.size();
                        results.values = arlStation;
                    }else {
                        //иначе получаем строку и сравниваем ее со строкой станции общего списка
                        constraint = constraint.toString().toLowerCase();
                        for (int i = 0; i < arlStation.size(); i++) {
                            String data = arlStation.get(i).station;
                            if (data.toLowerCase().contains(constraint.toString())) {
                                filteredArray.add(arlStation.get(i));
                            }
                        }
                        // устанавливаем отфильтрованные результаты и возвращаем
                        results.count = filteredArray.size();
                        results.values = filteredArray;
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    arlStationDisplayed = (ArrayList<StationModel>) results.values;
                    notifyDataSetChanged();
                }
            };
            return filter;
        }
    }
}
