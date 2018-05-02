# GV IAM Extension API
___
### <a name="signup-check"></a>Check account
Check an email address account status

    GET /signup?check={email}

**Parameters** :
  - check: a valid email address

**Produces**: Content-Type: application/json

```javascript
  {
    "email":"xxxxx", //checked email address
    "status": "UNKONWN | PENDING | CONFIRMED",    
  }  
```

----
### <a name="signup-create"></a>Account sign-up
Submit an account creation request

    POST /signup

**Consume** : application/json
```javascript
  {
    "email":"xxxxx",
    "fullname": "xxxxx",
    "password": "xxxxx"    
  }  
```

**Produces**: No cotent

**Errors**:
  - 400: _Invalid payload submitted_
  - 403: _Illegal password submitted_
  - 409: _An account exists for the same email address_

### <a name="signup-confirm"></a>Account confirmation
Confirm an account following a creation request

    PUT /signup

**Consume** : application/x-www-form-urlencoded
  - email = _[Email address related to an account creation request]_
  - token = _[Validation token issued to user after an account creation request]_

**Produces**: No cotent

**Errors**:
  - 404: _No matching account creation request found_
  - 403: _Illegal password submitted_

### <a name="password-reset"></a>Reset password
Submit a reset password request

    POST /restore

**Consume** : application/x-www-form-urlencoded
  - email = _[Email address related to an user account]_

**Produces**: No cotent

**Errors**:
  - 400: _Missing or invalid email address submitted_
  - 404: _No matching account found_

### <a name="password-change"></a>Change password
Confirm an account following a creation request

    PUT /restore

**Consume** : application/x-www-form-urlencoded
  - email = _[Email address related to an account reset request]_
  - token = _[Validation token issued to user after an account reset request]_
  - password = _[New user password]_
**Produces**: No cotent

**Errors**:
  - 404: _No matching account creation reset found_
  - 403: _Illegal password submitted_

### <a name="add-role"></a>Grant role

    POST /grant

**Consume** : application/x-www-form-urlencoded
  - username = _[Unique username identifying the user account]_
  - role = _[Role to be granted to user]_

**Produces**: No cotent

**Errors**:
  - 400: _Missing or invalid role submitted_
  - 404: _No matching account found_

### <a name="delete-role"></a>Revoke role

    DELETE /grant

**Consume** : application/x-www-form-urlencoded
  - username = _[Unique username identifying the user account]_
  - role = _[Role to be revoked ]_

**Produces**: No cotent

**Errors**:
  - 404: _No matching account found_
