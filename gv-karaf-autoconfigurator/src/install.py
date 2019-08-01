from funcs import *

if __name__ == '__main__':
    from sys import argv, exit
    from os import path

    try:
        karaf = argv[1]
        if not path.exists(karaf + "/bin/karaf"):
            raise FileNotFoundError
    except (ValueError, IndexError):
        print("Invalid parameter. Usage: python3 install.py <karaf_home>")
        exit(0)
    except FileNotFoundError:
        print("No karaf instance found in folder '" + karaf + "'.")
        exit(0)
    print("GreenVulcano ESB Installer - 2019 © All rights reserved")
    if not java_home():
        print("WARNING: Your JAVA_HOME is not set and therefore karaf might not work correctly. "
              "Proceed at your own risk.")
    replace_cfg(karaf)
    if install_gv(karaf):
        print("\nGreenVulcano Engine installed succesfully!\n"
              "Make sure to check out our quickstart guide to see which additional components you can implement.\n"
              "https://github.com/cs91chris/gv-engine/blob/master/quickstart-guide.md")
    else:
        print("Something went wrong. The program will now exit.")
