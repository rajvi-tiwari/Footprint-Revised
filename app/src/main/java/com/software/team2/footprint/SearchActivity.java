package com.software.team2.footprint;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchActivity extends AppCompatActivity {
    DatabaseHelper db;
    EditText mSymbol;
    Button mSearchButton;
    String symbol;
    private static final String TAG = "SearchActivity";
    public static final String BASE_URL = "https://www.alphavantage.co";
    public static final String API_KEY = "GVHMUUHWT2XJ9CAK";
    private RecyclerView.Adapter mSearchAdapter;
    private RecyclerView mSearchRecyclerView;
    private RecyclerView.LayoutManager mSearchLayoutManager;
    ArrayList<Stock> searcharrayList = new ArrayList<>();
    ArrayList<Stock> searchdisplayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = new DatabaseHelper(this);

        // Get values from edittext for symbol
        mSymbol = (EditText)findViewById(R.id.edittext_stock_symbol_search);
        mSearchButton = (Button)findViewById(R.id.button_search);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                symbol = mSymbol.getText().toString().trim();
                // Got symbol make API calls
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                AlphavantageApi searchAlpha = retrofit.create(AlphavantageApi.class);   // second check
                mSearchRecyclerView = findViewById(R.id.searchRecyclerView);
                mSearchRecyclerView.setHasFixedSize(true);


                Call<bExample> call = searchAlpha.getBestMatch("SYMBOL_SEARCH", symbol, API_KEY);
                call.enqueue(new Callback<bExample>() {
                    @Override
                    public void onResponse(Call<bExample> call, Response<bExample> response) {
                        if (!response.isSuccessful()) {
                            String temp = "Code";
                            temp += response.code();
                            Toast.makeText(SearchActivity.this,"Response unsuccessful. Code: " + temp, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<BestMatch> bestMatches = response.body().getBestMatches();

                        for (BestMatch bestMatch : bestMatches) {
                            Log.d(TAG,"adding");
                            searcharrayList.add(new Stock(bestMatch.get1Symbol(), bestMatch.get2Name(),"",""));
                            mSearchLayoutManager = new LinearLayoutManager(getParent());
                            mSearchAdapter = new SearchExampleAdapter(searcharrayList);
                            mSearchRecyclerView.setLayoutManager(mSearchLayoutManager);
                            mSearchRecyclerView.setAdapter(mSearchAdapter);

                        }

                        Log.i("REACHED HELPER","SUCESSS");
                        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                            @Override
                            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                                return false;
                            }

                            @Override
                            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                                Log.i("SWIPED ","SUCESSS");
                                Toast.makeText(SearchActivity.this,"  Added to Watchlist!", Toast.LENGTH_SHORT).show();
                                int position = viewHolder.getAdapterPosition();
                                String symbol = searcharrayList.get(position).getSymbol();
                                Log.i("SYMBOL is  ",symbol);
                                String name = searcharrayList.get(position).getName();
                                Log.i("NAME is ",name);
                                long a= db.addToWatchlist(name, symbol);
                                if(a != 0)
                                    Log.i("ADDED TO WATCHLIST ","SUCESSS");
                            }
                        });

                        helper.attachToRecyclerView(mSearchRecyclerView);

                    }

                    @Override
                    public void onFailure(Call<bExample> call, Throwable t) {
                        Toast.makeText(SearchActivity.this," API response Failed"+t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                searcharrayList.clear();
            }

        });


    }
}
