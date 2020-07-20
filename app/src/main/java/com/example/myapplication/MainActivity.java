package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Scene;
import androidx.transition.TransitionManager;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
//import android.os.FileUtils;
import org.apache.commons.io.FileUtils;  // fix from youtube comment
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_BACKLOG_CODE = 20;
    public static final int EDIT_TODAY_CODE = 30;

    public enum FILES {BACKLOG, TODAY};

    List<String> itemsBacklog;
    List<String> itemsToday;

    Button btnAdd;
    ImageButton btnSlide;
    CheckBox chkPriority;
    EditText etItem;
    RecyclerView rvItems;
    RecyclerView rvToday;
    ItemsAdapter itemsAdapter;
    ItemsAdapter itemsAdapterToday;

    boolean trayDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Simple To Do");

        btnAdd = findViewById(R.id.btnAdd);
        btnSlide = findViewById(R.id.btnSlideCard);
        chkPriority = findViewById(R.id.priorityCheckBox);
        etItem = findViewById(R.id.etItem);
        rvItems = findViewById(R.id.rvItems);
        rvToday = findViewById(R.id.rvToday);

        trayDown = false;

        loadItems();

        final ItemsAdapter.OnLongClickListener onLongClickListenerBacklog = new ItemsAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                // delete the item from the model
                itemsBacklog.remove(position);
                // notify the adapter which position got deleted
                itemsAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), "Item was removed", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        };
        ItemsAdapter.OnClickListener onClickListenerBacklog = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                Log.d("MainActivity", "Single click at position" + position);
                // create new activity
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                // pass the data being edited
                i.putExtra(KEY_ITEM_TEXT, itemsBacklog.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);
                // display the activity
                startActivityForResult(i, EDIT_BACKLOG_CODE);
            }
        };
        final ItemsAdapter.OnLongClickListener onLongClickListenerToday = new ItemsAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                // delete the item from the model
                itemsToday.remove(position);
                // notify the adapter which position got deleted
                itemsAdapterToday.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), "Item was removed", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        };
        final ItemsAdapter.OnClickListener onClickListenerToday = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                Log.d("MainActivity", "Single click at position" + position);
                // create new activity
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                // pass the data being edited
                i.putExtra(KEY_ITEM_TEXT, itemsToday.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);
                // display the activity
                startActivityForResult(i, EDIT_TODAY_CODE);
            }
        };

        itemsAdapter = new ItemsAdapter(itemsBacklog, onLongClickListenerBacklog, onClickListenerBacklog);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        itemsAdapterToday = new ItemsAdapter(itemsToday, onLongClickListenerToday, onClickListenerToday);
        rvToday.setAdapter(itemsAdapterToday);
        rvToday.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String note = etItem.getText().toString();
                RadioButton[] rb = {findViewById( R.id.typeTask), findViewById(R.id.typeEvent), findViewById(R.id.typeNote)};
                String prefix = "";

                // set priority
                if (chkPriority.isChecked()) prefix = "!";
                else prefix = "?";
                chkPriority.setChecked(false);

                // set task type
                if (rb[0].isChecked()) prefix += ".";
                if (rb[1].isChecked()) prefix += "0";
                if (rb[2].isChecked()) prefix += "-";

                // open card if closed
                if(trayDown) slideCard();
                // add to model
                itemsBacklog.add(prefix + note);
                // notify adapter that item has been inserted
                itemsAdapter.notifyItemInserted(itemsBacklog.size()-1);
                // clear edit text
                etItem.setText("");
                Toast.makeText(getApplicationContext(), "Item was added!", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });

        btnSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideCard();
            }
        });

        // unsure about this code for now

        ItemTouchHelper backlogIth = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        itemsToday.add(itemsBacklog.get(viewHolder.getAdapterPosition()));
                        itemsAdapterToday.notifyItemInserted(itemsToday.size()-1);
                        onLongClickListenerBacklog.onItemLongClicked(viewHolder.getAdapterPosition());
                        Toast.makeText(getApplicationContext(), "Item was added!", Toast.LENGTH_SHORT).show();
                        saveItems();
                    }
                }
        );
        backlogIth.attachToRecyclerView(rvItems);

        ItemTouchHelper todayIth = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        itemsBacklog.add(itemsToday.get(viewHolder.getAdapterPosition()));
                        itemsAdapter.notifyItemInserted(itemsBacklog.size()-1);
                        onLongClickListenerToday.onItemLongClicked(viewHolder.getAdapterPosition());
                        Toast.makeText(getApplicationContext(), "Item was added!", Toast.LENGTH_SHORT).show();
                        saveItems();
                    }
                }
        );
        todayIth.attachToRecyclerView(rvToday);
    }

    private void slideCard() {
        // create and run spring animation
        final View img = findViewById(R.id.slidingCard);
        final SpringAnimation anim = new SpringAnimation(img, DynamicAnimation.Y);
        float pixelPerSecond = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
        float endPos = (trayDown) ? 0 : img.getHeight()-100;
        anim.setStartVelocity(pixelPerSecond);
        anim.animateToFinalPosition(endPos);

        trayDown = !trayDown;

        //set the icon
        ImageButton ib = findViewById(R.id.btnSlideCard);
        if(trayDown) ib.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
        else ib.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle action bar button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.btnIndex) {
            slideCard();
        }
        return super.onOptionsItemSelected(item);
    }

    // handle result of edit activity
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && ( requestCode == EDIT_BACKLOG_CODE) || requestCode == EDIT_TODAY_CODE) {
            // retrieve updated text
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);
            // extract original position from pos key
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);

            if (requestCode == EDIT_TODAY_CODE) {
                itemsToday.set(position, itemText);
                itemsAdapterToday.notifyItemChanged(position);
            }
            if (requestCode == EDIT_BACKLOG_CODE) {
                //update the model at right pos w/ new item text
                itemsBacklog.set(position, itemText);
                // notifythe adapter
                itemsAdapter.notifyItemChanged(position);
            }
            // persist the changes
            saveItems();
            Toast.makeText(getApplicationContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }

    private File getDataFile(FILES f) {
        return new File(getFilesDir(), f.toString() + ".txt");
    }

    // this will load items by reading line-by-line in data.txt
    private void loadItems() {
        try {
            itemsBacklog = new ArrayList<>(FileUtils.readLines(getDataFile(FILES.BACKLOG)));
            itemsToday = new ArrayList<>(FileUtils.readLines(getDataFile(FILES.TODAY)));
        } catch (IOException e) {
            Log.e("MainActivity", "Error reading items", e);
            itemsBacklog = new ArrayList<>();
            itemsToday = new ArrayList<>();
        }
    }

    // this saves todos as lines in data.txt
    private void saveItems() {
        try {
            FileUtils.writeLines(getDataFile(FILES.BACKLOG), itemsBacklog);
            FileUtils.writeLines(getDataFile(FILES.TODAY), itemsToday);
        } catch (IOException e) {
            Log.e("MainActivity", "Error writing items", e);
        }

    }
}