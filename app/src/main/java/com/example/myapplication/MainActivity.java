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
import android.content.Context;
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

        // define fields
        btnAdd = findViewById(R.id.btnAdd);
        btnSlide = findViewById(R.id.btnSlideCard);
        chkPriority = findViewById(R.id.priorityCheckBox);
        etItem = findViewById(R.id.etItem);
        rvItems = findViewById(R.id.rvItems);
        rvToday = findViewById(R.id.rvToday);
        trayDown = false;

        loadItems();

        // define click listeners for recycler views
        final ItemsAdapter.OnLongClickListener onLongClickListenerBacklog = new ItemsAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                removeItem(position, itemsBacklog, itemsAdapter,true);
            }
        };
       final ItemsAdapter.OnClickListener onClickListenerBacklog = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                editItem(position, itemsBacklog, EDIT_BACKLOG_CODE);
            }
        };
        final ItemsAdapter.OnLongClickListener onLongClickListenerToday = new ItemsAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                removeItem(position, itemsToday, itemsAdapterToday, true);
            }
        };
        final ItemsAdapter.OnClickListener onClickListenerToday = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                editItem(position, itemsToday, EDIT_TODAY_CODE);
            }
        };

        // set up item adapters
        itemsAdapter = setupItemsAdapter(this, itemsBacklog, rvItems, itemsAdapter, onClickListenerBacklog, onLongClickListenerBacklog);
        itemsAdapterToday = setupItemsAdapter(this, itemsToday, rvToday, itemsAdapter, onClickListenerToday, onLongClickListenerToday);

        // set click listeners for misc. buttons
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemFromHotbar(true); }
        });
        btnSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideCard();
            }
        });

       // set up touch helpers
        setupTouchHelper(itemsBacklog, itemsToday, rvItems, itemsAdapter, itemsAdapterToday);
        setupTouchHelper(itemsToday, itemsBacklog, rvToday, itemsAdapterToday, itemsAdapter);
    }



    //  NON-OVERRIDE METHODS

    // this will load items by reading line-by-line
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

    private File getDataFile(FILES f) {
        return new File(getFilesDir(), f.toString() + ".txt");
    }

    // add an item to the list
    private void addItem(List<String> listTo, ItemsAdapter ia, String s, boolean toast) {
        listTo.add(s);
        ia.notifyItemInserted(listTo.size()-1);
        saveItems();
        if (toast) Toast.makeText(getApplicationContext(), "Item was added!", Toast.LENGTH_SHORT).show();
    }

    private void removeItem(int position, List<String> list, ItemsAdapter ia, boolean toast) {
        try {
            list.remove(position);
            ia.notifyItemRemoved(position);
            saveItems();
            if (toast) Toast.makeText(getApplicationContext(), "Item was removed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("MainActivity", "Error deleting item", e);
        }

    }

    private void editItem(int position, List<String> list, int code) {
        Log.d("MainActivity", "Single click at position" + position);
        // create new activity
        Intent i = new Intent(MainActivity.this, EditActivity.class);
        // pass the data being edited
        i.putExtra(KEY_ITEM_TEXT, list.get(position));
        i.putExtra(KEY_ITEM_POSITION, position);
        // display the activity
        startActivityForResult(i, code);
        Toast.makeText(getApplicationContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();
    }

    // this saves todos as lines in a .txt file
    private void saveItems() {
        try {
            FileUtils.writeLines(getDataFile(FILES.BACKLOG), itemsBacklog);
            FileUtils.writeLines(getDataFile(FILES.TODAY), itemsToday);
        } catch (IOException e) {
            Log.e("MainActivity", "Error writing items", e);
        }
    }

    private ItemsAdapter setupItemsAdapter (Context ctx, List<String> list, RecyclerView rv, ItemsAdapter ia, ItemsAdapter.OnClickListener cl, ItemsAdapter.OnLongClickListener lcl) {
        ia = new ItemsAdapter(list, lcl, cl);
        rv.setAdapter(ia);
        rv.setLayoutManager(new LinearLayoutManager(ctx));
        return ia;
    }

    private void addItemFromHotbar (boolean toast) {
        String note = etItem.getText().toString();
        RadioButton[] rb = {findViewById( R.id.typeTask), findViewById(R.id.typeEvent), findViewById(R.id.typeNote)};
        String prefix = "";
        // set prefix
        prefix = (chkPriority.isChecked()) ? "!" : "?";
        if (rb[0].isChecked()) prefix += ".";
        if (rb[1].isChecked()) prefix += "0";
        if (rb[2].isChecked()) prefix += "-";
        // cleanup
        chkPriority.setChecked(false);
        etItem.setText("");
        if(trayDown) slideCard();

        // call addItem
        addItem(itemsBacklog, itemsAdapter, prefix+note, toast);
    }

    private void slideCard() {
        // create and run spring animation
        ImageButton ib = findViewById(R.id.btnSlideCard);
        final View img = findViewById(R.id.slidingCard);
        final SpringAnimation anim = new SpringAnimation(img, DynamicAnimation.Y);
        float pixelPerSecond = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
        float endPos = (trayDown) ? 0 : img.getHeight()-100; // TODO: hardcoded value should be made relative
        anim.setStartVelocity(pixelPerSecond);
        //toggle state
        trayDown = !trayDown;
        if(trayDown) ib.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
        else ib.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
        //play animation
        anim.animateToFinalPosition(endPos);
    }

    private void setupTouchHelper (final List<String> listFrom, final List<String> listTo, RecyclerView rv, final ItemsAdapter iaFrom, final ItemsAdapter iaTo) {
        ItemTouchHelper ith = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.LEFT) {
                   @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        addItem(listTo, iaTo, listFrom.get(viewHolder.getAdapterPosition()),false);
                        removeItem(viewHolder.getAdapterPosition(), listFrom, iaFrom,false);
                        Toast.makeText(getApplicationContext(), "Item moved!", Toast.LENGTH_SHORT).show();
                    }
                });
        ith.attachToRecyclerView(rv);
    }
    


    // OVERRIDE METHODS (EXCEPT onCreate)

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle action bar button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btnIndex) {
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
                itemsBacklog.set(position, itemText);
                itemsAdapter.notifyItemChanged(position);
            }
            // persist the changes
            saveItems();
        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }

}