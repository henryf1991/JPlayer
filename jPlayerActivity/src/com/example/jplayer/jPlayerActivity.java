package com.example.jplayer;

import java.util.ArrayList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.jplayer.adapter.NavDrawerListAdapter;
import com.example.jplayer.fragments.PlayerFragment;
import com.example.jplayer.model.NavDrawerItem;
import com.example.jplayer.utils.MusicPreferences;
import com.example.jplayer.utils.MusicRetrieverTask;
import com.sherlock.navigationdrawer.compat.SherlockActionBarDrawerToggle;



public class jPlayerActivity extends SherlockFragmentActivity{
	
	static final String TAG="jPlayerActivity";
	
	public DrawerLayout drawerlayout;
	public ListView drawerList;
	public ActionBar actionbar;
	public SherlockActionBarDrawerToggle drawerToggle;
	public  ArrayList<NavDrawerItem> navDrawerItems; 
	public String[] drawerTitles;
	public int[] drawerKeys;
	public int[] drawerIcons;
	public PlayerFragment playerFragment;
	public NavDrawerListAdapter navAdapter;
	public FragmentManager fragmentManager;
	public MusicPreferences prefs;
	public int nav_pos=3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		prefs=new MusicPreferences(jPlayerActivity.this);
		nav_pos=prefs.getNavPos();
		nav_pos=nav_pos==-1?3:nav_pos;
		
		//Setting the ActionBar
		actionbar=getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeButtonEnabled(true);
			
		//Configuring the left drawer
		drawerlayout=(DrawerLayout)findViewById(R.id.drawer_layout);
		drawerlayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
		drawerlayout.setDrawerListener(new JPlayerDrawerListener());
		
		//left drawer listview
		drawerList=(ListView)findViewById(R.id.left_drawer);
		navAdapter = new NavDrawerListAdapter(this, getNavDrawerItems());
		drawerList.setAdapter(navAdapter);
		drawerList.setOnItemClickListener(new DrawerItemClickListener());
		setActionBarTitle(nav_pos);
		
		//Drawer Toggle
		drawerToggle = new SherlockActionBarDrawerToggle(this, drawerlayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);
		drawerToggle.syncState();
		
		navAdapter.setSelectedPosition(nav_pos);
		fragmentManager=getSupportFragmentManager();
		playerFragment=(PlayerFragment)fragmentManager.findFragmentByTag(PlayerFragment.TAG);
		if(playerFragment==null)
		{
			playerFragment=PlayerFragment.newInstance();			
			fragmentManager.beginTransaction().add(R.id.content_frame, playerFragment,PlayerFragment.TAG).commit();
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d("LIFECYCLE", "resume called");
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		Log.d("LIFECYCLE", "Calling saved instance");
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("LIFECYCLE", "pause called");
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		prefs.setNavPos(navAdapter.getSelectedPosition());
		Log.d("LIFECYCLE", "destroy called");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if(drawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}
	
	
	
	public ArrayList<NavDrawerItem> getNavDrawerItems() {
		navDrawerItems=new ArrayList<NavDrawerItem>();
		drawerTitles=getResources().getStringArray(R.array.player_menu_title);
		drawerKeys=getResources().getIntArray(R.array.player_menu_key);
		drawerIcons=new int[]{R.drawable.ic_action_collection,R.drawable.ic_action_artist_light,R.drawable.ic_action_album_light,R.drawable.ic_action_station,R.drawable.ic_action_saveplaylist,R.drawable.ic_action_playlist_light};
		for (int i=0;i<drawerTitles.length && i<drawerKeys.length;i++) {
			navDrawerItems.add(new NavDrawerItem(drawerTitles[i], drawerIcons[i],drawerKeys[i]));
		}
		return navDrawerItems;
	}
	
	public void setActionBarTitle(int pos)
	{
		actionbar.setTitle(navDrawerItems.get(pos).getTitle());
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {	
			setActionBarTitle(position);
			navAdapter.setSelectedPosition(position);
			new MusicRetrieverTask(jPlayerActivity.this, playerFragment).execute(navDrawerItems.get(position).getMusicKey());			
			drawerlayout.closeDrawer(drawerList);
		}
	}
	
	private class JPlayerDrawerListener implements DrawerLayout.DrawerListener
	{

		@Override
		public void onDrawerClosed(View drawerView) {
			// TODO Auto-generated method stub
			drawerToggle.onDrawerClosed(drawerView);
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			// TODO Auto-generated method stub
			drawerToggle.onDrawerOpened(drawerView);
		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {
			// TODO Auto-generated method stub
			drawerToggle.onDrawerSlide(drawerView, slideOffset);
		}

		@Override
		public void onDrawerStateChanged(int newState) {
			// TODO Auto-generated method stub
			drawerToggle.onDrawerStateChanged(newState);
		}
		
		
	}
	
}
