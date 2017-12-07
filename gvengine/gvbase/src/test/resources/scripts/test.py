def gvtest( p ):
    services = {"LIST_EXCEL":"1", "LIST_PDF":"1", "LIST_BIRT":"1"}   
    return "1" == services.get(p, "0")

RESULT = gvtest(data.getProperty("SVC"))
