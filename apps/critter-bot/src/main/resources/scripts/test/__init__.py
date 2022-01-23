from krita import *
import os
import os.path
import logging
import ctypes

def __main__(args):
    coreFolder = "~/critter/"
    outFolder = "~/critter/tmp"
    background = args[1] + ".png"

    #kritarunner test userid background etc
    newName = args[0] + ".png"

    doc = Krita.instance().openDocument(os.path.join(coreFolder, background))
    doc.setBatchmode(True)



    # Parameters from colorProfilesList
    doc.setColorSpace("RGBA", "U8", "sRGB-elle-V2-srgbtrc.icc")
    doc.exportImage(os.path.join(outFolder, newName), InfoObject())
    doc.close()
    pass
