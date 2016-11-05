package ru.pro2410.tutu;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ArrayList<String> arlistItemDrawer = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView lVitemDrawer;
    private ActionBar actionBar;
    private ScheduleFragment shFragment;
    private AboutFragment abFragment;
    private FragmentManager fragmentManager;
    private Button btnFromStation;
    private Button btnToStation;
    private Button btnDate;
    private View fragmentContainer;

    private static final int REQUEST_CODE_FROM = 1;
    private static final int REQUEST_CODE_TO = 2;
    private static final String TAG_Schedule = "shFragment";
    private static final String TAG_About = "abFragment";

    private static final int FRAGMENT_SCHEDULE = 0;
    private static final int FRAGMENT_ABOUT = 1;

    public class AboutFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // настраиваем и устанавливаем макет для фрагмента
            String versionApp = "";
            try {
                versionApp = getString(R.string.versionStr)+
                        getPackageManager().getPackageInfo(getPackageName(),0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            View v = inflater.inflate(R.layout.layout_fragment_about_app, container, false);
            ((TextView)v.findViewById(R.id.idVersion)).setText(versionApp);
            return v;
        }
    }
    public class ScheduleFragment extends Fragment  {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //используем уже надутый макет
            return fragmentContainer;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.titleActionBar);
        actionBar.setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.idDrawerLayout);
        lVitemDrawer = (ListView) findViewById(R.id.navListDrawer);

        DrawerListAdapter dLAdapterMenu = new DrawerListAdapter(this,arlistItemDrawer);
        lVitemDrawer.setAdapter(dLAdapterMenu);
        lVitemDrawer.setItemChecked(getResources().getInteger(R.integer.zero),true);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.llContainer);
        fragmentContainer = getLayoutInflater().inflate(R.layout.layout_schedule,frameLayout,false);
        btnFromStation = (Button) fragmentContainer.findViewById(R.id.idBtnFromStation);
        btnToStation = (Button) fragmentContainer.findViewById(R.id.idBtnToStation);
        btnDate = (Button) fragmentContainer.findViewById(R.id.idBtnDate);

        btnDate.setOnClickListener(this);
        btnToStation.setOnClickListener(this);
        btnFromStation.setOnClickListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(mDrawerToggle);

        lVitemDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                lVitemDrawer.setItemChecked(position,true);
                switch (position){
                    case FRAGMENT_SCHEDULE:
                        ScheduleFragment fragment1 = (ScheduleFragment) fragmentManager.findFragmentByTag(TAG_Schedule);
                        if (fragment1 == null){
                            fragmentManager.beginTransaction().replace(R.id.llContainer,shFragment,TAG_Schedule).commit();
                        }
                        break;
                    case FRAGMENT_ABOUT:
                        AboutFragment fragment2 = (AboutFragment) fragmentManager.findFragmentByTag(TAG_About);
                        if (fragment2 == null){
                            fragmentManager.beginTransaction().replace(R.id.llContainer,abFragment,TAG_About).commit();
                        }
                        break;
                }
                actionBar.setTitle(arlistItemDrawer.get(position));
                drawerLayout.closeDrawer(lVitemDrawer);
            }
        });
        shFragment = new ScheduleFragment();
        abFragment = new AboutFragment();
        fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction().add(R.id.llContainer,shFragment,TAG_Schedule).commit();

        initialArray();
    }
    @Override
    public void onClick(View v) {
        //обработчик для кнопок первого фрагмента
        Intent intent = new Intent(MainActivity.this,SearchStationActivity.class);
        switch (v.getId()){
            case R.id.idBtnFromStation:
                intent.putExtra(getString(R.string.fromOrTo),getString(R.string.citiesFrom));
                startActivityForResult(intent,REQUEST_CODE_FROM);
                break;
            case R.id.idBtnToStation:
                intent.putExtra(getString(R.string.fromOrTo),getString(R.string.citiesTo));
                startActivityForResult(intent,REQUEST_CODE_TO);
                break;
            case R.id.idBtnDate:
                DateDialog dateDialog = new DateDialog(btnDate);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                dateDialog.show(ft,getString(R.string.DatePicker));
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_FROM:
                if (resultCode == RESULT_OK) {
                    String station = data.getStringExtra(getString(R.string.station));
                    btnFromStation.setText(station);
                }else if (resultCode == RESULT_CANCELED){
                    Toast.makeText(MainActivity.this, getString(R.string.NoStation), Toast.LENGTH_SHORT).show();
                    btnFromStation.setText(getString(R.string.hintForFromStation));
                }
                break;
            case REQUEST_CODE_TO:
                if (resultCode == RESULT_OK) {
                    String station = data.getStringExtra(getString(R.string.station));
                    btnToStation.setText(station);
                }else if (resultCode == RESULT_CANCELED){
                    Toast.makeText(MainActivity.this, getString(R.string.NoStation), Toast.LENGTH_SHORT).show();
                    btnToStation.setText(getString(R.string.hintForToStation));
                }
                break;
        }
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //синхранизируем кнопку открытия меню для смены ее внешнего вида при открытии и закрытии
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //обрабатываем кнопку открытия меню
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initialArray() {
        arlistItemDrawer.add(getString(R.string.ScheduleItem1));
        arlistItemDrawer.add(getString(R.string.AboutAppItem2));
    }

    private class DrawerListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<String> mNavItems;

        public DrawerListAdapter(Context context, ArrayList<String> navItems) {
            // адптер для бокового меню
            mContext = context;
            mNavItems = navItems;
        }

        @Override
        public int getCount() {return mNavItems.size();}

        @Override
        public Object getItem(int position) {return mNavItems.get(position);}

        @Override
        public long getItemId(int position) {return 0;}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                view = inflater.inflate(R.layout.drawer_item, null);
            }
            else {view = convertView;}

            TextView textTitle = (TextView) view.findViewById(R.id.idTitleItem);
            textTitle.setText(mNavItems.get(position));
            return view;
        }
    }
}
