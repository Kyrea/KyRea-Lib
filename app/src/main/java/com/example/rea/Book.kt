package com.example.rea

enum class ReadingStatus(val label: String) {
    UNREAD("Başlanmadı"),
    READING("Okunuyor"),
    FINISHED("Tamamlandı"),
    DROPPED("Bırakıldı")
}

data class Book(
    val id: Int = 0,
    val title: String,
    val author: String,
    val coverUrl: String? = null,
    val description: String? = null,
    val isbn: String? = null,


    val status: ReadingStatus = ReadingStatus.UNREAD,
    val progress: Int = 0,      // Kullanıcının kaldığı sayfa
    val totalPages: Int = 0,    // Toplam sayfa (0 = girilmemiş)
    val rating: Int = 0,        // 1–5 yıldız (0 = puanlanmamış)
    val notes: String = ""      // Okuyucu notu
)