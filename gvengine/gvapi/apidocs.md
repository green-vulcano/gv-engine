# GVIAM API

- Administration API
  * [Get roles list](#roles)
  * [Get users list](#users)
  * [Get user](#user)
  * [Create user](#create_user)
  * [Update user](#update_user)
  * [Delete user](#delete_user)

- OAuth2 API
  * [Get Access Token](#accessToken)
  * [Refresh Token](#refreshToken)
  
- Configuration API
  * [Deploy](#deploy)
  

----

## Administration API

----
### <a name="roles"></a>Get roles list

    GET /admin/roles

**Produces**: Content-Type: application/json

```javascript
  [
    {"name":"string","description":"string"}, //...
  ]
```
----
### <a name="users"></a>Get users list

    GET /admin/users

**Produces**: Content-Type: application/json

```javascript
  [ {
    "username": "string",
      "expired": boolean,
      "enabled": boolean,
      "userInfo": {
                    "fullname":"string",
                    "email":"string"
                  },
      "roles":  {
                   "string<role.name>" : { "name":"string","description":"string"},
                //...
                }
    }, //...
    ]
```

----
### <a name="user"></a>Get user

    GET /admin/users/{username}

**Produces**: Content-Type: application/json

```javascript
  {
      "username": "string",
      "expired": boolean,
      "enabled": boolean,
      "userInfo": {
                    "fullname":"string",
                    "email":"string"
                  },
      "roles":  {
                   "string<role.name>" : { "name":"string","description":"string"},
                //...
                }
    }
```

**Errors**:  
   - `404 Not found` User not found

----
### <a name="create_user"></a>Create user

    POST /admin/users

**Consume**: Content-Type: application/json

```javascript
  {
      "username": "string",
      "userInfo": {
                    "fullname":"string",
                    "email":"string"
                  },
     "roles":  {
                  "string<role.name>" : { "name":"string","description":"string"},
                //...
                }
    }
```

**Response**: `201 Created`

**Errors**:
   - `406 Not acceptable` Invalid username
   - `409 Conflict` Username already exist

----
### <a name="update_user"></a>Update user

    PUT /admin/users/{username}

**Consume**: Content-Type: application/json

```javascript
  {
     "username": "string",
     "expired": boolean,
     "enabled": boolean,
     "userInfo": {
                   "fullname":"string",
                   "email":"string"
                 },
     "roles":  {
                 "string<role.name>" : { "name":"string","description":"string"},
                 //...
               }
  }
```

**Response**: `200 OK`

**Errors**:
  - `404 Not found` User not found
  - `406 Not acceptable` Invalid username
  - `409 Conflict` Username already exist

----
### <a name="delete_user"></a>Delete user

    DELETE /admin/users/{username}

**Response**: `202 Accepted`

**Errors**:  
   - `404 Not found` User not found

## OAuth2 API

----
### <a name="accessToken"></a>Get access token

    POST /oauth2/access_token

**Authentication** : Client identified by Basic Authentication

**Consume** : application/x-www-form-urlencoded
  - token_type = Bearer
  - grant_type = password
  - user = _[Resource owner username]_
  - password = _[Resource owner password]_
  
**Produces**: Content-Type: application/json

```javascript
  { "token_type":"Bearer",
    "access_token": "xxxx",
    "refresh_token":"xxxx",
    "expires_in": nnnn, //milliseconds
    "issue_date":"2017-01-30T12:00:35.0+02:00" //ISO-8601 
   }  
```

----
### <a name="refreshToken"></a>Refresh token

    POST /oauth2/access_token

**Authentication** : Client identified by Basic Authentication

**Consume** : application/x-www-form-urlencoded
  - grant_type = refresh
  - access_token = _[Expired access token]_
  - password = _[Valid refresh token]_
  
**Produces**: Content-Type: application/json

```javascript
  { "token_type":"Bearer",
    "access_token": "xxxx",
    "refresh_token":"xxxx",
    "expires_in": nnnn, //milliseconds
    "issue_date":"2017-01-30T12:00:35.0+02:00" //ISO-8601 
   }  
```

## Configuration API

----
### <a name="deploy"></a>Deploy

    POST /gvconfig/deploy/{configuration_id}

**Authentication** : Client identified by Basic Authentication

**Consume** :  multipart/form-data 
  - gvconfiguration = [ multipart type=application/zip]
