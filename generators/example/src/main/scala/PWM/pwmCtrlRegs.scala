package example.PWM

object PWMCtrlRegs {
  val period     = 0x000
  val duty       = 0x100
  val noOfCycles = 0x200
  val shamt      = 0x300
  val align      = 0x400
  val deadband   = 0x500
  val comEn      = 0x600
  val kill       = 0x700
  val enable     = 0x800
}
