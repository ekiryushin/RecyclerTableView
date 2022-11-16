# RecyclerTableView
Android-библиоткека для отображение данных в виде прокручиваемой таблицы. Данные для таблицы могут формироваться сразу или постранично.

# Подключение
В `build.gradle` приложения подключаем центральный репозиторий
```groovy
allprojects {
    repositories {
        mavenCentral()
    }
}
```
В зависимостях `build.gradle` нужного модуля подключаем данную библиотеку
```groovy
dependencies {
    implementation 'io.github.ekiryushin:recyclertableview:1.0.0'
}
```

# Пример использования

# License

   Copyright 2022 Eugene Kiryushin

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
