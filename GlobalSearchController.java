package com.example.kaisi_lagi;

import com.example.kaisi_lagi.MovieCast.MovieCast;
import com.example.kaisi_lagi.MovieCast.MovieCastRepository;
import com.example.kaisi_lagi.MovieMaster.MovieMaster;
import com.example.kaisi_lagi.MovieMaster.MovieRepository;
import com.example.kaisi_lagi.PeopleMaster.PeopleMaster;
import com.example.kaisi_lagi.PeopleMaster.PeopleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class GlobalSearchController {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    MovieCastRepository movieCastRepository;

    // ── MAIN SEARCH PAGE ──────────────────────────────────────────────────────
    @GetMapping("/search")
    public String search(
            @RequestParam(name = "searchText", required = false) String searchText,
            Model model) {
        return "GlobalSearchBar";
    }

    // ── SEARCH API (AJAX) ─────────────────────────────────────────────────────
    @GetMapping("/search/api")
    @ResponseBody
    public Map<String, List<Map<String, String>>> searchApi(
            @RequestParam("q") String searchText) {

        Map<String, List<Map<String, String>>> result = new HashMap<>();
        List<Map<String, String>> movieList  = new ArrayList<>();
        List<Map<String, String>> peopleList = new ArrayList<>();

        // Movies — anywhere match (surname bhi milega)
        movieRepository.findByMovieNameContainingIgnoreCase(searchText).forEach(m -> {
            Map<String, String> item = new HashMap<>();
            item.put("id",   m.getMovieId().toString());
            item.put("name", m.getMovieName());
            item.put("type", "Movie");
            movieList.add(item);
        });

        // People — anywhere match (surname bhi milega)
        peopleRepository.findByPeopleNameContainingIgnoreCase(searchText).forEach(p -> {
            Map<String, String> item = new HashMap<>();
            item.put("id",   p.getPid().toString());
            item.put("name", p.getPeopleName());
            item.put("type", p.getPrimaryRole() != null
                    ? p.getPrimaryRole().getRoleName()
                    : "");
            peopleList.add(item);
        });

        result.put("movies", movieList);
        result.put("people", peopleList);
        return result;
    }

    // ── SEARCH FORM SUBMIT ────────────────────────────────────────────────────
    @GetMapping("/search2")
    public String searchFromForm(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "type", required = false) String type,
            Model model) {

        String q = keyword.trim();
        String t = type != null ? type.trim() : "";

        // Search movies first
        List<MovieMaster> movies = movieRepository
                .findByMovieNameContainingIgnoreCase(q);

        if (!movies.isEmpty()) {
            return "redirect:/movie/detail/" + movies.get(0).getMovieId();
        }

        // Search people
        List<PeopleMaster> people;
        if (t.isEmpty()) {
            people = peopleRepository.findMovieByOnType(q);
        } else {
            people = peopleRepository.findByPeopleNameContainingIgnoreCaseAndPrimaryRole_RoleNameIgnoreCase(t, q);
        }

        if (!people.isEmpty()) {
            model.addAttribute("person", people.get(0));
            return "PeopleDetailPage";
        }

        model.addAttribute("message", "No Result Found!");
        return "GlobalSearchBar";
    }

    // ── MOVIE DETAIL REDIRECT ─────────────────────────────────────────────────
    @GetMapping("/movie/detail/{id:[0-9]+}")
    public String movieDetail(@PathVariable("id") Long id, Model model) {
        MovieMaster movie = movieRepository.findById(id).orElse(null);
        if (movie == null) {
            model.addAttribute("message", "Movie Not Found!");
            return "GlobalSearchBar";
        }
        model.addAttribute("movie", movie);
        return "redirect:/movies/" + id;
    }

    // ── PERSON DETAIL ─────────────────────────────────────────────────────────
    @GetMapping("/person/{id:[0-9]+}")
    public String getPeopleDetail(@PathVariable Long id, Model model) {
        PeopleMaster person = peopleRepository.findById(id).orElse(null);
        List<MovieCast> castList = movieCastRepository.findAllByPeople(person);
        model.addAttribute("person",   person);
        model.addAttribute("castList", castList);
        return "PeopleDetailPage";
    }
}