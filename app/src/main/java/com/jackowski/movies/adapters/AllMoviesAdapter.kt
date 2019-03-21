package com.jackowski.movies.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.jackowski.movies.R
import com.jackowski.movies.models.MoviesList
import com.jackowski.movies.utils.Constants
import kotlinx.android.synthetic.main.single_movie_item.view.*

class AllMoviesAdapter(
    private val context: Context, recyclerView: RecyclerView,
    private val onLoadMoreListener: OnLoadMoreListener?
) : RecyclerView.Adapter<AllMoviesAdapter.MoviesViewHolder>() {
    private var moviesList = MoviesList()
    private var moviesStorage = MoviesList()
    private var isLoadedMore = true
    var hasDataPrefetched = false
    var isLoading = false

    init {
        val linearLayoutManagerNested = recyclerView.layoutManager as LinearLayoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    recyclerView.post { doOnListScrolled(linearLayoutManagerNested) }
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviesViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.single_movie_item, parent, false)
        return MoviesViewHolder(view)
    }

    private fun doOnListScrolled(linearLayoutManagerNested: LinearLayoutManager) {
        val itemCount = linearLayoutManagerNested.itemCount
        val lastVisibleItemPosition = linearLayoutManagerNested.findLastVisibleItemPosition()
        if (hasDataPrefetched && !isLoading && lastVisibleItemPosition > (itemCount - 10)) {
            isLoading = true
            onLoadMoreListener?.onLoadMore()
        }
    }

    override fun getItemCount(): Int {
        return moviesList.movies.size
    }

    override fun onBindViewHolder(viewHolder: MoviesViewHolder, p1: Int) {
        val adapterPosition = viewHolder.adapterPosition
        val posterPath = moviesList.movies[adapterPosition]?.poster_path ?: return
        val url = Constants.BASE_IMAGE_URL + posterPath

        viewHolder.progressBar.visibility = VISIBLE

        handleImageLoading(url, viewHolder)
    }

    private fun handleImageLoading(url: String, viewHolder: MoviesViewHolder) {
        Glide.with(context)
            .load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    viewHolder.progressBar.visibility = GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    viewHolder.progressBar.visibility = GONE
                    return false
                }
            })
            .into(viewHolder.singleMovie)
    }

    fun updateList(it: MoviesList?) {
        moviesList.movies.addAll(it?.movies!!)
        moviesStorage.movies.addAll(it.movies)
        isLoading = false
        isLoadedMore = true
        notifyItemInserted(this.moviesList.movies.size - it.movies.size)
    }

    fun onMoreDataFetched(moviesList: MoviesList) {
        updateList(moviesList)
        isLoading = false
    }

    fun onDataPrefetched(response: MoviesList) {
        moviesList.movies.addAll(response.movies)
        moviesStorage.movies.addAll(response.movies)
        hasDataPrefetched = true
        notifyItemInserted(0)
    }

    fun filter(moviesList: MoviesList) {
        this.moviesList.movies = ArrayList(moviesList.movies)
        isLoading = false
        notifyDataSetChanged()
    }

    fun showAllMovies() {
        this.moviesList.movies = ArrayList(moviesStorage.movies)
        notifyDataSetChanged()
    }

    fun showEmptyMovies() {
        this.moviesList.movies.clear()
    }

    fun addMoreFilteredMovies(moviesList: MoviesList) {
        this.moviesList.movies.addAll(moviesList.movies)
        notifyItemInserted(this.moviesList.movies.size)
    }

    fun onRestoreChanges(moviesList: MoviesList, hasDataFetched: Boolean) {
        this.hasDataPrefetched = hasDataFetched
        this.moviesList.movies = ArrayList(moviesList.movies)
        moviesStorage.movies = ArrayList(moviesList.movies)
        notifyDataSetChanged()
    }

    inner class MoviesViewHolder(itemView: View) : ViewHolder(itemView) {
        val singleMovie = itemView.single_movie
        val progressBar = itemView.image_progress_bar
    }
}