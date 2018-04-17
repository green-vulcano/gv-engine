module Master
    include_package "it.greenvulcano.gvesb.buffer"
    [GVBuffer,Id]
    include_package "it.greenvulcano.gvesb.utils"
    include_package "it.greenvulcano.util.bin"
    [Dump,BinaryUtils]
    include_package "it.greenvulcano.util.xml"
    [XMLUtils]
    include_package "it.greenvulcano.util.xpath"
    [XPathFinder,XPathDOMBuilder]
    include_package "it.greenvulcano.util.txt"
    [DateUtils,DateDiff,TextUtils]
    include_package "it.greenvulcano.configuration"
    [XMLConfig]
    include_package "it.greenvulcano.gvesb.core.exc"
    [GVCoreException]

    java_import "it.greenvulcano.gvesb.j2ee.XAHelper"
    java_import "it.greenvulcano.util.thread.ThreadMap"

    java_import "java.util.HashMap"
    java_import "java.util.ArrayList"
    java_import "java.util.Calendar"

    # TEST
    include_package "tests.unit.gvrules.bean.figure"
    [Figure,Circle,Square,Triangle,FigureBag]
end

include Master

