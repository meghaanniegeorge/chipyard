package tracegen

import freechips.rocketchip.config.Parameters
import freechips.rocketchip.util.GeneratorApp

class TestHarness(implicit p: Parameters) extends freechips.rocketchip.groundtest.TestHarness

object Generator extends GeneratorApp {
  // specify the name that the generator outputs files as
  val longName = names.topModuleProject + "." + names.topModuleClass + "." + names.configs

  // generate files
  generateFirrtl
  generateAnno
  generateTestSuiteMakefrags
  generateArtefacts
}
