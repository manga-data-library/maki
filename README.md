# mandala-analyze-twitter

## Analytics Twitter Stream on Hadoop

Twitter Stream を Hadoop上で解析するためのツールです。

TwitterのStreaming APIを使用します。

## 事前準備

Twitterの開発者アカウントを取得し

* consumer_key
* consumer_secret
* access_token
* access_token_secret

を取得し、conf/twitter_conf.jsonに書いておいてください。

## システム要件

* Java 8+
* Gradle 2+


## 起動方法

パラメータに検索条件ファイルとTwitterアカウント情報を設定できます。  
指定がない場合はそれぞれ以下のデフォルトファイルが利用されます。

- 検索条件ファイル：conf/application.properties
- Twitterアカウント情報：conf/twitter_conf.json

```
java -jar maki.jar
java -jar maki.jar private/application.properties
java -jar maki.jar private/application.properties private/twitter_conf.json

# Gradleからも実行可能です
gradle run
gradle run -Pargs="private/application.properties"
gradle run -Pargs="private/application.properties private/twitter_conf.json"
```

実行するとlogsディレクトリ配下に処理結果のCSVファイルが作成されます。  
CSVファイルはGoogle BigQueryの解析で利用することを想定しています。


## コンパイル方法

gradleをインストールし、プロジェクトディレクトリ配下で以下のコマンドを実行してください。

    gradle build

また、以下のコマンドを実行すると、実行に必要なファイルがまとまったZIPファイルが`build/distributions`配下に作成されます。

    gradle distZip

このファイルを利用して実行する場合は、ZIPを展開後のディレクトリで、`bin/maki`または`bin/maki.bat`を実行してください。


## Google BigQueryでログファイルを解析する時のDDL

```
id: STRING ,name: STRING,tweet_text: STRING,source: STRING,retweet_count: INTEGER,favorite_count: INTEGER,created_at: STRING,latitude: STRING,longitude: STRING,media_url1: STRING,media_url2: STRING,media_url3: STRING,media_url4: STRING,unixtime: STRING
```



