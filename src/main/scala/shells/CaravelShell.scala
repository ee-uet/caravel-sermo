package shells

import chisel3._
import chisel3.experimental.{Analog, BaseModule, attach}

class LogicAnalyzerIO extends Bundle {
  val data_in = Input(UInt(128.W))
  val data_out = Output(UInt(128.W))
  val oenb = Input(UInt(128.W))
}

class UserIO extends Bundle {
  val in = Input(UInt(38.W))
  val out = Output(UInt(38.W))
  val oeb = Output(UInt(38.W))
}

class WishboneClockAndReset extends Bundle {
  val clk_i = Input(Bool())
  val rst_i = Input(Bool())
}

class WishboneIO extends Bundle {
  val stb_i = Input(Bool())
  val cyc_i = Input(Bool())
  val we_i = Input(Bool())
  val sel_i = Input(UInt(4.W))
  val dat_i = Input(UInt(32.W))
  val adr_i = Input(UInt(32.W))
  val ack_o = Output(Bool())
  val dat_o = Output(UInt(32.W))
}

trait HasCaravelIO{self: RawModule =>
  val wb = IO(new WishboneClockAndReset)
  val wbs = IO(new WishboneIO)
  val la = IO(new LogicAnalyzerIO)
  val io = IO(new UserIO)
  val irq = IO(Output(UInt(3.W)))
}

trait HasExtraIO{self: RawModule =>
  val user_clock2 = IO(Input(Bool()))
  val analog = IO(Analog(29.W))
}

trait HasPowerPins{self: RawModule =>
  val vdda1 = IO(Analog(1.W))
  val vdda2 = IO(Analog(1.W))
  val vssa1 = IO(Analog(1.W))
  val vssa2 = IO(Analog(1.W))
  val vccd1 = IO(Analog(1.W))
  val vccd2 = IO(Analog(1.W))
  val vssd1 = IO(Analog(1.W))
  val vssd2 = IO(Analog(1.W))

  def attachPowerPins[T <: BaseModule with HasPowerPins](above: T) = {
    attach(above.vdda1, vdda1)
    attach(above.vdda2, vdda2)
    attach(above.vssa1, vssa1)
    attach(above.vssa2, vssa2)
    attach(above.vccd1, vccd1)
    attach(above.vccd2, vccd2)
    attach(above.vssd1, vssd1)
    attach(above.vssd2, vssd2)

    above
  }
}

class CaravelShell extends RawModule with HasCaravelIO with HasExtraIO with HasPowerPins {

  def caravalModule[T <: BaseModule with HasPowerPins](gen: => T) = {

    val theModule = Module(gen)

    attach(theModule.vdda1, vdda1)
    attach(theModule.vdda2, vdda2)
    attach(theModule.vssa1, vssa1)
    attach(theModule.vssa2, vssa2)
    attach(theModule.vccd1, vccd1)
    attach(theModule.vccd2, vccd2)
    attach(theModule.vssd1, vssd1)
    attach(theModule.vssd2, vssd2)

    theModule
  }

}