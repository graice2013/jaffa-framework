CREATE TABLE J_PRINTER_DEFINITIONS (
        PRINTER_ID          VARCHAR(50) NOT NULL,
        DESCRIPTION          VARCHAR(100),
        SITE_CODE          VARCHAR(20),
        LOCATION_CODE          VARCHAR(40),
        REMOTE_PRINTER          CHAR(1) NOT NULL,
        REAL_PRINTER_NAME          VARCHAR(255),
        PRINTER_OPTION1          VARCHAR(255),
        PRINTER_OPTION2          VARCHAR(255),
        OUTPUT_TYPE          VARCHAR(20) NOT NULL,
        SCALE_TO_PAGE_SIZE   VARCHAR(30),
        CREATED_ON          DATETIME,
        CREATED_BY          VARCHAR(20),
        LAST_CHANGED_ON          DATETIME,
        LAST_CHANGED_BY          VARCHAR(20),
    CONSTRAINT J_PRINTER_DEFINITIONSP1 PRIMARY KEY(PRINTER_ID)
) TYPE=InnoDB
