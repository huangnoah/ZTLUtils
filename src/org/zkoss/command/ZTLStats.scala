package org.zkoss.command

import scala.io.Source
import org.streum.configrity.Configuration

object ZTLStats extends App {

  val conf = Configuration.load("config.properties")
  val workspace = conf[String]("workspace")
  val version = conf[String]("version")

  val lines = Source.fromFile(workspace + "zk/zktest/src/archive/test2/config.properties").getLines().toList
  val bfmt1 = "B" + version + """-\d{7}.*"""
  val bfmt2 = "B" + version + """-ZK-\d{2,4}.*"""

  val ffmt1 = "F" + version + """-\d{7}.*"""
  val ffmt2 = "F" + version + """-ZK-\d{2,4}.*"""

  val zfmt = "Z" + version + """-.*"""

  val ztl = "##ztl##"
  val outOfDate1 = "##Out of Date##"
  val outOfDate2 = "##Out Of Date##"
  val useless = "##useless##"
  val fileNotFound = "##File not found##"
  val fileNotExist = "##File not exist##"
  val wontFix = "##won't fix##"
  val tooHard = "##too hard to reproduce##"
  val unsupport = "##unsupported##"
  val requireConfig = "##require config##"

  val bfmts = List(bfmt1, bfmt2)
  val ffmts = List(ffmt1, ffmt2)

  val btestcases = lines.filter(str => bfmts.exists(fmt => str.matches(fmt)))
  val ftestcases = lines.filter(str => ffmts.exists(fmt => str.matches(fmt)))
  val ztestcases = lines.filter(str => str.matches(zfmt))

  println("ZTL Stats")
  val coord = List("B" + version -> btestcases, "F" + version -> ftestcases, "Z" + version -> ztestcases)
  coord.foreach(c2li => println("* " + c2li._1 + ": "
    + c2li._2.size))
  val ls = coord.map(_._2).flatten
  println("* Total: " + ls.size) 
}