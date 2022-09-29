﻿# Computer Vision School Tasks
Macroscop School 2017

Репозиторий содержит домашние задания, выполненные в рамках школы компьютерного зрения Macroscop осенью 2017 года.

Описание задач:

* Задание 1. Классификатор, который возвращает 1 или 0 в зависимости от того, есть ли на изображении огонь или нет. "Реализован" классом RawClassificator
* Задание 2. Детектор движения. Реализован классом MovDetector
* Задание 3. Сопоставление изображений в разных каналах для получения цветного. Реализован классом ImgAligner
* Задание 4. Классификация датасета [MNIST](http://yann.lecun.com/exdb/mnist/) с помощью логистической регрессии. Реализовано классом DigModel

# Требования

Для того, чтобы собрать проект на локальной машине, необходимо дополнительно установить библиотеки:

* Java 8
* [OpenCV](https://opencv.org/releases.html) для Java (2.4.*)
* [liblinear](https://www.csie.ntu.edu.tw/~cjlin/liblinear/) для Java. Используется в задании 4
* [jackson-core](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.9.2/). Нужен для загрузки конфигурации для TrainCreator

# Известные проблемы

* VideoLoader под Windows может не мочь загрузить видео. Для фикса необходимо добавить ```opencv\build\x64\vc14\bin\``` в перменную окружения PATH. 