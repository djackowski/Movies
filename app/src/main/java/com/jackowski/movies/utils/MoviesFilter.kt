package com.jackowski.movies.utils

import android.support.v7.widget.SearchView
import io.reactivex.subjects.PublishSubject

object MoviesFilter {
    fun fromView(searchView: SearchView): PublishSubject<String> {
        val subject: PublishSubject<String> = PublishSubject.create()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                subject.onNext(query!!)
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                subject.onNext(query!!)
                return true
            }
        })

        return subject
    }
}