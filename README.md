## Happy Patient
### Server Configuration
|域名|IPv4 位址|虛擬核心數目|記憶體大小|實體位置|
|:-:|:-:|:-:|:-:|:-:|
|azure.der3318.nctu.me|23.102.71.144|2|16 GB|Japan East|


### Database
#### Sqlite
* 單個檔案
* 搬遷、編輯容易，適合測試階段及小型專案
* 圖形介面下載 [官方連結](https://sqlitebrowser.org/dl/)


#### Tables
|Table          |Field          |Type           |Note                           |
|:-:            |:-:            |:-:            |:-:                            |
|users          |id             |INTEGER        |PRIMARY KEY                    |
|               |account        |VARCHAR(255)   |                               |
|               |password       |VARCHAR(255)   |                               |
|               |name           |VARCHAR(255)   |                               |
|               |type           |VARCHAR(255)   |                               |
|               |location       |VARCHAR(255)   |                               |
|               |motto          |VARCHAR(255)   |                               |
|               |intro          |VARCHAR(2047)  |                               |
|               |url_avatar     |VARCHAR(255)   |                               |
|               |ts_create      |DATETIME       |DEFAULT CURRENT_TIMESTAMP      |
|               |               |               |                               |
|boards         |id             |INTEGER        |PRIMARY KEY                    |
|               |name           |VARCHAR(255)   |                               |
|               |ts_create      |DATETIME       |DEFAULT CURRENT_TIMESTAMP      |
|               |               |               |                               |
|posts          |id             |INTEGER        |PRIMARY KEY                    |
|               |id_user        |INTEGER        |                               |
|               |id_board       |INTEGER        |                               |
|               |title          |VARCHAR(255)   |                               |
|               |content        |VARCHAR(2047)  |                               |
|               |url_avatar     |VARCHAR(255)   |                               |
|               |ts_create      |DATETIME       |DEFAULT CURRENT_TIMESTAMP      |
|               |               |               |                               |
|comments       |id             |INTEGER        |PRIMARY KEY                    |
|               |id_user        |INTEGER        |                               |
|               |id_post        |INTEGER        |                               |
|               |content        |VARCHAR(2047)  |                               |
|               |ts_create      |DATETIME       |DEFAULT CURRENT_TIMESTAMP      |
|               |               |               |                               |
|chatrooms      |id             |INTEGER        |PRIMARY KEY                    |
|               |id_user        |INTEGER        |                               |
|               |id_user_target |INTEGER        |                               |
|               |name           |VARCHAR(255)   |                               |
|               |url_avatar     |VARCHAR(255)   |                               |
|               |ts_create      |DATETIME       |DEFAULT CURRENT_TIMESTAMP      |
|               |               |               |                               |
|messages       |id             |INTEGER        |PRIMARY KEY                    |
|               |id_chatroom    |INTEGER        |                               |
|               |status         |INTEGER        |NEW=0 READ=1 OUT=2             |
|               |content        |VARCHAR(255)   |                               |
|               |ts_create      |DATETIME       |DEFAULT CURRENT_TIMESTAMP      |


### API
#### 0 - About
|URL 前綴|版本|最後修改日期|
|:-:|:-:|:-:|
|[DOMAIN]/api/v1|Version 1|2020 Febuary 24th|


#### 1 - Login

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/login|HTTP POST|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|account|String|使用者帳號|v|
|password|String|使用者密碼|v|

範例－`{"account":"hello", "password":"wor1d"}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「0」為登入成功，「-1」為密碼錯誤，「-2」為帳號不存在|
|token|String|code 為零時存在，獨一無二的權限識別碼，用於後續溝通時的權限管控|
|id|Integer|code 為零時存在，代表該使用者的 ID|

範例－`{"code":0, "token":"mqspoq4fc", "id":202}` 或是 `{"code":-1}`


#### 2 - Get User Information

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/user|HTTP POST|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|id|Integer|查詢對象的使用者 ID|v|

範例－`{"id":203}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「0」為查詢成功，「-1」為查無使用者|
|account|String|code 為零時存在，即對象使用者帳號|
|name|String|code 為零時存在，即對象使用者暱稱|
|type|String|code 為零時存在，即對象使用者類別|
|location|String|code 為零時存在，即對象使用者所在縣市|
|motto|String|code 為零時存在，即對象使用者的座右銘|
|intro|String|code 為零時存在，即對象使用者的自介|
|url_avatar|String|code 為零時存在，即對象使用者的頭像圖片網址|

範例－`{"code":0, "account":"temp", "name":"有錢人", ..., "url_avatar":"https://imgur/user.png"}`


#### 3 - Update User Information

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/user/update|HTTP POST|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|token|String|權限識別碼，用於識別使用者的身分|v|
|id|Integer|變更對象（即是自己）的使用者 ID|v|
|password|String|若有，則嘗試將其設為新的密碼||
|name|String|若有，則嘗試將其設為新的暱稱||
|type|String|若有，則嘗試將其設為新的類別||
|location|String|若有，則嘗試將其設為新的所在縣市||
|motto|String|若有，則嘗試將其設為新的座右銘||
|intro|String|若有，則嘗試將其設為新的自介||
|url_avatar|String|若有，則嘗試將其設為新的頭像圖片網址||

範例－`{"token":"mqspoq5fc", "id":202, "type":"肺癌末期病患", "motto":"明天就好了"}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「0」為更新成功，「-1」為查無使用者，「-2」為權限不合法|

範例－`{"code":-2}`


#### 4 - Get List of Boards

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/boards|HTTP POST|Application/JSON|Application/JSON|UTF-8|

##### Request Body
None


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，應為「0」，表示查詢成功|
|boards|Object Array|code 為零時存在，元素結構參考下幾列|
|boards[index].id|Integer|看板 ID，可用於查詢看版下的文章|
|boards[index].name|String|即看板名字|

範例－`{"code":0, "boards":[]}` 或是 `{"code":0, "boards":[{"id":0, "name":"糖尿病版"}]}`


#### 5 - Get Posts with Filters

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/posts|HTTP POST|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|policy|Integer|排序策略，「0」為不指定，「1」為從新到舊，「2」為從關聯性高到低|v|
|id_user|Integer|若有，則只回傳該使用者 ID 所發表的文章||
|id_board|Integer|若有，則只回傳該看版 ID 底下的文章，「-1」則表示個人發文||
|keyword|String|若有，則只回傳包含該關鍵字的文章||
|ts|Datatime String|格式為「YYYY-MM-DD HH:MM」，若有，則只回傳該時間點後的新文章||

範例－`{"policy":1, "id_user":202, "ts":"2020-02-26 20:02"}` 或是 `{"policy":2, "keyword":"血友病童"}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「0」為查詢成功，「-1」為策略不存在|
|posts|Object Array|code 為零時存在，元素結構參考下幾列|
|posts[index].id|Integer|文章 ID，可用於查詢看文章的留言或進行編輯|
|posts[index].id_user|Integer|文章作者的使用者 ID|
|posts[index].id_board|Integer|文章所屬看版的 ID，「-1」則表示個人發文|
|posts[index].title|String|文章標題|
|posts[index].content|String|文章內容|
|posts[index].url_avatar|String|文章縮圖網址|
|posts[index].ts_create|Datetime String|文章發表日期，格式為「YYYY-MM-DD HH:MM」|

範例－`{"code":0, "posts":[]}` 或是 `{"code":0, "posts":[{"id":4, "user_id":202, "id_board":5, "title":"沒錢付手術費", "content":"唉", "url_avatar":"https://post.png", "ts_create":"2020-02-26 21:00"}, {"id":8, ...}]}`


#### 6 - Publish or Update a Post

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/post/update|HTTP POST|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|id|Integer|文章 ID，若有，且文章存在，則會更新該文章||
|token|String|權限識別碼，用於識別使用者的身分|v|
|id_user|Integer|欲發文者（即是自己）的使用者 ID|v|
|id_board|Integer|欲發表文章的看版 ID，「-1」為個人發文|v|
|title|String|文章標題|v|
|content|String|文章內容|v|
|url_avatar|String|文章縮圖網址|v|

範例－`{"token":"mqspoq5fc", "id_user":202, "id_board":5, "title":"旁邊的病人好正", "content":"有卦嗎", "url_avatar":"https://beauty.png"}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「0」為發文或更新成功，「-1」為查無使用者，「-2」為看版不存在，「-3」為資訊有缺漏或空白，「-4」則是使用者權限不合法|
|id|Integer|code 為零時存在，表示新文章的 ID|

範例－`{"code":-3}` 或是 `{"code":0, "id":9}`


#### 7 - Get Post Information and Related Comments

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/post|HTTP POST|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|id|Integer|欲瀏覽的文章 ID|v|

範例－`{"id": 4}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「0」為查詢成功，「-1」為查無此文章|
|id_user|Integer|code 為零時存在，該文章作者的使用者 ID|
|id_board|Integer|code 為零時存在，該文章所屬看版的 ID|
|title|String|code 為零時存在，即文章標題|
|content|String|code 為零時存在，即文章內容|
|url_avatar|String|code 為零時存在，即文章縮圖網址|
|ts_create|Datetime String|code 為零時存在，文章發表日期，格式為「YYYY-MM-DD HH:MM」|
|comments|Object Array|code 為零時存在，元素結構參考下幾列|
|comments[i].id|Integer|留言 ID，修改或編輯時使用|
|comments[i].id_user|Integer|該留言使用者的 ID|
|comments[i].content|String|該留言的內文|
|comments[i].ts_create|Datetime String|該留言的發表日期，格式為「YYYY-MM-DD HH:MM」|

範例－`{"code":0, "id_user":202, "id_board":5, "title":"沒錢付手術費", "content":"唉", "url_avatar":"https://post.png", "ts_create":"2020-02-26 21:00", "comments":[{"id":1034, "id_user":204, "content":"我也是", "ts_create":"2020-02-26 21:05"}]}`


#### 8 - Publish or Update a Comment

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/comment/update|HTTP POST|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|id|Integer|留言 ID，若有，且留言存在，則會更新該文章||
|token|String|權限識別碼，用於識別使用者的身分|v|
|id_user|Integer|欲留言者（即是自己）的使用者 ID|v|
|id_post|Integer|欲留言回覆的文章 ID|v|
|content|String|留言內容|v|

範例－`{"token":"mqspoq5fk", "id_user":202, "id_post":4, "content":"好想當醫生賺大錢"}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「0」為回覆成功，「-1」為查無使用者，「-2」為文章不存在，「-3」為資訊有缺漏或空白，「-4」則是使用者權限不合法|
|id|Integer|code 為零時存在，表示新文章的 ID|

範例－`{"code":-4}` 或是 `{"code":0, "id":1039}`


#### 9 - Get Chatrooms of a User

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/chatrooms|HTTP POST|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|token|String|權限識別碼，用於識別使用者的身分|v|
|id|Integer|使用者 ID|v|

範例－`{"token":"mqspoq5fc", "id":202}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「0」為查詢成功，「-1」為查無使用者，「-2」是使用者權限不合法|
|chatrooms|Object Array|code 為零時存在，元素結構參考下幾列|
|chatrooms[index].id|Integer|聊天室 ID，用於瀏覽對話記錄或發訊|
|chatrooms[index].id_user_target|Integer|聊天室對象的使用者 ID|
|chatrooms[index].name|String|聊天室名稱，預設是對象名字|
|chatrooms[index].url_avatar|String|聊天室頭像網址|
|chatrooms[index].lastmsg_status|Integer|最後一則訊息的狀態，「0」為新，「1」為舊，「2」為已發送|
|chatrooms[index].lastmsg_content|String|最後一則訊息的內容|
|chatrooms[index].lastmsg_ts|Datetime String|最後一則訊息的時間，格式為「YYYY-MM-DD HH:MM」|

範例－`{"code":0, "chatrooms":[{"id":67, "id_user_target":205, "name":"李承軒", "url_avatar":"http://handsome.jpg", "lastmsg_status":0, "lastmsg_content":"晚安", ,"lastmsg_ts":"2020-02-28 23:55"}]}`


#### 10 - Update a Chatroom

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/chatroom/update|HTTP POST|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|id|Integer|聊天室 ID|v|
|token|String|權限識別碼，用於識別使用者的身分|v|
|id_user|Integer|欲編輯聊天室（即是自己）的使用者 ID|v|
|name|String|若有，則更新聊天室的名字||
|url_avatar|String|若有，則更新聊天室的縮圖網址||

範例－`{"id":68, "token":"mqspoq5fc", "id_user":202, "name":"有錢承軒"}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「0」為更新成功，「-1」為聊天室不存在，「-2」為查無使用者，「-3」則是使用者權限不合法|

範例－`{"code":-1}`


#### 11 - Get Messages Between Users

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/messages|HTTP POST|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|token|String|權限識別碼，用於識別使用者的身分|v|
|id|Integer|使用者 ID|v|
|id_user_target|Integer|聊天對象的使用者 ID|v|

範例－`{"token":"mqspoq5fc", "id":202, "id_user_target":206}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「0」為查詢成功，「-1」為查無使用者，「-2」為查無聊天室，「-3」為權限不合法|
|id_chatroom|Integer|code 為零時存在，代表兩人之間的聊天室 ID|
|name|String|code 為零時存在，代表兩人之間的聊天室名稱|
|url_avatar|String|code 為零時存在，代表兩人之間的聊天室縮圖網址|
|messages|Object Array|code 為零時存在，最新的一百筆，元素結構參考下幾列|
|messages[index].status|Integer|訊息狀態，「0」為新，「1」為舊，「2」為已發送|
|messages[index].content|String|訊息內容|
|messages[index].ts_create|Datetime String|訊息時間，格式為「YYYY-MM-DD HH:MM」|

範例－`{"code":0, "id_chatroom":69, "name":"漂亮的護士", "url_avatar":"http://nurse.png", "messages":[{"status":2, "content":"一次多少", "ts_create":"2020-03-01 22:15"}, {"status":1, "content":"一萬可以嗎", "ts_create":"2020-03-01 22:20"}]}`


#### 12 - Send Message

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/message/update|HTTP POST|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|token|String|權限識別碼，用於識別使用者的身分|v|
|id|Integer|使用者 ID|v|
|id_user_target|Integer|聊天對象的使用者 ID|v|
|content|String|訊息內容|v|

範例－`{"token":"mqspoq5fc", "id":202, "id_user_target":206, "content":"現金沒那麼多"}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「0」為發送成功，「-1」為查無使用者，「-2」為權限不合法|
|id_chatroom|Integer|code 為零時存在，代表兩人之間的聊天室 ID|
|name|String|code 為零時存在，代表兩人之間的聊天室名稱|
|url_avatar|String|code 為零時存在，代表兩人之間的聊天室縮圖網址|
|messages|Object Array|code 為零時存在，最新的一百筆，元素結構參考下幾列|
|messages[index].status|Integer|訊息狀態，「0」為新，「1」為舊，「2」為已發送|
|messages[index].content|String|訊息內容|
|messages[index].ts_create|Datetime String|訊息時間，格式為「YYYY-MM-DD HH:MM」|

範例－`{"code":0, "id_chatroom":69, "name":"漂亮的護士", "url_avatar":"http://nurse.png", "messages":[{"status":2, "content":"一次多少", "ts_create":"2020-03-01 22:15"}, {"status":1, "content":"一萬可以嗎", "ts_create":"2020-03-01 22:20"}, {"status":2, "content":"現金沒那麼多", "ts_create":"2020-03-01 22:25"}]}`

