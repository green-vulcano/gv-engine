services = {"LIST_EXCEL"=>"1", "LIST_PDF"=>"1", "LIST_BIRT"=>"1"}
svc = $data.getProperty("SVC")
("1" == services[svc])