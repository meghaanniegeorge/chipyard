package example.PWM

import chisel3._
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import sifive.blocks.util.BasicBusBlocker

class PWMModule(val w: Int) extends Module {
  val io = IO(new Bundle {
    val pwmout = Output(Bool())
    val period = Input(UInt(w.W))
    val duty   = Input(UInt(w.W))
    val noOfCycles = Input(UInt(w.W))
    val comEn  = Input(Bool())
    val shamt = Input(UInt(w.W))
    val align = Input(UInt())
    val deadband = Input(UInt(w.W))
    val enable = Input(Bool())
  })

  val counter = RegInit(0.U(w.W))
  val cycleCounter = RegInit(0.U((2*w).W))
  val shcounter = RegInit(0.U(w.W))
  val shamt = RegInit(0.U(w.W))
  val duty  = RegInit(0.U(w.W))

  when((io.deadband > 0.U) && !io.comEn && (io.align === 2.U)){
    shamt := io.shamt + io.deadband + io.period - io.duty
    duty  := io.duty - io.deadband
  }.elsewhen((io.deadband > 0.U) && io.comEn && (io.align === 2.U)){
    duty := io.duty + io.deadband 
    shamt := io.shamt + io.period - io.duty
  }.elsewhen((io.deadband > 0.U) && !io.comEn && (io.align === 1.U)){
    shamt := io.shamt + io.deadband + ((io.period - io.duty)/2.U)
    duty  := io.duty - io.deadband
  }.elsewhen((io.deadband > 0.U) && io.comEn && (io.align === 1.U)){
    duty := io.duty + io.deadband 
    shamt := io.shamt + ((io.period - io.duty)/2.U)
  }.elsewhen((io.deadband > 0.U) && !io.comEn && (io.align === 0.U)){
    shamt := io.shamt + io.deadband
    duty  := io.duty - io.deadband
  }.elsewhen((io.deadband > 0.U) && io.comEn && (io.align === 0.U)){
    duty := io.duty + io.deadband 
    shamt := io.shamt 
  }.elsewhen((io.deadband === 0.U) && (io.align === 2.U)){
    shamt := io.shamt + io.period - io.duty
    duty := io.duty
  }.elsewhen((io.deadband === 0.U) && (io.align === 1.U)){
    shamt := io.shamt + ((io.period - io.duty)/2.U)
    duty := io.duty
  }.otherwise{
    shamt := io.shamt
    duty := io.duty
  }

  when(io.enable){
    shcounter := shcounter + 1.U
    when(shcounter >= shamt){
      when (counter >= (io.period - 1.U)) {
        counter := 0.U
      } .otherwise {
        counter := counter + 1.U
      }
      cycleCounter := cycleCounter + 1.U
    }
  }

  io.pwmout := (!io.comEn && 
                io.enable && 
                (counter < duty) && 
                ((cycleCounter < (io.period*io.noOfCycles)) || (io.noOfCycles === ((1.U << w) - 1.U))) && 
                (shcounter >= shamt)) ||
                (io.comEn && 
                io.enable && 
                ((cycleCounter < io.period*io.noOfCycles) || (io.noOfCycles === ((1.U << w) - 1.U))) && 
                (counter >= duty))
}

case class PWMParams(
	address : BigInt = 0x5000,
  noOfOutputs : Int = 1)

class PWMPortIO(val c: PWMParams) extends Bundle{
	val pwmout = Output(Vec(c.noOfOutputs, Bool()))
}

