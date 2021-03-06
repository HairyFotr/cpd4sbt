/*
 * This file is part of cpd4sbt.
 * 
 * Copyright (c) 2010-2013 Joachim Hofer
 * All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.johoop.cpd4sbt

import java.io.File

import sbt._
import Keys._

object CopyPasteDetector extends Plugin with Settings {
  
  def cpdAction(reportSettings: ReportSettings, sourceSettings: SourceSettings, maxMem: Int, 
      classpath: Classpath, streams: TaskStreams) {
    
    IO createDirectory reportSettings.path
    
    val commandLine = List("java", 
        "-Xmx%dm" format maxMem, 
        "-Dfile.encoding=%s" format reportSettings.encoding,
        "-cp", PathFinder(classpath.files).absString, 
        "net.sourceforge.pmd.cpd.CPD",
        "--minimum-tokens", sourceSettings.minTokens.toString,
        "--language", sourceSettings.language.name,
        "--encoding", sourceSettings.encoding,
        "--format", "net.sourceforge.pmd.cpd.%sRenderer" format reportSettings.format.name) ++
        sourceSettings.dirs.filter(_.isDirectory).flatMap(file => List("--files", file.getPath)) ++
        (if (sourceSettings.skipDuplicateFiles) List("--skip-duplicate-files") else List())

    streams.log debug "Executing: %s".format(commandLine mkString "\n")
    
    Process(commandLine) #> (reportSettings.path / reportSettings.name) ! streams.log
  }
}
