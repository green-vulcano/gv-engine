services = {"LIST_EXCEL":"1", "LIST_PDF":"1", "LIST_BIRT":"1"}
svc = data.getProperty("SVC")
RESULT = "1" == services.get(svc, "0")