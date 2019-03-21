package com.jackowski.movies

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.jackowski.movies.adapters.AllMoviesAdapter
import com.jackowski.movies.adapters.OnLoadMoreListener
import com.jackowski.movies.api.Service
import com.jackowski.movies.models.MoviesList
import com.jackowski.movies.mvp.presenters.MainActivityPresenter
import com.jackowski.movies.mvp.views.MainActivityView
import kotlinx.android.synthetic.main.activity_main.*

const val COLUMNS_NUMBER = 2

class MainActivity : BaseInternetConnectionActivity(), OnLoadMoreListener, MainActivityView {

    private val service: Service = Service.create(this)
    private lateinit var presenter: MainActivityPresenter
    private lateinit var adapter: AllMoviesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = ViewModelProviders.of(this).get(MainActivityPresenter::class.java)
        presenter.setService(service)
        presenter.setOnRestoreChanges(savedInstanceState != null)

        all_movies_recycler_view.layoutManager = GridLayoutManager(this, COLUMNS_NUMBER)
        adapter = AllMoviesAdapter(this@MainActivity, all_movies_recycler_view, this)
        all_movies_recycler_view.adapter = adapter
        all_movies_recycler_view.setHasFixedSize(true)
    }

    override fun onResume() {
        super.onResume()
        presenter.onAttach(this)
    }

    override fun onPause() {
        presenter.onDetach()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_view_menu_item, menu)
        val searchViewItem = menu?.findItem(R.id.action_search)
        val searchView = searchViewItem?.actionView as SearchView

        searchViewItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                presenter.onFilter(searchView)
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                presenter.onStopFilter()
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onInternetConnectionAvailable() {
        presenter.onInternetConnectionAvailable()
    }

    override fun onInternetConnectionLost() {
        //irrelevant
    }

    override fun onDataPrefetched(moviesList: MoviesList) {
        adapter.onDataPrefetched(moviesList)
    }

    override fun onMoreDataFetched(moviesList: MoviesList) {
        adapter.onMoreDataFetched(moviesList)
    }

    override fun onFetchingDataError() {
        Toast.makeText(this, getString(R.string.no_internet_connection_message), Toast.LENGTH_LONG).show()
    }

    override fun showOnLoadingScreen() {
        fetching_data_progress_bar.visibility = View.VISIBLE
    }

    override fun hideOnLoadingScreen() {
        fetching_data_progress_bar.visibility = View.INVISIBLE
    }

    override fun onRestoreChanges(hasDataFetched: Boolean, moviesList: MoviesList) {
        adapter.onRestoreChanges(moviesList, hasDataFetched)
    }

    override fun onLoadMore() {
        presenter.onLoadMore()
    }

    override fun showAllMovies() {
        adapter.showAllMovies()
    }

    override fun showEmptyList() {
        adapter.showEmptyMovies()
    }

    override fun showFilteredMovies(moviesList: MoviesList) {
        adapter.filter(moviesList)
    }

    override fun addMoreFilteredMovies(moviesList: MoviesList) {
        adapter.addMoreFilteredMovies(moviesList)
    }
}
