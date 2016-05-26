load("nashorn:mozilla_compat.js");
importPackage(Packages.it.greenvulcano.gvesb.buffer);
importPackage(Packages.it.greenvulcano.gvesb.utils);
importPackage(Packages.it.greenvulcano.util.xml);
importPackage(Packages.it.greenvulcano.util.xpath);
importPackage(Packages.it.greenvulcano.configuration);
importPackage(Packages.org.zeromq);
importPackage(Packages.java.lang);
//importPackage(Packages.java.util);

/**
 Remove leading and tailing spaces from str
 */
function trim(str) {
    return str.replace(/^\s*/, "").replace(/\s*$/, "");
}
