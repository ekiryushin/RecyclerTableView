[![](https://jitpack.io/v/ekiryushin/RecyclerTableView.svg)](https://jitpack.io/#ekiryushin/RecyclerTableView)
# RecyclerTableView
Android-библиотека на основе `RecyclerView` для отображение данных в виде прокручиваемой таблицы. Данные для таблицы могут формироваться сразу или постранично.

# Подключение
В `settings.gradle` приложения подключаем репозиторий
```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```
В зависимостях `build.gradle` нужного модуля подключаем данную библиотеку
```groovy
dependencies {
    implementation 'com.github.ekiryushin:RecyclerTableView:1.0.1'
}
```

# Пример использования (Статичные данные)
1) Добавляем view
```xml
<io.github.ekiryushin.recyclertableview.RecyclerTableView
    android:id="@+id/recycler_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```
<details><summary>Дополнительные параметры для view</summary>

| **Параметр** | **Значение по умолчанию** | **Описание** |
|:---:|:---:|:---:|
|app:divider_size|1dp|Толщина линий между ячейками|
|app:divider_color|#000000|Цвет линий между ячейками|

</details>

2) Создаем ViewHolder, наследованный от `RecyclerTableViewHolder` для отображения данных в ячейке. Логику отображения реализуем в `onBind`.
```kotlin
class StatisticViewHolder(private val binding: CellItemBinding):
    RecyclerTableViewHolder<String>(binding.root) {

    override fun onBind(cell: String, row: Int, column: Int) {
        binding.cellValue.text = cell

        //чередование цветов в строках таблицы
        val colorId = if (row % 2 == 0) R.color.row_even_background else R.color.row_odd_background
        binding.root.setBackgroundResource(colorId)

        //жирный текст в четных столбцах
        val type = if (column % 2 == 0) Typeface.BOLD else Typeface.NORMAL
        binding.cellValue.typeface = Typeface.defaultFromStyle(type)
    }
}
```
3) Создаем Adapter, наследованный от `RecyclerTableViewAdapter` для хранения данных и передачи их в ViewHolder.
```kotlin
class StatisticAdapter(data: List<List<String>>): RecyclerTableViewAdapter<String>(data) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerTableViewHolder<String> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CellItemBinding.inflate(inflater, parent, false)
        return StatisticViewHolder(binding)
    }
}
```
4) Настраиваем отображение данных
```kotlin
val data: List<List<String>> = listOf()
val dataAdapter = StatisticAdapter(data)
val countColumns = dataAdapter.countColumns
with(binding) {
    recyclerView.adapter = dataAdapter
    recyclerView.layoutManager = TableLayoutManager(requireContext(), countColumns)
    recyclerView.itemAnimator = DefaultItemAnimator()
}
```

# Пример использования (постраничная подгрузка)
Данные для таблицы должны подгружаться полными строками, __но в адаптер передаваться как одноранговый список элементов со всех столбцов__.

# Зависимости в библиотеке
| **Библиотека** | **Версия** |
|:---:|:---:|
|androidx.core:core-ktx|1.7.0|
|org.jetbrains.kotlinx:kotlinx-coroutines-core|1.6.4|
|androidx.recyclerview:recyclerview|1.2.1|
|androidx.paging:paging-runtime-ktx|3.1.1|

# License

    Copyright 2023 Eugene Kiryushin

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
