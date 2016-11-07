# GVScheduler API

- Scheduling API
  * [Get schedule](#schedule)
  * [Get schedule entry](#get_entry)
  * [Delete schedule entry](#delete_entry)
  * [Pause schedule entry](#pause_entry)
  * [Resume schedule entry](#resume_entry)
  * [Create schedule entry](#create_entry)

----

## Scheduling API

----
### <a name="schedule"></a>Get schedule

    GET /schedules

**Produces**: Content-Type: application/json

**Response**: `200 OK`

```javascript
  {
    "<id>"  : {
               "id" : "string",
               "description" : "string",
               "status" : "string",
               "cronExpression" : "string"
               },
      //...
  }
```

----
### <a name="get_entry"></a>Get schedule entry

    GET /schedules/{id}

**Produces**: Content-Type: application/json

**Response**: `200 OK`

```javascript
      {
        "id" : "string",
        "description" : "string",
        "status" : "string",
        "cronExpression" : "string"
       }
```

**Errors**:  
   - `404 Not found`

----
### <a name="delete_entry"></a>Delete schedule entry

   DELETE /schedules/{id}

**Response**: `200 OK`

**Errors**:  
  - `404 Not found`

----
### <a name="pause_entry"></a>Pause schedule entry

   PUT /schedules/{id}/pause

**Response**: `202 Accepted`

**Errors**:  
  - `404 Not found`

----
### <a name="resume_entry"></a>Resume schedule entry

   PUT /schedules/{id}/resume

**Response**: `202 Accepted`

**Errors**:  
  - `404 Not found`

----
### <a name="create_entry"></a>Create schedule entry

    POST /schedule/{service}/{operaton}

**Consume**: Content-Type: application/json

```javascript
  {
      "cronExpression": "string",
      "[optional]properties": {
                    "<key>string":"<value>string",
                    //...
                    },
      "[optional]object":  // accepts any type
    }
```

**Response**: `201 Created`
