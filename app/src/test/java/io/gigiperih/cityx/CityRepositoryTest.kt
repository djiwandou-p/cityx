package io.gigiperih.cityx

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.gigiperih.cityx.data.City
import org.junit.Test
import kotlin.system.measureTimeMillis

class CityRepositoryTest {
    private val smallDataSet = getResource(fileName = "cities_2.json")
    private val mediumDataSet = getResource(fileName = "cities_100.json")
    private val incompleteDataSet = getResource(fileName = "incomplete.json")
    private val invalidDataSet = getResource(fileName = "invalid.json")

    @Test
    fun `given valid json file, when parsing is succeed, should return correct list of cities`() {
        val result = loadData(smallDataSet)

        assertThat(result).apply {
            isEqualTo(FakeData.expectedSample)
            hasSize(2)
            containsNoDuplicates()
        }
    }

    @Test
    fun `given valid but incomplete json file, when parsing is failing, should return null`() {
        val result = loadData(incompleteDataSet)

        assertThat(result).apply {
            isNull()
        }
    }

    @Test(expected = JsonEncodingException::class)
    fun `given invalid json file, when parsing is failing, should throws JsonEncodingException`() {
        loadData(invalidDataSet)
    }

    @Test
    fun `given unsorted list of city, when sorting is success, should return sorted list`() {
        val unsortedList = loadData(smallDataSet)
        val result = unsortedList.sortAlphabetically()

        assertThat(result).apply {
            isEqualTo(FakeData.sortedSample)
        }
    }

    @Test
    fun `given null value of city, when sorting is failing, should return null`() {
        val unsortedList = loadData(smallDataSet)
        val result = unsortedList.sortAlphabetically()

        assertThat(result).apply {
            isNull()
        }
    }

    @Test
    fun `given valid large and small json file, relative sorting time complexity should be better than linear`() {
        val result = mutableListOf<City>()
        val timeExecSmall = measureTimeMillis {
            loadData(FakeData.validSample).sortAlphabetically()?.let { result.addAll(it) }
        }
        result.clear()

        // delta time by adding previous timeExecSmall assuming JVM was already warmed up
        val timeExecLarge = measureTimeMillis {
            loadData(FakeLargeData.jsonSample).sortAlphabetically()?.let { result.addAll(it) }
        } + timeExecSmall

        // verify time execution
        assertThat(timeExecSmall).apply {
            isLessThan(timeExecLarge)
        }

        // verify size
        assertThat(FakeData.expectedSample.size).apply {
            isLessThan(FakeLargeData.expectedSample.size)
        }

        // linear time complexity
        // if list size is 2 and took ~1ms to complete
        // then list size 100 should be ~50ms

        // requirement: should be better than linear time complexity: O(n)
        assertThat(timeExecLarge).apply {
            isLessThan(timeExecSmall * 50)
        }
    }

    // TODO refactor
    private fun loadData(json: String?): List<City>? {
        return try {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val listType = Types.newParameterizedType(List::class.java, City::class.java)

            val adapter = moshi.adapter<List<City>>(listType)

            json?.let { adapter.fromJson(it) }
        } catch (e: JsonDataException) {
            null
        }
    }

    // TODO refactor (might use faster algorithm?)
    private fun List<City>?.sortAlphabetically(): List<City>? {
        if (this == null) return null
        return this.sortedBy { it.name }
    }

    private fun getResource(fileName: String) =
        this::class.java.classLoader?.getResource(fileName)?.readText()
}