abstract class PWM(busWidthBytes: Int, c: PWMParams)(implicit p: Parameters)
	extends IORegisterRouter(
		RegisterRouterParams(
	    name      = "PWM",
	    compat    = Seq("sifive,pwm0"),
      base      = c.address,
      beatBytes = busWidthBytes),
    new PWMPortIO(c))
	with HasInterruptSources{

  def nInterrupts = 1

  lazy val module = new LazyModuleImp(this){
    val base       = Vec(Seq.fill(c.noOfOutputs){Module(new PWMModule(32)).io})
    val period     = Reg(Vec(c.noOfOutputs, UInt()))
    val duty       = Reg(Vec(c.noOfOutputs, UInt()))
    val noOfCycles = Reg(Vec(c.noOfOutputs, UInt()))
    val shamt      = Reg(Vec(c.noOfOutputs, UInt()))
    val align      = Reg(Vec(c.noOfOutputs, UInt()))
    val deadband   = Reg(Vec(c.noOfOutputs, UInt()))
    val enable     = RegInit(false.B)
    val comEn      = RegInit(VecInit(Seq.fill(c.noOfOutputs){false.B}))
    val kill       = RegInit(VecInit(Seq.fill(c.noOfOutputs){false.B}))
    
    for(i <- 0 until c.noOfOutputs){
      port.pwmout(i)     := base(i).pwmout
      base(i).period     := period(i)
      base(i).duty       := duty(i)
      base(i).noOfCycles := noOfCycles(i)
      base(i).shamt      := shamt(i)
      base(i).align      := align(i)
      base(i).comEn      := comEn(i)
      base(i).deadband   := deadband(i)
      base(i).enable     := enable && !kill(i)

      regmap(
        PWMCtrlRegs.period + (i*0x04) -> Seq(
          RegField(32, period(i))),
        PWMCtrlRegs.duty + (i*0x04)   -> Seq(
          RegField(32, duty(i))),
        PWMCtrlRegs.noOfCycles + (i*0x04)-> Seq(
          RegField(32, noOfCycles(i))),
        PWMCtrlRegs.shamt + (i*0x04)-> Seq(
          RegField(32, shamt(i))),
        PWMCtrlRegs.align + (i*0x04)-> Seq(
          RegField(32, align(i))),
        PWMCtrlRegs.deadband + (i*0x04)-> Seq(
          RegField(32, deadband(i))),
        PWMCtrlRegs.comEn + (i*0x04)-> Seq(
          RegField(1, comEn(i))),
        PWMCtrlRegs.kill + (i*0x04) -> Seq(
          RegField(1, kill(i))))
    }
    regmap(PWMCtrlRegs.enable -> Seq(
      RegField(1, enable)))
  }
}

class TLPWM(busWidthBytes: Int, params: PWMParams)(implicit p: Parameters)
	extends PWM(busWidthBytes, params) with HasTLControlRegMap

case class PWMAttachParams(
	pwm: PWMParams,
  controlBus: TLBusWrapper,
  intNode: IntInwardNode,
  blockerAddr: Option[BigInt] = None,
  intXType: ClockCrossingType = NoCrossing,
  controlXType: ClockCrossingType = NoCrossing,
  mclock: Option[ModuleValue[Clock]] = None,
  mreset: Option[ModuleValue[Bool]] = None)
  (implicit val p: Parameters)

object PWM {
  val nextId = { var i = -1; () => { i += 1; i} }
	def attach(params: PWMAttachParams): TLPWM = {
    implicit val p = params.p
    val name = s"pwm_${nextId()}"
	  val cbus = params.controlBus
	  val pwm = LazyModule(new TLPWM(cbus.beatBytes, params.pwm))
	  pwm.suggestName(name)
	  cbus.coupleTo(s"device_named_$name") { bus =>
      val blockerNode = params.blockerAddr.map(BasicBusBlocker(_, cbus, cbus.beatBytes, name))
      (pwm.controlXing(params.controlXType)
        := TLFragmenter(cbus)
        := blockerNode.map { _ := bus } .getOrElse { bus })
    }
    params.intNode := pwm.intXing(params.intXType)
  	InModuleBody { pwm.module.clock := params.mclock.map(_.getWrappedValue).getOrElse(cbus.module.clock) }
  	InModuleBody { pwm.module.reset := params.mreset.map(_.getWrappedValue).getOrElse(cbus.module.reset) }

    pwm
  }

	def attachAndMakePort(params: PWMAttachParams): ModuleValue[PWMPortIO] = {
   	val pwm = attach(params)
  	val pwmNode = pwm.ioNode.makeSink()(params.p)
  	InModuleBody { pwmNode.makeIO()(ValName(pwm.name)) }
	}
}

