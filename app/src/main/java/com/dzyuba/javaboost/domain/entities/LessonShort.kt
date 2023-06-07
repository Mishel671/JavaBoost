package com.dzyuba.javaboost.domain.entities

data class LessonShort(
    val id: Long,
    val title: String,
    val description: String,
    val detailDescription: String,
    val tags: List<Tags>,
    val progress: Int = 0
) {
    enum class Tags(val value: String) {
        THEORY("theory"), PRACTICE("practice"), TEST("test");

        companion object {
            fun String.toTag(): Tags? {
                for (tag in Tags.values()) {
                    if (tag.value.equals(this, ignoreCase = true)) {
                        return tag
                    }
                }
                return null
            }
        }
    }

}