package com.epam.spark

import com.epam.spark.OsType.OsType
import org.apache.spark.rdd.RDD
import org.apache.spark.scheduler.AccumulableInfo
import org.apache.spark.{Accumulator, SparkConf, SparkContext}

object OsCounter {

  val appName:String = "Server Log Analyzer"
  val master:String = "local"
  var sc:SparkContext = _
  var osAccums:collection.mutable.Map[OsType, Accumulator[Int]] = collection.mutable.Map[OsType, Accumulator[Int]]()


  def main(args: Array[String]) {
    if (args.length != 2){
      print("Please use 2 parameters: [source file, dest file]")
      System.exit(1)
    }

    val conf = new SparkConf().setAppName(appName).setMaster(master)
    sc = new SparkContext(conf)

    initAccums()

    val dataSet = sc.textFile(args(0))

    //using transformations to count browsers
    val osStats = getOsStats(dataSet)
    osStats.saveAsTextFile("hdfs://"+ args(1))

    //using accumulators to count browsers
    accumulateOsStats(dataSet)
    val osStatsAccum = getAccumulatedValues()
    osStatsAccum.saveAsTextFile("hdfs://"+ args(1)+"Acc")

  }

  def initAccums(){
      OsType.values.foreach(osType => osAccums(osType) = sc.accumulator[Int](0))
    }

  def getOsStats(text: RDD[String]):RDD[String]={
    val osStats = text.map(s=>s.split("""(?:[\"\]\[]|[\]] [\"\]\[]|[\]])|(?:( - - ))"""))
      .map(line => (OsDetector.getOsType(line(8)).toString, 1))
      .reduceByKey(_ + _)
      .map{_.swap}
      .sortByKey(false)
      .map{_.swap}
      .map{case (key: String, value: Int) =>( key.toString +";"+ value.toString)}

    return osStats
  }

  def accumulateOsStats(text: RDD[String]){
    val osStats = text.map(s=>s.split("""(?:[\"\]\[]|[\]] [\"\]\[]|[\]])|(?:( - - ))"""))
      .map(line => line(8))
      .foreach(clientStr => osAccums(OsDetector.getOsType(clientStr)) += 1)
  }

  def getAccumulatedValues(): RDD[String] = {
    sc.parallelize(
      osAccums
        .filter(accum => (accum._2.value != 0))
        .map(entry => (entry._1.toString +";"+entry._2.value))
        .toList
    )
  }




}
