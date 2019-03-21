package com.jackowski.movies.mvp.presenters

import android.support.v7.widget.SearchView
import com.jackowski.movies.api.Service
import com.jackowski.movies.models.MoviesList
import com.jackowski.movies.mvp.views.MainActivityView
import com.jackowski.movies.utils.MoviesFilter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivityPresenter : Presenter<MainActivityView>() {
    private lateinit var service: Service
    private var onRestoreChanges = false
    private var hasPrefetchedData = false
    private var loadMoreDataFailed = false
    private var prefetchDataFailed = false
    private var filterStarted = false
    private var pageToFetch = 1
    private var filterPageToFetch = 1
    private var moviesList = MoviesList()
    private var compositeDisposable = CompositeDisposable()
    private var filterDisposables = CompositeDisposable()
    private var currentFilterQuery: String = ""

    override fun onAttach(view: MainActivityView) {
        super.onAttach(view)
        if (onRestoreChanges) {
            view.onRestoreChanges(hasPrefetchedData, moviesList)
        }
        prefetchData()
    }

    private fun prefetchData() {
        if (hasPrefetchedData) return

        view?.showOnLoadingScreen()

        fetchLatestData()
    }

    private fun fetchLatestData() {
        compositeDisposable.add(service.getNowPlayingMovies(pageToFetch.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                moviesList.movies = response.movies
                view?.hideOnLoadingScreen()
                pageToFetch++
                view?.onDataPrefetched(response)
                prefetchDataFailed = false
                hasPrefetchedData = true
            }, { error -> onPrefetchDataError(error) })
        )
    }

    override fun onDetach() {
        compositeDisposable.clear()
        filterDisposables.clear()
        super.onDetach()
    }

    fun onLoadMore() {
        if (!hasPrefetchedData) return

        if(filterStarted && filterPageToFetch > 1) {
            getNextFilteredMovies()
        } else {
            getNextNowPlayingMovies()
        }
    }

    private fun getNextNowPlayingMovies() {
        compositeDisposable.add(
            service.getNowPlayingMovies(pageToFetch.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    moviesList.movies.addAll(response.movies)
                    view?.onMoreDataFetched(response)
                    pageToFetch++
                    loadMoreDataFailed = false
                }, { error -> onLoadMoreDataError(error) })
        )
    }

    private fun getNextFilteredMovies() {
        filterDisposables.add(
            service.searchMovieByTitle(currentFilterQuery, filterPageToFetch.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view?.addMoreFilteredMovies(it)
                    if (it.movies.size > 0) {
                        filterPageToFetch++
                    }
                },
                    { error -> error.printStackTrace() })
        )
    }

    private fun onLoadMoreDataError(error: Throwable) {
        showError(error)
        loadMoreDataFailed = true
    }

    fun onInternetConnectionAvailable() {
        if (prefetchDataFailed) {
            prefetchData()
        }

        if (loadMoreDataFailed) {
            onLoadMore()
        }
    }

    private fun showError(error: Throwable) {
        view?.onFetchingDataError()
        error.printStackTrace()
    }

    private fun onPrefetchDataError(error: Throwable) {
        showError(error)
        prefetchDataFailed = true
    }

    fun setOnRestoreChanges(onRestoreChanges: Boolean) {
        this.onRestoreChanges = onRestoreChanges
    }

    fun setService(service: Service) {
        this.service = service
    }

    fun onFilter(searchView: SearchView) {
        filterStarted = true
        filterDisposables.add(
            MoviesFilter.fromView(searchView)
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ filterMovies(it) },
                    { error -> error.printStackTrace() })
        )
    }

    private fun filterMovies(currentFilterQuery: String) {
        filterPageToFetch = 1
        this.currentFilterQuery = currentFilterQuery
        if (currentFilterQuery.isEmpty()) {
            view?.showAllMovies()
        } else {
            fetchInitialFilteredMovies(currentFilterQuery)
        }
    }

    private fun fetchInitialFilteredMovies(currentFilterQuery: String) {
        filterDisposables.add(
            service.searchMovieByTitle(currentFilterQuery, filterPageToFetch.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view?.showFilteredMovies(it)
                    filterPageToFetch++
                },
                    { error -> error.printStackTrace() })
        )
    }

    fun onStopFilter() {
        filterStarted = false
        filterDisposables.clear()
        view?.showAllMovies()
    }
}
