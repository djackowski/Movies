package com.jackowski.movies.models

import com.google.gson.annotations.SerializedName

class MoviesList {
    @SerializedName("results")
    var movies = ArrayList<Movie?>()
}
