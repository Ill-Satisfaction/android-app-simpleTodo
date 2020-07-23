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
    public static final int EDIT_ITEM_CODE = 40;

    public enum FILES {BACKLOG, TODAY};

    List<Collection> collections;
    Collection activeCollection;

    Button btnAdd;
    ImageButton btnSlide;
    CheckBox chkPriority;
    EditText etItem;
    RecyclerView rvItems;
    RecyclerView rvToday;

    boolean trayDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Simple To Do1");

        // define fields
        btnAdd = findViewById(R.id.btnAdd);
        btnSlide = findViewById(R.id.btnSlideCard);
        chkPriority = findViewById(R.id.priorityCheckBox);
        etItem = findViewById(R.id.etItem);
        rvItems = findViewById(R.id.rvItems);
        rvToday = findViewById(R.id.rvToday);
        trayDown = false;

        loadItems();
        activeCollection = collections.get(0);

        // set up items adapters for recycler views
        setupItemsAdapter(this, activeCollection, rvItems);
        setupItemsAdapter(this, collections.get(1), rvToday);

        // set up touch helpers for recycler views
        setupTouchHelper(rvItems, activeCollection, collections.get(1));
        setupTouchHelper(rvToday, collections.get(1), activeCollection);

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
    }



    //  DATA DISPLAY

    private void setupItemsAdapter (Context ctx, Collection c, RecyclerView rv) {
        final ItemsAdapter.OnLongClickListener lcl = new ItemsAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                removeItem(position, true);
            }
        };
        final ItemsAdapter.OnClickListener cl = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                editItem(position);
            }
        };

        ItemsAdapter ia = new ItemsAdapter(c, lcl, cl);
        c.setIa(ia);
        rv.setAdapter(ia);
        rv.setLayoutManager(new LinearLayoutManager(ctx));
    }

    private void setupTouchHelper (RecyclerView rv, final Collection cFrom, final Collection cTo) {
        ItemTouchHelper ith = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.LEFT) {
                   @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        addItem( cTo, cFrom.getItems().get(viewHolder.getAdapterPosition()),false );
                        removeItem(viewHolder.getAdapterPosition(), cFrom, false);
                        Toast.makeText(getApplicationContext(), "Item moved!", Toast.LENGTH_SHORT).show();
                    }
                });
        ith.attachToRecyclerView(rv);
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
        activeCollection = (trayDown) ? collections.get(1) : collections.get(0);
        //play animation
        anim.animateToFinalPosition(endPos);
    }



    //  CREATE UPDATE DELETE

    private void addItem(Collection c, String s, boolean toast) {
        c.getItems().add(s);
        c.getIa().notifyItemInserted(c.getItems().size()-1);
        saveItems();
        if (toast) Toast.makeText(getApplicationContext(), "Item was added!", Toast.LENGTH_SHORT).show();
    }

    private void addItemFromHotbar (boolean toast) {
        RadioButton[] rb = {findViewById( R.id.typeTask), findViewById(R.id.typeEvent), findViewById(R.id.typeNote)};
        String text = "";
        // set prefix
        text = (chkPriority.isChecked()) ? "!" : "?";
        if (rb[0].isChecked()) text += ".";
        if (rb[1].isChecked()) text += "0";
        if (rb[2].isChecked()) text += "-";
        text += etItem.getText().toString();
        // cleanup
        chkPriority.setChecked(false);
        etItem.setText("");
        if(trayDown) slideCard();

        // call addItem
        addItem(collections.get(0), text, toast);
    }

    private void removeItem(int position, boolean toast) {
        removeItem(position, activeCollection, toast);
    }

    private void removeItem(int position, Collection c, boolean toast) {
        try {
            for (String s : c.getItems()) Log.e("MainActivity", "item: "+s); //log to make sure that the lists are working right

            c.getItems().remove(position);
            c.getIa().notifyItemRemoved(position);
            saveItems();

            if (toast) Toast.makeText(getApplicationContext(), "Item was removed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("MainActivity", "Error deleting item", e);
        }
    }

    private void editItem(int position) {
        editItem(position, activeCollection.getItems(), EDIT_ITEM_CODE);
    }

    private void editItem(int position, List<String> list, int code) {
        Log.d("MainActivity", "Single click at position" + position);
        for (String s : list) Log.e("MainActivity", "item: "+s); //log to make sure that the lists are working right
        // create new activity
        Intent i = new Intent(MainActivity.this, EditActivity.class);
        // pass the data being edited
        i.putExtra(KEY_ITEM_TEXT, list.get(position));
        i.putExtra(KEY_ITEM_POSITION, position);
        // display the activity
        startActivityForResult(i, code);
        Toast.makeText(getApplicationContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();
    }

    // handle result of edit activity
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == EDIT_ITEM_CODE) {
            // retrieve updated text
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);
            // extract original position from pos key
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);

            activeCollection.getItems().set(position, itemText);
            activeCollection.getIa().notifyItemChanged(position);
            // persist the changes
            saveItems();
        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }



    //  ACTION BAR

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



    //  PERSISTENCE AND DATA MANAGEMENT

    private void loadItems() {
        collections = new ArrayList<Collection>();
        try {
            for (FILES f : FILES.values()) {
                collections.add(new Collection(f, FileUtils.readLines(getDataFile(f))));
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Error reading items", e);
        }
    }

    // this saves todos as lines in a .txt file
    private void saveItems() {
        try {
            for (Collection c : collections)
                FileUtils.writeLines(getDataFile(c.getFilename()), c.getItems());
        } catch (IOException e) {
            Log.e("MainActivity", "Error writing items", e);
        }
    }

    private File getDataFile(FILES f) {
        File file = new File(getFilesDir(), f.toString() + ".txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

}