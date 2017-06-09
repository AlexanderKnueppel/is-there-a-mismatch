/*
 * This file is part of the Linux Variability Modeling Tools (LVAT).
 *
 * Copyright (C) 2010 Steven She <shshe@gsd.uwaterloo.ca>
 *
 * LVAT is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * LVAT is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LVAT.  (See files COPYING and COPYING.LESSER.)  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package gsd.linux.tools

import java.io.PrintStream
import gsd.linux._

/**
 * Outputs all choice members as a comma-separated list of feature ids.
 *
 * @author Steven She (shshe@gsd.uwaterloo.ca)
 */
object ChoiceMain {

  def main(args: Array[String]): Unit = {
    if (args.size == 0) {
      System.err.println("Parameters: <extract file> [output file]")
      System.exit(1)
    }

    val extract = KConfigParser.parseKConfigFile(args head)
    val out = if (args.size > 1) new PrintStream(args(1)) else System.out

    val mutex = extract.choices collect {
      case x@CChoice(_,_,true,false,_,_) => x
    }

    val xor   = extract.choices collect {
      case x@CChoice(_,Prompt(_,Yes),true,true,_,_) => x
    }

    val xorC  = (extract.choices collect {
      case x@CChoice(_,_,true,true,_,_) => x
    }) filterNot (xor contains)

    val or    = extract.choices collect {
      case x@CChoice(_,_,false,true,_,_) => x
    }

    out.println("=== Mutex Groups ===")
    mutex foreach { g =>
      out println (g.cs map { _.name } mkString ",")
    }

    out.println("=== Xor Groups ===")
    xor foreach { g =>
      out println (g.cs map { _.name } mkString ",")
    }

    out.println("=== Conditional Xor Groups ===")
    xorC foreach { g =>
      out println (g.cs map { _.name } mkString ",")
    }

    out.println("=== Or Groups ===")
    or foreach { g =>
      out println (g.cs map { _.name } mkString ",")
    }
  }
}