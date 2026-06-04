package com.example.rea

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookViewModel(private val repository: FileRepository) : ViewModel() {

    val myLibrary: StateFlow<List<Book>> = repository.allBooks

    private val _searchResults = MutableStateFlow<List<ApiBook>>(emptyList())
    val searchResults: StateFlow<List<ApiBook>> = _searchResults


    private val _pendingSearchQuery = MutableStateFlow<String?>(null)
    val pendingSearchQuery: StateFlow<String?> = _pendingSearchQuery


    private var fullDiscoverPool = listOf<ApiBook>()
    private val _displayedDiscoverBooks = MutableStateFlow<List<ApiBook>>(emptyList())
    val displayedDiscoverBooks: StateFlow<List<ApiBook>> = _displayedDiscoverBooks

    private val itemsPerPage = 30

    init {
        initializeDiscoverFeed()
    }

    fun updateSearchQuery(query: String) {
        _pendingSearchQuery.value = query
    }

    fun consumePendingSearchQuery(): String? {
        val q = _pendingSearchQuery.value
        _pendingSearchQuery.value = null
        return q
    }


    fun updateBookTracking(
        book:       Book,
        status:     ReadingStatus,
        progress:   Int,
        totalPages: Int,
        rating:     Int,
        notes:      String
    ) {
        val updated = book.copy(
            status     = status,
            progress   = progress,
            totalPages = totalPages,
            rating     = rating,
            notes      = notes
        )
        viewModelScope.launch {
            repository.updateBook(updated)
        }
    }

    private fun initializeDiscoverFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            val randomKeywords = listOf(
                "VRMMO", "Xuanhuan", "LitRPG", "Fantasy",
                "Magic", "Sci-Fi", "Cyberpunk", "Adventure", "Thriller"
            )
            val keyword = randomKeywords.random()
            try {
                val results      = repository.searchBooksFromApi(keyword, limit = 100)
                val coveredBooks = results.filter { it.coverId != null }
                if (coveredBooks.size > 30) {
                    fullDiscoverPool = coveredBooks
                    repository.saveDiscoverCache(coveredBooks)
                    withContext(Dispatchers.Main) {
                        _displayedDiscoverBooks.value = emptyList()
                        loadNextDiscoverPage()
                    }
                } else {
                    loadFromCacheFallback()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadFromCacheFallback()
            }
        }
    }

    private fun loadFromCacheFallback() {
        val cached = repository.discoverCache.value
        if (cached.isNotEmpty() && cached.size >= 10) {
            fullDiscoverPool = cached
            viewModelScope.launch(Dispatchers.Main) {
                _displayedDiscoverBooks.value = emptyList()
                loadNextDiscoverPage()
            }
        }
    }

    fun loadNextDiscoverPage() {
        val currentSize = _displayedDiscoverBooks.value.size
        if (currentSize < fullDiscoverPool.size) {
            val nextBatch = fullDiscoverPool.drop(currentSize).take(itemsPerPage)
            _displayedDiscoverBooks.value = _displayedDiscoverBooks.value + nextBatch
        }
    }


    fun searchBooks(query: String) {
        viewModelScope.launch {
            val results = repository.searchBooksFromApi(query, limit = 40)
            _searchResults.value = results.filter { it.coverId != null }
        }
    }

    fun addBookToLibrary(apiBook: ApiBook) {
        viewModelScope.launch {
            repository.saveBookWithDetails(apiBook)
        }
    }

    fun saveManualBook(
        title:       String,
        author:      String,
        description: String,
        isbn:        String,
        coverUrl:    String?
    ) {
        repository.saveManualBook(title, author, description, isbn, coverUrl)
    }

    fun checkLocalLibraryByIsbn(isbn: String): Book? {
        return repository.findBookByIsbn(isbn)
    }
}

class BookViewModelFactory(private val repository: FileRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookViewModel(repository) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel Sınıfı")
    }
}