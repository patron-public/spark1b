package com.epam.spark

import com.epam.spark.OsType._

object OsDetector {
  val osMap:Map[OsType, String] = Map(
    OsType.WA360 -> "360WebApp",
    OsType.WINDOWS_8 -> "Windows NT 6.2" ,
    OsType.WINDOWS_7 -> "Windows NT 6.1" ,
    OsType.WINDOWS_VISTA -> "Windows NT 6.0" ,
    OsType.WINDOWS_XP -> "Windows NT 5.1" ,
    OsType.WINDOWS_2000 -> "Windows NT 5.0" ,
    OsType.WINDOWS_NT -> "Windows NT 4." ,
    OsType.MAC_OS_X -> "Mac OS X" ,
    OsType.UBUNTU -> "X11; Ubuntu" ,
    OsType.LINUX -> "X11; Linux"
  )

  def getOsType(clientStr: String):OsType ={
    var result:OsType = OsType.OTHER

    osMap.foreach {
      entry => {
        if (clientStr.contains(entry._2)) result = entry._1
      }
    }
    return result
  }
}
