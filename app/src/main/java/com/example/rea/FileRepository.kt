package com.example.rea

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository(private val context: Context) {
    private val fileName = "my_library.json"
    private val discoverFileName = "discover_cache.json"
    private val gson = Gson()


    private val repoScope = CoroutineScope(Dispatchers.IO)

    private val _allBooks = MutableStateFlow<List<Book>>(emptyList())
    val allBooks: StateFlow<List<Book>> = _allBooks

    private val _discoverCache = MutableStateFlow<List<ApiBook>>(emptyList())
    val discoverCache: StateFlow<List<ApiBook>> = _discoverCache

    init {

        repoScope.launch {
            loadBooks()
            loadDiscoverCache()
        }
    }

    private fun loadBooks() {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<Book>>() {}.type
            val books: List<Book> = gson.fromJson(json, type) ?: emptyList()
            _allBooks.value = books
        }
    }

    private fun loadDiscoverCache() {
        val file = File(context.filesDir, discoverFileName)
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<ApiBook>>() {}.type
            val cachedBooks: List<ApiBook> = gson.fromJson(json, type) ?: emptyList()
            _discoverCache.value = cachedBooks
        }
    }

    fun saveDiscoverCache(books: List<ApiBook>) {
        _discoverCache.value = books
        // YENİ: 100 kitabı dosyaya yazma işlemini arka planda sessizce yapıyoruz
        repoScope.launch {
            val file = File(context.filesDir, discoverFileName)
            file.writeText(gson.toJson(books))
        }
    }

    suspend fun searchBooksFromApi(query: String, limit: Int = 20): List<ApiBook> {
        return try {
            val response = ApiClient.apiService.searchBooks(query, limit)
            response.docs
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveBookWithDetails(apiBook: ApiBook) {

        withContext(Dispatchers.IO) {
            var finalDescription: String? = "Bu kitap için bir açıklama bulunamadı."
            try {
                val detailsUrl = "https://openlibrary.org${apiBook.key}.json"
                val details = ApiClient.apiService.getBookDetails(detailsUrl)
                if (details.description != null) {
                    finalDescription = if (details.description.isJsonObject) {
                        details.description.asJsonObject.get("value").asString
                    } else {
                        details.description.asString
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val coverUrl = if (apiBook.coverId != null) {
                "https://covers.openlibrary.org/b/id/${apiBook.coverId}-M.jpg"
            } else null

            val currentBooks = _allBooks.value.toMutableList()
            val newId = (currentBooks.maxOfOrNull { it.id } ?: 0) + 1

            val newBook = Book(
                id = newId,
                title = apiBook.title,
                author = apiBook.authorNames?.firstOrNull() ?: "Yazar Bilinmiyor",
                coverUrl = coverUrl,
                description = finalDescription
            )

            currentBooks.add(newBook)
            _allBooks.value = currentBooks

            val file = File(context.filesDir, fileName)
            file.writeText(gson.toJson(currentBooks))
        }
    }


    fun updateBook(updatedBook: Book) {
        val currentBooks = _allBooks.value.toMutableList()
        val index = currentBooks.indexOfFirst { it.id == updatedBook.id }

        if (index != -1) {
            currentBooks[index] = updatedBook
            _allBooks.value = currentBooks


            repoScope.launch {
                val file = File(context.filesDir, fileName)
                file.writeText(gson.toJson(currentBooks))
            }
        }
    }
    fun saveManualBook(title: String, author: String, description: String, isbn: String, coverUrl: String?) {
        val currentBooks = _allBooks.value.toMutableList()
        val newId = (currentBooks.maxOfOrNull { it.id } ?: 0) + 1

        val newBook = Book(
            id = newId,
            title = title,
            author = author,
            coverUrl = coverUrl,
            description = description,
            isbn = isbn
        )

        currentBooks.add(newBook)
        _allBooks.value = currentBooks

        repoScope.launch {
            val file = File(context.filesDir, fileName)
            file.writeText(gson.toJson(currentBooks))
        }
    }

    fun findBookByIsbn(isbn: String): Book? {
        return _allBooks.value.find { it.isbn == isbn }
    }
}