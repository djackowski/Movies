package com.jackowski.movies.mvp.views

import com.jackowski.movies.models.MoviesList

interface MainActivityView : View {

    fun onDataPrefetched(moviesList: MoviesList)

    fun onMoreDataFetched(moviesList: MoviesList)

    fun onFetchingDataError()

    fun showOnLoadingScreen()

    fun hideOnLoadingScreen()

    fun onRestoreChanges(hasDataFetched: Boolean, moviesList: MoviesList)

    fun showFilteredMovies(moviesList: MoviesList)

    fun showEmptyList()

    fun showAllMovies()

    fun addMoreFilteredMovies(moviesList: MoviesList)
}