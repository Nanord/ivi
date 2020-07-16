## Модуль парсинга mongogo

### Настройка окружения

* Java version 1.8
* Maven3
* Браузур firefox: _sudo apt install firefox_
* Веб-драйвер для firefox: https://github.com/mozilla/geckodriver/releases

### Описание параметров в файле application.properties:

output.path - путь в файловой системе, куда будут помещены файлы с результатами парсинга

save.type - в настоящий момент реализована работа только с выгрузкой в csv

thread.pool.size.parser - кол-во потоков на асинхронную обработку полученного url от _CatalogProducer_.

use.multithreading = парсинг будет происходить последовательно или асинхронно(не влиет на потоки сбора каталога и получения страниц)

buffer.catalog - макс. кол-во взятых в обработку фильмов

timeout.before.get.page.megogo - таймаут перед получением страницы megogo

timeout.before.get.page.kinopoisk - таймаут перед получением страницы kinopoisk

webdriver.gecko.driver - Путь до binary webdriver

### Запуск
* Установить зависимости и собрать jar: _mcn clean install_
* Запустить полученный jar файл: java -jar megogo-1.0-SNAPSHOT.jar

### Принцип работы
Модуль работает в многопоточном режиме выгружая каждый фильм в файл по мере его получения. 

Модуль использует минимум 5 потоков.
* Получение страницы Mongogo
* Получение страницы Kinopoisk
* Выгрузка каталога
* Парсинг страниц Mongogo и Kinopoisk
* Сохранение в файл 

Получение страниц реализовано отдельным сервисом _PageService_, который получает страницы в отдельных потоках, для предотвращения перегрузки сайта.

Для получения страницы сайта Mongogo используется Jsoup

Для получения страницы сайта Kinopoisk используется Selenium. Сайт может выдать капчу при частом соединении. Модуль об этом уведомит, остановит парсинг и попросит ввести капчу предоставив ссылку на картинку.   

* Сервис _CatalogProducer_ генерирует ссылки на фильмы и складывает в буфер
* Сервис _ParserService_ берёт ссылку из буфера, по мере поступления и инициирует обработку(асинхронно или последовательно)
* Обработка сайта Mongogo и получение ссылки на кинопоиск. Сервис _MongogoParserService_
* Обработка сайта Kinopoisk и дополнение выходных данных. Сервис _KinopoiskParserService_
* Сохранение в фильмов в файл по мере поступления данных(фалй обновляется дополняя данные). Сервис _SaveFile_

Результаты парсинга сохраняются в папку формата  parsed_MM_dd_YYYY__HH_mm_ss/mongogo.csv

##### ! Если задачи не поступают в буфер каталога, в течении 10 минут, парсинг прекращается для предотвращения бесконечного цикла.

