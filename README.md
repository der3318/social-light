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
|[SUFFIX]/login|HTTP GET|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|account|String|使用者帳號|v|
|password|String|使用者密碼|v|

範例－`{"account":"hello", "password":"wor1d"}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「０」為登入成功，「-1」為密碼錯誤，「-2」為帳號不存在|
|token|String|code 為零時存在，獨一無二的權限識別碼，用於後續溝通時的權限管控|
|id|Integer|code 為零時存在，代表該使用者的 ID|

範例－`{"code":0, "token":"mqspoq4fc", "id":202}` 或是 `{"code":-1}`


#### 2 - Get User Information

|Path|Protocol|Request Content Type|Response Content Type|Charset|
|:-:|:-:|:-:|:-:|:-:|
|[SUFFIX]/user|HTTP GET|Application/JSON|Application/JSON|UTF-8|

##### Request Body
|Key|Type|Description|Required|
|:-:|:-:|:-:|:-:|
|id|Integer|查詢對象的使用者 ID|v|

範例－`{"id":203}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「０」為查詢成功，「-1」為查無使用者|
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
|[SUFFIX]/user|HTTP POST|Application/JSON|Application/JSON|UTF-8|

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

範例－`{"token":"mqspoq5fc", "id":202, "type":"肺癌末期病患" , "motto":"明天就好了"}`


##### Returned Content
|Key|Type|Description|
|:-:|:-:|:-:|
|code|Integer|狀態識別碼，「０」為更新成功，「-1」為查無使用者，「-2」為權限不合法|

範例－`{"code":-2}`

