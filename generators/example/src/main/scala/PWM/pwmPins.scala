package example.PWM

import Chisel._
import chisel3.experimental.{withClockAndReset}
import sifive.blocks.devices.pinctrl.{Pin}

class PWMSignals[T <: Data](private val pingen: () => T) extends Bundle {
  val pwmout = pingen()
}

class PWMPins[T <: Pin](pingen: () => T) extends PWMSignals[T](pingen)


object PWMPinsFromPort {
  def apply[T <: Pin] (pins: PWMSignals[T], port: PWMPortIO, c: PWMParams, clock: Clock, reset: Bool): Unit = {
    withClockAndReset(clock, reset){
    	for(i <- 0 until c.noOfOutputs){
      	pins.pwmout.outputPin(port.pwmout(i))
      }
    }
  }

  def apply[T <: Pin] (pins: PWMSignals[T], port: PWMPortIO, c: PWMParams): Unit = {
    for(i <- 0 until c.noOfOutputs){
    	pins.pwmout.outputPin(port.pwmout(i))
    }
  }
}
