package com.github.ekiryushin.recyclertableview.example.utils

import kotlin.math.max
import kotlin.random.Random

object StringUtils {

    /**
     * Сгенерировать строку произвольной длины
     * @param maxLength максимальная длина строки
     */
    fun getRandomText(maxLength: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyz "
        val random = Random(System.nanoTime())
        val size = random.nextInt(maxLength) +1
        val result = java.lang.StringBuilder(size)
        for (ind in 0 until size) {
            result.append(chars[max(random.nextInt(chars.length), 1)])
        }
        return String(result)
    }
}