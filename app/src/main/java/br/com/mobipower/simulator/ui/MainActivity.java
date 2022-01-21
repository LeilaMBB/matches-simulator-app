package br.com.mobipower.simulator.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.List;
import java.util.Random;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import br.com.mobipower.simulator.R;
import br.com.mobipower.simulator.data.MatchesApi;
import br.com.mobipower.simulator.databinding.ActivityMainBinding;
import br.com.mobipower.simulator.domain.Match;
import br.com.mobipower.simulator.ui.adapter.MatchesAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MatchesApi matchesApi;
    private MatchesAdapter matchesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        binding = ActivityMainBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        setupHyypClient();
        setupMatchesList();
        setupMatchesRefresh();
        setupFloatingActionButton();

    }

    private void setupHyypClient() {
        Retrofit retrofit = new Retrofit.Builder ()
                .baseUrl ("https://leilambb.github.io/matches-simulator-api/")
                .addConverterFactory (GsonConverterFactory.create ())
                .build ();
        matchesApi = retrofit.create (MatchesApi.class);
    }

    private void setupFloatingActionButton() {
        binding.fabSimulate.setOnClickListener (view -> {
            view.animate ().rotation (360).setDuration (500).setListener (new AnimatorListenerAdapter () {
                @Override
                public void onAnimationCancel(Animator animation) {
                    Random random = new Random ();
                    for (int i = 0; i<matchesAdapter.getItemCount (); i++){
                        Match match = matchesAdapter.getMatches ().get(i);
                        match.getHomeTeam ().setScore (random.nextInt (match.getHomeTeam ().getStars () + 1));
                        match.getAwayTeam().setScore (random.nextInt (match.getAwayTeam().getStars () + 1));
                        matchesAdapter.notifyItemChanged (i);
                    }
                }
            });
        });
    }


    private void setupMatchesRefresh() {
        binding.srlMatches.setOnRefreshListener (this::findMatchesFromApi);
    }

    private void setupMatchesList() {
        binding.rvMatches.setHasFixedSize (true);
        binding.rvMatches.setLayoutManager (new LinearLayoutManager (this));
        findMatchesFromApi ();
    }

    private void findMatchesFromApi() {
        binding.srlMatches.setRefreshing (true);
        matchesApi.getMatches().enqueue(new Callback<List<Match>> () {
            @Override
            public void onResponse(Call<List<Match>> call, Response<List<Match>> response) {
                if (response.isSuccessful ()) {
                    List<Match> matches = response.body ();
                    matchesAdapter = new MatchesAdapter(matches);
                    binding.rvMatches.setAdapter (matchesAdapter);
                    //Log.i ("SIMULATOR","Deu tudo certo! Partidas: " + matches.size ());
                } else {
                    showErrorMessage();
                }
                binding.srlMatches.setRefreshing (false);
            }

            @Override
            public void onFailure(Call<List<Match>> call, Throwable t) {
                showErrorMessage ();
                binding.srlMatches.setRefreshing (false);
            }
        });
    }

    private void showErrorMessage() {
        Snackbar.make (binding.fabSimulate, R.string.error_api, Snackbar.LENGTH_LONG).show ();
    }
}
