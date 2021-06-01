package MC_Module

import chisel3._
//import sv2chisel.helpers.vecconvert._
import chisel3.util.Cat
//import chisel3.stage.{ChiselStage, ChiselGeneratorAnnotation}

class quadIO extends Bundle {
  val quadA = Input(Bool())
  val quadB = Input(Bool())

  val reg_count_we =  Input(Bool())
  val reg_count_di =  Input(UInt(32.W))
  val reg_count_do =  Output(UInt(32.W))

  val reg_cfg_we =  Input(Bool())
  val reg_cfg_di =  Input(UInt(16.W))
  val reg_cfg_do =  Output(UInt(16.W))

  // Speed output
  val reg_speed_do = Output(SInt(16.W))
}

class Quad_Encoder extends Module {
 // val reset = IO(Input(Bool()))
 val io = IO(new quadIO)

// NOTE: The following statements are auto generated based on existing output reg of the original verilog source
  val quadA_delayed = Reg(UInt(3.W))
  val quadB_delayed = Reg(UInt(3.W))
  val count_reg = Reg(UInt(32.W))
  val count_reg2 = Reg(UInt(16.W))

  val speed_enable = RegInit(Bool(), false.B)
  val speed_interval = RegInit(UInt(14.W), 100.U)
  val speed_counter = RegInit(UInt(14.W), 0.U)
  val count_xx = RegInit(Bool(), true.B)
  val count_old = RegInit(UInt(32.W), 0.U)
  val qei_speed = RegInit(UInt(16.W), 0.U)


  io.reg_count_do := count_reg

  quadA_delayed := Cat(quadA_delayed(1), quadA_delayed(0), io.quadA)
  quadB_delayed := Cat(quadB_delayed(1), quadB_delayed(0), io.quadB)

  val count_2x = WireInit(Bool(), quadA_delayed(1)^quadA_delayed(2))
  val count_4x = WireInit(Bool(), quadA_delayed(1)^quadA_delayed(2)^quadB_delayed(1)^quadB_delayed(2))
  val count_direction = WireInit(Bool(), quadA_delayed(1)^quadB_delayed(2))
  val count_pulses = Mux(count_xx, count_2x, count_4x)
 // val count = Reg(UInt(8.W))
 val scount_flag = (speed_counter === speed_interval)

  when(count_pulses) {
    when(count_direction) {
      count_reg := count_reg + 1.U
    } .otherwise {
      count_reg := count_reg - 1.U
    }
  }

  when(scount_flag || count_pulses) {
    when(scount_flag) {
      qei_speed := count_reg2
      count_reg2 := 0.U
    }.otherwise {
      count_reg2 := count_reg2 + 1.U
    }
  }

  // Speed calculation
  speed_counter:= Mux(scount_flag, 0.U, speed_counter + 1.U)
  /*
  when(scount_flag) {
    count_old := count_reg
   // qei_speed :=  count_reg - count_old
    speed_counter := 0.U
  }.elsewhen(speed_enable) {
    speed_counter := speed_counter + 1.U
  } */

  // Configuration register
  io.reg_cfg_do := Cat(speed_enable, count_xx, speed_interval)
  io.reg_speed_do := qei_speed(15,0).asSInt()

  when(io.reg_count_we) {
    count_reg := io.reg_count_di
  }.elsewhen(io.reg_cfg_we){
    speed_interval := io.reg_cfg_di(13,0)
    count_xx := io.reg_cfg_di(14)
    speed_enable := io.reg_cfg_di(15)
  }

}

object QEI_generate extends App {
  //(new chisel3.stage.ChiselStage).execute(args, () => new quad)

  chisel3.Driver.execute(args, () => new Quad_Encoder)

}