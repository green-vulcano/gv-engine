create table SERVICE_INFORMATIONS (
    SYSTEM       VARCHAR2(100) not null,
    SERVICE      VARCHAR2(100) not null,
    ID           VARCHAR2(24) not null,
    START_TIME   NUMBER not null,
    STOP_TIME    NUMBER not null,
    START_DATE   DATE not null,
    STOP_DATE    DATE not null,
    STATE        NUMBER,
    ERROR_CODE   NUMBER,
    PACKAGE_NAME VARCHAR2(250) not null,
    PROCESS_NAME VARCHAR2(250) not null,
    PROCESS_TIME NUMBER not null
)