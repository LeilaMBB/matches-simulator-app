package br.com.mobipower.simulator.data;

import java.util.List;

import br.com.mobipower.simulator.domain.Match;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MatchesApi {

    @GET("matches.json")
    Call<List<Match>> getMatches();


}
