from os import path, remove, environ
import sys as s
import data as d
import pexpect
import time


def java_home():
    if environ.get('JAVA_HOME') is None:
        return False
    return True


def replace_cfg(karaf):
    time.sleep(1)
    print("Configuring etc/org.ops4j.pax.url.mvn.cfg ...")
    home = karaf + "/etc/org.ops4j.pax.url.mvn.cfg"
    remove(home)
    try:
        new = open(home, 'w+')
        new.write(d.pax_url_mvn_cfg)
    except Exception as e:
        print("Something went wrong. Program will exit. See error report for details.")
        with open("report.log", 'w+') as report:
            report.write(str(e))
        s.exit()


def install_gv(karaf):
    karaf = "bash " + karaf + "/bin/karaf"
    try:
        p = pexpect.spawn(karaf)
        p.expect("root", timeout=10)
        p.sendline("feature:repo-add mvn:it.greenvulcano.gvesb/features/4.1.0-SNAPSHOT/xml/features && "
                   "feature:install gvengine")
        p.expect("root", timeout=2)
    except pexpect.TIMEOUT:
        print("Creating GreenV/ folder...")
        time.sleep(2)
        print("Closing karaf instance...")
        p.close(force=True)
        time.sleep(2)
        return True
    except pexpect.EOF:
        print("A karaf instance is already running! Please terminate it before executing GV Installer.")
        return False
        
