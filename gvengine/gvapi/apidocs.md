# GVIAM API

- Administration API
  * [Get roles list](#roles)
  * [Get users list](#users)
  * [Get user](#user)
  * [Create user](#create_user)
  * [Update user](#update_user)
  * [Delete user](#delete_user)
  * [Change user enablement](#enable_user)
  * [Reset user password](#reset_user)

- Auhtentication API
  * [Authenticate](#authenticate)

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
