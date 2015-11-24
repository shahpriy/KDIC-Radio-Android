package edu.grinnell.kdic;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Stack;

import edu.grinnell.kdic.schedule.GetSchedule;
import edu.grinnell.kdic.schedule.ScheduleFragment;
import edu.grinnell.kdic.visualizer.VisualizeFragment;

public class MainActivity extends AppCompatActivity {

    VisualizeFragment visualizeFragment;
    ScheduleFragment scheduleFragment;
    Toolbar navigationToolbar;
    Toolbar playbackToolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Stack<Integer> backStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigation();

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            scheduleFragment = new ScheduleFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            scheduleFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, scheduleFragment, ScheduleFragment.TAG)
                    .commit();
            backStack.add(R.id.schedule);

        }

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, 0);
        if (sharedPreferences.getBoolean(Constants.FIRST_RUN, true)) {
            GetSchedule getSchedule = new GetSchedule(MainActivity.this);
            getSchedule.execute();
            sharedPreferences.edit().putBoolean(Constants.FIRST_RUN, false).apply();
        }

        visualizeFragment = new VisualizeFragment();

        playbackToolbar = (Toolbar) findViewById(R.id.playback_toolbar);
        View.OnClickListener onToggleVisualizeFragment = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backStack.peek() != R.id.visualizer) {
                    showVisualizeFragment();
                    backStack.add(R.id.visualizer);
                } else {
                    hideVisualizeFragment();
                    backStack.pop();
                }
                updateNavigationView();
            }
        };
        playbackToolbar.setNavigationOnClickListener(onToggleVisualizeFragment);
        playbackToolbar.setOnClickListener(onToggleVisualizeFragment);

    }

    private void setupNavigation() {
        // get toolbar and nav drawer
        navigationToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // set toolbar as actionbar
        setSupportActionBar(navigationToolbar);

        // initialize navigation drawer
        navigationToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        navigationToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // set up backstack
        backStack = new Stack<>();

        // set onclick listeners to navigation menu items
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (backStack.peek() != menuItem.getItemId()) {
                    if (backStack.peek() == R.id.visualizer) {
                        hideVisualizeFragment();
                        backStack.pop();
                    }
                    backStack.push(menuItem.getItemId());

                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    switch (menuItem.getItemId()) {
                        case R.id.schedule:
                            ft.replace(R.id.fragment, scheduleFragment, ScheduleFragment.TAG)
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        case R.id.visualizer:
                            showVisualizeFragment();
                            break;
                        case R.id.favorites:
                            ft.replace(R.id.fragment, new AboutFragment(), AboutFragment.TAG)
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        case R.id.blog:
                            ft.replace(R.id.fragment, new BlogWebViewFragment(), BlogWebViewFragment.TAG)
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        case R.id.about:
                            ft.replace(R.id.fragment, new AboutFragment(), AboutFragment.TAG)
                                    .addToBackStack(null)
                                    .commit();
                            break;
                        default:
                            break;
                    }
                }

                // close the drawer after something is clicked
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    public void updateNavigationView() {
        navigationView.setCheckedItem(backStack.peek());
    }


    public void hideVisualizeFragment() {
        // hide visualize fragment
        playbackToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_up_white_24dp);
        getSupportFragmentManager().popBackStack();
    }

    public void showVisualizeFragment() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_bottom)
                .replace(R.id.fragment, visualizeFragment, VisualizeFragment.TAG)
                .addToBackStack(null)
                .commit();
        playbackToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_down_white_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.update_schedule) {
            GetSchedule getSchedule = new GetSchedule(MainActivity.this);
            getSchedule.execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (backStack.size() > 1) {
            int menuId = backStack.pop();
            if (menuId == R.id.visualizer) {
                playbackToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_up_white_24dp);
            }
            updateNavigationView();
        }
        super.onBackPressed();
    }
}
