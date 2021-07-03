package io.gigiperih.cityx.domain.repository

import io.gigiperih.cityx.data.City
import io.gigiperih.cityx.data.Trie

interface CityRepository {
    fun get(file: String): List<City>?

    fun getTrie(cities: List<City>?): Trie
}