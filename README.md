## Happy Patient
### Database
#### Sqlite
* File based
* Convinent to migrate and test during development
* Download GUI [here](https://sqlitebrowser.org/dl/)


#### Tables
|Table          |Field          |Type           |Note                           |
|:-:            |:-:            |:-:            |:-:                            |
|users          |id             |INTEGER        |PRIMARY KEY                    |
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
|               |ts_create      |DATETIME       |                               |
|               |               |               |                               |
|comments       |id             |INTEGER        |PRIMARY KEY                    |
|               |id_user        |INTEGER        |                               |
|               |id_post        |INTEGER        |                               |
|               |content        |VARCHAR(2047)  |                               |
|               |ts_create      |DATETIME       |                               |
|               |               |               |                               |
|chatrooms      |id             |INTEGER        |PRIMARY KEY                    |
|               |id_user        |INTEGER        |                               |
|               |id_user_target |INTEGER        |                               |
|               |name           |VARCHAR(255)   |                               |
|               |url_avatar     |VARCHAR(255)   |                               |
|               |ts_create      |DATETIME       |                               |
|               |               |               |                               |
|messages       |id             |INTEGER        |PRIMARY KEY                    |
|               |id_chatroom    |INTEGER        |                               |
|               |status         |INTEGER        |NEW=0 READ=1 OUT=2             |
|               |content        |VARCHAR(255)   |                               |









